package cn.service.impl;

import cn.entity.User;
import cn.repository.UserRepository;
import cn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ChuangLan on 2016/11/12.
 */
@Service
public class UserServiceImpl implements UserService {
	
    @Autowired
    private UserRepository userRepository;

    public void save(User user) {
    	userRepository.save(user);
    }

    public User findByName(String name) {
        return this.userRepository.findByName(name);
    }

}
