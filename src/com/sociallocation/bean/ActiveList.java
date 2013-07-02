package com.sociallocation.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sociallocation.app.AppException;
import com.sociallocation.bean.Active.ObjectReply;
import com.sociallocation.util.StringUtils;

import android.util.Xml;

/**
 * 动�?列表实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class ActiveList extends Entity{

	public final static int CATALOG_LASTEST = 1;//�?��
	public final static int CATALOG_ATME = 2;//@�?
	public final static int CATALOG_COMMENT = 3;//评论
	public final static int CATALOG_MYSELF = 4;//我自�?
	
	private int pageSize;
	private int activeCount;
	private List<Active> activelist = new ArrayList<Active>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getActiveCount() {
		return activeCount;
	}
	public List<Active> getActivelist() {
		return activelist;
	}

	public static ActiveList parse(InputStream inputStream) throws IOException, AppException {
		ActiveList activelist = new ActiveList();
		Active active = null;
        //获得XmlPullParser解析�?
        XmlPullParser xmlParser = Xml.newPullParser();
        try {        	
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有�?��文档，结束文档，�?��标签，结束标签，文本等等事件�?
            int evtType=xmlParser.getEventType();
			//�?��循环，直到文档结�?   
			while(evtType!=XmlPullParser.END_DOCUMENT){ 
	    		String tag = xmlParser.getName(); 
			    switch(evtType){ 
			    	case XmlPullParser.START_TAG:
			    		if(tag.equalsIgnoreCase("activeCount")) 
			    		{
			    			activelist.activeCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			activelist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase("active")) 
			    		{ 
			    			active = new Active();
			    		}
			    		else if(active != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	active.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("portrait"))
				            {			            	
				            	active.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("message"))
				            {			            	
				            	active.setMessage(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("author"))
				            {			            	
				            	active.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("authorid"))
				            {			            	
				            	active.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase("catalog"))
				            {			            	
				            	active.setActiveType(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objectID"))
				            {			            	
				            	active.setObjectId(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objecttype"))
				            {			            	
				            	active.setObjectType(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objectcatalog"))
				            {			            	
				            	active.setObjectCatalog(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("objecttitle"))
				            {			            	
				            	active.setObjectTitle(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase("objectreply"))
				            {			            	
				            	active.setObjectReply(new ObjectReply());	            	
				            }
				            else if(active.getObjectReply()!=null && tag.equalsIgnoreCase("objectname"))
				            {			            	
				            	active.getObjectReply().objectName = xmlParser.nextText();		            	
				            }
				            else if(active.getObjectReply()!=null && tag.equalsIgnoreCase("objectbody"))
				            {			            	
				            	active.getObjectReply().objectBody = xmlParser.nextText();		            	
				            }
				            else if(tag.equalsIgnoreCase("commentCount"))
				            {			            	
				            	active.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {			            	
				            	active.setPubDate(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase("tweetimage"))
				            {			            	
				            	active.setTweetimage(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase("appclient"))
				            {			            	
				            	active.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("url"))
				            {			            	
				            	active.setUrl(xmlParser.nextText());			            	
				            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	activelist.setNotice(new Notice());
			    		}
			            else if(activelist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				activelist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	activelist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	activelist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	activelist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合�?
				       	if (tag.equalsIgnoreCase("active") && active != null) { 
				       		activelist.getActivelist().add(active); 
				       		active = null; 
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
        return activelist;       
	}
}
