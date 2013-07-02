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
 * 动弹列表实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class TweetList extends Entity{
	
	public final static int CATALOG_LASTEST = 0;
	public final static int CATALOG_HOT = -1;

	private int pageSize;
	private int tweetCount;
	private List<Tweet> tweetlist = new ArrayList<Tweet>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getTweetCount() {
		return tweetCount;
	}
	public List<Tweet> getTweetlist() {
		return tweetlist;
	}

	public static TweetList parse(InputStream inputStream) throws IOException, AppException {
		TweetList tweetlist = new TweetList();
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
			    		if(tag.equalsIgnoreCase("tweetCount")) 
			    		{
			    			tweetlist.tweetCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			tweetlist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase(Tweet.NODE_START)) 
			    		{ 
			    			tweet = new Tweet();
			    		}
			    		else if(tweet != null)
			    		{	
				            if(tag.equalsIgnoreCase(Tweet.NODE_ID))
				            {			      
				            	tweet.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_FACE))
				            {			            	
				            	tweet.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_BODY))
				            {			            	
				            	tweet.setBody(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_AUTHOR))
				            {			            	
				            	tweet.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_AUTHORID))
				            {			            	
				            	tweet.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_COMMENTCOUNT))
				            {			            	
				            	tweet.setCommentCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_PUBDATE))
				            {			            	
				            	tweet.setPubDate(xmlParser.nextText());	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_IMGSMALL))
				            {			            	
				            	tweet.setImgSmall(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_IMGBIG))
				            {			            	
				            	tweet.setImgBig(xmlParser.nextText());			            	
				            }
				            else if(tag.equalsIgnoreCase(Tweet.NODE_APPCLIENT))
				            {			            	
				            	tweet.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));				            	
				            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	tweetlist.setNotice(new Notice());
			    		}
			            else if(tweetlist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				tweetlist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	tweetlist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	tweetlist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	tweetlist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合�?
				       	if (tag.equalsIgnoreCase("tweet") && tweet != null) { 
				       		tweetlist.getTweetlist().add(tweet); 
				       		tweet = null; 
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
        return tweetlist;       
	}
}
