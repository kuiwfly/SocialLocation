package com.sociallocation.bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sociallocation.app.AppException;

public class LoginInfo extends Base {
	public static final String TAG = "LoginInfo" ;
	
	public static final int LOGIN_DIRECT = 0 ;
	public static final int LOGIN_QQ = 1 ;
	public static final int LOGIN_SINA = 2 ;
	private int uid;
	private boolean isLogin ;	
	private Result result ;
	private boolean isRememberme ;
	public boolean isRememberme() {
		return isRememberme;
	}
	public void setRememberme(boolean isRememberme) {
		this.isRememberme = isRememberme;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	private String username ;
	private String password ;
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public boolean isLogin() {
		return isLogin;
	}
	public void setLogin(boolean isLogin) {
		this.isLogin = isLogin;
	}
	public static LoginInfo parse(String jsonstr) throws IOException, AppException {
		Log.e(TAG, "JSON String:"+jsonstr) ;
		LoginInfo loginInfo = new LoginInfo();
		Result res = new Result() ;
		try {
			JSONObject jsonObject = new JSONObject(jsonstr);
			res.setErrorCode(jsonObject.getInt("code")) ;
			if(res.getErrorCode() == Result.RET_SUCC){
				loginInfo.setUid(jsonObject.getInt("userid")) ;
				loginInfo.setLogin(true) ;
				Log.e(TAG, "Login Successfully!" ) ;
			}else{
				loginInfo.setLogin(false) ;
				res.setErrorMessage(jsonObject.getString("errorCode")) ;
				Log.e(TAG, "Login Failed! Errorcode:"+res.getErrorMessage() ) ; ;
			}
			loginInfo.setResult(res) ;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString()) ;
			throw AppException.json(e) ;
		} 
		return loginInfo ;
	}
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
}
