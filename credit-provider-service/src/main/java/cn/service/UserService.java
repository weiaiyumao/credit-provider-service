package cn.service;

import org.springframework.stereotype.Repository;

import cn.entity.User;

/**
 * Created by WunHwanTseng on 2016/11/12.
 */
@Repository
public interface UserService {
    void save(User user);

    User findByName(String name);

}
