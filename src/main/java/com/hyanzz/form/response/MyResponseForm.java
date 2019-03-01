package com.hyanzz.form.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator on 2019/2/19.
 */

/***
 * 统一返回数据
 * @param <T>
 */
@Data
@NoArgsConstructor
public class MyResponseForm<T> {
    private int code = 0;
    private String message = "request success";
    private T data;

    private static final int SUCCESS = 0;
    private static final int ERROR = 1;


    /**
     * 构造函数
     * @param data 需要返回的数据
     */
    public MyResponseForm(T data) {
        this.data = data;
    }


    /**
     * 请求正确执行: code = 0
     *
     * @param message 提示信息，默认：request success
     * @return MyResponseForm
     */
    @JsonIgnore
    public MyResponseForm<T> returnSuccess(String... message) {
        this.code = SUCCESS;
        if (message == null || message.length == 0) {
            this.message = "request success";
        } else {
            this.message = message[0];
        }
        return this;
    }

    /**
     * 请求出现未知错误: code = 1
     *
     * @param message 提示信息，默认：request error
     * @return MyResponseForm
     */
    @JsonIgnore
    public MyResponseForm<T> returnError(String... message) {
        this.code = ERROR;
        if (message == null || message.length == 0) {
            this.message = "request error";
        } else {
            this.message = message[0];
        }
        return this;
    }
}
