package com.spark.bitrade;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.core.Menu;
import com.spark.bitrade.entity.SysRole;
import com.spark.bitrade.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebApplicationTest {

	@Autowired
	private AdvertiseService advertiseService;
	@Autowired
	private TestQueryDSLService testQueryDSLService;
	@Autowired
	private WithdrawRecordService withdrawRecordService;
	//@Test
	public void testConfig(){
		System.out.print(advertiseService.findOne(1L).getCreateTime());
	}
	@Autowired
	private SysRoleService sysRoleService;

@Autowired
private ExchangeOrderDetailService exchangeOrderDetailService ;

@Autowired
private SysAdvertiseService sysAdvertiseService ;

@Autowired
private SysRoleService roleService ;

	@Test
	public void testPage() throws Exception {
		SysRole role = roleService.findOne(28L);
		System.out.println(role);
		//sysAdvertiseService.deleteBatch(new String[]{"111","222"});
		//exchangeOrderDetailService.add();
		/*EntityPage<ExchangeOrderDetailAggregation> result = exchangeOrderDetailService.findAllByPageNo(1,15);
		System.out.println(result);
		System.out.println("orderId-----------:"+result.getList().get(2).getOrderId());
		System.out.println(result.getList());*/
	}

	@Test
	public void test() throws Exception {
		withdrawRecordService.test();
	}

	@Test
	public void testPermissions(){
		List<Menu> list = sysRoleService.toMenus(sysRoleService.getAllPermission(), 0L);
		System.out.println(JSON.toJSONString(list));
	}
}
