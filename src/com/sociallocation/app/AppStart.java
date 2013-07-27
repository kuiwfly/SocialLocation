package com.sociallocation.app;


import com.example.sociallocation.R;
import com.sociallocation.activity.LoginDialog;
import com.sociallocation.activity.MainActivity;
import com.sociallocation.activity.RegistorDialog;
import com.sociallocation.bean.LoginInfo;
import com.sociallocation.bean.User;
import com.sociallocation.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * 
 * @version 1.0
 * @created 2012-3-21
 */
public class AppStart extends Activity {
	AppContext appContext = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View view = View.inflate(this, R.layout.start, null);
		setContentView(view);
        
		//渐变展示启动�?		
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
		
			
		appContext = (AppContext)getApplication();
		String cookie = appContext.getProperty("cookie");
		if(StringUtils.isEmpty(cookie)) {
			String cookie_name = appContext.getProperty("cookie_name");
			String cookie_value = appContext.getProperty("cookie_value");
			if(!StringUtils.isEmpty(cookie_name) && !StringUtils.isEmpty(cookie_value)) {
				cookie = cookie_name + "=" + cookie_value;
				appContext.setProperty("cookie", cookie);
				appContext.removeProperty("cookie_domain","cookie_name","cookie_value","cookie_version","cookie_path");
			}
		}
    }
    
    /**
     * 跳转�?..
     */
    private void redirectTo(){        
////        Intent intent = new Intent(this, MainActivity.class);
//    	Log.i("wk", "start") ;
//    	Intent intent = new Intent(this, LoginDialog.class);
    	LoginInfo loginInfo = appContext.getLoginInfo() ;
    	Intent intent = null ;
    	if(!loginInfo.isLogin()){
    		intent = new Intent(this, LoginDialog.class);
    	}else{
    		intent = new Intent(this, MainActivity.class);
    	}
        startActivity(intent);
        Log.i("wk", "stop") ;
        finish();
    }
}