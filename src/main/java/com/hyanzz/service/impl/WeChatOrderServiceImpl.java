package com.hyanzz.service.impl;

import com.hyanzz.config.MyStaticPropertyUtil;
import com.hyanzz.domain.MyHttpClientResult;
import com.hyanzz.domain.WeChatRechargeOrder;
import com.hyanzz.exception.CustomException;
import com.hyanzz.form.request.UserRechargeForm;
import com.hyanzz.service.WeChatOrderService;
import com.hyanzz.utils.MyHttpClientUtils;
import com.hyanzz.utils.MyHttpUtils;
import com.hyanzz.wxpay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Administrator on 2019/2/19.
 */
@Slf4j
@Service
public class WeChatOrderServiceImpl implements WeChatOrderService {

    @Autowired
    private MyStaticPropertyUtil myStaticPropertyUtil;

    @Override
    public String generateOrders(UserRechargeForm userRechargeForm) throws Exception {
        WeChatRechargeOrder weChatRechargeOrder = null;
        try {
            //时间戳+用户id 创建订单号
            String orderId = System.currentTimeMillis()+""+userRechargeForm.getAccessUserId();
            //TODO 微信统一下单所需要的常量
            String appid = myStaticPropertyUtil.getWxAppid();//appid
            String wxPayMchid = myStaticPropertyUtil.getWxPayMchid();//微信支付商户号
            String key = myStaticPropertyUtil.getWxPayKey();//微信支付key
            String notifyUrl= myStaticPropertyUtil.getNotifyUrl();//回调地址 用于接收支付结果
            String nonceStr = WXPayUtil.generateNonceStr();//随机字符串
            DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
            Map<String, String> data = new HashMap<>();
            data.put("appid", appid);
            data.put("mch_id", wxPayMchid);
            //标题
            data.put("body", "充值金额:" + userRechargeForm.getMoney()+"元");
            data.put("nonce_str", nonceStr);
            data.put("out_trade_no",orderId );//订单号
            //交易金额默认为人民币交易，接口中参数支付金额单位为【分】，参数值不能带小数
            data.put("total_fee", decimalFormat.format(userRechargeForm.getMoney()*100));
            //终端ip
            data.put("spbill_create_ip", MyHttpUtils.getRealClientIP());
            data.put("notify_url", notifyUrl);//微信回调接口
            data.put("trade_type", "NATIVE");  // trade_type=NATIVE时返回，此url用于生成支付二维码，然后提供给用户进行扫码支付。
            SortedMap<String, String> sortMap = new TreeMap<String, String>(data);
            data.put("sign", WXPayUtil.generateSignature(sortMap, key));
            String xml = WXPayUtil.mapToXml(data);
            //微信统一下单接口
            String wxPayOrder = myStaticPropertyUtil.getWxPayOrder();
            MyHttpClientResult result = MyHttpClientUtils.httpPostRequest(wxPayOrder, xml, 5000, 5000);
            Boolean validate = WXPayUtil.isSignatureValid(result.getResult(), key);
            if (validate) {
                //获取返回结果
                weChatRechargeOrder = WXPayUtil.xmlToBean(result.getResult(), WeChatRechargeOrder.class);
                //TODO 保存充值订单信息
                saveRechargeOrder(orderId);
                log.info("微信统一下单成功,订单号:{},金额:{}",orderId,userRechargeForm.getMoney());
                return myStaticPropertyUtil.getQrCodePrefix()+weChatRechargeOrder.getCodeUrl();
            }
        } catch (Exception e) {
            log.error("生成充值订单失败,",e);
            throw new CustomException("生成充值订单失败");
        }
        return null;
    }

    //TODO 保存充值订单信息
    private void saveRechargeOrder(String orderId) throws Exception {

    }
}
