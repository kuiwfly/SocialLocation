package com.sociallocation.activity;



import com.example.sociallocation.R;
import com.sociallocation.api.ApiClient;
import com.sociallocation.app.AppContext;
import com.sociallocation.app.AppException;
import com.sociallocation.bean.LoginInfo;
import com.sociallocation.bean.Result;
import com.sociallocation.bean.User;
import com.sociallocation.util.StringUtils;
import com.sociallocation.util.UIHelper;

import android.R.integer;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

/**
 * 用户登录对话�?
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class RegistorDialog extends BaseActivity{
	
	private ViewSwitcher mViewSwitcher;
	private ImageButton btn_close;
	private Button btn_login;
	private AutoCompleteTextView mAccount;
	private EditText mPwd;
	private EditText mPwdAgain ;
	private AnimationDrawable loadingAnimation;
	private View loginLoading;
	private CheckBox chb_rememberMe;
	private int curLoginType;
	private InputMethodManager imm;
	
	public final static int LOGIN_OTHER = 0x00;
	public final static int LOGIN_MAIN = 0x01;
	public final static int LOGIN_SETTING = 0x02;
	
	public final static int MSG_LOGIN_SUCC = 1 ;
	public final static int MSG_LOGIN_FAIL = 0 ;
	public final static int MSG_APP_EXCEPTION = -1 ;	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registor_dialog);
        
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        
        curLoginType = getIntent().getIntExtra("LOGINTYPE", LOGIN_OTHER);
        
        mViewSwitcher = (ViewSwitcher)findViewById(R.id.registordialog_view_switcher);       
        loginLoading = (View)findViewById(R.id.registor_loading);
        mAccount = (AutoCompleteTextView)findViewById(R.id.registor_account);
        mPwd = (EditText)findViewById(R.id.registor_password);
        mPwdAgain = (EditText)findViewById(R.id.registor_password_again) ;
        mAccount.setOnKeyListener(onKey) ;
        mPwd.setOnKeyListener(onKey) ;
        mPwdAgain.setOnKeyListener(onKey) ;
        chb_rememberMe = (CheckBox)findViewById(R.id.registor_checkbox_rememberMe);
        
//        btn_close = (ImageButton)findViewById(R.id.registor_close_button);
//        btn_close.setOnClickListener(UIHelper.finish(this));        
        
        btn_login = (Button)findViewById(R.id.registor_btn_registor);
        btn_login.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//隐藏软键�?
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);  
				
				String account = mAccount.getText().toString();
				String pwd = mPwd.getText().toString();
				String pwdAgain = mPwdAgain.getText().toString() ;
				boolean isRememberMe = chb_rememberMe.isChecked();
				
				if(StringUtils.isEmpty(account)){
					UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_email_null));
					return;
				}
				if(StringUtils.isEmpty(pwd)){
					UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
					return;
				}
				if(StringUtils.isEmpty(pwdAgain)){
					UIHelper.ToastMessage(v.getContext(), getString(R.string.msg_login_pwd_null));
					return;
				}
		        if(!pwd.equals(pwdAgain)){
					UIHelper.ToastMessage(v.getContext(), "�����������벻��ͬ��");
					return;
		        }
		        loadingAnimation = (AnimationDrawable)loginLoading.getBackground();
		        loadingAnimation.start();
		        mViewSwitcher.showNext();
		        
		        login(account, pwd, 2, true);
			}
		});
   
//        AppContext ac = (AppContext)getApplication();
//        User user = ac.getLoginInfo();
//        if(user==null || !user.isRememberMe()) return;
//        if(!StringUtils.isEmpty(user.getAccount())){
//        	mAccount.setText(user.getAccount());
//        	mAccount.selectAll();
//        	chb_rememberMe.setChecked(user.isRememberMe());
//        }
//        if(!StringUtils.isEmpty(user.getPwd())){
//        	mPwd.setText(user.getPwd());
//        }
    }
    OnKeyListener onKey=new OnKeyListener() {  		  
		@Override  		  
		public boolean onKey(View v, int keyCode, KeyEvent event) {  		  
			// TODO Auto-generated method stub  			
			if(keyCode == KeyEvent.KEYCODE_ENTER){  
				switch(v.getId()){
				case R.id.registor_account:
					mPwd.requestFocus() ;
					break ;
				case R.id.registor_password:
					mPwdAgain.requestFocus() ;
					break ;
				case R.id.registor_password_again:
	
					break ;
				default:
					break ;
				}
				return true;  			  
			}  			  
			return false;  
		}  
    	  
    };       
    //登录验证
    private void login(final String account, final String pwd, final int type,final boolean isRememberMe) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if(msg.what == MSG_LOGIN_SUCC){
					LoginInfo loginInfo = (LoginInfo)msg.obj;
					if(loginInfo != null){
						ApiClient.cleanCookie();
						UIHelper.sendBroadCast(RegistorDialog.this, loginInfo.getNotice());
						UIHelper.ToastMessage(RegistorDialog.this, R.string.msg_login_success);
//						if(curLoginType == LOGIN_MAIN){
							//跳转--加载用户动�?
							Intent intent = new Intent(RegistorDialog.this, MainActivity.class);
							intent.putExtra("LOGIN", true);
							startActivity(intent);
//						}else if(curLoginType == LOGIN_SETTING){
//							//跳转--用户设置页面
////							Intent intent = new Intent(LoginDialog.this, Setting.class);
////							intent.putExtra("LOGIN", true);
////							startActivity(intent);
//						}
						finish();
					}
				}else if(msg.what == MSG_LOGIN_FAIL){
					mViewSwitcher.showPrevious();
					UIHelper.ToastMessage(RegistorDialog.this, getString(R.string.msg_login_fail)+msg.obj);
				}else if(msg.what == MSG_APP_EXCEPTION){
					mViewSwitcher.showPrevious();
					((AppException)msg.obj).makeToast(RegistorDialog.this);
				}
			}
		};
		new Thread(){
			public void run() {
				Message msg =new Message();
				try {
					AppContext ac = (AppContext)getApplication(); 
					LoginInfo loginInfo = ac.loginVerify(account,pwd,type) ;
	                Result res = loginInfo.getResult();
	                if(res.OK()){
	                	msg.what = MSG_LOGIN_SUCC;
	                	msg.obj = loginInfo;
	                }else{
	                	msg.what = MSG_LOGIN_FAIL;
	                	msg.obj = res.getErrorMessage();
	                }
	            } catch (AppException e) {
	            	e.printStackTrace();
			    	msg.what = MSG_APP_EXCEPTION;
			    	msg.obj = e;
	            }
				handler.sendMessage(msg);
			}
		}.start();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
    		this.onDestroy();
    	}
    	return super.onKeyDown(keyCode, event);
    }
}
