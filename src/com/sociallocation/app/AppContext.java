package com.sociallocation.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

import com.sociallocation.api.ApiClient;
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
import com.sociallocation.bean.User;
import com.sociallocation.bean.UserInformation;
import com.sociallocation.util.CyptoUtils;
import com.sociallocation.util.FileUtils;
import com.sociallocation.util.ImageUtils;
import com.sociallocation.util.StringUtils;
import com.sociallocation.util.UIHelper;


import android.R.integer;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.CacheManager;

/**
 * å…¨å±€åº”ç”¨ç¨‹åº�ç±»ï¼šç”¨äºŽä¿�å­˜å’Œè°ƒç”¨å…¨å±?º”ç”¨é…�ç½®å�Šè®¿é—®ç½‘ç»œæ•°æ�®
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
@SuppressWarnings("deprecation")
public class AppContext extends Application {
	public static final String TAG = "Appcontext" ;
	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;
	
	public static final int PAGE_SIZE = 20;//é»˜è®¤åˆ†é¡µå¤§å°�
	private static final int CACHE_TIME = 60*60000;//ç¼“å­˜å¤±æ•ˆæ—¶é—´
	
	private boolean login = false;	//ç™»å½•çŠ¶æ?
	private int loginUid = 0;	//ç™»å½•ç”¨æˆ·çš„id
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
	
	private String saveImagePath;//ä¿�å­˜å›¾ç‰‡è·¯å¾„
	
	private Handler unLoginHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == 1){
//				UIHelper.ToastMessage(AppContext.this, getString(R.string.msg_login_error));
//				UIHelper.showLoginDialog(AppContext.this);
			}
		}		
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
        //æ³¨å†ŒAppå¼‚å¸¸å´©æºƒå¤„ç�†å™?
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
        
        init();
	}

	/**
	 * åˆ�å§‹åŒ?
	 */
	private void init(){
		//è®¾ç½®ä¿�å­˜å›¾ç‰‡çš„è·¯å¾?
		saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
		if(StringUtils.isEmpty(saveImagePath)){
			setProperty(AppConfig.SAVE_IMAGE_PATH, AppConfig.DEFAULT_SAVE_IMAGE_PATH);
			saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
		}
	}
	
	/**
	 * æ£?µ‹å½“å‰�ç³»ç»Ÿå£°éŸ³æ˜¯å�¦ä¸ºæ­£å¸¸æ¨¡å¼?
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE); 
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}
	
	/**
	 * åº”ç”¨ç¨‹åº�æ˜¯å�¦å�‘å‡ºæ��ç¤ºéŸ?
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}
	
	/**
	 * æ£?µ‹ç½‘ç»œæ˜¯å�¦å�¯ç”¨
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * èŽ·å�–å½“å‰�ç½‘ç»œç±»åž‹
	 * @return 0ï¼šæ²¡æœ‰ç½‘ç»?  1ï¼šWIFIç½‘ç»œ   2ï¼šWAPç½‘ç»œ    3ï¼šNETç½‘ç»œ
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
	
	/**
	 * åˆ¤æ–­å½“å‰�ç‰ˆæœ¬æ˜¯å�¦å…¼å®¹ç›®æ ‡ç‰ˆæœ¬çš„æ–¹æ³?
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}
	
	/**
	 * èŽ·å�–Appå®‰è£…åŒ…ä¿¡æ�?
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try { 
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
		if(info == null) info = new PackageInfo();
		return info;
	}
	
	/**
	 * èŽ·å�–Appå”¯ä¸€æ ‡è¯†
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if(StringUtils.isEmpty(uniqueID)){
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}
	

	public boolean isLogin() {
		return login;
	}

	public int getLoginUid() {
		return this.loginUid;
	}
	

	public void Logout() {
		ApiClient.cleanCookie();
		this.cleanCookie();
		this.login = false;
		this.loginUid = 0;
	}
	

	public Handler getUnLoginHandler() {
		return this.unLoginHandler;
	}
	
	public void initLoginInfo() {
		User loginUser = getUserInfo();
		if(loginUser!=null && loginUser.getUid()>0 && loginUser.isRememberMe()){
			this.loginUid = loginUser.getUid();
			this.login = true;
		}else{
			this.Logout();
		}
	}

//	public User loginVerify(String account, String pwd) throws AppException {
//		return ApiClient.login(this, account, pwd);
//	}
	public LoginInfo loginVerify(String account, String pwd,int type,boolean isLogin) throws AppException {
		return ApiClient.login(this, account, pwd,type,isLogin);
	}	
	public void commitException(String exceptionDsp){
		
	}
	public User getUserDetail(int userid)throws AppException{
		return ApiClient.getUserDetail(this,userid) ;
	}
	public MyInformation getMyInformation(boolean isRefresh) throws AppException {
		MyInformation myinfo = null;
		String key = "myinfo_"+loginUid;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				myinfo = ApiClient.myInformation(this, loginUid);
				if(myinfo != null && myinfo.getName().length() > 0){
					Notice notice = myinfo.getNotice();
					myinfo.setNotice(null);
					myinfo.setCacheKey(key);
					saveObject(myinfo, key);
					myinfo.setNotice(notice);
				}
			}catch(AppException e){
				myinfo = (MyInformation)readObject(key);
				if(myinfo == null)
					throw e;
			}
		} else {
			myinfo = (MyInformation)readObject(key);
			if(myinfo == null)
				myinfo = new MyInformation();
		}
		return myinfo;
	}	
	

	public UserInformation getInformation(int uid, int hisuid, String hisname, int pageIndex, boolean isRefresh) throws AppException {
		String _hisname = ""; 
		if(!StringUtils.isEmpty(hisname)){
			_hisname = hisname;
		}
		UserInformation userinfo = null;
		String key = "userinfo_"+uid+"_"+hisuid+"_"+(URLEncoder.encode(hisname))+"_"+pageIndex+"_"+PAGE_SIZE; 
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {			
			try{
				userinfo = ApiClient.information(this, uid, hisuid, _hisname, pageIndex, PAGE_SIZE);
				if(userinfo != null && pageIndex == 0){
					Notice notice = userinfo.getNotice();
					userinfo.setNotice(null);
					userinfo.setCacheKey(key);
					saveObject(userinfo, key);
					userinfo.setNotice(notice);
				}
			}catch(AppException e){
				userinfo = (UserInformation)readObject(key);
				if(userinfo == null)
					throw e;
			}
		} else {
			userinfo = (UserInformation)readObject(key);
			if(userinfo == null)
				userinfo = new UserInformation();
		}
		return userinfo;
	}
	
	public Result updateRelation(int uid, int hisuid, int newrelation) throws AppException {
		return ApiClient.updateRelation(this, uid, hisuid, newrelation);
	}
	

	public Result updatePortrait(File portrait) throws AppException {
		return ApiClient.updatePortrait(this, loginUid, portrait);
	}
	
	/**
	 * æ¸…ç©ºé€šçŸ¥æ¶ˆæ�¯
	 * @param uid
	 * @param type 1:@æˆ‘çš„ä¿¡æ�¯ 2:æœªè¯»æ¶ˆæ�¯ 3:è¯„è®ºä¸ªæ•° 4:æ–°ç²‰ä¸�ä¸ªæ•?
	 * @return
	 * @throws AppException
	 */
	public Result noticeClear(int uid, int type) throws AppException {
		return ApiClient.noticeClear(this, uid, type);
	}
	
	/**
	 * èŽ·å�–ç”¨æˆ·é€šçŸ¥ä¿¡æ�¯
	 * @param uid
	 * @return
	 * @throws AppException
	 */
	public Notice getUserNotice(int uid) throws AppException {
		return ApiClient.getUserNotice(this, uid);
	}
	
	/**
	 * ç”¨æˆ·æ”¶è—�åˆ—è¡¨
	 * @param type 0:å…¨éƒ¨æ”¶è—� 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @param pageIndex é¡µé�¢ç´¢å¼• 0è¡¨ç¤ºç¬¬ä¸€é¡?
	 * @return
	 * @throws AppException
	 */
	public FavoriteList getFavoriteList(int type, int pageIndex, boolean isRefresh) throws AppException {
		FavoriteList list = null;
		String key = "favoritelist_"+loginUid+"_"+type+"_"+pageIndex+"_"+PAGE_SIZE; 
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getFavoriteList(this, loginUid, type, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (FavoriteList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (FavoriteList)readObject(key);
			if(list == null)
				list = new FavoriteList();
		}
		return list;
	}
	
	/**
	 * ç”¨æˆ·ç²‰ä¸�ã€�å…³æ³¨äººåˆ—è¡¨
	 * @param relation 0:æ˜¾ç¤ºè‡ªå·±çš„ç²‰ä¸?1:æ˜¾ç¤ºè‡ªå·±çš„å…³æ³¨è?
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public FriendList getFriendList(int relation, int pageIndex, boolean isRefresh) throws AppException {
		FriendList list = null;
		String key = "friendlist_"+loginUid+"_"+relation+"_"+pageIndex+"_"+PAGE_SIZE; 
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getFriendList(this, loginUid, relation, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (FriendList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (FriendList)readObject(key);
			if(list == null)
				list = new FriendList();
		}
		return list;
	}
	
	/**
	 * æ–°é—»åˆ—è¡¨
	 * @param catalog
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws ApiException
	 */
	public NewsList getNewsList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		NewsList list = null;
		String key = "newslist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getNewsList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (NewsList)readObject(key);
				if(list == null)
					throw e;
			}		
		} else {
			list = (NewsList)readObject(key);
			if(list == null)
				list = new NewsList();
		}
		return list;
	}
	
	/**
	 * æ–°é—»è¯¦æƒ…
	 * @param news_id
	 * @return
	 * @throws ApiException
	 */
	public News getNews(int news_id, boolean isRefresh) throws AppException {		
		News news = null;
		String key = "news_"+news_id;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				news = ApiClient.getNewsDetail(this, news_id);
				if(news != null){
					Notice notice = news.getNotice();
					news.setNotice(null);
					news.setCacheKey(key);
					saveObject(news, key);
					news.setNotice(notice);
				}
			}catch(AppException e){
				news = (News)readObject(key);
				if(news == null)
					throw e;
			}
		} else {
			news = (News)readObject(key);
			if(news == null)
				news = new News();
		}
		return news;		
	}
	
	/**
	 * ç”¨æˆ·å�šå®¢åˆ—è¡¨
	 * @param authoruid
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public BlogList getUserBlogList(int authoruid, String authorname, int pageIndex, boolean isRefresh) throws AppException {
		BlogList list = null;
		String key = "userbloglist_"+authoruid+"_"+(URLEncoder.encode(authorname))+"_"+loginUid+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getUserBlogList(this, authoruid, authorname, loginUid, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (BlogList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (BlogList)readObject(key);
			if(list == null)
				list = new BlogList();
		}
		return list;
	}
	
	/**
	 * å�šå®¢åˆ—è¡¨
	 * @param type æŽ¨è��ï¼šrecommend æœ?–°ï¼šlatest
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public BlogList getBlogList(String type, int pageIndex, boolean isRefresh) throws AppException {
		BlogList list = null;
		String key = "bloglist_"+type+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getBlogList(this, type, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (BlogList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (BlogList)readObject(key);
			if(list == null)
				list = new BlogList();
		}
		return list;
	}
	
	/**
	 * å�šå®¢è¯¦æƒ…
	 * @param blog_id
	 * @return
	 * @throws AppException
	 */
	public Blog getBlog(int blog_id, boolean isRefresh) throws AppException {
		Blog blog = null;
		String key = "blog_"+blog_id;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				blog = ApiClient.getBlogDetail(this, blog_id);
				if(blog != null){
					Notice notice = blog.getNotice();
					blog.setNotice(null);
					blog.setCacheKey(key);
					saveObject(blog, key);
					blog.setNotice(notice);
				}
			}catch(AppException e){
				blog = (Blog)readObject(key);
				if(blog == null)
					throw e;
			}
		} else {
			blog = (Blog)readObject(key);
			if(blog == null)
				blog = new Blog();
		}
		return blog;
	}
	

	
	
	
	
	
	
	/**
	 * å¸–å­�åˆ—è¡¨
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws ApiException
	 */
	public PostList getPostList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		PostList list = null;
		String key = "postlist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {		
			try{
				list = ApiClient.getPostList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (PostList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (PostList)readObject(key);
			if(list == null)
				list = new PostList();
		}
		return list;
	}
	
	/**
	 * Tagç›¸å…³å¸–å­�åˆ—è¡¨
	 * @param tag
	 * @param pageIndex
	 * @return
	 * @throws ApiException
	 */
	public PostList getPostListByTag(String tag, int pageIndex, boolean isRefresh) throws AppException {
		PostList list = null;
		String key = "postlist_"+(URLEncoder.encode(tag))+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {		
			try{
				list = ApiClient.getPostListByTag(this, tag, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (PostList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (PostList)readObject(key);
			if(list == null)
				list = new PostList();
		}
		return list;
	}
	
	/**
	 * è¯»å�–å¸–å­�è¯¦æƒ…
	 * @param post_id
	 * @return
	 * @throws ApiException
	 */
	public Post getPost(int post_id, boolean isRefresh) throws AppException {		
		Post post = null;
		String key = "post_"+post_id;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {	
			try{
				post = ApiClient.getPostDetail(this, post_id);
				if(post != null){
					Notice notice = post.getNotice();
					post.setNotice(null);
					post.setCacheKey(key);
					saveObject(post, key);
					post.setNotice(notice);
				}
			}catch(AppException e){
				post = (Post)readObject(key);
				if(post == null)
					throw e;
			}
		} else {
			post = (Post)readObject(key);
			if(post == null)
				post = new Post();
		}
		return post;		
	}
	
	/**
	 * åŠ¨å¼¹åˆ—è¡¨
	 * @param catalog -1 çƒ­é—¨ï¼? æœ?–°ï¼Œå¤§äº? æŸ�ç”¨æˆ·çš„åŠ¨å¼¹(uid)
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public TweetList getTweetList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		TweetList list = null;
		String key = "tweetlist_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;		
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getTweetList(this, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (TweetList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (TweetList)readObject(key);
			if(list == null)
				list = new TweetList();
		}
		return list;
	}
	
	/**
	 * èŽ·å�–åŠ¨å¼¹è¯¦æƒ…
	 * @param tweet_id
	 * @return
	 * @throws AppException
	 */
	public Tweet getTweet(int tweet_id, boolean isRefresh) throws AppException {
		Tweet tweet = null;
		String key = "tweet_"+tweet_id;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				tweet = ApiClient.getTweetDetail(this, tweet_id);
				if(tweet != null){
					Notice notice = tweet.getNotice();
					tweet.setNotice(null);
					tweet.setCacheKey(key);
					saveObject(tweet, key);
					tweet.setNotice(notice);
				}
			}catch(AppException e){
				tweet = (Tweet)readObject(key);
				if(tweet == null)
					throw e;
			}
		} else {
			tweet = (Tweet)readObject(key);
			if(tweet == null)
				tweet = new Tweet();
		}
		return tweet;
	}
	
	/**
	 * åŠ¨æ?åˆ—è¡¨
	 * @param catalog 1æœ?–°åŠ¨æ? 2@æˆ?3è¯„è®º 4æˆ‘è‡ªå·?
	 * @param id
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public ActiveList getActiveList(int catalog, int pageIndex, boolean isRefresh) throws AppException {
		ActiveList list = null;
		String key = "activelist_"+loginUid+"_"+catalog+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getActiveList(this, loginUid, catalog, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (ActiveList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (ActiveList)readObject(key);
			if(list == null)
				list = new ActiveList();
		}
		return list;
	}
	
	/**
	 * ç•™è¨€åˆ—è¡¨
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public MessageList getMessageList(int pageIndex, boolean isRefresh) throws AppException {
		MessageList list = null;
		String key = "messagelist_"+loginUid+"_"+pageIndex+"_"+PAGE_SIZE;
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getMessageList(this, loginUid, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (MessageList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (MessageList)readObject(key);
			if(list == null)
				list = new MessageList();
		}
		return list;
	}
	
	/**
	 * å�šå®¢è¯„è®ºåˆ—è¡¨
	 * @param id å�šå®¢Id
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public BlogCommentList getBlogCommentList(int id, int pageIndex, boolean isRefresh) throws AppException {
		BlogCommentList list = null;
		String key = "blogcommentlist_"+id+"_"+pageIndex+"_"+PAGE_SIZE;		
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getBlogCommentList(this, id, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (BlogCommentList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (BlogCommentList)readObject(key);
			if(list == null)
				list = new BlogCommentList();
		}
		return list;
	}
	
	/**
	 * è¯„è®ºåˆ—è¡¨
	 * @param catalog 1æ–°é—» 2å¸–å­� 3åŠ¨å¼¹ 4åŠ¨æ?
	 * @param id æŸ�æ�¡æ–°é—»ï¼Œå¸–å­�ï¼ŒåŠ¨å¼¹çš„id æˆ–è?æŸ�æ�¡ç•™è¨€çš„friendid
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public CommentList getCommentList(int catalog, int id, int pageIndex, boolean isRefresh) throws AppException {
		CommentList list = null;
		String key = "commentlist_"+catalog+"_"+id+"_"+pageIndex+"_"+PAGE_SIZE;		
		if(isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try{
				list = ApiClient.getCommentList(this, catalog, id, pageIndex, PAGE_SIZE);
				if(list != null && pageIndex == 0){
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			}catch(AppException e){
				list = (CommentList)readObject(key);
				if(list == null)
					throw e;
			}
		} else {
			list = (CommentList)readObject(key);
			if(list == null)
				list = new CommentList();
		}
		return list;
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
	public SearchList getSearchList(String catalog, String content, int pageIndex, int pageSize) throws AppException {
		return ApiClient.getSearchList(this, catalog, content, pageIndex, pageSize);
	}
	
	/**
	 * å�‘å¸–å­?
	 * @param post ï¼ˆuidã€�titleã€�catalogã€�contentã€�isNoticeMeï¼?
	 * @return
	 * @throws AppException
	 */
	public Result pubPost(Post post) throws AppException {
		return ApiClient.pubPost(this, post);
	}
	
	/**
	 * å�‘åŠ¨å¼?
	 * @param Tweet-uid & msg & image
	 * @return
	 * @throws AppException
	 */
	public Result pubTweet(Tweet tweet) throws AppException {
		return ApiClient.pubTweet(this, tweet);
	}
	
	/**
	 * åˆ é™¤åŠ¨å¼¹
	 * @param uid
	 * @param tweetid
	 * @return
	 * @throws AppException
	 */
	public Result delTweet(int uid, int tweetid) throws AppException {
		return ApiClient.delTweet(this, uid, tweetid);
	}
	
	/**
	 * å�‘é?ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param receiver æŽ¥å�—è€…çš„ç”¨æˆ·id
	 * @param content æ¶ˆæ�¯å†…å®¹ï¼Œæ³¨æ„�ä¸�èƒ½è¶…è¿?50ä¸ªå­—ç¬?
	 * @return
	 * @throws AppException
	 */
	public Result pubMessage(int uid, int receiver, String content) throws AppException {
		return ApiClient.pubMessage(this, uid, receiver, content);
	}
	
	/**
	 * è½¬å�‘ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param receiver æŽ¥å�—è€…çš„ç”¨æˆ·å�?
	 * @param content æ¶ˆæ�¯å†…å®¹ï¼Œæ³¨æ„�ä¸�èƒ½è¶…è¿?50ä¸ªå­—ç¬?
	 * @return
	 * @throws AppException
	 */
	public Result forwardMessage(int uid, String receiver, String content) throws AppException {
		return ApiClient.forwardMessage(this, uid, receiver, content);
	}
	
	/**
	 * åˆ é™¤ç•™è¨€
	 * @param uid ç™»å½•ç”¨æˆ·uid
	 * @param friendid ç•™è¨€è€…id
	 * @return
	 * @throws AppException
	 */
	public Result delMessage(int uid, int friendid) throws AppException {
		return ApiClient.delMessage(this, uid, friendid);
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
	public Result pubComment(int catalog, int id, int uid, String content, int isPostToMyZone) throws AppException {
		return ApiClient.pubComment(this, catalog, id, uid, content, isPostToMyZone);
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
	public Result replyComment(int id, int catalog, int replyid, int authorid, int uid, String content) throws AppException {
		return ApiClient.replyComment(this, id, catalog, replyid, authorid, uid, content);
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
	public Result delComment(int id, int catalog, int replyid, int authorid) throws AppException {
		return ApiClient.delComment(this, id, catalog, replyid, authorid);
	}
	
	/**
	 * å�‘è¡¨å�šå®¢è¯„è®º
	 * @param blog å�šå®¢id
	 * @param uid ç™»é™†ç”¨æˆ·çš„uid
	 * @param content è¯„è®ºå†…å®¹
	 * @return
	 * @throws AppException
	 */
	public Result pubBlogComment(int blog, int uid, String content) throws AppException {
		return ApiClient.pubBlogComment(this, blog, uid, content);
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
	public Result replyBlogComment(int blog, int uid, String content, int reply_id, int objuid) throws AppException {
		return ApiClient.replyBlogComment(this, blog, uid, content, reply_id, objuid);
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
	public Result delBlogComment(int uid, int blogid, int replyid, int authorid, int owneruid) throws AppException {
		return ApiClient.delBlogComment(this, uid, blogid, replyid, authorid, owneruid);
	}
	
	/**
	 * åˆ é™¤å�šå®¢
	 * @param uid ç™»å½•ç”¨æˆ·çš„uid
	 * @param authoruid å�šå®¢ä½œè?uid
	 * @param id å�šå®¢id
	 * @return
	 * @throws AppException
	 */
	public Result delBlog(int uid, int authoruid, int id) throws AppException { 	
		return ApiClient.delBlog(this, uid, authoruid, id);
	}
	
	/**
	 * ç”¨æˆ·æ·»åŠ æ”¶è—�
	 * @param uid ç”¨æˆ·UID
	 * @param objid æ¯”å¦‚æ˜¯æ–°é—»ID æˆ–è?é—®ç­”ID æˆ–è?åŠ¨å¼¹ID
	 * @param type 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @return
	 * @throws AppException
	 */
	public Result addFavorite(int uid, int objid, int type) throws AppException {
		return ApiClient.addFavorite(this, uid, objid, type);
	}
	
	/**
	 * ç”¨æˆ·åˆ é™¤æ”¶è—�
	 * @param uid ç”¨æˆ·UID
	 * @param objid æ¯”å¦‚æ˜¯æ–°é—»ID æˆ–è?é—®ç­”ID æˆ–è?åŠ¨å¼¹ID
	 * @param type 1:è½¯ä»¶ 2:è¯�é¢˜ 3:å�šå®¢ 4:æ–°é—» 5:ä»£ç �
	 * @return
	 * @throws AppException
	 */
	public Result delFavorite(int uid, int objid, int type) throws AppException { 	
		return ApiClient.delFavorite(this, uid, objid, type);
	}
	
	/**
	 * ä¿�å­˜ç™»å½•ä¿¡æ�¯
	 * @param username
	 * @param pwd
	 */
	public void saveUserInfo(final User user) {
		this.loginUid = user.getUid();
		this.login = true;
		setProperties(new Properties(){{
			setProperty("user.uid", String.valueOf(user.getUid()));
			setProperty("user.name", user.getName());
			setProperty("user.face", FileUtils.getFileName(user.getFace()));//ç”¨æˆ·å¤´åƒ�-æ–‡ä»¶å�?
			setProperty("user.account", user.getAccount());
			setProperty("user.pwd", CyptoUtils.encode("oschinaApp",user.getPwd()));
			setProperty("user.location", user.getLocation());
			setProperty("user.followers", String.valueOf(user.getFollowers()));
			setProperty("user.fans", String.valueOf(user.getFans()));
			setProperty("user.score", String.valueOf(user.getScore()));
			setProperty("user.isRememberMe", String.valueOf(user.isRememberMe()));//æ˜¯å�¦è®°ä½�æˆ‘çš„ä¿¡æ�¯
		}});		
	}
	
	/**
	 * æ¸…é™¤ç™»å½•ä¿¡æ�¯
	 */
	public void cleanLoginInfo() {
		this.loginUid = 0;
		this.login = false;
		removeProperty("user.uid","user.name","user.face","user.account","user.pwd",
				"user.location","user.followers","user.fans","user.score","user.isRememberMe");
	}
	public void saveLoginInfo(final LoginInfo info){
		setProperties(new Properties(){{
			setProperty("logininfo.islogin", String.valueOf(info.isLogin()));
			setProperty("logininfo.rememberme", String.valueOf(info.isRememberme()));
			setProperty("logininfo.userid", String.valueOf(info.getUid()));
			setProperty("logininfo.username", info.getUsername());
			setProperty("logininfo.password",  CyptoUtils.encode("SocialLocation",info.getPassword())) ;
			//setProperty("logininfo.password",  info.getPassword()) ;
		}});		
	}
	public LoginInfo getLoginInfo(){
		LoginInfo loginInfo = new LoginInfo() ;
		loginInfo.setLogin(StringUtils.toBool(getProperty("logininfo.islogin"))) ;
		loginInfo.setRememberme(StringUtils.toBool(getProperty("logininfo.rememberme"))) ;
		loginInfo.setUid(StringUtils.toInt(getProperty("logininfo.userid"))) ;
		loginInfo.setUsername(getProperty("logininfo.username")) ;
		loginInfo.setPassword(CyptoUtils.decode("SocialLocation", getProperty("logininfo.password"))) ;
		//loginInfo.setPassword(getProperty("logininfo.password")) ;
		return loginInfo ;
	}
	/**
	 * 
	 * @return
	 */
	public User getUserInfo() {		
		User lu = new User();		
		lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
		lu.setName(getProperty("user.name"));
		lu.setFace(getProperty("user.face"));
		lu.setAccount(getProperty("user.account"));
		lu.setPwd(CyptoUtils.decode("oschinaApp",getProperty("user.pwd")));
		lu.setLocation(getProperty("user.location"));
		lu.setFollowers(StringUtils.toInt(getProperty("user.followers"), 0));
		lu.setFans(StringUtils.toInt(getProperty("user.fans"), 0));
		lu.setScore(StringUtils.toInt(getProperty("user.score"), 0));
		lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
		return lu;
	}
	
	/**
	 * ä¿�å­˜ç”¨æˆ·å¤´åƒ�
	 * @param fileName
	 * @param bitmap
	 */
	public void saveUserFace(String fileName,Bitmap bitmap) {
		try {
			ImageUtils.saveImage(this, fileName, bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * èŽ·å�–ç”¨æˆ·å¤´åƒ�
	 * @param key
	 * @return
	 * @throws AppException
	 */
	public Bitmap getUserFace(String key) throws AppException {
		FileInputStream fis = null;
		try{
			fis = openFileInput(key);
			return BitmapFactory.decodeStream(fis);
		}catch(Exception e){
			throw AppException.run(e);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * æ˜¯å�¦åŠ è½½æ˜¾ç¤ºæ–‡ç« å›¾ç‰‡
	 * @return
	 */
	public boolean isLoadImage()
	{
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		//é»˜è®¤æ˜¯åŠ è½½çš„
		if(StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}
	
	/**
	 * è®¾ç½®æ˜¯å�¦åŠ è½½æ–‡ç« å›¾ç‰‡
	 * @param b
	 */
	public void setConfigLoadimage(boolean b)
	{
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}
	
	/**
	 * æ˜¯å�¦å�‘å‡ºæ��ç¤ºéŸ?
	 * @return
	 */
	public boolean isVoice()
	{
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		//é»˜è®¤æ˜¯å¼€å�¯æ��ç¤ºå£°éŸ?
		if(StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}
	
	/**
	 * è®¾ç½®æ˜¯å�¦å�‘å‡ºæ��ç¤ºéŸ?
	 * @param b
	 */
	public void setConfigVoice(boolean b)
	{
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}
	
	/**
	 * æ˜¯å�¦å�¯åŠ¨æ£?Ÿ¥æ›´æ–°
	 * @return
	 */
	public boolean isCheckUp()
	{
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		//é»˜è®¤æ˜¯å¼€å�?
		if(StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}
	
	/**
	 * è®¾ç½®å�¯åŠ¨æ£?Ÿ¥æ›´æ–°
	 * @param b
	 */
	public void setConfigCheckUp(boolean b)
	{
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}
	
	/**
	 * æ˜¯å�¦å·¦å�³æ»‘åŠ¨
	 * @return
	 */
	public boolean isScroll()
	{
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		//é»˜è®¤æ˜¯å…³é—­å·¦å�³æ»‘åŠ?
		if(StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}
	
	/**
	 * è®¾ç½®æ˜¯å�¦å·¦å�³æ»‘åŠ¨
	 * @param b
	 */
	public void setConfigScroll(boolean b)
	{
		setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
	}
	
	/**
	 * æ˜¯å�¦Httpsç™»å½•
	 * @return
	 */
	public boolean isHttpsLogin()
	{
		String perf_httpslogin = getProperty(AppConfig.CONF_HTTPS_LOGIN);
		//é»˜è®¤æ˜¯http
		if(StringUtils.isEmpty(perf_httpslogin))
			return false;
		else
			return StringUtils.toBool(perf_httpslogin);
	}
	
	/**
	 * è®¾ç½®æ˜¯æ˜¯å�¦Httpsç™»å½•
	 * @param b
	 */
	public void setConfigHttpsLogin(boolean b)
	{
		setProperty(AppConfig.CONF_HTTPS_LOGIN, String.valueOf(b));
	}
	
	/**
	 * æ¸…é™¤ä¿�å­˜çš„ç¼“å­?
	 */
	public void cleanCookie()
	{
		removeProperty(AppConfig.CONF_COOKIE);
	}
	
	/**
	 * åˆ¤æ–­ç¼“å­˜æ•°æ�®æ˜¯å�¦å�¯è¯»
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile)
	{
		return readObject(cachefile) != null;
	}
	
	/**
	 * åˆ¤æ–­ç¼“å­˜æ˜¯å�¦å­˜åœ¨
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile)
	{
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists())
			exist = true;
		return exist;
	}
	
	/**
	 * åˆ¤æ–­ç¼“å­˜æ˜¯å�¦å¤±æ•ˆ
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile)
	{
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if(data.exists() && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if(!data.exists())
			failure = true;
		return failure;
	}
	
	/**
	 * æ¸…é™¤appç¼“å­˜
	 */
	public void clearAppCache()
	{
		//æ¸…é™¤webviewç¼“å­˜
		File file = CacheManager.getCacheFileBaseDir();  
		if (file != null && file.exists() && file.isDirectory()) {  
		    for (File item : file.listFiles()) {  
		    	item.delete();  
		    }  
		    file.delete();  
		}  		  
		deleteDatabase("webview.db");  
		deleteDatabase("webview.db-shm");  
		deleteDatabase("webview.db-wal");  
		deleteDatabase("webviewCache.db");  
		deleteDatabase("webviewCache.db-shm");  
		deleteDatabase("webviewCache.db-wal");  
		//æ¸…é™¤æ•°æ�®ç¼“å­˜
		clearCacheFolder(getFilesDir(),System.currentTimeMillis());
		clearCacheFolder(getCacheDir(),System.currentTimeMillis());
		//2.2ç‰ˆæœ¬æ‰�æœ‰å°†åº”ç”¨ç¼“å­˜è½¬ç§»åˆ°sdå�¡çš„åŠŸèƒ½
//		if(isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)){
//			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),System.currentTimeMillis());
//		}
		//æ¸…é™¤ç¼–è¾‘å™¨ä¿�å­˜çš„ä¸´æ—¶å†…å®¹
		Properties props = getProperties();
		for(Object key : props.keySet()) {
			String _key = key.toString();
			if(_key.startsWith("temp"))
				removeProperty(_key);
		}
	}	
	
	/**
	 * æ¸…é™¤ç¼“å­˜ç›®å½•
	 * @param dir ç›®å½•
	 * @param numDays å½“å‰�ç³»ç»Ÿæ—¶é—´
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {          
	    int deletedFiles = 0;         
	    if (dir!= null && dir.isDirectory()) {             
	        try {                
	            for (File child:dir.listFiles()) {    
	                if (child.isDirectory()) {              
	                    deletedFiles += clearCacheFolder(child, curTime);          
	                }  
	                if (child.lastModified() < curTime) {     
	                    if (child.delete()) {                   
	                        deletedFiles++;           
	                    }    
	                }    
	            }             
	        } catch(Exception e) {       
	            e.printStackTrace();    
	        }     
	    }       
	    return deletedFiles;     
	}
	
	/**
	 * å°†å¯¹è±¡ä¿�å­˜åˆ°å†…å­˜ç¼“å­˜ä¸?
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}
	
	/**
	 * ä»Žå†…å­˜ç¼“å­˜ä¸­èŽ·å�–å¯¹è±¡
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key){
		return memCacheRegion.get(key);
	}
	
	/**
	 * ä¿�å­˜ç£�ç›˜ç¼“å­˜
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try{
			fos = openFileOutput("cache_"+key+".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		}finally{
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * èŽ·å�–ç£�ç›˜ç¼“å­˜æ•°æ�®
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try{
			fis = openFileInput("cache_"+key+".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		}finally{
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * ä¿�å­˜å¯¹è±¡
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try {
				oos.close();
			} catch (Exception e) {}
			try {
				fos.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * è¯»å�–å¯¹è±¡
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file){
		if(!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try{
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable)ois.readObject();
		}catch(FileNotFoundException e){
		}catch(Exception e){
			e.printStackTrace();
			//å��åº�åˆ—åŒ–å¤±è´¥ - åˆ é™¤ç¼“å­˜æ–‡ä»¶
			if(e instanceof InvalidClassException){
				File data = getFileStreamPath(file);
				data.delete();
			}
		}finally{
			try {
				ois.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
		return null;
	}

	public boolean containsProperty(String key){
		Properties props = getProperties();
		 return props.containsKey(key);
	}
	
	public void setProperties(Properties ps){
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties(){
		return AppConfig.getAppConfig(this).get();
	}
	
	public void setProperty(String key,String value){
		AppConfig.getAppConfig(this).set(key, value);
	}
	
	public String getProperty(String key){
		return AppConfig.getAppConfig(this).get(key);
	}
	public void removeProperty(String...key){
		AppConfig.getAppConfig(this).remove(key);
	}

	/**
	 * èŽ·å�–å†…å­˜ä¸­ä¿�å­˜å›¾ç‰‡çš„è·¯å¾„
	 * @return
	 */
	public String getSaveImagePath() {
		return saveImagePath;
	}
	/**
	 * è®¾ç½®å†…å­˜ä¸­ä¿�å­˜å›¾ç‰‡çš„è·¯å¾„
	 * @return
	 */
	public void setSaveImagePath(String saveImagePath) {
		this.saveImagePath = saveImagePath;
	}	
	
}
