package com.sociallocation.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;



import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.sociallocation.app.AppContext;
import com.sociallocation.app.AppException;
import com.sociallocation.bean.ActiveList;
import com.sociallocation.bean.Blog;
import com.sociallocation.bean.BlogCommentList;
import com.sociallocation.bean.BlogList;
import com.sociallocation.bean.CommentList;
import com.sociallocation.bean.FavoriteList;
import com.sociallocation.bean.FriendList;
import com.sociallocation.bean.LoginInfo;
import com.sociallocation.bean.MessageList;
import com.sociallocation.bean.MyInformation;
import com.sociallocation.bean.News;
import com.sociallocation.bean.NewsList;
import com.sociallocation.bean.Notice;
import com.sociallocation.bean.Post;
import com.sociallocation.bean.PostList;
import com.sociallocation.bean.Result;
import com.sociallocation.bean.SearchList;
import com.sociallocation.bean.Tweet;
import com.sociallocation.bean.TweetList;
import com.sociallocation.bean.URLs;
import com.sociallocation.bean.Update;
import com.sociallocation.bean.User;
import com.sociallocation.bean.UserInformation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class ApiClient {
	public static final String TAG = "ApiClient" ;
	public static final String UTF_8 = "UTF-8";
	public static final String DESC = "descend";
	public static final String ASC = "ascend";
	
	private final static int TIMEOUT_CONNECTION = 20000;
	private final static int TIMEOUT_SOCKET = 20000;
	private final static int RETRY_TIME = 3;

	private static String appCookie;
	private static String appUserAgent;

	public static void cleanCookie() {
		appCookie = "";
	}
	
	private static String getCookie(AppContext appContext) {
		if(appCookie == null || appCookie == "") {
			appCookie = appContext.getProperty("cookie");
		}
		return appCookie;
	}
	
	private static String getUserAgent(AppContext appContext) {
		if(appUserAgent == null || appUserAgent == "") {
			StringBuilder ua = new StringBuilder(URLs.HOST);
			ua.append('/'+appContext.getPackageInfo().versionName+'_'+appContext.getPackageInfo().versionCode);
			ua.append("/Android");
			ua.append("/"+android.os.Build.VERSION.RELEASE);
			ua.append("/"+android.os.Build.MODEL);
			ua.append("/"+appContext.getAppId());
			appUserAgent = ua.toString();
		}
		return appUserAgent;
	}
	
	private static HttpClient getHttpClient() {        
        HttpClient httpClient = new HttpClient();
		// 
		httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        // 
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		// 
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT_CONNECTION);
		// 
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT_SOCKET);
		// 
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}	
	
	private static GetMethod getHttpGet(String url, String cookie, String userAgent) {
		GetMethod httpGet = new GetMethod(url);
		// è®¾ç½® è¯·æ±‚è¶…æ—¶æ—¶é—´
		httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGet.setRequestHeader("Host", URLs.HOST);
		httpGet.setRequestHeader("Connection","Keep-Alive");
		httpGet.setRequestHeader("Cookie", cookie);
		httpGet.setRequestHeader("User-Agent", userAgent);
		return httpGet;
	}
	
	private static PostMethod getHttpPost(String url, String cookie, String userAgent) {
		PostMethod httpPost = new PostMethod(url);
		// 
		httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpPost.setRequestHeader("Host", URLs.HOST);
		//httpPost.setRequestHeader("Content-Type", "application/json") ;
		httpPost.setRequestHeader("Connection","Keep-Alive");
		httpPost.setRequestHeader("Cookie", cookie);
		httpPost.setRequestHeader("User-Agent", userAgent);
		return httpPost;
	}
	
	private static String _MakeURL(String p_url, Map<String, Object> params) {
		StringBuilder url = new StringBuilder(p_url);
		if(url.indexOf("?")<0)
			url.append('?');

		for(String name : params.keySet()){
			url.append('&');
			url.append(name);
			url.append('=');
			url.append(String.valueOf(params.get(name)));
		}

		return url.toString().replace("?&", "?");
	}
	
	/**
	 * getè¯·æ±‚URL
	 * @param url
	 * @throws AppException 
	 */
	private static InputStream http_get(AppContext appContext, String url) throws AppException {	
		//System.out.println("get_url==> "+url);
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);
		
		HttpClient httpClient = null;
		GetMethod httpGet = null;

		String responseBody = "";
		int time = 0;
		do{
			try 
			{
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, cookie, userAgent);			
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
				responseBody = httpGet.getResponseBodyAsString();
				//System.out.println("XMLDATA=====>"+responseBody);
				break;				
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿè‡´å‘½çš„å¼‚å¸¸ï¼Œå�¯èƒ½æ˜¯å��è®®ä¸�å¯¹æˆ–è€…è¿”å›žçš„å†…å®¹æœ‰é—®é¢?
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿç½‘ç»œå¼‚å¸¸
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// 
				httpGet.releaseConnection();
				httpClient = null;
			}
		}while(time < RETRY_TIME);
		
		responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		if(responseBody.contains("result") && responseBody.contains("errorCode") && appContext.containsProperty("user.uid")){
			try {
				Result res = Result.parse(new ByteArrayInputStream(responseBody.getBytes()));	
				if(res.getErrorCode() == 0){
					appContext.Logout();
					appContext.getUnLoginHandler().sendEmptyMessage(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return new ByteArrayInputStream(responseBody.getBytes());
	}
	
	private static InputStream _post(AppContext appContext, String url, Map<String, Object> params, Map<String,File> files) throws AppException {
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);
		
		HttpClient httpClient = null;
		PostMethod httpPost = null;
		
	
		int length = (params == null ? 0 : params.size()) + (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
        if(params != null)
        for(String name : params.keySet()){
        	parts[i++] = new StringPart(name, String.valueOf(params.get(name)), UTF_8);
        }
        if(files != null)
        for(String file : files.keySet()){
        	try {
				parts[i++] = new FilePart(file, files.get(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        	
        }
		
		String responseBody = "";
		int time = 0;
		do{
			try 
			{
				httpClient = getHttpClient();
				httpPost = getHttpPost(url, cookie, userAgent);	        
		        httpPost.setRequestEntity(new MultipartRequestEntity(parts,httpPost.getParams()));		        
		        int statusCode = httpClient.executeMethod(httpPost);
		        if(statusCode != HttpStatus.SC_OK) 
		        {
		        	throw AppException.http(statusCode);
		        }
		        else if(statusCode == HttpStatus.SC_OK) 
		        {
		            Cookie[] cookies = httpClient.getState().getCookies();
		            String tmpcookies = "";
		            for (Cookie ck : cookies) {
		                tmpcookies += ck.toString()+";";
		            }
		            //ä¿�å­˜cookie   
	        		if(appContext != null && tmpcookies != ""){
	        			appContext.setProperty("cookie", tmpcookies);
	        			appCookie = tmpcookies;
	        		}
		        }
		     	responseBody = httpPost.getResponseBodyAsString();
		        //System.out.println("XMLDATA=====>"+responseBody);
		     	break;	     	
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿè‡´å‘½çš„å¼‚å¸¸ï¼Œå�¯èƒ½æ˜¯å��è®®ä¸�å¯¹æˆ–è€…è¿”å›žçš„å†…å®¹æœ‰é—®é¢?
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿç½‘ç»œå¼‚å¸¸
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// é‡Šæ”¾è¿žæŽ¥
				httpPost.releaseConnection();
				httpClient = null;
			}
		}while(time < RETRY_TIME);
        
        responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
		if(responseBody.contains("result") && responseBody.contains("errorCode") && appContext.containsProperty("user.uid")){
			try {
				Result res = Result.parse(new ByteArrayInputStream(responseBody.getBytes()));	
				if(res.getErrorCode() == 0){
					appContext.Logout();
					appContext.getUnLoginHandler().sendEmptyMessage(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
        return new ByteArrayInputStream(responseBody.getBytes());
	}
	private static String _postToStr(AppContext appContext, String url, Map<String, Object> params, Map<String,File> files) throws AppException {
		String cookie = getCookie(appContext);
		String userAgent = getUserAgent(appContext);
		
		HttpClient httpClient = null;
		PostMethod httpPost = null;
		
	
		int length = (params == null ? 0 : params.size()) + (files == null ? 0 : files.size());
		Part[] parts = new Part[length];
		int i = 0;
        if(params != null)
        for(String name : params.keySet()){
        	parts[i++] = new StringPart(name, String.valueOf(params.get(name)), UTF_8);
        	Log.e(TAG,"name:"+name+" param:"+params.get(name)) ;
        }
        if(files != null)
        for(String file : files.keySet()){
        	try {
				parts[i++] = new FilePart(file, files.get(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        	
        }
		
		String responseBody = "";
		int time = 0;
		do{
			try 
			{
				httpClient = getHttpClient();
				httpPost = getHttpPost(url, cookie, userAgent);	        
				
				httpPost.setRequestEntity(new MultipartRequestEntity(parts,httpPost.getParams()));
				Log.e(TAG,"params:"+httpPost.getParameter("username")) ;
		        for(String name : params.keySet()){

		        	httpPost.addParameter(name, String.valueOf(params.get(name))) ;
		        }
				Log.e(TAG,"params:"+parts.toString()) ;
		        int statusCode = httpClient.executeMethod(httpPost);
		        if(statusCode != HttpStatus.SC_OK) 
		        {
		        	throw AppException.http(statusCode);
		        }
		        else if(statusCode == HttpStatus.SC_OK) 
		        {
		            Cookie[] cookies = httpClient.getState().getCookies();
		            String tmpcookies = "";
		            for (Cookie ck : cookies) {
		                tmpcookies += ck.toString()+";";
		            }
		            //ä¿�å­˜cookie   
	        		if(appContext != null && tmpcookies != ""){
	        			appContext.setProperty("cookie", tmpcookies);
	        			appCookie = tmpcookies;
	        		}
		        }
		     	responseBody = httpPost.getResponseBodyAsString();
		     	break;	     	
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				httpPost.releaseConnection();
				httpClient = null;
			}
		}while(time < RETRY_TIME);
        
//        responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
//		if(responseBody.contains("result") && responseBody.contains("errorCode") && appContext.containsProperty("user.uid")){
//			try {
//				Result res = Result.parse(new ByteArrayInputStream(responseBody.getBytes()));	
//				if(res.getErrorCode() == 0){
//					appContext.Logout();
//					appContext.getUnLoginHandler().sendEmptyMessage(1);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}			
//		}
        return responseBody;
	}

	private static Result http_post(AppContext appContext, String url, Map<String, Object> params, Map<String,File> files) throws AppException, IOException {
        return Result.parse(_post(appContext, url, params, files));  
	}	
	

	public static Bitmap getNetBitmap(String url) throws AppException {
		//System.out.println("image_url==> "+url);
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		Bitmap bitmap = null;
		int time = 0;
		do{
			try 
			{
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, null, null);
				int statusCode = httpClient.executeMethod(httpGet);
				if (statusCode != HttpStatus.SC_OK) {
					throw AppException.http(statusCode);
				}
		        InputStream inStream = httpGet.getResponseBodyAsStream();
		        bitmap = BitmapFactory.decodeStream(inStream);
		        inStream.close();
		        break;
			} catch (HttpException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿè‡´å‘½çš„å¼‚å¸¸ï¼Œå�¯èƒ½æ˜¯å��è®®ä¸�å¯¹æˆ–è€…è¿”å›žçš„å†…å®¹æœ‰é—®é¢?
				e.printStackTrace();
				throw AppException.http(e);
			} catch (IOException e) {
				time++;
				if(time < RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {} 
					continue;
				}
				// å�‘ç”Ÿç½‘ç»œå¼‚å¸¸
				e.printStackTrace();
				throw AppException.network(e);
			} finally {
				// é‡Šæ”¾è¿žæŽ¥
				httpGet.releaseConnection();
				httpClient = null;
			}
		}while(time < RETRY_TIME);
		return bitmap;
	}
	
	/**
	 * æ£?Ÿ¥ç‰ˆæœ¬æ›´æ–°
	 * @param url
	 * @return
	 */
	public static Update checkVersion(AppContext appContext) throws AppException {
		try{
			return Update.parse(http_get(appContext, URLs.UPDATE_VERSION));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param username
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	public static LoginInfo login(AppContext appContext, String username, String pwd, int type,boolean isLogin) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("username", username);
		params.put("password", pwd);
		params.put("type",type) ;
		
		Log.e(TAG,"username:"+username) ;
		Log.e(TAG,"password:"+pwd) ;
		//params.put("keep_login", 1);
		
		String loginurl = null ;
		if(isLogin){
			loginurl = URLs.LOGIN_VALIDATE_HTTP;
			if(appContext.isHttpsLogin()){
				loginurl = URLs.LOGIN_VALIDATE_HTTPS;
			}
		}else{
			loginurl = URLs.SIGNUP_VALIDATE_HTTP;
			if(appContext.isHttpsLogin()){
				loginurl = URLs.SIGNUP_VALIDATE_HTTPS;
			}			
		}
		
		try{
			return LoginInfo.parse(_postToStr(appContext, loginurl, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	public static boolean validateRegistorName(AppContext appContext, String registorname) throws AppException{
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("registorname",registorname) ;
		
		String url = URLs.URL_API_VALIDATEUSERNAME ;
		String responseStr = null ;
		try{
			responseStr=_postToStr(appContext, url, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}		
		return true ;
	}
	public static MyInformation myInformation(AppContext appContext, int uid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
				
		try{
			return MyInformation.parse(_post(appContext, URLs.MY_INFORMATION, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * æ›´æ–°ç”¨æˆ·å¤´åƒ�
	 * @param appContext
	 * @param uid å½“å‰�ç”¨æˆ·uid
	 * @param portrait æ–°ä¸Šä¼ çš„å¤´åƒ�
	 * @return
	 * @throws AppException
	 */
	public static Result updatePortrait(AppContext appContext, int uid, File portrait) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		
		Map<String, File> files = new HashMap<String, File>();
		files.put("portrait", portrait);
				
		try{
			return http_post(appContext, URLs.PORTRAIT_UPDATE, params, files);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–ç”¨æˆ·ä¿¡æ�¯ä¸ªäººä¸“é¡µï¼ˆåŒ…å�«è¯¥ç”¨æˆ·çš„åŠ¨æ€�ä¿¡æ�¯ä»¥å�Šä¸ªäººä¿¡æ�¯ï¼‰
	 * @param uid è‡ªå·±çš„uid
	 * @param hisuid è¢«æŸ¥çœ‹ç”¨æˆ·çš„uid
	 * @param hisname è¢«æŸ¥çœ‹ç”¨æˆ·çš„ç”¨æˆ·å�?
	 * @param pageIndex é¡µé�¢ç´¢å¼•
	 * @param pageSize æ¯�é¡µè¯»å�–çš„åŠ¨æ€�ä¸ªæ•?
	 * @return
	 * @throws AppException
	 */
	public static UserInformation information(AppContext appContext, int uid, int hisuid, String hisname, int pageIndex, int pageSize) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("hisuid", hisuid);
		params.put("hisname", hisname);
		params.put("pageIndex", pageIndex);
		params.put("pageSize", pageSize);
				
		try{
			return UserInformation.parse(_post(appContext, URLs.USER_INFORMATION, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * æ›´æ–°ç”¨æˆ·ä¹‹é—´å…³ç³»ï¼ˆåŠ å…³æ³¨ã€�å�–æ¶ˆå…³æ³¨ï¼‰
	 * @param uid è‡ªå·±çš„uid
	 * @param hisuid å¯¹æ–¹ç”¨æˆ·çš„uid
	 * @param newrelation 0:å�–æ¶ˆå¯¹ä»–çš„å…³æ³?1:å…³æ³¨ä»?
	 * @return
	 * @throws AppException
	 */
	public static Result updateRelation(AppContext appContext, int uid, int hisuid, int newrelation) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("hisuid", hisuid);
		params.put("newrelation", newrelation);
				
		try{
			return Result.parse(_post(appContext, URLs.USER_UPDATERELATION, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–ç”¨æˆ·é€šçŸ¥ä¿¡æ�¯
	 * @param uid
	 * @return
	 * @throws AppException
	 */
	public static Notice getUserNotice(AppContext appContext, int uid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
				
		try{
			return Notice.parse(_post(appContext, URLs.USER_NOTICE, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * æ¸…ç©ºé€šçŸ¥æ¶ˆæ�¯
	 * @param uid
	 * @param type 1:@æˆ‘çš„ä¿¡æ�¯ 2:æœªè¯»æ¶ˆæ�¯ 3:è¯„è®ºä¸ªæ•° 4:æ–°ç²‰ä¸�ä¸ªæ•?
	 * @return
	 * @throws AppException
	 */
	public static Result noticeClear(AppContext appContext, int uid, int type) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("type", type);
				
		try{
			return Result.parse(_post(appContext, URLs.NOTICE_CLEAR, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * ç”¨æˆ·ç²‰ä¸�ã€�å…³æ³¨äººåˆ—è¡¨
	 * @param uid
	 * @param relation 0:æ˜¾ç¤ºè‡ªå·±çš„ç²‰ä¸?1:æ˜¾ç¤ºè‡ªå·±çš„å…³æ³¨è?
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static FriendList getFriendList(AppContext appContext, final int uid, final int relation, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.FRIENDS_LIST, new HashMap<String, Object>(){{
			put("uid", uid);
			put("relation", relation);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return FriendList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–èµ„è®¯åˆ—è¡¨
	 * @param url
	 * @param catalog
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static NewsList getNewsList(AppContext appContext, final int catalog, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.NEWS_LIST, new HashMap<String, Object>(){{
			put("catalog", catalog);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return NewsList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–èµ„è®¯çš„è¯¦æƒ?
	 * @param url
	 * @param news_id
	 * @return
	 * @throws AppException
	 */
	public static News getNewsDetail(AppContext appContext, final int news_id) throws AppException {
		String newUrl = _MakeURL(URLs.NEWS_DETAIL, new HashMap<String, Object>(){{
			put("id", news_id);
		}});
		
		try{
			return News.parse(http_get(appContext, newUrl));			
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–æŸ�ç”¨æˆ·çš„å�šå®¢åˆ—è¡¨
	 * @param authoruid
	 * @param uid
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static BlogList getUserBlogList(AppContext appContext, final int authoruid, final String authorname, final int uid, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.USERBLOG_LIST, new HashMap<String, Object>(){{
			put("authoruid", authoruid);
			put("authorname", URLEncoder.encode(authorname));
			put("uid", uid);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});

		try{
			return BlogList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–å�šå®¢åˆ—è¡¨
	 * @param type æŽ¨è��ï¼šrecommend æœ?–°ï¼šlatest
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static BlogList getBlogList(AppContext appContext, final String type, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.BLOG_LIST, new HashMap<String, Object>(){{
			put("type", type);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});

		try{
			return BlogList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * åˆ é™¤æŸ�ç”¨æˆ·çš„å�šå®¢
	 * @param uid
	 * @param authoruid
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public static Result delBlog(AppContext appContext, int uid, int authoruid, int id) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("authoruid", authoruid);
		params.put("id", id);

		try{
			return http_post(appContext, URLs.USERBLOG_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–å�šå®¢è¯¦æƒ…
	 * @param blog_id
	 * @return
	 * @throws AppException
	 */
	public static Blog getBlogDetail(AppContext appContext, final int blog_id) throws AppException {
		String newUrl = _MakeURL(URLs.BLOG_DETAIL, new HashMap<String, Object>(){{
			put("id", blog_id);
		}});
		
		try{
			return Blog.parse(http_get(appContext, newUrl));			
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–å¸–å­�åˆ—è¡¨
	 * @param url
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public static PostList getPostList(AppContext appContext, final int catalog, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.POST_LIST, new HashMap<String, Object>(){{
			put("catalog", catalog);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});

		try{
			return PostList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * é€šè¿‡TagèŽ·å�–å¸–å­�åˆ—è¡¨
	 * @param url
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public static PostList getPostListByTag(AppContext appContext, final String tag, final int pageIndex, final int pageSize) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("tag", tag);
		params.put("pageIndex", pageIndex);
		params.put("pageSize", pageSize);		

		try{
			return PostList.parse(_post(appContext, URLs.POST_LIST, params, null));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–å¸–å­�çš„è¯¦æƒ?
	 * @param url
	 * @param post_id
	 * @return
	 * @throws AppException
	 */
	public static Post getPostDetail(AppContext appContext, final int post_id) throws AppException {
		String newUrl = _MakeURL(URLs.POST_DETAIL, new HashMap<String, Object>(){{
			put("id", post_id);
		}});
		try{
			return Post.parse(http_get(appContext, newUrl));			
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘å¸–å­?
	 * @param post ï¼ˆuidã€�titleã€�catalogã€�contentã€�isNoticeMeï¼?
	 * @return
	 * @throws AppException
	 */
	public static Result pubPost(AppContext appContext, Post post) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", post.getAuthorId());
		params.put("title", post.getTitle());
		params.put("catalog", post.getCatalog());
		params.put("content", post.getBody());
		params.put("isNoticeMe", post.getIsNoticeMe());				
		
		try{
			return http_post(appContext, URLs.POST_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–åŠ¨å¼¹åˆ—è¡¨
	 * @param uid
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static TweetList getTweetList(AppContext appContext, final int uid, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.TWEET_LIST, new HashMap<String, Object>(){{
			put("uid", uid);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return TweetList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–åŠ¨å¼¹è¯¦æƒ…
	 * @param tweet_id
	 * @return
	 * @throws AppException
	 */
	public static Tweet getTweetDetail(AppContext appContext, final int tweet_id) throws AppException {
		String newUrl = _MakeURL(URLs.TWEET_DETAIL, new HashMap<String, Object>(){{
			put("id", tweet_id);
		}});
		try{
			return Tweet.parse(http_get(appContext, newUrl));			
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘åŠ¨å¼?
	 * @param Tweet-uid & msg & image
	 * @return
	 * @throws AppException
	 */
	public static Result pubTweet(AppContext appContext, Tweet tweet) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", tweet.getAuthorId());
		params.put("msg", tweet.getBody());
				
		Map<String, File> files = new HashMap<String, File>();
		if(tweet.getImageFile() != null)
			files.put("img", tweet.getImageFile());
		
		try{
			return http_post(appContext, URLs.TWEET_PUB, params, files);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}

	/**
	 * åˆ é™¤åŠ¨å¼¹
	 * @param uid
	 * @param tweetid
	 * @return
	 * @throws AppException
	 */
	public static Result delTweet(AppContext appContext, int uid, int tweetid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("tweetid", tweetid);

		try{
			return http_post(appContext, URLs.TWEET_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–åŠ¨æ?åˆ—è¡¨
	 * @param uid
	 * @param catalog 1æœ?–°åŠ¨æ?  2@æˆ? 3è¯„è®º  4æˆ‘è‡ªå·?
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static ActiveList getActiveList(AppContext appContext, final int uid,final int catalog, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.ACTIVE_LIST, new HashMap<String, Object>(){{
			put("uid", uid);
			put("catalog", catalog);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return ActiveList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–ç•™è¨€åˆ—è¡¨
	 * @param uid
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public static MessageList getMessageList(AppContext appContext, final int uid, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.MESSAGE_LIST, new HashMap<String, Object>(){{
			put("uid", uid);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return MessageList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘é?ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param receiver æŽ¥å�—è€…çš„ç”¨æˆ·id
	 * @param content æ¶ˆæ�¯å†…å®¹ï¼Œæ³¨æ„�ä¸�èƒ½è¶…è¿?50ä¸ªå­—ç¬?
	 * @return
	 * @throws AppException
	 */
	public static Result pubMessage(AppContext appContext, int uid, int receiver, String content) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("receiver", receiver);
		params.put("content", content);

		try{
			return http_post(appContext, URLs.MESSAGE_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * è½¬å�‘ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param receiver æŽ¥å�—è€…çš„ç”¨æˆ·å�?
	 * @param content æ¶ˆæ�¯å†…å®¹ï¼Œæ³¨æ„�ä¸�èƒ½è¶…è¿?50ä¸ªå­—ç¬?
	 * @return
	 * @throws AppException
	 */
	public static Result forwardMessage(AppContext appContext, int uid, String receiverName, String content) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("receiverName", receiverName);
		params.put("content", content);

		try{
			return http_post(appContext, URLs.MESSAGE_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * åˆ é™¤ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param friendid ç•™è¨€è€…id
	 * @return
	 * @throws AppException
	 */
	public static Result delMessage(AppContext appContext, int uid, int friendid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("friendid", friendid);

		try{
			return http_post(appContext, URLs.MESSAGE_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–å�šå®¢è¯„è®ºåˆ—è¡¨
	 * @param id å�šå®¢id
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static BlogCommentList getBlogCommentList(AppContext appContext, final int id, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.BLOGCOMMENT_LIST, new HashMap<String, Object>(){{
			put("id", id);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return BlogCommentList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘è¡¨å�šå®¢è¯„è®º
	 * @param blog å�šå®¢id
	 * @param uid ç™»é™†ç”¨æˆ·çš„uid
	 * @param content è¯„è®ºå†…å®¹
	 * @return
	 * @throws AppException
	 */
	public static Result pubBlogComment(AppContext appContext, int blog, int uid, String content) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("blog", blog);
		params.put("uid", uid);
		params.put("content", content);
		
		try{
			return http_post(appContext, URLs.BLOGCOMMENT_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘è¡¨å�šå®¢è¯„è®º
	 * @param blog å�šå®¢id
	 * @param uid ç™»é™†ç”¨æˆ·çš„uid
	 * @param content è¯„è®ºå†…å®¹
	 * @param reply_id è¯„è®ºid
	 * @param objuid è¢«è¯„è®ºçš„è¯„è®ºå�‘è¡¨è€…çš„uid
	 * @return
	 * @throws AppException
	 */
	public static Result replyBlogComment(AppContext appContext, int blog, int uid, String content, int reply_id, int objuid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("blog", blog);
		params.put("uid", uid);
		params.put("content", content);
		params.put("reply_id", reply_id);
		params.put("objuid", objuid);
		
		try{
			return http_post(appContext, URLs.BLOGCOMMENT_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * åˆ é™¤å�šå®¢è¯„è®º
	 * @param uid ç™»å½•ç”¨æˆ·çš„uid
	 * @param blogid å�šå®¢id
	 * @param replyid è¯„è®ºid
	 * @param authorid è¯„è®ºå�‘è¡¨è€…çš„uid
	 * @param owneruid å�šå®¢ä½œè?uid
	 * @return
	 * @throws AppException
	 */
	public static Result delBlogComment(AppContext appContext, int uid, int blogid, int replyid, int authorid, int owneruid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("blogid", blogid);		
		params.put("replyid", replyid);
		params.put("authorid", authorid);
		params.put("owneruid", owneruid);

		try{
			return http_post(appContext, URLs.BLOGCOMMENT_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–è¯„è®ºåˆ—è¡¨
	 * @param catalog 1æ–°é—»  2å¸–å­�  3åŠ¨å¼¹  4åŠ¨æ?
	 * @param id
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static CommentList getCommentList(AppContext appContext, final int catalog, final int id, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.COMMENT_LIST, new HashMap<String, Object>(){{
			put("catalog", catalog);
			put("id", id);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return CommentList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * å�‘è¡¨è¯„è®º
	 * @param catalog 1æ–°é—»  2å¸–å­�  3åŠ¨å¼¹  4åŠ¨æ?
	 * @param id æŸ�æ�¡æ–°é—»ï¼Œå¸–å­�ï¼ŒåŠ¨å¼¹çš„id
	 * @param uid ç”¨æˆ·uid
	 * @param content å�‘è¡¨è¯„è®ºçš„å†…å®?
	 * @param isPostToMyZone æ˜¯å�¦è½¬å�‘åˆ°æˆ‘çš„ç©ºé—? 0ä¸�è½¬å�? 1è½¬å�‘
	 * @return
	 * @throws AppException
	 */
	public static Result pubComment(AppContext appContext, int catalog, int id, int uid, String content, int isPostToMyZone) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("catalog", catalog);
		params.put("id", id);
		params.put("uid", uid);
		params.put("content", content);
		params.put("isPostToMyZone", isPostToMyZone);
		
		try{
			return http_post(appContext, URLs.COMMENT_PUB, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}

	/**
	 * 
	 * @param id è¡¨ç¤ºè¢«è¯„è®ºçš„æŸ�æ�¡æ–°é—»ï¼Œå¸–å­�ï¼ŒåŠ¨å¼¹çš„id æˆ–è?æŸ�æ�¡æ¶ˆæ�¯çš?friendid 
	 * @param catalog è¡¨ç¤ºè¯¥è¯„è®ºæ‰€å±žä»€ä¹ˆç±»åž‹ï¼š1æ–°é—»  2å¸–å­�  3åŠ¨å¼¹  4åŠ¨æ?
	 * @param replyid è¡¨ç¤ºè¢«å›žå¤�çš„å�•ä¸ªè¯„è®ºid
	 * @param authorid è¡¨ç¤ºè¯¥è¯„è®ºçš„åŽŸå§‹ä½œè?id
	 * @param uid ç”¨æˆ·uid ä¸?ˆ¬éƒ½æ˜¯å½“å‰�ç™»å½•ç”¨æˆ·uid
	 * @param content å�‘è¡¨è¯„è®ºçš„å†…å®?
	 * @return
	 * @throws AppException
	 */
	public static Result replyComment(AppContext appContext, int id, int catalog, int replyid, int authorid, int uid, String content) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("catalog", catalog);
		params.put("id", id);
		params.put("uid", uid);
		params.put("content", content);
		params.put("replyid", replyid);
		params.put("authorid", authorid);
		
		try{
			return http_post(appContext, URLs.COMMENT_REPLY, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * åˆ é™¤è¯„è®º
	 * @param id è¡¨ç¤ºè¢«è¯„è®ºå¯¹åº”çš„æŸ�æ�¡æ–°é—»,å¸–å­�,åŠ¨å¼¹çš„id æˆ–è?æŸ�æ�¡æ¶ˆæ�¯çš?friendid
	 * @param catalog è¡¨ç¤ºè¯¥è¯„è®ºæ‰€å±žä»€ä¹ˆç±»åž‹ï¼š1æ–°é—»  2å¸–å­�  3åŠ¨å¼¹  4åŠ¨æ?&ç•™è¨€
	 * @param replyid è¡¨ç¤ºè¢«å›žå¤�çš„å�•ä¸ªè¯„è®ºid
	 * @param authorid è¡¨ç¤ºè¯¥è¯„è®ºçš„åŽŸå§‹ä½œè?id
	 * @return
	 * @throws AppException
	 */
	public static Result delComment(AppContext appContext, int id, int catalog, int replyid, int authorid) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("id", id);
		params.put("catalog", catalog);
		params.put("replyid", replyid);
		params.put("authorid", authorid);

		try{
			return http_post(appContext, URLs.COMMENT_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * ç”¨æˆ·æ”¶è—�åˆ—è¡¨
	 * @param uid ç”¨æˆ·UID
	 * @param type 0:å…¨éƒ¨æ”¶è—� 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @param pageIndex é¡µé�¢ç´¢å¼• 0è¡¨ç¤ºç¬¬ä¸€é¡?
	 * @param pageSize æ¯�é¡µçš„æ•°é‡?
	 * @return
	 * @throws AppException
	 */
	public static FavoriteList getFavoriteList(AppContext appContext, final int uid, final int type, final int pageIndex, final int pageSize) throws AppException {
		String newUrl = _MakeURL(URLs.FAVORITE_LIST, new HashMap<String, Object>(){{
			put("uid", uid);
			put("type", type);
			put("pageIndex", pageIndex);
			put("pageSize", pageSize);
		}});
		
		try{
			return FavoriteList.parse(http_get(appContext, newUrl));		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}	
	
	/**
	 * ç”¨æˆ·æ·»åŠ æ”¶è—�
	 * @param uid ç”¨æˆ·UID
	 * @param objid æ¯”å¦‚æ˜¯æ–°é—»ID æˆ–è?é—®ç­”ID æˆ–è?åŠ¨å¼¹ID
	 * @param type 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @return
	 * @throws AppException
	 */
	public static Result addFavorite(AppContext appContext, int uid, int objid, int type) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("objid", objid);
		params.put("type", type);

		try{
			return http_post(appContext, URLs.FAVORITE_ADD, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * ç”¨æˆ·åˆ é™¤æ”¶è—�
	 * @param uid ç”¨æˆ·UID
	 * @param objid æ¯”å¦‚æ˜¯æ–°é—»ID æˆ–è?é—®ç­”ID æˆ–è?åŠ¨å¼¹ID
	 * @param type 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @return
	 * @throws AppException
	 */
	public static Result delFavorite(AppContext appContext, int uid, int objid, int type) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("uid", uid);
		params.put("objid", objid);
		params.put("type", type);

		try{
			return http_post(appContext, URLs.FAVORITE_DELETE, params, null);		
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	
	/**
	 * èŽ·å�–æ�œç´¢åˆ—è¡¨
	 * @param catalog å…¨éƒ¨:all æ–°é—»:news  é—®ç­”:post è½¯ä»¶:software å�šå®¢:blog ä»£ç �:code
	 * @param content æ�œç´¢çš„å†…å®?
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws AppException
	 */
	public static SearchList getSearchList(AppContext appContext, String catalog, String content, int pageIndex, int pageSize) throws AppException {
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("catalog", catalog);
		params.put("content", content);
		params.put("pageIndex", pageIndex);
		params.put("pageSize", pageSize);

		try{
			return SearchList.parse(_post(appContext, URLs.SEARCH_LIST, params, null));	
		}catch(Exception e){
			if(e instanceof AppException)
				throw (AppException)e;
			throw AppException.network(e);
		}
	}
	

	

	


}
