package com.miaoshaproject.service;

import com.miaoshaproject.service.model.PromoModel;

/**
 * Created on 2021/1/11.
 *
 * @author 小逸
 * @description
 */
public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);
}
