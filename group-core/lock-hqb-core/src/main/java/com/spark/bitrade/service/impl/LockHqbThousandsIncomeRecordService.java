package com.spark.bitrade.service.impl;

import com.spark.bitrade.vo.AnnualRateOfWeekVO;
import com.spark.bitrade.entity.LockHqbCoinSettging;
import com.spark.bitrade.entity.LockHqbIncomeRecord;
import com.spark.bitrade.entity.LockHqbThousandsIncomeRecord;
import com.spark.bitrade.mapper.dao.LockHqbThousandsIncomeRecordMapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.spark.bitrade.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
public class LockHqbThousandsIncomeRecordService extends ServiceImpl<LockHqbThousandsIncomeRecordMapper, LockHqbThousandsIncomeRecord> implements ILockHqbThousandsIncomeRecordService {

    @Autowired
    private LockHqbThousandsIncomeRecordMapper lockHqbThousandsIncomeRecordMapper;

    @Autowired
    private ILockHqbCoinSettgingService iLockHqbCoinSettgingService;

    @Autowired
    private ILockHqbIncomeRecordService iLockHqbIncomeRecordService;

    @Override
    public BigDecimal getAnnulYieldOfDay(String coinSymbol, Date date, String appId) {
        /// 日年化率=日万份收益×365 ×100%

        String dateStr = DateUtil.getDateYMD(date);
        //  日万份收益
        BigDecimal tenThousandIncome = this.yesterdayThousandsIncome(appId, coinSymbol, Integer.valueOf(dateStr)).getTenThousandIncome();

        // 日年化率 小数格式
        return tenThousandIncome.multiply(new BigDecimal("365"));
    }

    @Override
    @Cacheable(cacheNames = "lockHqb", key = "'entity:lockHqb:thousandIncome:' + #date + '-' + #appId + '-' + #symbol")
    public LockHqbThousandsIncomeRecord getTenThousandIncomeToday(String symbol, String appId, Date date) {
        // 日期 转换为 YYMMDD 格式
        String dateStr = DateUtil.getDateYMD(date);

        LockHqbThousandsIncomeRecord result = lockHqbThousandsIncomeRecordMapper.findTenThousandIncomeOfDay(symbol, dateStr, appId);

        // 查询为空，新增一条万份收益记录
        if (result == null) {
            log.info("============ 查询日期" + DateUtil.getDate(new Date()) + "万份收益记录为空，准备新增 ============");
            LockHqbCoinSettging lockHqbCoinSettging = iLockHqbCoinSettgingService.findValidSettingByAppIdAndSymbol(appId, symbol);

            if (lockHqbCoinSettging == null) {
                return null;
            }

            String id = appId + symbol + DateUtil.getDateYMD(date);

            LockHqbThousandsIncomeRecord lockHqbThousandsIncomeRecord = new LockHqbThousandsIncomeRecord();
            lockHqbThousandsIncomeRecord.setTenThousandIncome(lockHqbCoinSettging.getDayRate());
            lockHqbThousandsIncomeRecord.setCoinSymbol(lockHqbCoinSettging.getCoinSymbol());
            lockHqbThousandsIncomeRecord.setId(id);
            lockHqbThousandsIncomeRecord.setAppId(appId);
            lockHqbThousandsIncomeRecord.setOpTime(Long.valueOf(dateStr));

            lockHqbThousandsIncomeRecordMapper.insert(lockHqbThousandsIncomeRecord);
            return lockHqbThousandsIncomeRecord;
        }
        return result;
    }

