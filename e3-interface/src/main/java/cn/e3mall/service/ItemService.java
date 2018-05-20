package cn.e3mall.service;

import cn.e3mall.pojo.TbItem;
import cn.e3mall.common.pojo.*;
public interface ItemService {
	TbItem getItemById(long itemId);
	EasyUIDataGridResult getItemList(int page,int rows);
}
