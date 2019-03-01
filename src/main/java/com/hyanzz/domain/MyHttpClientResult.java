package com.hyanzz.domain;

import lombok.Data;

/**
 * Created by Administrator on 2019/2/19.
 */
@Data
public class MyHttpClientResult {
    //http请求状态码
    private int code;
    //http请求结果
    private String result;
}
