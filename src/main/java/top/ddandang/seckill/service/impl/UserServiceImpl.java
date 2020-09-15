package top.ddandang.seckill.service.impl;

import top.ddandang.seckill.model.pojo.User;
import top.ddandang.seckill.mapper.UserMapper;
import top.ddandang.seckill.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author D
 * @since 2020-09-13
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
