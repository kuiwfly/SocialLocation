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
 * 帖子列表实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class PostList extends Entity{

	public final static int CATALOG_ASK = 1;
	public final static int CATALOG_SHARE = 2;
	public final static int CATALOG_OTHER = 3;
	public final static int CATALOG_JOB = 4;
	public final static int CATALOG_SITE = 5;
	
	private int pageSize;
	private int postCount;
	private List<Post> postlist = new ArrayList<Post>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getPostCount() {
		return postCount;
	}
	public List<Post> getPostlist() {
		return postlist;
	}

	public static PostList parse(InputStream inputStream) throws IOException, AppException {
		PostList postlist = new PostList();
		Post post = null;
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
			    		if(tag.equalsIgnoreCase("postCount")) 
			    		{
			    			postlist.postCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			postlist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase(Post.NODE_START)) 
			    		{ 
			    			post = new Post();
			    		}
			    		else if(post != null)
			    		{	
				            if(tag.equalsIgnoreCase(Post.NODE_ID))
				            {			      
				            	post.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_TITLE))
				            {			            	
				            	post.setTitle(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_FACE))
				            {			            	
				            	post.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_AUTHOR))
				            {			            	
				            	post.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_AUTHORID))
				            {			            	
				            	post.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_ANSWERCOUNT))
				            {			            	
				            	post.setAnswerCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_VIEWCOUNT))
				            {			            	
				            	post.setViewCount(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase(Post.NODE_PUBDATE))
				            {			            	
				            	post.setPubDate(xmlParser.nextText());         	
				            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	postlist.setNotice(new Notice());
			    		}
			            else if(postlist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				postlist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	postlist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	postlist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	postlist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合�?
				       	if (tag.equalsIgnoreCase("post") && post != null) { 
				    	   postlist.getPostlist().add(post); 
				           post = null; 
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
        return postlist;       
	}
}
