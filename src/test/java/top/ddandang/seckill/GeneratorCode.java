package top.ddandang.seckill;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.ArrayList;

public class GeneratorCode {

    public static void main(String[] args) {
        AutoGenerator generator = new AutoGenerator();
        //全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        //项目路径
        String projectPath = System.getProperty("user.dir");
        globalConfig.setOutputDir(projectPath+"/src/main/java");

        //作者名
        globalConfig.setAuthor("D");
        //是否打开输出目录 是否直接打开目录
        globalConfig.setOpen(false);
        //是否覆盖
        globalConfig.setFileOverride(false);
        //去掉Service的I前缀
        globalConfig.setServiceName("%sService");
        //ID策略 IdType.AUTO自增
        globalConfig.setIdType(IdType.AUTO);
        //日期类型 DateType.ONLY_DATE 使用 java.util.date 的Date
        //DateType.SQL_PACK java.sql 包下的Date
        //推荐使用 DateType.TIME_PACK 会使用 java.time.LocalDateTime jdk1.8以上才支持
        globalConfig.setDateType(DateType.TIME_PACK);
        //开启 swagger2 模式
        globalConfig.setSwagger2(true);

        generator.setGlobalConfig(globalConfig);

        //设置数据源
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:p6spy:mysql://localhost/seckill_demo?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&useSSL=false");
        dataSourceConfig.setDriverName("com.p6spy.engine.spy.P6SpyDriver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("123456");
        //MySql数据库
        dataSourceConfig.setDbType(DbType.MYSQL);

        generator.setDataSource(dataSourceConfig);

        // 包的配置
        PackageConfig packageConfig = new PackageConfig();
        //父包模块名
        packageConfig.setModuleName("");
        //包名
        packageConfig.setParent("top.ddandang.seckill");
        packageConfig.setEntity("model.pojo");
        packageConfig.setMapper("mapper");
        packageConfig.setService("service");
        packageConfig.setController("controller");

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        //映射的表名
        strategy.setInclude("commodity_order");
        //映射规则 NamingStrategy.no_change 则以表的字段直接输出不转换
        //NamingStrategy.underline_to_camel下划线转驼峰命名  user_id -> userId
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //设置Lombok需要导入依赖
        strategy.setEntityLombokModel(true);
        //控制器是否为RestController
        strategy.setRestControllerStyle(true);
        //逻辑删除的名字
        strategy.setLogicDeleteFieldName("deleted");
        //乐观锁
        strategy.setVersionFieldName("version");
        //自动填充配置
        //这里注意填写数据库中的字段名
        TableFill create_time = new TableFill("gmt_create", FieldFill.INSERT);
        TableFill update_time = new TableFill("gmt_modified", FieldFill.INSERT_UPDATE);
        TableFill deleted = new TableFill("deleted", FieldFill.INSERT);
        ArrayList<TableFill> tableFills = new ArrayList<>();
        tableFills.add(create_time);
        tableFills.add(update_time);
        tableFills.add(deleted);
        strategy.setTableFillList(tableFills);

        //驼峰转连字符
        //@RequestMapping("/managerUserActionHistory") -> @RequestMapping("/manager-user-action-history")
        strategy.setControllerMappingHyphenStyle(true);

        //数据库表配置
        generator.setStrategy(strategy);

        //包相关配置
        generator.setPackageInfo(packageConfig);

        //执行
        generator.execute();
    }
}
