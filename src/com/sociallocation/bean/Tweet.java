package com.sociallocation.bean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;



import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sociallocation.app.AppException;
import com.sociallocation.util.StringUtils;

import android.util.Xml;

/**
 * 动弹实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Tweet extends Entity{

	public final static String NODE_ID = "id";
	public final static String NODE_FACE = "portrait";
	public final static String NODE_BODY = "body";
	public final static String NODE_AUTHORID = "authorid";
	public final static String NODE_AUTHOR = "author";
	public final static String NODE_PUBDATE = "pubDate";
	public final static String NODE_COMMENTCOUNT = "commentCount";
	public final static String NODE_IMGSMALL = "imgSmall";
	public final static String NODE_IMGBIG = "imgBig";
	public final static String NODE_APPCLIENT = "appclient";
	public final static String NODE_START = "tweet";
	
	public final static int CLIENT_MOBILE = 2;
	public final static int CLIENT_ANDROID = 3;
	public final static int CLIENT_IPHONE = 4;
	public final static int CLIENT_WINDOWS_PHONE = 5;
	
	private String face;
	private String body;
	private String author;
	private int authorId;
	private int commentCount;
	private String pubDate;
	private String imgSmall;
	private String imgBig;
	private File imageFile;
	private int appClient; 
	
	public int getAppClient() {
		return appClient;
	}
	public void setAppClient(int appClient) {
		this.appClient = appClient;
	}
	public File getImageFile() {
		return imageFile;
	}
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}	
	public String getImgSmall() {
		return imgSmall;
	}
	public void setImgSmall(String imgSmall) {
		this.imgSmall = imgSmall;
	}
	public String getImgBig() {
		return imgBig;
	}
	public void setImgBig(String imgBig) {
		this.imgBig = imgBig;
	}
	public String getFace() {
		return face;
	}
	public void setFace(String face) {
		this.face = face;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
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
	
	public static Tweet parse(InputStream inputStream) throws IOException, AppException {
		Tweet tweet = null;
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
			    		if(tag.equalsIgnoreCase(NODE_START))
			    		{
			    			tweet = new Tweet();
			    		}
			    		else if(tweet != null)
			    		{	
				            if(tag.equalsIgnoreCase(NODE_ID))
				            {			      
				            	tweet.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase(NODE_FACE))
				            {			            	
				            	tweet.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_BODY))
				            {			            	
				            	tweet.setBody(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_AUTHOR))
				            {			            	
				            	tweet.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_AUTHORID))
				            {			            	
				            	tweet.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_COMMENTCOUNT))
				            {			            	
				            	tweet.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_PUBDATE))
				            {			            	
				            	tweet.setPubDate(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(NODE_IMGSMALL))
				            {			            	
				            	tweet.setImgSmall(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_IMGBIG))
				            {			            	
				            	tweet.setImgBig(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase(NODE_APPCLIENT))
				            {			            	
				            	tweet.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            //通知信息
				            else if(tag.equalsIgnoreCase("notice"))
				    		{
				            	tweet.setNotice(new Notice());
				    		}
				            else if(tweet.getNotice() != null)
				    		{
				    			if(tag.equalsIgnoreCase("atmeCount"))
					            {			      
				    				tweet.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("msgCount"))
					            {			            	
					            	tweet.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("reviewCount"))
					            {			            	
					            	tweet.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
					            }
					            else if(tag.equalsIgnoreCase("newFansCount"))
					            {			            	
					            	tweet.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
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
        return tweet;       
	}
}
