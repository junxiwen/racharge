package com.hyanzz.service;

import com.hyanzz.form.request.UserRechargeForm;

/**
 * Created by Administrator on 2019/2/19.
 */
public interface WeChatOrderService {
    //生成订单并返回二维码
    String generateOrders(UserRechargeForm userRechargeForm) throws Exception;
}
