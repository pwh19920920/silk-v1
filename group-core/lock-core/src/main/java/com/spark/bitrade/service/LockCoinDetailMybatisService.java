package com.spark.bitrade.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.constant.LockType;
import com.spark.bitrade.entity.LockCoinDetail;
import com.spark.bitrade.entity.LockCoinDetailBuilder;
import com.spark.bitrade.entity.UnLockCoinDetailBuilder;
import com.spark.bitrade.entity.UnlockCoinDetail;
import com.spark.bitrade.mapper.dao.LockCoinDetailMapper;
import com.spark.bitrade.vo.StoLockDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class LockCoinDetailMybatisService {

	@Autowired
	private LockCoinDetailMapper mapper; //mybatis接口


	/**
	 * 通过用户和活动id查询锁仓记录
	 * @author tansitao
	 * @time 2018/6/22 14:33 
	 */
	@ReadDataSource
	public PageInfo<LockCoinDetailBuilder> queryPageByMemberAndActId(long activitieId, long memberId, LockType lockType, int pageNum, int pageSize){
		Page<LockCoinDetailBuilder> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		this.mapper.pageQueryByMemberAndActivitId(activitieId, memberId, lockType.getOrdinal());
		return page.toPageInfo();
	}

	/**
	 * 通过用户查询所有锁仓记录
	 * @author tansitao
	 * @time 2018/6/22 14:33 
	 */
	@ReadDataSource
	public PageInfo<LockCoinDetailBuilder> queryPageByMember(long memberId, int type, int pageNum, int pageSize){
		Page<LockCoinDetailBuilder> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		this.mapper.pageQueryByMember(memberId, type);
		return page.toPageInfo();
	}

	/**
	  * 通过用户查询锁仓记录
	  * @author fatKarin
	  * @time 2018/6/22 14:33 
	  */
	@ReadDataSource
	public PageInfo<LockCoinDetailBuilder> queryPageByMemberAndTypeAndSymbol(long memberId, int type, int pageNum, int pageSize, String symbol){
		Page<LockCoinDetailBuilder> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		this.mapper.pageQueryByMemberAndTypeAndSymbol(memberId, type, symbol);
		return page.toPageInfo();
	}

	/**
	  * 查询单条锁仓记录
	  * @author tansitao
	  * @time 2018/6/22 14:33 
	  */
	@ReadDataSource
	public LockCoinDetailBuilder findOneById(long id){
		return this.mapper.findOneByid(id);
	}

	/**
	  * 查询解锁记录
	  * @author tansitao
	  * @time 2018/6/22 14:33 
	  */
	@ReadDataSource
	public PageInfo<UnLockCoinDetailBuilder> queryPageHandleLockByMember(long memberId, int pageNum, int pageSize){
		Page<UnLockCoinDetailBuilder> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		this.mapper.findHandleLockDetail(memberId);
		return page.toPageInfo();
	}

	/**
	 * 查询解锁记录
	 * @author tansitao
	 * @time 2018/6/22 17:18 
	 */
	@ReadDataSource
	public List<UnlockCoinDetail> findHandleUnLockByMember(long lockCoinDetailId, int pageNum, int pageSize){
//		Page<UnlockCoinDetail> page = PageHelper.startPage(pageNum, pageSize);
		//PageHelper会自动拦截到下面这查询sql
		List<UnlockCoinDetail> unlockCoinDetails = this.mapper.findHandleUnLockDetail(lockCoinDetailId);
		return unlockCoinDetails;
	}

	@ReadDataSource
	public LockCoinDetail getMissRewardLock(Long lockCoinDetailId) {
		return mapper.queryLockDetailById(lockCoinDetailId);
	}

	@ReadDataSource
	public boolean isExistRewardRecord(Long lockCoinDetailId) {
		int row = mapper.isExistRewardRecord(lockCoinDetailId);
		return row > 0 ? true : false;
	}
}
