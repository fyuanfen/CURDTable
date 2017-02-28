package com.zyy.repository;

import com.zyy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by zyy on 2017/2/26.
 */
// 数据库访问层
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsernameAndPwd(String username, String pwd);
    User findByUsername(String username);
    List<User> findByUsernameContaining(String username);

    @Modifying
    @Transactional
    @Query("update User u set u.username = ?1, u.pwd = ?2 where u.id = ?3")
    int setUsernameAndPwd(String username, String pwd, Long id);
}
