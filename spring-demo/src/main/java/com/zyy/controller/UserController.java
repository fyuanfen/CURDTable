package com.zyy.controller;

import com.zyy.entity.User;
import com.zyy.repository.UserRepo;
import com.zyy.tools.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by zyy on 2017/2/26.
 */
@RestController
public class UserController {

    @Autowired
    private UserRepo userRepo;

    @RequestMapping(value= "/findall")
    public List<User> findAllUsers(){
        return userRepo.findAll();
    }

    @RequestMapping(value = "/findbyname")
    public List<User> findByName(@RequestParam String username) {
        List<User> userlist = userRepo.findByUsernameContaining(username);
        return userlist;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody User user) {
        User user1 = userRepo.findByUsernameAndPwd(user.getUsername(), user.getPwd());
        if (user1 != null) {
            return "登录成功";
        }
        else {
            return "登录失败";
        }
    }
    @RequestMapping(value = "/add")
    public JsonResult addUser(@RequestBody User user) {

        JsonResult result;
        if (userRepo.findByUsername(user.getUsername()) != null) {
            result = new JsonResult(100,"失败了",null);
        } else {
            User newuser = userRepo.save(user);
            result = new JsonResult(200,"成功",newuser);
        }
        return result;


    }
    @RequestMapping(value = "/update")
    public JsonResult updateUser(@RequestBody User user) {
        JsonResult result;
        if (userRepo.findOne(user.getId()) != null) {
            userRepo.setUsernameAndPwd(user.getUsername(),user.getPwd(),user.getId());
            result = new JsonResult(100,"成功了",null);
        } else {
            result = new JsonResult(200,"失败",null);
        }
        return result;
    }

    @RequestMapping(value = "/delete")
    public void deleteUser(@RequestBody User user){
        userRepo.delete(user.getId());

    }


}
