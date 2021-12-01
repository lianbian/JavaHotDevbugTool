package net.lianbian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.lianbian.mapper.UserMapper;
import net.lianbian.entity.User;
import net.lianbian.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public List<Map<String, Object>> selectRelateList() {
        return userMapper.selectRelateList();
    }
}
