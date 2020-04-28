package com.spark.bitrade.constant;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * rongyu
 * 分页接受参数
 */
@Data
@ApiModel(description = "分页参数")
public class PageModel {

    @ApiModelProperty(value = "页码",name = "pageNo",required = true)
    Integer pageNo = 1;
    @ApiModelProperty(value = "每页条数",name = "pageSize",required = true)
    Integer pageSize = 10;
    @ApiModelProperty(value = "排序，1：降序，0：升序",name = "direction")
    List<Sort.Direction> direction;
    @ApiModelProperty(value = "排序字段",name = "property")
    List<String> property;

    public void setSort(){
        if(property==null||property.size()==0){
            List<String> list = new ArrayList<>();
            list.add("id");
            List<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            property = list ;
            direction = directions ;
        }
    }
    private Sort getSort() {
        List<Sort.Order> orders = null;
        setSort();
        if(direction.size() == property.size()) {
            orders = new ArrayList<>();
            int length = direction.size();
            for (int i = 0; i < length; i++) {
                orders.add(new Sort.Order(direction.get(i), property.get(i)));
            }
        }
        return new Sort(orders);
    }

    @ApiModelProperty(hidden = true)
    public Pageable getPageable() {
        Sort sort = getSort();
        if (sort == null)
            return new PageRequest(pageNo - 1, pageSize);
        return new PageRequest(pageNo - 1, pageSize, sort);
    }

    @ApiModelProperty(hidden = true)
    public Order directoryToOrder(Sort.Direction direction){
       return direction.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
    }

    @ApiModelProperty(hidden = true)
    public List<Order> toOrders(List<Sort.Direction> list){
        List<Order> orders = new ArrayList<>();
        for(Sort.Direction direction:list){
            orders.add(directoryToOrder(direction));
        }
        return orders ;
    }

    @ApiModelProperty(hidden = true)
    public List<OrderSpecifier> getOrderSpecifiers(){
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();
        setSort();
        if(this.getProperty()!=null){
            for(int i = 0 ; i < this.getProperty().size() ;i++){
                Path path = ExpressionUtils.path(Path.class,this.getProperty().get(i));
                OrderSpecifier orderSpecifier = new OrderSpecifier(this.toOrders(this.getDirection()).get(i),path);
                orderSpecifiers.add(orderSpecifier);
            }
        }
        return orderSpecifiers ;
    }


}
