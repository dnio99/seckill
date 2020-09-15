package top.ddandang.seckill.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.ddandang.seckill.enums.ResultCode;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 封装向客户端发回的响应数据
 * </p>
 *
 * @author D
 * @version 2.0
 * @date 2020/6/5 8:26
 */
@Data
@NoArgsConstructor
@ApiModel(description = "返回响应数据")
public class R {
    /**
     * 响应是否成功
     */
    @ApiModelProperty(value = "是否成功")
    private Boolean success;
    /**
     * 响应码
     */
    @ApiModelProperty(value = "响应状态码")
    private Integer code;
    /**
     * 响应信息
     */
    @ApiModelProperty(value = "响应信息")
    private String message;
    /**
     * 成功返回的数据
     */
    @ApiModelProperty(value = "成功返回的数据")
    private Map<String, Object> data = new HashMap<>();

    public R(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static R success() {
        return new R(ResultCode.SUCCESS.getSuccess()
                , ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    public static R failed() {
        return new R(ResultCode.FAILED.getSuccess()
                , ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage());
    }

    public static R error() {
        return new R(ResultCode.ERROR.getSuccess()
                , ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * @param map 存放数据的map
     * @return 响应的数据
     */
    public R data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * @param key   参数名
     * @param value 数据
     * @return 返回的数据 key-value
     */
    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 自定义返回信息
     *
     * @param message 返回信息
     * @return 返回this
     */
    public R message(String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * 自定义返回状态码
     *
     * @param code 状态码
     * @return 返回this
     */
    public R code(Integer code) {
        this.setCode(code);
        return this;
    }

    /**
     * 自定义响应状态
     *
     * @param success 成功状态
     * @return 返回this
     */
    public R success(Boolean success) {
        this.setSuccess(success);
        return this;
    }
}
