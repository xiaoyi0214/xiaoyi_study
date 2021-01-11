package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

/**
 * Created on 2021/1/10.
 *
 * @author 小逸
 * @description
 */
public interface OrderService {

    OrderModel createOrder(Integer userId,Integer itemId,Integer amount) throws BusinessException;


}
