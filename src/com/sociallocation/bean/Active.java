package com.sociallocation.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;



import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sociallocation.app.AppException;
import com.sociallocation.util.StringUtils;

import android.util.Xml;

/**
 * 动�?实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Active extends Entity {

	public final static int CATALOG_OTHER = 0;//其他
	public final static int CATALOG_NEWS = 1;//新闻
	public final static int CATALOG_POST = 2;//帖子
	public final static int CATALOG_TWEET = 3;//动弹
	public final static int CATALOG_BLOG = 4;//博客
	
	public final static int CLIENT_MOBILE = 2;
	public final static int CLIENT_ANDROID = 3;
	public final static int CLIENT_IPHONE = 4;
	public final static int CLIENT_WINDOWS_PHONE = 5;
	
	private String face;
	private String message;
	private String author;
	private int authorId;
	private int activeType;
	private int objectId;
	private int objectType;
	private int objectCatalog;	
	private String objectTitle;
	private ObjectReply objectReply;
	private int commentCount;
	private String pubDate;
	private String tweetimage;
	private int appClient;
	private String url;
	
	public static class ObjectReply implements Serializable{
		public String objectName;
		public String objectBody;
	} 	
	
	public void setObjectReply(ObjectReply objectReply) {
		this.objectReply = objectReply;
	}
	public ObjectReply getObjectReply() {
		return objectReply;
	}
	public String getTweetimage() {
		return tweetimage;
	}
	public void setTweetimage(String tweetimage) {
		this.tweetimage = tweetimage;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getAuthorId() {
		return authorId;
	}
	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}
	public int getActiveType() {
		return activeType;
	}
	public void setActiveType(int activeType) {
		this.activeType = activeType;
	}
	public int getObjectId() {
		return objectId;
	}
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}	
	public int getObjectType() {
		return objectType;
	}
	public void setObjectType(int objectType) {
		this.objectType = objectType;
	}
	public int getObjectCatalog() {
		return objectCatalog;
	}
	public void setObjectCatalog(int objectCatalog) {
		this.objectCatalog = objectCatalog;
	}
	public String getObjectTitle() {
		return objectTitle;
	}
	public void setObjectTitle(String objectTitle) {
		this.objectTitle = objectTitle;
	}	
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public String getPubDate() {
		return pubDate;
	}
	public void setPubDate(String pubDate) {
		this.pubDate = pubDate;
	}
	public int getAppClient() {
		return appClient;
	}
	public void setAppClient(int appClient) {
		this.appClient = appClient;
	}	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static Active parse(InputStream inputStream) throws IOException, AppException {
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
			    		if(tag.equalsIgnoreCase("active"))
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
				            //通知信息
				            else if(tag.equalsIgnoreCase("notice"))
				    		{
				            	active.setNotice(new Notice());
				    		}
				            else if(active.getNotice() != null)
				    		{
				    			if(tag.equalsIgnoreCase("atmeCount"))
					            {			      
				    				active.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("msgCount"))
					            {			            	
					            	active.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("reviewCount"))
					            {			            	
					            	active.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("newFansCount"))
					            {			            	
					            	active.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
				    		}
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:		    		
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
        return active;       
	}
}
