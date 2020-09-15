package top.ddandang.seckill.service;

import top.ddandang.seckill.model.pojo.CommodityOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author D
 * @since 2020-09-13
 */
public interface CommodityOrderService extends IService<CommodityOrder> {

    /**
     * 演示超卖现象
     *
     * @param userId 用户Id
     * @param goodsId  商品Id
     * @return 生成的订单Id
     */
    int overSold(Integer userId, Integer goodsId);


    /**
     * 使用乐观锁防止超卖，乐观锁用户体验也更好
     *
     * @param userId 用户Id
     * @param goodsId  商品Id
     * @return 生成的订单Id
     */
    int optimisticLockSold(Integer userId, Integer goodsId);

    /**
     * redis计数器实现限流
     * @return true则不拦截 false进行拦截
     */
    boolean redisCurrentLimit();
}
