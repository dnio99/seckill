package top.ddandang.seckill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.ddandang.seckill.mapper.UserMapper;

import javax.annotation.Resource;

@SpringBootTest
class SeckillApplicationTests {

    @Resource
    UserMapper userMapper;

    @Test
    void contextLoads() {
    }
}
