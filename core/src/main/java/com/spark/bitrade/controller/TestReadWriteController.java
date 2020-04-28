package com.spark.bitrade.controller;

import com.github.pagehelper.PageInfo;
import com.spark.bitrade.entity.TestReadWrite;
import com.spark.bitrade.service.TestReadWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import springfox.documentation.annotations.ApiIgnore;

/***
 * 数据读写分离测试控制类
  *  mybatis提供1写多读配置（其中写支撑事务）
  *
  * 示例：
 *    mybatis接口：
 *      http://api.cex.cn/exchange/testRW/get/1     获取数据
 *      http://api.cex.cn/exchange/testRW/add?id=t04&userName=test004    添加数据，t100会触发异常
 *      http://api.cex.cn/exchange/testRW/addAndRead?id=t04&userName=test004  新添加数据，再读取数据
 *      http://api.cex.cn/exchange/testRW/readAndAdd?id=t04&userName=test004  先读取数据，再新添加数据
 *      http://api.cex.cn/exchange/testRW/queryPage   分页查询数据
 *
 *    JPA接口（注：数据源无法切换）：
 *      http://api.cex.cn/exchange/testRW/getJpa/1     获取数据
 *      http://api.cex.cn/exchange/testRW/getJpaRead/1     获取数据
 *      http://api.cex.cn/exchange/testRW/addJpa?id=t04&userName=test004   添加数据，t100会触发异常
 *      http://api.cex.cn/exchange/testRW/addAndReadJpa?id=t04&userName=test004  新添加数据，再读取数据
 *      http://api.cex.cn/exchange/testRW/readAndAddJpa?id=t04&userName=test004  先读取数据，再新添加数据
 *
 * @author yangch
 * @time 2018.06.20 14:26
 */
@ApiIgnore
@Controller
@RequestMapping("/testRW")
public class TestReadWriteController {
	@Autowired
	private TestReadWriteService userService;
	
	@RequestMapping("/hello")
	@ResponseBody
	public String hello(){
		return "hello";
	}
	/**
	 * mybatis 测试插入
	 * @return
	 */
	@RequestMapping("/add")
	@ResponseBody
	public String add(String id, String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.insertUser(u);
		return u.getId()+"    " + u.getUserName();
	}
	/**
	 * mybatis 测试插入
	 * @return
	 */
	@RequestMapping("/addJpa")
	@ResponseBody
	public String addJpa(String id, String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.insertUserJpa(u);
		return u.getId()+"    " + u.getUserName();
	}

	/**
	 * 测试读
	 * @param id
	 * @return
	 */
	@RequestMapping("/get/{id}")
	@ResponseBody
	public String findById(@PathVariable("id") String id){
		TestReadWrite u = this.userService.findById(id);
		return u.getId()+"    " + u.getUserName();
	}
	/**
	 * 测试读
	 * @param id
	 * @return
	 */
	@RequestMapping("/getJpa/{id}")
	@ResponseBody
	public String findByIdJpa(@PathVariable("id") String id){
		TestReadWrite u = this.userService.findByIdJpa(id);
		return u.getId()+"    " + u.getUserName();
	}
	@RequestMapping("/getJpaRead/{id}")
	@ResponseBody
	public String findByIdJpaRead(@PathVariable("id") String id){
		TestReadWrite u = this.userService.findByIdJpaRead(id);
		return u.getId()+"    " + u.getUserName();
	}

	/**
	 * mybatis测试写然后读
	 * @param id
	 * @param userName
	 * @return
	 */
	@RequestMapping("/addAndRead")
	@ResponseBody
	public String addAndRead(String id,String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.wirteAndRead(u);
		return u.getId()+"    " + u.getUserName();
	}
	/**
	 * JPA测试写然后读（无效果）
	 * @param id
	 * @param userName
	 * @return
	 */
	//@RequestMapping("/addAndReadJpa")
	//@ResponseBody
	public String addAndReadJpa(String id,String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.wirteAndReadJpa(u);
		return u.getId()+"    " + u.getUserName();
	}
	
	/**
	 * mybatis测试读然后写
	 * @param id
	 * @param userName
	 * @return
	 */
	@RequestMapping("/readAndAdd")
	@ResponseBody
	public String readAndWrite(String id,String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.readAndWirte(u);
		return u.getId()+"    " + u.getUserName();
	}
	/**
	 * JPA测试读然后写（无效果）
	 * @param id
	 * @param userName
	 * @return
	 */
	//@RequestMapping("/readAndAddJpa")
	//@ResponseBody
	public String readAndWriteJpa(String id,String userName){
		TestReadWrite u = new TestReadWrite();
		u.setId(id);
		u.setUserName(userName);
		this.userService.readAndWirteJpa(u);
		return u.getId()+"    " + u.getUserName();
	}
	
	/**
	 * 测试分页插件
	 * @return
	 */
	@RequestMapping("/queryPage")
	@ResponseBody
	public String queryPage(){
		PageInfo<TestReadWrite> page = this.userService.queryPage("tes", 1, 2);
		StringBuilder sb = new StringBuilder();
		sb.append("<br/>总页数=" + page.getPages());
		sb.append("<br/>总记录数=" + page.getTotal()) ;
		for(TestReadWrite u : page.getList()){
			sb.append("<br/>" + u.getId() + "      " + u.getUserName());
		}
		System.out.println("分页查询....\n" + sb.toString());
		return sb.toString();
	}
	
}
