package com.spark.bitrade.service;

import com.spark.bitrade.annotation.ReadDataSource;
import com.spark.bitrade.dao.UpdatingDao;
import com.spark.bitrade.entity.Updating;
import com.spark.bitrade.mapper.dao.UpdatingMapper;
import com.spark.bitrade.service.Base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc:
 * Author: lingxing
 * Create Date Time: 2018-07-20 13:27:00
 */

@Service
public class UpdatingService extends BaseService {
    @Autowired
    private UpdatingMapper updatingMapper;
    @Autowired
    private UpdatingDao updatingDao;

    @ReadDataSource
    public List<Updating>getAll(){
        return  updatingMapper.getAll();
    }
    public Updating update(Updating updating){
        Updating u=updatingMapper.findById(updating.getId());
        if(u!=null){ updatingDao.save(updating);}
        return updating;
    }
    @ReadDataSource
    public Updating  findByUpdatingId(int id){
        return  updatingMapper.findById(id);
    }
}
