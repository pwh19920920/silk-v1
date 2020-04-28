package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.annotation.WriteDataSource;
import com.spark.bitrade.core.PageData;
import com.spark.bitrade.entity.TestReadWrite;
import com.spark.bitrade.mapper.dao.TestReadWriteMapper;
import com.spark.bitrade.dao.TestReadWriteRepository;
import com.spark.bitrade.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 读写分离测试服务类
 *
 * 如果需要事务，自行在方法上添加@Transactional
 * 如果方法有内部有数据库操作，则必须指定@WriteDataSource还是@ReadDataSource
 * 
 * 注：AOP ，内部方法之间互相调用时，如果是this.xxx()这形式，不会触发AOP拦截，可能会导致无法决定数据库是走写库还是读库
 * 方法：
 *   为了触发AOP的拦截，调用内部方法时，需要特殊处理下，看方法getService()
 *
 */
@Service
public class TestReadWriteService {

	@Autowired
	private TestReadWriteMapper mapper; //mybatis接口

	@Autowired
	private TestReadWriteRepository repository; //jpa DAO接口

	//mybatis事务保存接口
	//@CacheEvict(cacheNames = {"testRW","testRW2"},allEntries = true) //清除缓存（所有）
	@CacheEvict(cacheNames = {"testRW"},allEntries = true)
	@WriteDataSource
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.DEFAULT,readOnly=false)
	public void insertUser(TestReadWrite u){
		this.mapper.insert(u);
	
		//如果类上面没有@Transactional,方法上也没有，哪怕throw new RuntimeException,数据库也会成功插入数据
		if(u.getId().equalsIgnoreCase("t100")) {
			throw new RuntimeException("mybatis：测试插入事务");
		}
	}

	//jpa 事务保存接口
	@CacheEvict(cacheNames = "testRW", key = "'entity:testRw:'+#u.id") //清除缓存
	@Transactional(propagation=Propagation.REQUIRED,isolation=Isolation.DEFAULT,readOnly=false)
	public void insertUserJpa(TestReadWrite u){
		this.repository.save(u);

		//如果类上面没有@Transactional,方法上也没有，哪怕throw new RuntimeException,数据库也会成功插入数据
		if(u.getId().equalsIgnoreCase("t100")) {
			throw new RuntimeException("jpa测试插入事务");
		}
	}


	/**
	 * 写事务里面调用读
	 * @param u
	 */
	public void wirteAndRead(TestReadWrite u){
		getService().insertUser(u);//这里走写库，那后面的读也都要走写库
		//这是刚刚插入的
		TestReadWrite uu = getService().findById(u.getId());
		System.out.println("==mybatis：读写混合测试中的读(刚刚插入的)====id="+u.getId()+",  user_name=" + uu.getUserName());
		//为了测试,3个库中id=1的user_name是不一样的
		TestReadWrite uuu = getService().findById("1");
		System.out.println("==mybatis：读写混合测试中的读====id=1,  user_name=" + uuu.getUserName());
	}
	public void wirteAndReadJpa(TestReadWrite u){
		getService().insertUserJpa(u);//这里走写库，那后面的读也都要走写库
		//这是刚刚插入的
		TestReadWrite uu = getService().findByIdJpaRead(u.getId());
		System.out.println("==JPA：读写混合测试中的读(刚刚插入的)====id="+u.getId()+",  user_name=" + uu.getUserName());
		//为了测试,3个库中id=1的user_name是不一样的
		TestReadWrite uuu = getService().findByIdJpaRead("1");
		System.out.println("==JPA：读写混合测试中的读====id=1,  user_name=" + uuu.getUserName());
	}
	
	public void readAndWirte(TestReadWrite u){
		//为了测试,3个库中id=1的user_name是不一样的
		TestReadWrite uu = getService(). findById("1");
		System.out.println("==mybatis：读写混合测试中的读====id=1,user_name=" + uu.getUserName());
		getService().insertUser(u);
	}
	public void readAndWirteJpa(TestReadWrite u){
		//为了测试,3个库中id=1的user_name是不一样的
		TestReadWrite uu = getService(). findByIdJpaRead("1");
		System.out.println("==JPA：读写混合测试中的读====id=1,user_name=" + uu.getUserName());
		getService().insertUserJpa(u);
	}

	//mybatis的查询接口
	@ReadDataSource
	//@Cacheable(cacheNames = "testRW", key = "'entity:testRw:'+#id") //设置数据缓存
	public TestReadWrite findById(String id){
		System.out.println("----------findById service--------------------------");
		TestReadWrite u = this.mapper.findById(id);
		return u;
	}

	//JPA接口的查询，不指定读写库
	@Cacheable(cacheNames = "testRW", key = "'entity:testRw:'+#id")
	public TestReadWrite findByIdJpa(String id){
		System.out.println("----------findByIdJpa service--------------------------");
		TestReadWrite u = this.repository.findOne(id);
		return u;
	}
	//从只读库读取（用此注解无效果）
	@ReadDataSource
	@Cacheable(cacheNames = "testRW2", key = "'entity:testRw:'+#id")
	public TestReadWrite findByIdJpaRead(String id){
		System.out.println("----------findByIdJpaRead service--------------------------");
		TestReadWrite u = this.repository.findOne(id);
		return u;
	}
	
	
	@ReadDataSource
	public PageInfo<TestReadWrite> queryPage(String userName, int pageNum, int pageSize){
		//Page<TestReadWrite> page = PageHelper.startPage(pageNum, pageSize);
		Page<TestReadWrite> page = PageData.startPageOfcompatibility(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		this.mapper.query(userName);
		return page.toPageInfo();
	}
	
	private TestReadWriteService getService(){
		// 采取这种方式的话，
		//@EnableAspectJAutoProxy(exposeProxy=true,proxyTargetClass=true)
		//必须设置为true
	/*	if(AopContext.currentProxy() != null){
			return (UserService)AopContext.currentProxy();
		}else{
			return this;
		}
		*/
		return SpringContextUtil.getBean(this.getClass());
	}
	
}
