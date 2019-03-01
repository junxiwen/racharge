package com.hyanzz.web;

/**
 * Created by Administrator on 2019/2/19.
 */

import com.hyanzz.common.MyConst;
import com.hyanzz.config.MyStaticPropertyUtil;
import com.hyanzz.domain.WeChatRechargeOrder;
import com.hyanzz.form.request.UserRechargeForm;
import com.hyanzz.form.response.MyResponseForm;
import com.hyanzz.service.WeChatOrderService;
import com.hyanzz.wxpay.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/***
 * 微信充值接口
 */
@Slf4j
//@RestController
@Controller
@RequestMapping("/recharge")
public class WeChatRechargeController {

    @Autowired
    private MyStaticPropertyUtil myStaticPropertyUtil;

    @Autowired
    private WeChatOrderService weChatOrderService;
    /***
     * 生成订单信息 并且生成充值扫描二维码
     * 业务流程说明：
     （1）商户后台系统根据用户选购的商品生成订单。
     （2）用户确认支付后调用微信支付【统一下单API】生成预支付交易；
     （3）微信支付系统收到请求后生成预支付交易单，并返回交易会话的二维码链接code_url。
     （4）商户后台系统根据返回的code_url生成二维码。
     （5）用户打开微信“扫一扫”扫描二维码，微信客户端将扫码内容发送到微信支付系统。
     （6）微信支付系统收到客户端请求，验证链接有效性后发起用户支付，要求用户授权。
     （7）用户在微信客户端输入密码，确认支付后，微信客户端提交授权。
     （8）微信支付系统根据用户授权完成支付交易。
     （9）微信支付系统完成支付交易后给微信客户端返回交易结果，并将交易结果通过短信、微信消息提示用户。微信客户端展示支付交易结果页面。
     （10）微信支付系统通过发送异步消息通知商户后台系统支付结果。商户后台系统需回复接收情况，通知微信后台系统不再发送该单的支付通知。
     （11）未收到支付通知的情况，商户后台系统调用【查询订单API】。
     （12）商户确认订单已支付后给用户发货。
     * @return
     */
    @PostMapping("/getQrCode")
    public MyResponseForm<String> saveOrderAndCreateQrCode(UserRechargeForm userRechargeForm){
        try {
            //TODO 方便测试，金额设置为0.01
            userRechargeForm.setMoney(0.01);
            //微信统一下单并返回二维码URL
            String qrCode = weChatOrderService.generateOrders(userRechargeForm);
            return new MyResponseForm<>(qrCode).returnSuccess();
        } catch (Exception e) {
            log.error("生成二维码失败,", e);
            return new MyResponseForm().returnError("生成二维码失败");
        }
    }


    @GetMapping("/testRedirect")
    public String testRedirect() throws Exception{
        return "redirect:http://www.qq.com";
    }

    /***
     * 微信回调商户接口
     * @param request
     * @param response
     */
    @PostMapping(value = "/wxPayCallback")
    public String wxPayCallback(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();
        result.put("return_code", MyConst.WeCHAT_RETURN_CODE_FAIL);
        String content = "";
        String key = myStaticPropertyUtil.getWxPayKey();
        String appid = myStaticPropertyUtil.getWxAppid();
        String mchid = myStaticPropertyUtil.getWxPayMchid();
        // 获取收到的报文
        BufferedReader reader1 = null;
        try {
            reader1 = request.getReader();
            String line = "";
            StringBuffer inputString = new StringBuffer();
            while ((line = reader1.readLine()) != null) {
                inputString.append(line);
            }
            request.getReader().close();
            content = inputString.toString();
            log.info("wechat callback:{}", content);
            WeChatRechargeOrder weChatRechargeOrder = WXPayUtil.xmlToBean(content, WeChatRechargeOrder.class);
            String code = weChatRechargeOrder.getReturnCode();
            if (code.equals(MyConst.WeCHAT_RETURN_CODE_SUCCESS)) {
                Boolean validate = WXPayUtil.isSignatureValid(content, key);
                if (validate) {//校验正确
                    if (appid.equals(weChatRechargeOrder.getAppid()) && mchid.equals(weChatRechargeOrder.getMchId())) {
                        result.put("return_code", MyConst.WeCHAT_RETURN_CODE_SUCCESS);
                        String orderId = weChatRechargeOrder.getOrderId();//订单号
                        //TODO 根据订单号 修改订单信息
                        log.error("微信回调OK，充值成功,订单号:{}",orderId);
                    } else {
                        result.put("return_msg", "商户信息错误");
                    }
                } else {
                    result.put("return_msg", "签名错误");
                }
            }
            String xml = WXPayUtil.mapToXml(result);
            log.info("微信回调处理完毕——————————————————————————————");
            log.info("返回给微信的xml:{}", xml);
            //response.getWriter().write(xml);
            return "redirect:http://www.baidu.com";
        } catch (Exception e) {
            log.error("微信回调处理异常,错误信息:", e);
            return "redirect:http://www.qq.com";
        }
    }
}
