package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.util.E3Result;
import cn.e3mall.common.util.IDUtils;
import cn.e3mall.common.util.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemCat;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;
@Service
public class ItemServiceImpl implements ItemService {
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemDescMapper itemDescMapper;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Resource
	private Destination topicDestination;
	@Autowired
	private JedisClient jedisClient;
	@Value("${ITEM_INFO_PRE}")
	private String ITEM_INFO_PRE;
	@Override
	public TbItem getItemById(long itemId) {
		try {
			String json = jedisClient.get(ITEM_INFO_PRE+":"+itemId+":BASE");
			if (StringUtils.isNoneBlank(json)) {
				TbItem tbItem=JsonUtils.jsonToPojo(json, TbItem.class);
				return tbItem;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
	    try {
	    	if (tbItem!=null) {
	    		jedisClient.set(ITEM_INFO_PRE+":"+itemId+":BASE", JsonUtils.objectToJson(tbItem));	
	    		jedisClient.expire(ITEM_INFO_PRE+":"+itemId+":BASE", 3600);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tbItem;
	}
	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		PageHelper.startPage(page, rows);
		TbItemExample tbItemExample=new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(tbItemExample);
		PageInfo<TbItem>pageInfo=new PageInfo<>(list);
		EasyUIDataGridResult result=new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);
		return result;
	}
	@Override
	public E3Result addItem(TbItem item, String desc) {
		final long itemId=IDUtils.genItemId();
		item.setId(itemId);
		//1-正常 ，2-下架，3-删除
		item.setStatus((byte)1);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		itemMapper.insert(item);
		TbItemDesc itemDesc=new TbItemDesc();
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(new Date());
		itemDescMapper.insert(itemDesc);
		
		jmsTemplate.send(topicDestination,new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				Message createMessage = session.createTextMessage(itemId+"");
				return createMessage;
			}
		});
		
		return E3Result.ok();
	}
	@Override
	public TbItemDesc getItemDescById(long itemId) {
		try {
			String json = jedisClient.get(ITEM_INFO_PRE+":"+itemId+":DESC");
			if (StringUtils.isNoneBlank(json)) {
				TbItemDesc tbItemDesc=JsonUtils.jsonToPojo(json, TbItemDesc.class);
				return tbItemDesc;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		TbItemDesc tbItemDesc= itemDescMapper.selectByPrimaryKey(itemId);
		try {
	    	if (tbItemDesc!=null) {
	    		jedisClient.set(ITEM_INFO_PRE+":"+itemId+":DESC", JsonUtils.objectToJson(tbItemDesc));
	    		jedisClient.expire(ITEM_INFO_PRE+":"+itemId+":DESC", 3600);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tbItemDesc;
	}
}
