package com.sociallocation.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sociallocation.app.AppException;
import com.sociallocation.util.StringUtils;

import android.util.Xml;

/**
 * 消息列表实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class MessageList extends Entity{

	private int pageSize;
	private int messageCount;
	private List<Messages> messagelist = new ArrayList<Messages>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getMessageCount() {
		return messageCount;
	}
	public List<Messages> getMessagelist() {
		return messagelist;
	}

	public static MessageList parse(InputStream inputStream) throws IOException, AppException {
		MessageList msglist = new MessageList();
		Messages msg = null;
        //获得XmlPullParser解析�?
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有�?��文档，结束文档，�?��标签，结束标签，文本等等事件�?
            int evtType=xmlParser.getEventType();
			//�?��循环，直到文档结�?   
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
	    		int depth = xmlParser.getDepth();
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(depth==2 && tag.equalsIgnoreCase("messageCount")) 
			    		{
			    			msglist.messageCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			msglist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase("message")) 
			    		{ 
			    			msg = new Messages();
			    		}
			    		else if(msg != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	msg.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("portrait"))
				            {			            	
				            	msg.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("friendid"))
				            {
				            	msg.setFriendId(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("friendname"))
				            {
				            	msg.setFriendName(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("content"))
				            {			            	
				            	msg.setContent(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("sender"))
				            {			            	
				            	msg.setSender(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("senderid"))
				            {			            	
				            	msg.setSenderId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(depth==4 && tag.equalsIgnoreCase("messageCount"))
				            {			            	
				            	msg.setMessageCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {			            	
				            	msg.setPubDate(xmlParser.nextText());     	
				            }
				            else if(tag.equalsIgnoreCase("appclient"))
				            {			            	
				            	msg.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	msglist.setNotice(new Notice());
			    		}
			            else if(msglist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				msglist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	msglist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	msglist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	msglist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合�?
				       	if (tag.equalsIgnoreCase("message") && msg != null) { 
				       		msglist.getMessagelist().add(msg); 
				       		msg = null; 
				       	}
				       	break; 
			    }
			    //如果xml没有结束，则导航到下�?��节点
			    evtType=xmlParser.next();
			}		
        } catch (XmlPullParserException e) {
			throw AppException.xml(e);
        } finally {
        	inputStream.close();	
        }      
        return msglist;       
	}
}
