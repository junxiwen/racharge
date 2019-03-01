package com.hyanzz;

import com.hyanzz.form.request.UserRechargeForm;
import com.hyanzz.service.WeChatOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RechargeApplicationTests {

	@Autowired
	private WeChatOrderService weChatOrderService;


	@Test
	public void contextLoads() throws Exception{
		UserRechargeForm userRechargeForm = new UserRechargeForm();
		userRechargeForm.setMoney(0.01);
		userRechargeForm.setAccessUserId(007);
		String url = weChatOrderService.generateOrders(userRechargeForm);
		System.out.println("二维码:"+url);
	}

}
