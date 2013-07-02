package com.sociallocation.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.sociallocation.app.AppException;
import com.sociallocation.bean.Comment.Refer;
import com.sociallocation.bean.Comment.Reply;
import com.sociallocation.util.StringUtils;

import android.util.Xml;

/**
 * 博客评论列表实体�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class BlogCommentList extends Entity{
	
	private int pageSize;
	private int allCount;
	private List<Comment> commentlist = new ArrayList<Comment>();
	
	public int getPageSize() {
		return pageSize;
	}
	public int getAllCount() {
		return allCount;
	}
	public List<Comment> getCommentlist() {
		return commentlist;
	}

	public static BlogCommentList parse(InputStream inputStream) throws IOException, AppException {
		BlogCommentList commlist = new BlogCommentList();
		Comment comm = null;
		Reply reply = null;
		Refer refer = null;
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
			    		if(tag.equalsIgnoreCase("allCount")) 
			    		{
			    			commlist.allCount = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if(tag.equalsIgnoreCase("pageSize")) 
			    		{
			    			commlist.pageSize = StringUtils.toInt(xmlParser.nextText(),0);
			    		}
			    		else if (tag.equalsIgnoreCase("comment")) 
			    		{ 
			    			comm = new Comment();
			    		}
			    		else if(comm != null)
			    		{	
				            if(tag.equalsIgnoreCase("id"))
				            {			      
				            	comm.id = StringUtils.toInt(xmlParser.nextText(),0);
				            }
				            else if(tag.equalsIgnoreCase("portrait"))
				            {			            	
				            	comm.setFace(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("author"))
				            {			            	
				            	comm.setAuthor(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("authorid"))
				            {			            	
				            	comm.setAuthorId(StringUtils.toInt(xmlParser.nextText(),0));		            	
				            }
				            else if(tag.equalsIgnoreCase("content"))
				            {			            	
				            	comm.setContent(xmlParser.nextText());
				            }
				            else if(tag.equalsIgnoreCase("pubDate"))
				            {			            	
				            	comm.setPubDate(xmlParser.nextText());		            	
				            }
				            else if(tag.equalsIgnoreCase("appclient"))
				            {			            	
				            	comm.setAppClient(StringUtils.toInt(xmlParser.nextText(),0));			            	
				            }
				            else if(tag.equalsIgnoreCase("reply"))
				            {			            	
				            	reply = new Reply();         	
				            }
				            else if(reply!=null && tag.equalsIgnoreCase("rauthor"))
				            {
				            	reply.rauthor = xmlParser.nextText();
				            }
				            else if(reply!=null && tag.equalsIgnoreCase("rpubDate"))
				            {
				            	reply.rpubDate = xmlParser.nextText();
				            }
				            else if(reply!=null && tag.equalsIgnoreCase("rcontent"))
				            {
				            	reply.rcontent = xmlParser.nextText();
				            }
				            else if(tag.equalsIgnoreCase("refer"))
				            {			            	
				            	refer = new Refer();         	
				            }
				            else if(refer!=null && tag.equalsIgnoreCase("refertitle"))
				            {
				            	refer.refertitle = xmlParser.nextText();
				            }
				            else if(refer!=null && tag.equalsIgnoreCase("referbody"))
				            {
				            	refer.referbody = xmlParser.nextText();
				            }
			    		}
			            //通知信息
			            else if(tag.equalsIgnoreCase("notice"))
			    		{
			            	commlist.setNotice(new Notice());
			    		}
			            else if(commlist.getNotice() != null)
			    		{
			    			if(tag.equalsIgnoreCase("atmeCount"))
				            {			      
			    				commlist.getNotice().setAtmeCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("msgCount"))
				            {			            	
				            	commlist.getNotice().setMsgCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("reviewCount"))
				            {			            	
				            	commlist.getNotice().setReviewCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
				            else if(tag.equalsIgnoreCase("newFansCount"))
				            {			            	
				            	commlist.getNotice().setNewFansCount(StringUtils.toInt(xmlParser.nextText(),0));
				            }
			    		}
			    		break;
			    	case XmlPullParser.END_TAG:	
					   	//如果遇到标签结束，则把对象添加进集合�?
				       	if (tag.equalsIgnoreCase("comment") && comm != null) { 
				       		commlist.getCommentlist().add(comm); 
				       		comm = null; 
				       	}
				       	else if (tag.equalsIgnoreCase("reply") && comm!=null && reply!=null) { 
				       		comm.getReplies().add(reply);
				       		reply = null; 
				       	}
				       	else if(tag.equalsIgnoreCase("refer") && comm!=null && refer!=null) {
				       		comm.getRefers().add(refer);
				       		refer = null;
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
        return commlist;       
	}
}