    @Override
    public BigDecimal getAverageAnnulYieldOfWeek(Date yesterday, String coinSymbol, String appId) {
        Calendar calendar = Calendar.getInstance();
        // 过去七日万份收益总和
        BigDecimal amountOfWeek = new BigDecimal("0");
        // 过去7天平均日利率 = 过去7天日利率的和÷7
        BigDecimal result;
        //  数据查询为空计数器
        int nullDataCounter = 0;

        calendar.setTime(yesterday);

        // 循环查询日万份收益，计算过去七日万份收益总和
        for (int i = 0; i < 7; i++) {
            String dateStr = DateUtil.getDateYMD(calendar.getTime());

            LockHqbThousandsIncomeRecord lockHqbThousandsIncomeRecord = this.yesterdayThousandsIncome(appId, coinSymbol, Integer.valueOf(dateStr));
            if (lockHqbThousandsIncomeRecord == null) {
                calendar.add(Calendar.HOUR, -24);
                nullDataCounter++;
                continue;
            }
            BigDecimal tenThousandIncomeADay = lockHqbThousandsIncomeRecord.getTenThousandIncome();
            amountOfWeek = amountOfWeek.add(tenThousandIncomeADay);
            calendar.add(Calendar.HOUR, -24);
        }
        // 过去7日万份收益记录 全为空， 返回 null
        if(nullDataCounter == 7) {
            return null;
        }
        // 根据公式计算过去7天平均日利率
        result = amountOfWeek.divide(new BigDecimal(7 - nullDataCounter), 8, BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros();

        return result;
    }

    @Override
    public LockHqbThousandsIncomeRecord yesterdayThousandsIncome(String appId, String unit, Integer time) {
        return lockHqbThousandsIncomeRecordMapper.yesterdayThousandsIncome(appId, unit, time);
    }

    @Override
    public List<LockHqbThousandsIncomeRecord> lastMonthThousandsIncome(String appId, String unit, Integer limit) {
        return lockHqbThousandsIncomeRecordMapper.lastMonthThousandsIncome(appId, unit, limit);
    }

    @Override
    public AnnualRateOfWeekVO getAnnualRateOfWeekVO(Long memberId, String symbol, String appId, Date queryDate) {
        Calendar calendar = Calendar.getInstance();
        AnnualRateOfWeekVO annualRateOfWeekVO = new AnnualRateOfWeekVO();

        // 将 日期 设为 当前日期
        calendar.setTime(queryDate);
        // 将 日期 设为 当前日期的前一天
        calendar.add(Calendar.HOUR, -24);
        // 过去7天平均日利率
        BigDecimal averageAnnulYieldOfWeek = getAverageAnnulYieldOfWeek(calendar.getTime(), symbol, appId);

        if(averageAnnulYieldOfWeek == null){
            return null;
        }

        // 七日年利率
        BigDecimal annualRateOfWeek = averageAnnulYieldOfWeek.multiply(new BigDecimal("365"));
        // 昨日收益记录
        LockHqbThousandsIncomeRecord lockHqbThousandsIncomeRecord
                = this.yesterdayThousandsIncome(appId, symbol, Integer.valueOf(DateUtil.getDateYMD(calendar.getTime())));

        annualRateOfWeekVO.setAnnualRateOfWeek(annualRateOfWeek);
        annualRateOfWeekVO.setDate(DateUtil.getDate(calendar.getTime()));
        annualRateOfWeekVO.setIncome(lockHqbThousandsIncomeRecord != null ? lockHqbThousandsIncomeRecord.
                getTenThousandIncome().multiply(new BigDecimal("10000")) : BigDecimal.ZERO);

        return annualRateOfWeekVO;
    }

    @Override
    public List<AnnualRateOfWeekVO> getRecentMonthAnnualRateOfWeekVO(Long memberId, String symbol, String appId, Date currentDay) {
        List<AnnualRateOfWeekVO> annualRateOfWeekVOList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(currentDay);
        // 循环获取最近一个月7日年化率
        for (int i = 0; i < 30; i++) {
            AnnualRateOfWeekVO tempVO = this.getAnnualRateOfWeekVO(memberId, symbol, appId, calendar.getTime());
            calendar.add(Calendar.HOUR, -24);
            // 如果七日年化率对象为空，则不加入list
            if(tempVO == null) {
                continue;
            }
            annualRateOfWeekVOList.add(tempVO);
        }
        return annualRateOfWeekVOList;
    }
}
