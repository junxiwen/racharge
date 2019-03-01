package com.hyanzz.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2019/2/15.
 */
@Slf4j
@Component
@Data
@PropertySource("classpath:static/config.yml")
public class MyStaticPropertyUtil {
    @Value("${qrCodePrefix}")
    private String qrCodePrefix;//二维码前缀
    @Value("${wxAppid}")
    private String wxAppid;//appid
    @Value("${wxPayMchid}")
    private String wxPayMchid;
    @Value("${wxPayKey}")
    private String wxPayKey;
    @Value("${wxPayOrder}")
    private String wxPayOrder;//微信统一下单接口
    @Value("${notifyUrl}")
    private String notifyUrl;//回调地址
}
