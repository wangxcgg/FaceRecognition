package com.arcsoft.sdk_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.widget.HListView;
import com.moons.wangxc.UserFaceInfo;
import com.moons.wangxc.util.FuncUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/4/27.
 */

public class RegisterActivity extends Activity implements SurfaceHolder.Callback {
	private final String TAG = this.getClass().getSimpleName().toString();
	private final static int MSG_CODE = 0x1000;
	private final static int MSG_EVENT_REG = 0x1001;
	private final static int MSG_EVENT_NO_FACE = 0x1002;
	private final static int MSG_EVENT_NO_FEATURE = 0x1003;
	private final static int MSG_EVENT_FD_ERROR = 0x1004;
	private final static int MSG_EVENT_FR_ERROR = 0x1005;
	private UIHandler mUIHandler; //处理注册信息handler
	// Intent data.
	private String 		mFilePath; //图片路径信息

	private SurfaceView mSurfaceView; //SurfaceView就是指一个在表层的View对象,view可以显式在SurfaceView之上
	private SurfaceHolder mSurfaceHolder; //SurfaceHolder保存了一个Surface对象的引用，用它来处理Surface的生命周期
	private Bitmap mBitmap; //图片
	private Rect src = new Rect(); //原矩形图
	private Rect dst = new Rect(); //目标矩形图
	private Thread view;
	private EditText mEditText;
	private ExtImageView mExtImageView;
	private HListView mHListView; //一个横向显示的listview
	private RegisterViewAdapter mRegisterViewAdapter; //注册显示适配器
	private AFR_FSDKFace mAFR_FSDKFace; //人脸信息

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_register);
		//initial data.获取图片路径信息
		if (!getIntentData(getIntent().getExtras())) {
			Log.e(TAG, "getIntentData fail!");
			this.finish() ;
		}

		mRegisterViewAdapter = new RegisterViewAdapter(this);
		mHListView = (HListView)findViewById(R.id.hlistView);
		mHListView.setAdapter(mRegisterViewAdapter);
		mHListView.setOnItemClickListener(mRegisterViewAdapter);

		mUIHandler = new UIHandler(); //处理注册信息handler
		mBitmap = Application.decodeImage(mFilePath); //解析图片路径下文件，生成Bitmap
		src.set(0,0,mBitmap.getWidth(),mBitmap.getHeight()); //左上右下
		mSurfaceView = (SurfaceView)this.findViewById(R.id.surfaceView); //sufaceview实例
		mSurfaceView.getHolder().addCallback(this);//注册SurfaceHolder的回调方法
		view = new Thread(new Runnable() {
			@Override
			public void run() {
				while (mSurfaceHolder == null) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight() * 3 / 2];
				ImageConverter convert = new ImageConverter(); //图片转换器
				convert.initial(mBitmap.getWidth(), mBitmap.getHeight(), ImageConverter.CP_PAF_NV21);//转为NV21
				if (convert.convert(mBitmap, data)) {
					Log.d(TAG, "convert ok!");//转化结果，data数据就是NV21
				}
				convert.destroy();

				AFD_FSDKEngine engine = new AFD_FSDKEngine(); //人脸检测引擎
				AFD_FSDKVersion version = new AFD_FSDKVersion(); //人脸检测库版本
				List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>(); //检测到的人脸(矩形面部区域和角度)list
				//初始化人脸检测引擎（appid,sdkkey...人脸数）
				AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
				Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + err.getCode());
				if (err.getCode() != AFD_FSDKError.MOK) {
					Message reg = Message.obtain();
					reg.what = MSG_CODE;
					reg.arg1 = MSG_EVENT_FD_ERROR;
					reg.arg2 = err.getCode();
					mUIHandler.sendMessage(reg);
				}
				err = engine.AFD_FSDK_GetVersion(version);//获取人脸检测库版本
				Log.d(TAG, "AFD_FSDK_GetVersion =" + version.toString() + ", " + err.getCode());
				//AFD_FSDK_StillImageFaceDetection方法实现人脸检测,result返回检测到的人脸（矩形面部区域和角度）
				err  = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
				Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());
				while (mSurfaceHolder != null) {
					Canvas canvas = mSurfaceHolder.lockCanvas();
					if (canvas != null) {
						Paint mPaint = new Paint();
						boolean fit_horizontal = canvas.getWidth() / (float)src.width() < canvas.getHeight() / (float)src.height() ? true : false;
						float scale = 1.0f;
						if (fit_horizontal) {
							scale = canvas.getWidth() / (float)src.width();
							dst.left = 0;
							dst.top = (canvas.getHeight() - (int)(src.height() * scale)) / 2;
							dst.right = dst.left + canvas.getWidth();
							dst.bottom = dst.top + (int)(src.height() * scale);
						} else {
							scale = canvas.getHeight() / (float)src.height();
							dst.left = (canvas.getWidth() - (int)(src.width() * scale)) / 2;
							dst.top = 0;
							dst.right = dst.left + (int)(src.width() * scale);
							dst.bottom = dst.top + canvas.getHeight();
						}
						canvas.drawBitmap(mBitmap, src, dst, mPaint);
						canvas.save();
						canvas.scale((float) dst.width() / (float) src.width(), (float) dst.height() / (float) src.height());
						canvas.translate(dst.left / scale, dst.top / scale);
						for (AFD_FSDKFace face : result) { //用红色笔画出所有人脸矩形区域
							mPaint.setColor(Color.RED);
							mPaint.setStrokeWidth(10.0f);
							mPaint.setStyle(Paint.Style.STROKE);
							canvas.drawRect(face.getRect(), mPaint);
						}
						canvas.restore();
						mSurfaceHolder.unlockCanvasAndPost(canvas);
						break;
					}
				}

				if (!result.isEmpty()) { //检测到的人脸（矩形区域和角度)非空
					AFR_FSDKVersion version1 = new AFR_FSDKVersion(); //人脸识别库版本
					AFR_FSDKEngine engine1 = new AFR_FSDKEngine(); //人脸识别引擎
					AFR_FSDKFace result1 = new AFR_FSDKFace(); //人脸信息（特征值）
					AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);//初始化人脸识别引擎
					Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error1.getCode());
					if (error1.getCode() != AFD_FSDKError.MOK) {
						Message reg = Message.obtain();
						reg.what = MSG_CODE;
						reg.arg1 = MSG_EVENT_FR_ERROR;
						reg.arg2 = error1.getCode();
						mUIHandler.sendMessage(reg);
					}
					error1 = engine1.AFR_FSDK_GetVersion(version1);//获取人脸识别库版本
					Log.d("com.arcsoft", "FR=" + version.toString() + "," + error1.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
					//人脸信息特征提取数函数,result1返回人脸特征信息
					//输入的data数据为NV21格式（如Camera里NV21格式的preview数据;)
					// 人脸坐标一般使用人脸检测返回的Rect传入；
					// 人脸角度请按照人脸检测引擎返回的值传入。
					//这里只提取了第一个人脸
					error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
					Log.d("com.arcsoft", "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
					if(error1.getCode() == error1.MOK) {
						mAFR_FSDKFace = result1.clone();
						int width = result.get(0).getRect().width();
						int height = result.get(0).getRect().height();
						Bitmap face_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
						Canvas face_canvas = new Canvas(face_bitmap);
						face_canvas.drawBitmap(mBitmap, result.get(0).getRect(), new Rect(0, 0, width, height), null);
						Message reg = Message.obtain();
						reg.what = MSG_CODE;
						reg.arg1 = MSG_EVENT_REG;
						reg.obj = face_bitmap;
						mUIHandler.sendMessage(reg);
					} else {
						Message reg = Message.obtain();
						reg.what = MSG_CODE;
						reg.arg1 = MSG_EVENT_NO_FEATURE;
						mUIHandler.sendMessage(reg);
					}
					error1 = engine1.AFR_FSDK_UninitialEngine();
					Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine : " + error1.getCode());
				} else {
					Message reg = Message.obtain();
					reg.what = MSG_CODE;
					reg.arg1 = MSG_EVENT_NO_FACE;
					mUIHandler.sendMessage(reg);
				}
				err = engine.AFD_FSDK_UninitialFaceEngine();
				Log.d(TAG, "AFD_FSDK_UninitialFaceEngine =" + err.getCode());
			}
		});
		view.start();

	}

	/**
	 * @note bundle data :
	 * String imagePath
	 *
	 * @param bundle
	 */
	private boolean getIntentData(Bundle bundle) {
		try {
			mFilePath = bundle.getString("imagePath"); //获取图片路径
			if (mFilePath == null || mFilePath.isEmpty()) {
				return false;
			}
			Log.i(TAG, "getIntentData:" + mFilePath);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	//SurfaceView的生命周期
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSurfaceHolder = holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceHolder = null;
		try {
			view.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class UIHandler extends android.os.Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MSG_CODE) {
				if (msg.arg1 == MSG_EVENT_REG) {
					LayoutInflater inflater = LayoutInflater.from(RegisterActivity.this);
					View layout = inflater.inflate(R.layout.dialog_register, null);
					mEditText = (EditText) layout.findViewById(R.id.editview);
					mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
					mExtImageView = (ExtImageView) layout.findViewById(R.id.extimageview);
					mExtImageView.setImageBitmap((Bitmap) msg.obj);
					final Bitmap face = (Bitmap) msg.obj;
					new AlertDialog.Builder(RegisterActivity.this)
							.setTitle("请输入注册名字")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setView(layout)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									((Application)RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(mEditText.getText().toString(), mAFR_FSDKFace);//添加人脸信息
									TodoFaceListDBIssues(mEditText.getText().toString(),mFilePath);
									mRegisterViewAdapter.notifyDataSetChanged(); //刷新横向listview内容
									dialog.dismiss();
								}
							})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							})
							.show();
				} else if(msg.arg1 == MSG_EVENT_NO_FEATURE ){
					Toast.makeText(RegisterActivity.this, "人脸特征无法检测，请换一张图片", Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_NO_FACE ){
					Toast.makeText(RegisterActivity.this, "没有检测到人脸，请换一张图片", Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_FD_ERROR ){
					Toast.makeText(RegisterActivity.this, "FD初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_FR_ERROR){
					Toast.makeText(RegisterActivity.this, "FR初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void TodoFaceListDBIssues(String username, String filePath) {
		UserFaceInfo userFaceInfo=new UserFaceInfo();
		userFaceInfo.setName(username);
		userFaceInfo.setCollectionDateTime(System.currentTimeMillis());
		userFaceInfo.setStatus(0);
        if(!Application.getFaceListDB().isNameExist(username)){
			Application.getFaceListDB().addMemberFace(userFaceInfo);
		}
		else{
			Application.getFaceListDB().updateMemberFaceByName_CollectionTimeAndStatus(userFaceInfo);
		}
		FuncUtil.copyFile(filePath,Application.getImagePath(),username+".jpg");
	}

	class Holder {
		ExtImageView siv;
		TextView tv;
	}

	class RegisterViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
		Context mContext;
		LayoutInflater mLInflater;

		public RegisterViewAdapter(Context c) {
			// TODO Auto-generated constructor stub
			mContext = c;
			mLInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder = null;
			if (convertView != null) {
				holder = (Holder) convertView.getTag();
			} else {
				convertView = mLInflater.inflate(R.layout.item_sample, null);
				holder = new Holder();
				holder.siv = (ExtImageView) convertView.findViewById(R.id.imageView1);
				holder.tv = (TextView) convertView.findViewById(R.id.textView1);
				convertView.setTag(holder);
			}

			if (!((Application)mContext.getApplicationContext()).mFaceDB.mRegister.isEmpty()) {
				FaceDB.FaceRegist face = ((Application) mContext.getApplicationContext()).mFaceDB.mRegister.get(position);
				holder.tv.setText(face.mName);
				//holder.siv.setImageResource(R.mipmap.ic_launcher);
				convertView.setWillNotDraw(false);
			}

			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d("onItemClick", "onItemClick = " + position + "pos=" + mHListView.getScroll());
			final String name = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.get(position).mName;
			final int count = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.get(position).mFaceList.size();
			new AlertDialog.Builder(RegisterActivity.this)
					.setTitle("删除注册名:" + name)
					.setMessage("包含:" + count + "个注册人脸特征信息")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((Application)mContext.getApplicationContext()).mFaceDB.delete(name);
							mRegisterViewAdapter.notifyDataSetChanged();
							dialog.dismiss();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
		}
	}
}
