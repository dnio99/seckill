# 基于SpringBoot实现的秒杀系统Demo

#### 介绍
基于SpringBoot + Mybatis-Plus + Redis + 令牌桶 + Sentinel实现的一个秒杀系统的Demo

#### 详细文档可以参照CSDN地址
[https://blog.csdn.net/d875708765/article/details/108570008](https://blog.csdn.net/d875708765/article/details/108570008)

# 秒杀系统

## 1、什么是秒杀

“秒杀”这一词多半出现在购物方面，但是又不单单只是购物，比如12306购票和学校抢课（大学生的痛苦）也可以看成一个秒杀。秒杀应该是一个“三高”，这个三高不是指高血脂，高血压和高血糖。而是指“高并发”，“高性能”和“高可用”。

## 2、秒杀的问题

### 2.1、超卖

超卖是秒杀系统最大的一个问题，如果出现超卖这个锅只有程序员进行背了，有些秒杀系统宁愿用户等待时间长点或者体验稍微的降低一点也不愿意出现超卖的限制。（系统每出现一次超卖，就损失一位程序员）

### 2.2、高并发

秒杀系统会在一瞬间发送一（亿）点点请求，这时候如果服务器蹦了那就会出现常见的页面了，所以通常一个秒杀系统都是一个单独的服务（**单一职责**）。这个可以通过限流和负载均衡处理。

### 2.3、恶意请求

恶意请求的意思就是一个人可能一次性购买很多（有时候全部也不在话下），然后再将这些东西转手卖出去。这时候是不是浮现出两个字“**黄牛**”，这tm不是黄牛是什么。逼近一个人的手速再快（多年单身的手速），也比不过机器请求。不要小看一些黄牛可能他们使用的系统比一些大公司的系统都要NB。

### 2.4、高性能

如果一个秒杀系统在你点击了**抢购按钮**的时候然后出现一个**loading**的图标一直在那里转啊转一直转了几十分钟，然后通知你商品已售空（哈哈哈哈），刺激。

## 3、解决方法

1. 乐观锁防止超卖（记得加事务）
2. Redis
3. Alibaba Sentinel限流和熔断
4. 谷歌令牌桶限流
5. 负载均衡等

## 4、项目落地实现

### 4.1、数据库准备

**用户表（user）**

![image-20200913193315278](http://dd.ddandang.top/typora/image/image-20200913193315278.png)

**商品表（goods）**

![image-20200913193353446](http://dd.ddandang.top/typora/image/image-20200913193353446.png)

**订单表（commodity_order）**

注意：订单表表名不要叫order，会出大问题。order是数据库的一个关键之，如果真的叫这个名字则需要在查询的时候加上       **``**



![image-20200913193441499](http://dd.ddandang.top/typora/image/image-20200913193441499.png)

```sql
/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : localhost:3306
 Source Schema         : seckill_demo

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 13/09/2020 19:46:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for commodity_order
-- ----------------------------
DROP TABLE IF EXISTS `commodity_order`;
CREATE TABLE `commodity_order`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '订单Id',
  `user_id` int(0) NOT NULL COMMENT '用户Id',
  `goods_id` int(0) NOT NULL COMMENT '商品Id',
  `gmt_create` datetime(0) NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商品名',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `stock` int(0) NOT NULL COMMENT '库存',
  `sale` int(0) NOT NULL COMMENT '售卖数量',
  `version` int(0) NOT NULL COMMENT '乐观锁版本号',
  `gmt_create` datetime(0) NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '用户Id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '姓名',
  `usernam` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `gmt_create` datetime(0) NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
```



### 4.2、项目创建

这里使用了SpringBoot创建项目，自己的项目使用了Mybatis-Plus的代码生成器这里依赖就不写出来了，还有基本的SpringBoot依赖也没有写出来。

导入的依赖有：

```xml
    <!--MybatisPlus-->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.3.2</version>
    </dependency>
    <!--swagger-->
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>2.10.5</version>
    </dependency>
    <!--druid-->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.22</version>
    </dependency>
    <!-- mysql -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.20</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!--guava包含谷歌令牌桶-->
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>29.0-jre</version>
    </dependency>
```


