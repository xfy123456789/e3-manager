package cn.e3mall.service;

import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemCat;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.common.pojo.*;
import cn.e3mall.common.util.E3Result;
public interface ItemService {
	TbItem getItemById(long itemId);
	EasyUIDataGridResult getItemList(int page,int rows);
	E3Result addItem(TbItem item,String desc);
	TbItemDesc getItemDescById(long itemId);
}
