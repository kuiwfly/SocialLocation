package com.sociallocation.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.sociallocation.R;
import com.sociallocation.app.AppContext;
import com.sociallocation.app.AppException;
import com.sociallocation.bean.Result;
import com.sociallocation.util.FileUtils;
import com.sociallocation.util.ImageUtils;
import com.sociallocation.util.StringUtils;
import com.sociallocation.util.UIHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

public class UserInfoDialog extends BaseActivity {
	private final static int CROP = 200;
	
	private ImageView back;
	private ImageView refresh;
	private ImageView face;
	private ImageView gender;
	private LoadingDialog loading;
	
	private boolean bEditMode = false ;
	private final static String FILE_SAVEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SocialLocation/Portrait/";
	private Uri origUri;
	private Uri cropUri;
	private File protraitFile;
	private Bitmap protraitBitmap;
	private String protraitPath;
	
	
	private void initUI(){
		back = (ImageView)findViewById(R.id.user_info_back);
		refresh = (ImageView)findViewById(R.id.user_info_refresh);
		face = (ImageView)findViewById(R.id.user_info_userface);
		gender = (ImageView)findViewById(R.id.user_info_gender);
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info);		

	}
	public void onButtonClick(View view){
		switch (view.getId()) {
		case R.id.user_info_editer:
			bEditMode = true ;
			break;
		case R.id.user_info_userface:
			CharSequence[] items = {
					getString(R.string.img_from_album),
					getString(R.string.img_from_camera)
			};
			imageChooseItem(items);
			break ;
		default:
			break;
		}
	}
	public void imageChooseItem(CharSequence[] items )
	{
		AlertDialog imageDialog = new AlertDialog.Builder(this).setTitle("ѡ��ͷ��").setIcon(android.R.drawable.btn_star).setItems(items,
			new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int item)
				{	
					//判断是否挂载了SD�?
					String storageState = Environment.getExternalStorageState();		
					if(storageState.equals(Environment.MEDIA_MOUNTED)){
						File savedir = new File(FILE_SAVEPATH);
						if (!savedir.exists()) {
							savedir.mkdirs();
						}
					}					
					else{
						UIHelper.ToastMessage(UserInfoDialog.this, "无法保存上传的头像，请�s柤�D卡是否挂�?");
						return;
					}

					//输出裁剪的临时文�?
					String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
					//照片命名
					String origFileName = "osc_" + timeStamp + ".jpg";
					String cropFileName = "osc_crop_" + timeStamp + ".jpg";
					
					//裁剪头像的绝对路�?
					protraitPath = FILE_SAVEPATH + cropFileName;
					protraitFile = new File(protraitPath);
					
					origUri = Uri.fromFile(new File(FILE_SAVEPATH, origFileName));
					cropUri = Uri.fromFile(protraitFile);
					
					//相册选图
					if(item == 0) {
						startActionPickCrop(cropUri);
					}
					//手机拍照
					else if(item == 1){
						startActionCamera(origUri);
					}
				}}).create();
		
		 imageDialog.show();
	}
	private void startActionPickCrop(Uri output) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		intent.putExtra("output", output);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比�?
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		startActivityForResult(Intent.createChooser(intent, "选择图片"),ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
	}
	private void startActionCamera(Uri output) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
		startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
	}
	private void startActionCrop(Uri data, Uri output) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(data, "image/*");
		intent.putExtra("output", output);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);// 裁剪框比�?
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", CROP);// 输出图片大小
		intent.putExtra("outputY", CROP);
		startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
	}
	private void uploadNewPhoto() {
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				if(loading != null)	loading.dismiss();
				if(msg.what == 1 && msg.obj != null){
					Result res = (Result)msg.obj;
					//提示信息
					UIHelper.ToastMessage(UserInfoDialog.this, res.getErrorMessage());
					if(res.OK()){
						//显示新头�?
						face.setImageBitmap(protraitBitmap);
					}
				}else if(msg.what == -1 && msg.obj != null){
					((AppException)msg.obj).makeToast(UserInfoDialog.this);
				}
			}
		};
			
		if(loading != null){
			loading.setLoadText("正在上传头像···");
			loading.show();	
		}
		
		new Thread(){
			public void run() 
			{
	        	//获取头像缩略�?
	        	if(!StringUtils.isEmpty(protraitPath) && protraitFile.exists())
	        	{
	        		protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath, 200, 200);
	        	}
		        
				if(protraitBitmap != null)
				{	
					Message msg = new Message();
					try {
						Result res = ((AppContext)getApplication()).updatePortrait(protraitFile);
						if(res!=null && res.OK()){
							//保存新头像到缓存
							//String filename = FileUtils.getFileName(user.getFace());
							String filename = FileUtils.getFileName("portrait");
							ImageUtils.saveImage(UserInfoDialog.this, filename, protraitBitmap);
						}
						msg.what = 1;
						msg.obj = res;
					} catch (AppException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					} catch(IOException e) {
						e.printStackTrace();
					}
					handler.sendMessage(msg);
				}				
			};
		}.start();
    }
	@Override 
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{ 
    	if(resultCode != RESULT_OK) return;
		
    	switch(requestCode){
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
    			startActionCrop(origUri, cropUri);//拍照后�t�?
    			break;
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
    		case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
    			uploadNewPhoto();//上传新照�?
    			break;
    	}
	}
}
