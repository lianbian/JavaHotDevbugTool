package net.lianbian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lianbian.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService extends IService<User> {
    List<Map<String, Object>> selectRelateList();
}
