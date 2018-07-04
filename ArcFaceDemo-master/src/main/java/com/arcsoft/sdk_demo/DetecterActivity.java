package com.arcsoft.sdk_demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.ageestimation.ASAE_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.arcsoft.genderestimation.ASGE_FSDKVersion;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView.OnCameraListener;
import com.moons.wangxc.CheckInInfo;
import com.moons.wangxc.util.FuncUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class DetecterActivity extends Activity implements OnCameraListener, View.OnTouchListener, Camera.AutoFocusCallback {
    private final String TAG = this.getClass().getSimpleName();

    private int mWidth, mHeight, mFormat; //图片宽高和格式
    private CameraSurfaceView mSurfaceView;
    private CameraGLSurfaceView mGLSurfaceView;
    private Camera mCamera;
    AFT_FSDKVersion version = new AFT_FSDKVersion(); //人脸跟踪库版本
    AFT_FSDKEngine engine = new AFT_FSDKEngine(); //人脸跟踪引擎
    ASAE_FSDKVersion mAgeVersion = new ASAE_FSDKVersion(); //年龄预测库版本
    ASAE_FSDKEngine mAgeEngine = new ASAE_FSDKEngine(); //年龄预测引擎
    ASGE_FSDKVersion mGenderVersion = new ASGE_FSDKVersion(); //性别预测库版本
    ASGE_FSDKEngine mGenderEngine = new ASGE_FSDKEngine(); //性别预测引擎
    List<AFT_FSDKFace> result = new ArrayList<>(); //跟踪到的人脸（矩形框和角度信息）list
    List<ASAE_FSDKAge> ages = new ArrayList<>(); //预测到年龄信息list
    List<ASGE_FSDKGender> genders = new ArrayList<>(); //预测到的性别信息list

    int mCameraID; //摄像头ID
    int mCameraRotate; //相机旋转的角度
    boolean mCameraMirror;
    byte[] mImageNV21 = null; //这里的mImageNV21数据就是NV21格式
    FRAbsLoop mFRAbsLoop = null; //虹软写好的人脸识别线程
    AFT_FSDKFace mAFT_FSDKFace = null; //人脸(矩形框和角度信息)
    Handler mHandler;

    Runnable hide = new Runnable() {
        @Override
        public void run() {
            mTextView.setAlpha(0.2f);
            mImageView.setImageAlpha(28);
        }
    };

    private ConcurrentHashMap<String, Long> latelyRecord = new ConcurrentHashMap<String, Long>();
//    private Timer mTimer = new Timer();
//    private TimerTask task = new TimerTask() {
//        public void run() {
//            if (latelyRecord != null) {
//                latelyRecord = new HashMap<String, Long>();
//            }
//            latelyRecord.clear();
//        }
//    };


    private Handler mCheckInHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast toast = null;
                    try {
                        toast = Toast.makeText(DetecterActivity.this, msg.getData().getString("name") + "签到成功" + "时间:" +
                                FuncUtil.longToString(System.currentTimeMillis(), "yyyy-MM-dd HH:mm"), Toast.LENGTH_SHORT);
                        saveLocalRecord(msg.getData().getString("name"), System.currentTimeMillis());
                        TodoVerifyRecordDBIssues(msg.getData().getString("name"), System.currentTimeMillis());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                case 1:
//                    Toast.makeText(DetecterActivity.this, msg.getData().getString("name") + "签到失败", Toast.LENGTH_SHORT).show();
                    Toast.makeText(DetecterActivity.this, "签到失败,请录入或更新人脸", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(DetecterActivity.this, "已签到", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    private void saveLocalRecord(String name, long time) {
        if (latelyRecord.size() > 100) {
            latelyRecord.clear();//超过100条记录就清空,一分钟不超过100个人打卡
        }
        latelyRecord.put(name, time); //保存签到记录
    }


    private void TodoVerifyRecordDBIssues(String name, long verifyDateTime) {
        CheckInInfo checkInInfo = new CheckInInfo();
        checkInInfo.setVerifyResult("sucess");
        checkInInfo.setName(name);
        checkInInfo.setVerifyDateTime(verifyDateTime);
        try {
            checkInInfo.setStrDateTime(FuncUtil.longToString(verifyDateTime, "yyyy-MM-dd HH:mm"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        checkInInfo.setReportStatus(1);
        Application.getVerifyRecordDB().addCheckIn(checkInInfo);
    }


    class FRAbsLoop extends AbsLoop {

        AFR_FSDKVersion version = new AFR_FSDKVersion();//人脸识别SDK版本
        AFR_FSDKEngine engine = new AFR_FSDKEngine();//人脸识别引擎
        AFR_FSDKFace result = new AFR_FSDKFace(); //人脸信息(特征值和字节大小)
        List<FaceDB.FaceRegist> mResgist = ((Application) DetecterActivity.this.getApplicationContext()).mFaceDB.mRegister; //人脸库
        List<ASAE_FSDKFace> face1 = new ArrayList<>(); //人脸(矩形区域和角度)list,变量1
        List<ASGE_FSDKFace> face2 = new ArrayList<>(); //人脸(矩形区域和角度)list，变量2

        @Override
        public void setup() {
            //初始化人脸识别引擎
            AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());
            error = engine.AFR_FSDK_GetVersion(version);
            Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
        }

        @Override
        public void loop() {
            if (mImageNV21 != null) {
                long time = System.currentTimeMillis();
                //人脸信息特征提取数函数,result返回人脸特征信息
                //输入的data数据为NV21格式（如Camera里NV21格式的preview数据；
                // 人脸坐标一般使用人脸检测返回的Rect传入；
                // 人脸角度请按照人脸检测引擎返回的值传入。
                AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
                final long duration_feture = (System.currentTimeMillis() - time);
                Log.d(TAG, "AFR_FSDK_ExtractFRFeature cost :" + duration_feture + "ms");

                Log.d(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + ", error=" + error.getCode());
                AFR_FSDKMatching score = new AFR_FSDKMatching(); //score 用于存放人脸对比的相似度值(置信度)
                float max = 0.0f;
                String name = null;//用户存放匹配的姓名
                time = System.currentTimeMillis();
                //遍历人脸库
                for (FaceDB.FaceRegist fr : mResgist) {
                    //遍历特征值
                    for (AFR_FSDKFace face : fr.mFaceList) {
                        //人脸匹配函数，score返回相似度值，第一和第二个参数为传入的人脸特征信息
                        error = engine.AFR_FSDK_FacePairMatching(result, face, score);
                        Log.d(TAG, "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
                        if (max < score.getScore()) {
                            max = score.getScore(); //取相似度最大值
                            name = fr.mName; //获取匹配的姓名
                        }
                    }
                }
                final long duration_pair = (System.currentTimeMillis() - time);//人脸匹配耗时
                Log.d(TAG, "AFR_FSDK_FacePairMatching cost :" + duration_pair + "ms");

                //age & gender
                face1.clear();
                face2.clear();
                time = System.currentTimeMillis();
                face1.add(new ASAE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
                face2.add(new ASGE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
                ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_AgeEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face1, ages);
                ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_GenderEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face2, genders);
                Log.d(TAG, "ASAE_FSDK_AgeEstimation_Image:" + error1.getCode() + ",ASGE_FSDK_GenderEstimation_Image:" + error2.getCode());
                Log.d(TAG, "age:" + ages.get(0).getAge() + ",gender:" + genders.get(0).getGender());
                final String age = ages.get(0).getAge() == 0 ? "年龄未知" : ages.get(0).getAge() + "岁";
                final String gender = genders.get(0).getGender() == -1 ? "性别未知" : (genders.get(0).getGender() == 0 ? "男" : "女");
                final long duration_age = (System.currentTimeMillis() - time);
                Log.d(TAG, "age & gender cost :" + duration_age + "ms");

                //crop
                byte[] data = mImageNV21;
                time = System.currentTimeMillis();
                YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
                ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
                yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 80, ops);
                final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
                try {
                    ops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final long duration_bmp = (System.currentTimeMillis() - time);
                Log.d(TAG, "compressToJpeg cost :" + duration_bmp + "ms");

                if (max > 0.6f) {
                    //fr success.
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("name", name);
                    message.setData(bundle);
                    if ((latelyRecord.get(name) != null) &&
                            (0 < System.currentTimeMillis() - latelyRecord.get(name).longValue()) &&
                            (System.currentTimeMillis() - latelyRecord.get(name).longValue() < 60 * 1000)) {
                        message.what = 2;
                    } else {
                        message.what = 0;
                    }
                    mCheckInHandler.sendMessage(message);

                    final float max_score = max;
                    Log.d(TAG, "fit Score:" + max + ", NAME:" + name);
                    final String mNameShow = name;
                    mHandler.removeCallbacks(hide);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            mTextView.setAlpha(1.0f);
                            mTextView.setText(mNameShow + "," + gender + "," + age);
                            mTextView.setTextColor(Color.RED);
                            mTextView1.setVisibility(View.VISIBLE);
                            mTextView1.setText("置信度：" + (float) ((int) (max_score * 1000)) / 1000.0);
                            mTextView1.setTextColor(Color.RED);

                            mTextView_feture.setText("特征编码耗时：" + duration_feture + "ms");
                            mTextView_pair.setText("比对耗时：" + duration_pair + "ms");
                            mTextView_age.setText("性别-年龄分析：" + duration_age + "ms");
                            mTextView_bmp.setText("bmp转存耗时：" + duration_bmp + "ms");

                            mImageView.setRotation(mCameraRotate);
                            if (mCameraMirror) {
                                mImageView.setScaleY(-1);
                            }
                            mImageView.setImageAlpha(255);
                            mImageView.setImageBitmap(bmp);
                        }
                    });
                } else {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("name", name);
                    message.setData(bundle);
                    message.what = 1;
                    mCheckInHandler.sendMessage(message);
                    final String mNameShow = "未识别";
                    DetecterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setAlpha(1.0f);
                            mTextView1.setVisibility(View.VISIBLE);
                            mTextView1.setText(gender + "," + age);
                            mTextView1.setTextColor(Color.RED);
                            mTextView.setText(mNameShow);
                            mTextView.setTextColor(Color.RED);
                            mImageView.setImageAlpha(255);
                            mImageView.setRotation(mCameraRotate);
                            if (mCameraMirror) {
                                mImageView.setScaleY(-1);
                            }
                            mImageView.setImageBitmap(bmp);
                        }
                    });
                }
                mImageNV21 = null;
            }

        }

        @Override
        public void over() {
            AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
            Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
        }
    }

    private TextView mTextView;
    private TextView mTextView1;
    private TextView mTextView_feture, mTextView_pair, mTextView_age, mTextView_bmp;
    private ImageView mImageView;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        mCameraID = getIntent().getIntExtra("Camera", 0) == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCameraRotate = getIntent().getIntExtra("Camera", 0) == 0 ? 0 : 270;
        mCameraMirror = getIntent().getIntExtra("Camera", 0) == 0 ? false : true;
        mWidth = 1280;
        mHeight = 960;
        mFormat = ImageFormat.NV21; //图像数据格式、
        mHandler = new Handler();

        setContentView(R.layout.activity_camera);
        mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
        mGLSurfaceView.setOnTouchListener(this);
        mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.setOnCameraListener(this);
        mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
        mSurfaceView.debug_print_fps(true, true);

        //snap
        mTextView = (TextView) findViewById(R.id.textView);
        mTextView.setText("");
        mTextView1 = (TextView) findViewById(R.id.textView1);
        mTextView1.setText("");

        mTextView_feture = (TextView) findViewById(R.id.tv_feture);
        mTextView_feture.setText("特征编码耗时：");
        mTextView_pair = (TextView) findViewById(R.id.tv_pair);
        mTextView_pair.setText("比对耗时：");
        mTextView_age = (TextView) findViewById(R.id.tv_age);
        mTextView_age.setText("性别-年龄分析：");
        mTextView_bmp = (TextView) findViewById(R.id.tv_bmp);
        mTextView_bmp.setText("bmp转存耗时：");

        mImageView = (ImageView) findViewById(R.id.imageView);
        //初始化人脸跟踪引擎
        AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
        err = engine.AFT_FSDK_GetVersion(version);
        Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());
        //初始化年龄预测引擎
        ASAE_FSDKError error = mAgeEngine.ASAE_FSDK_InitAgeEngine(FaceDB.appid, FaceDB.age_key);
        Log.d(TAG, "ASAE_FSDK_InitAgeEngine =" + error.getCode());
        error = mAgeEngine.ASAE_FSDK_GetVersion(mAgeVersion);
        Log.d(TAG, "ASAE_FSDK_GetVersion:" + mAgeVersion.toString() + "," + error.getCode());
        //初始化性别预测引擎
        ASGE_FSDKError error1 = mGenderEngine.ASGE_FSDK_InitgGenderEngine(FaceDB.appid, FaceDB.gender_key);
        Log.d(TAG, "ASGE_FSDK_InitgGenderEngine =" + error1.getCode());
        error1 = mGenderEngine.ASGE_FSDK_GetVersion(mGenderVersion);
        Log.d(TAG, "ASGE_FSDK_GetVersion:" + mGenderVersion.toString() + "," + error1.getCode());

        mFRAbsLoop = new FRAbsLoop();
        mFRAbsLoop.start(); //开启人脸检测的线程
