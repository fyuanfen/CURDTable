# Vue+Spring+Mysql实现可以增删改查的表格

1. 前端使用vue框架实现页面展示，Axios 基于Promise 的 HTTP 请求客户端,可同时在浏览器和 node.js 中使用 
2. 后台服务器使用Spring构建，简化创建 JPA 数据访问层和跨存储的持久层。
3. 数据库使用mysql

## 一、构建后台数据库
### 1. 使用IDEA新建一个JAVA Spring项目,添加Web,JPA,MySql依赖。
### 2. 实现数据库访问和服务器端口配置
将以下代码添加到resource文件夹的application.properties中
```
server.port=8000
# 配置mysql

spring.datasource.url=jdbc:mysql://localhost:3306/myapp?useUnicode=true&amp;characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
# 启动时会根据实体类生成表，当实体类属性变动的时候，表结构也会更新，在初期开发阶段使用此选项。
spring.jpa.hibernate.ddl-auto=update
# hibernate操作的时候在控制台显示真实的sql语句
spring.jpa.show-sql=true
# 让控制器输出的json字符串格式更美观
spring.jackson.serialization.indent_output=true
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true
```

### 3. CORS跨域
不同子域的网页互相访问资源时，需要进行跨与操作。
本实验采取在后台服务器，配置cors，实现跨域请求。
首先，新建一个webconfig类
```javascript
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    // 允许跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .maxAge(3600);
    }

    // 自定义http消息转换
    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(customJackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
    }
}
```

这里要注意到，因为使用axios发送http请求，而发送post请求使用的是json格式数据[关于axios在node中post的使用](https://cnodejs.org/topic/57e17beac4ae8ff239776de5)。因此还需要添加上面那段http转换代码。

### 4. 创建一个entity包，新建一个名为User的类，并使用@Entity标注其对应数据库的user表。设置id,username,password属性，分别映射到mysql数据表的字段。
### 5. 创建数据访问层接口，继承自JpaRepository类，可以完成数据库操作。其中会自动根据方法名称实现相关操作，因此写函数名字的时候一定要注意格式哦～～～
```java
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsernameAndPwd(String username, String pwd);
    User findByUsername(String username);
    List<User> findByUsernameContaining(String username);
}
```

### 6. 创建控制层实例，这里可以写一些增删改查相关逻辑。
这里有几点需要注意的，一般的页面映射函数如下：
```java
@RequestMapping(value = "/findbyname")
    public List<User> findByName(@RequestParam String username) {
        List<User> userlist = userRepo.findByUsernameContaining(username);
        return userlist;
    }
```
 对于ajax和axios的get操作，参数都可以写成@RequestParam的形式。但是之前说过，axios对于post请求发送的是一个json格式的数据。因此函数形式就变成了下面这样：
```java
@RequestMapping(value = "/add")
    public String addUser(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()) != null) {
            return "已经有这个人了哦～";
        } else {
            userRepo.save(user);
            return "添加成功";
        }

    }
   ```
   这里用的是@RequestBody
   
## Vue前台： 

### 1. 关于使用sass语法，可以在webpack.config.js中进行配置，在我的项目中，我是在vue-loader.config.js中进行配置,具体可参考[vue-loader文档pre-processors](http://vue-loader.vuejs.org/en/configurations/pre-processors.html)
``` 
module.exports = {
  loaders: [utils.cssLoaders({
    sourceMap: isProduction
      ? config.build.productionSourceMap
      : config.dev.cssSourceMap,
    extract: isProduction
  }),
      {scss: 'vue-style-loader!css-loader!sass-loader' },// <style lang="scss">
      {sass: 'vue-style-loader!css-loader!sass-loader?indentedSyntax}' // <style lang="sass">
}
  ]
```
### 2. 关于es-lint，使用vue-cli构建的项目，默认只会提示而不会修改。因此需要在package.json文件中的lint脚本命令加上--fix，格式如下： 
```
"scripts": {
  "lint": "eslint --ext .js,.vue src --fix"
  }
```
 这样就可以自动格式化js和.vue代码了。
 
 
### 3.  关于实现可编辑的表格控件，普通做法是使用js直接对表单dom元素进行appendChild和remove操作。而vue可以实现的更优雅一些。
 
首先在表格组件的data里初始化一个list对象initialItems用来保存获得的后台数据。对于页面的增删查改只需要相应的改变数组内容。但是这样并不能实现动态更新，所以还需要声明一个计算属性items，这样就可以在initialItems改变的时候自动更新显示内容。具体如下：
 
 ```javascript
      data () {
        return {
          initialItems: []
        }
      },
      computed: {
        items () {
          return this.initialItems
        }
      },
      mounted () {
        var that = this
        // Make a request for a user with a given ID
        axios.get('http://localhost:8000/findall')
            .then(function (response) {
              that.initialItems = response.data
            })
            .catch(function (error) {
              console.log(error)
            })
      },
      ```
       这样只需要在增删查改操作时改变initialItems对象，则computed属性就会自动更新，页面元素也会更新。
 
  
