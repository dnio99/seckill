package top.ddandang.seckill.service.impl;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import top.ddandang.seckill.mapper.GoodsMapper;
import top.ddandang.seckill.model.pojo.CommodityOrder;
import top.ddandang.seckill.mapper.CommodityOrderMapper;
import top.ddandang.seckill.model.pojo.Goods;
import top.ddandang.seckill.service.CommodityOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.ddandang.seckill.utils.RedisUtil;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author D
 * @since 2020-09-13
 */
@Service
@Slf4j
public class CommodityOrderServiceImpl extends ServiceImpl<CommodityOrderMapper, CommodityOrder> implements CommodityOrderService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private CommodityOrderMapper commodityOrderMapper;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 接口限流的Key值
     */
    private final String LIMIT_KEY = "limitKey";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int overSold(Integer userId, Integer goodsId) {
        //判断库存是否充足
        Goods goods = checkInventory(goodsId);
        //扣除库存
        deductInventory(goods);
        //生成订单
        return generateOrders(userId, goodsId);

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int optimisticLockSold(Integer userId, Integer goodsId) {
        //判断库存是否充足
        Goods goods = checkInventory(goodsId);
        //扣除库存 使用了乐观锁
        deductInventoryOptimisticLock(goods);
        //生成订单
        return generateOrders(userId, goodsId);
    }

    /**
     * 检测商品的库存
     *
     * @param goodsId 商品Id
     * @return 如果库存充足则返回商品的信息 否则抛出异常
     */
    private Goods checkInventory(Integer goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        // 如果库存等于售卖 则商品售空
        if (goods.getSale() >= goods.getStock()) {
            throw new RuntimeException(goods.getName() + "已经售空！！");
        }
        return goods;
    }

    /**
     * 给用户生成商品订单
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 生成的订单Id
     */
    private Integer generateOrders(Integer userId, Integer goodsId) {
        CommodityOrder order = new CommodityOrder()
                .setUserId(userId)
                .setGoodsId(goodsId);
        System.out.println(order);
        int row = commodityOrderMapper.insert(order);
        if (row == 0) {
            throw new RuntimeException("生成订单失败！！");
        }
        return order.getId();
    }

    /**
     * 扣除库存（增加售卖数量）这里没有使用乐观锁
     *
     * @param goods 商品信息
     */
    private void deductInventory(Goods goods) {
        int updateRows = goodsMapper.updateSaleNoOptimisticLock(goods);
        if (updateRows == 0) {
            throw new RuntimeException("抢购失败，请重试！");
        }
    }

    /**
     * 扣除库存（增加售卖数量）使用了乐观锁
     *
     * @param goods 商品信息
     */
    private void deductInventoryOptimisticLock(Goods goods) {
        int updateRows = goodsMapper.updateSaleOptimisticLock(goods);
        if (updateRows == 0) {
            throw new RuntimeException("抢购失败，请重试！");
        }
    }

    /**
     * 使用redis计数器进行限流
     * @return true可以正常访问 false为限制访问
     */
    @Override
    @Synchronized
    public boolean redisCurrentLimit() {
        // 返回加一之后的值
        long incr = redisUtil.incr(LIMIT_KEY, 1);
        if (incr >= 200) {
            redisUtil.decr(LIMIT_KEY, 1);
            return false;
        }
        return true;
    }

    /**
     * initialDelay 服务启动100秒后执行一次
     * fixedRate 每隔50毫秒执行一次
     */
//    @Scheduled(initialDelay = 100,fixedRate = 50)
//    public void decrease() {
//        Integer count = (Integer) redisUtil.get(LIMIT_KEY);
//        log.info("count = {}", count);
//        if (count != null && count > 0) {
//            redisUtil.decr(LIMIT_KEY, 2);
//        }
//    }
}
