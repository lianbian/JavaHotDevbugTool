package net.lianbian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lianbian.entity.User;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User> {
    List<Map<String, Object>> selectRelateList();
}