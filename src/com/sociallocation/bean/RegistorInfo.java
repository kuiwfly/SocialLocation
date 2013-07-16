package com.sociallocation.bean;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.sociallocation.app.AppException;

public class RegistorInfo extends Base {
	public static final String TAG = "RegistorInfo" ;
	private int uid;
	private boolean isLogin ;	
	private Result result ;	
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
	public Result getResult() {
		return result;
	}
	public void setResult(Result result) {
		this.result = result;
	}
	public static RegistorInfo parse(String jsonstr) throws IOException, AppException {
		RegistorInfo registorInfo = new RegistorInfo() ;
		Result res = new Result() ;
		try {
			JSONObject jsonObject = new JSONObject(jsonstr);
			res.setErrorCode(jsonObject.getInt("code")) ;
			if(res.getErrorCode() == Result.RET_SUCC){
				registorInfo.setUid(jsonObject.getInt("userid")) ;
				registorInfo.setLogin(true) ;
				Log.e(TAG, "Login Successfully!" ) ;
			}else{
				registorInfo.setLogin(false) ;
				res.setErrorMessage(jsonObject.getString("errorCode")) ;
				Log.e(TAG, "Login Failed! Errorcode:"+res.getErrorMessage() ) ; ;
			}
			registorInfo.setResult(res) ;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString()) ;
			throw AppException.json(e) ;
		} 		
		return registorInfo ;
	}
}