//        mTimer.schedule(task, 0, 6000);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mFRAbsLoop.shutdown(); //关闭人脸识别线程
        AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
        Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

        ASAE_FSDKError err1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
        Log.d(TAG, "ASAE_FSDK_UninitAgeEngine =" + err1.getCode());

        ASGE_FSDKError err2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
        Log.d(TAG, "ASGE_FSDK_UninitGenderEngine =" + err2.getCode());
//        mTimer.cancel();
    }

    @Override
    public Camera setupCamera() {
        // TODO Auto-generated method stub
        mCamera = Camera.open(mCameraID);//打开相机
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mWidth, mHeight);
            parameters.setPreviewFormat(mFormat);

            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
            }
            for (Integer format : parameters.getSupportedPreviewFormats()) {
                Log.d(TAG, "FORMAT:" + format);
            }

            List<int[]> fps = parameters.getSupportedPreviewFpsRange();
            for (int[] count : fps) {
                Log.d(TAG, "T:");
                for (int data : count) {
                    Log.d(TAG, "V=" + data);
                }
            }
            //parameters.setPreviewFpsRange(15000, 30000);
            //parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
            //parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            //parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
            //parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            //parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            mWidth = mCamera.getParameters().getPreviewSize().width;
            mHeight = mCamera.getParameters().getPreviewSize().height;
        }
        return mCamera;
    }

    @Override
    public void setupChanged(int format, int width, int height) {

    }

    @Override
    public boolean startPreviewLater() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
        long time = System.currentTimeMillis();
        //人脸跟踪函数,result返回跟踪到人脸数组(矩形区域和角度信息)
        AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
        Log.d(TAG, "AFT_FSDK_FaceFeatureDetect cost :" + (System.currentTimeMillis() - time) + "ms");


        Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
        Log.d(TAG, "Face=" + result.size());
        for (AFT_FSDKFace face : result) {
            Log.d(TAG, "Face:" + face.toString());
        }
        if (mImageNV21 == null) {
            if (!result.isEmpty()) {
                mAFT_FSDKFace = result.get(0).clone(); //只读取一张人脸
                mImageNV21 = data.clone();
            } else {
                mHandler.postDelayed(hide, 3000);
            }
        }
        //copy rects
        Rect[] rects = new Rect[result.size()];
        for (int i = 0; i < result.size(); i++) {
            rects[i] = new Rect(result.get(i).getRect());
        }
        //clear result.
        result.clear();
        //return the rects for render.
        return rects;
    }

    @Override
    public void onBeforeRender(CameraFrameData data) {

    }

    @Override
    public void onAfterRender(CameraFrameData data) {
        mGLSurfaceView.getGLES2Render().draw_rect((Rect[]) data.getParams(), Color.GREEN, 2);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        CameraHelper.touchFocus(mCamera, event, v, this);
        return false;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            Log.d(TAG, "Camera Focus SUCCESS!");
        }
    }
}
