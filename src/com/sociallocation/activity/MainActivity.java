package com.sociallocation.activity;

import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickActionGrid;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

import com.example.sociallocation.R;
import com.sociallocation.app.AppContext;
import com.sociallocation.util.UIHelper;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;

public class MainActivity extends BaseActivity {
	public static final String TAG = "MainActivity" ;
	//public static final int QUICKACTION_LOGIN_OR_LOGOUT = 0;
	public static final int QUICKACTION_USERINFO = 0;
	public static final int QUICKACTION_SOFTWARE = 1;
	public static final int QUICKACTION_SEARCH = 2;
	public static final int QUICKACTION_SETTING = 3;
	public static final int QUICKACTION_USERMANAGER=4 ;
	public static final int QUICKACTION_EXIT = 5;
	
	private AppContext appContext;
	
	private QuickActionWidget mGrid;
	private RadioButton fbNews;
	private RadioButton fbQuestion;
	private RadioButton fbTweet;
	private RadioButton fbactive;
	private ImageView fbSetting;
	private void initUI(){
		if (!appContext.isNetworkConnected()){
			UIHelper.ToastMessage(this, R.string.network_not_connected);
		}
		initFootBar() ;
		initQuickActionGrid() ;
		
	}
	private void initFootBar() {
		fbNews = (RadioButton) findViewById(R.id.main_footbar_news);
		fbQuestion = (RadioButton) findViewById(R.id.main_footbar_question);
		fbTweet = (RadioButton) findViewById(R.id.main_footbar_tweet);
		fbactive = (RadioButton) findViewById(R.id.main_footbar_active);

		fbSetting = (ImageView) findViewById(R.id.main_footbar_setting);
		fbSetting.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mGrid.show(v);
			}
		});
	}
	private void initQuickActionGrid() {
		mGrid = new QuickActionGrid(this);

		mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_myinfo,
				R.string.main_menu_myinfo));
		mGrid.addQuickAction(new MyQuickAction(this,
				R.drawable.ic_menu_software, R.string.main_menu_software));
		mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_search,
				R.string.main_menu_search));
		mGrid.addQuickAction(new MyQuickAction(this,
				R.drawable.ic_menu_setting, R.string.main_menu_setting));
		mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_login,
				R.string.main_menu_usermanager));		
		mGrid.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_exit,
				R.string.main_menu_exit));

		mGrid.setOnQuickActionClickListener(mActionListener);
		
	}
	private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
		public void onQuickActionClicked(QuickActionWidget widget, int position) {
			switch (position) {
			case QUICKACTION_USERMANAGER:// 鐢ㄦ埛鐧诲綍-娉ㄩ攢
				//UIHelper.loginOrLogout(Main.this);
				UIHelper.Logout(MainActivity.this) ;
				break;
			case QUICKACTION_USERINFO:// 鎴戠殑璧勬枡
				//UIHelper.showUserInfo(Main.this);
				break;
			case QUICKACTION_SOFTWARE:// 寮€婧愯蒋浠?
				//UIHelper.showSoftware(Main.this);
				break;
			case QUICKACTION_SEARCH:// 鎼滅储
				//UIHelper.showSearch(Main.this);
				break;
			case QUICKACTION_SETTING:// 璁剧疆
//				UIHelper.showSetting(MainActivity.this);
				break;
			case QUICKACTION_EXIT:// 閫€鍑?
				UIHelper.Exit(MainActivity.this);
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		appContext = (AppContext) getApplication();

		initUI() ;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent= new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return super.onKeyDown(keyCode, event);
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {

			mGrid.show(fbSetting, true);
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {

		} else {
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;		
	}

}
