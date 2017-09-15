package cn.repository;

import cn.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by WunHwanTseng on 2016/11/12.
 */
public interface UserRepository extends MongoRepository<User, String> {
    
    User findByName(String name);
}
