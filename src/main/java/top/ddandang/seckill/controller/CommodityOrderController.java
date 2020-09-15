package top.ddandang.seckill.controller;


import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import top.ddandang.seckill.service.CommodityOrderService;
import top.ddandang.seckill.utils.LeakyBucket;
import top.ddandang.seckill.utils.R;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author D
 * @since 2020-09-13
 */
@RestController
@RequestMapping("/commodity-order")
@Slf4j
public class CommodityOrderController {

    @Resource
    private CommodityOrderService commodityOrderService;

    /**
     * 创建漏桶
     */
    private final static LeakyBucket leakyBucket = new LeakyBucket(1, 5);

    /**
     * 创建令牌桶的实例
     */
    private final RateLimiter rateLimiter = RateLimiter.create(100);


    /**
     * 会出现超卖的接口
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/overSold")
    public R overSold(Integer userId, Integer goodsId) {
        log.info("用户Id = {},商品Id = {}", userId, goodsId);
        try {
            int orderId = commodityOrderService.overSold(userId, goodsId);
            return R.success().data("orderId", orderId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }

    /**
     * 悲观锁解决超卖，一次只有一个线程进入后面的线程都在阻塞
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/pessimisticLockSold")
    public R pessimisticLockSold(Integer userId, Integer goodsId) {
        log.info("用户Id = {},商品Id = {}", userId, goodsId);
        try {
            synchronized (this) {
                int orderId = commodityOrderService.overSold(userId, goodsId);
                return R.success().data("orderId", orderId);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }

    /**
     * 乐观锁解决超卖
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/optimisticLockSold")
    public R optimisticLockSold(Integer userId, Integer goodsId) {
        log.info("用户Id = {},商品Id = {}", userId, goodsId);
        try {
            int orderId = commodityOrderService.optimisticLockSold(userId, goodsId);
            return R.success().data("orderId", orderId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }

    private static int count = 0;

    /**
     * 乐观锁解决超卖 + 漏桶限流
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/optimisticLockAndLeakyBucketSold")
    public R optimisticLockAndLeakyBucketSold(Integer userId, Integer goodsId) {
        try {
            boolean enterBucket = leakyBucket.enterBucket();
            if (!enterBucket) {
                log.error("系统繁忙请稍后再试");
                return R.failed().message("系统繁忙请稍后再试");
            }
            count++;
            log.error("请求的次数 = {}", count);
//            int orderId = commodityOrderService.optimisticLockSold(userId, goodsId);
            return R.success().data("orderId", 1);
        } catch (RuntimeException e) {
            return R.failed().message(e.getMessage());
        }
    }

    /**
     * 乐观锁解决超卖 + 令牌桶限流
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/optimisticLockAndTokenBucketSold")
    public R optimisticLockAndTokenBucketSold(Integer userId, Integer goodsId) {
        try {
//            double acquire = rateLimiter.acquire();
//            log.info("等待时间：{}", acquire);

            //获取1个令牌 如果等待时间超过了1秒则拒绝
            boolean acquire = rateLimiter.tryAcquire(1, 1, TimeUnit.SECONDS);
            if (!acquire) {
                log.error("系统繁忙请稍后再试");
                return R.failed().message("系统繁忙请稍后再试");
            }
            count++;
            log.info("请求的次数 = {}", count);
            int orderId = commodityOrderService.optimisticLockSold(userId, goodsId);
            return R.success().data("orderId", orderId);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }

    /**
     * 乐观锁解决超卖 + 令牌桶限流
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/optimisticLockAndRedisCountSold")
    public R optimisticLockAndRedisCountSold(Integer userId, Integer goodsId) {
        try {
            boolean acquire = commodityOrderService.redisCurrentLimit();
            if (!acquire) {
                log.error("系统繁忙请稍后再试");
                return R.failed().message("系统繁忙请稍后再试");
            }
            count++;
            log.info("请求的次数 = {}", count);
            //int orderId = commodityOrderService.optimisticLockSold(userId, goodsId);
            return R.success().data("orderId", 1);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }

    /**
     * 乐观锁解决超卖 + Alibaba Sentinel限流
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 订单编号
     */
    @PostMapping("/optimisticLockAndAlibabaSentinelSold")
    public R optimisticLockAndAlibabaSentinelSold(Integer userId, Integer goodsId) {
        try {
            count++;
            log.info("请求的次数 = {}", count);
            //int orderId = commodityOrderService.optimisticLockSold(userId, goodsId);
            return R.success().data("orderId", 1);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return R.failed().message(e.getMessage());
        }
    }
}

