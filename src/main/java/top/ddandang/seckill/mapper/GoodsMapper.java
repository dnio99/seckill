package top.ddandang.seckill.mapper;

import org.apache.ibatis.annotations.Update;
import top.ddandang.seckill.model.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author D
 * @since 2020-09-13
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 不包含乐观锁
     * 根据商品Id去扣除库存 数据库操作只有一个线程去操作
     *
     * @param goods 商品
     * @return 影响行数
     */
    @Update("update goods set sale = sale + 1, gmt_modified = now() where id = #{id}")
    int updateSaleNoOptimisticLock(Goods goods);

    /**
     * 包含乐观锁
     * 根据商品Id去扣除库存 数据库操作只有一个线程去操作
     * 注意version++的时候不要在java里面，应该直接在mysql语句中写
     *
     * @param goods 商品
     * @return 影响行数
     */
    @Update("update goods set sale = sale + 1,version = version + 1, gmt_modified = now() where id = #{id} and version = #{version}")
    int updateSaleOptimisticLock(Goods goods);
}
