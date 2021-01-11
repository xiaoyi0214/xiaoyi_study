package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;
import com.sun.tools.internal.xjc.reader.dtd.bindinfo.BIUserConversion;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

/**
 * Created on 2021/1/10.
 *
 * @author 小逸
 * @description
 */

public interface ItemService {

    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    /**
     * 扣减库存
     * */
    boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException;

    void increaseSales(Integer itemId,Integer amount) throws BusinessException;
}
