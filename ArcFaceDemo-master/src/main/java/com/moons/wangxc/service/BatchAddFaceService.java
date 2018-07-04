package com.moons.wangxc.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.FaceDB;
import com.guo.android_extend.image.ImageConverter;
import com.moons.wangxc.UserFaceInfo;
import com.moons.wangxc.util.FuncUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchAddFaceService extends Service {
    private static final String TAG = "BatchAddFaceService";
    private static MyHandler myHandler;
    private static ExecutorService executorService = Executors.newFixedThreadPool(5); // 固定五个线程来执行任务
    private static String faceImagePath = null;
    public static Context context;
    private final static int MSG_CODE = 0x1000;
    private final static int MSG_EVENT_REG = 0x1001;
    private final static int MSG_EVENT_NO_FACE = 0x1002;
    private final static int MSG_EVENT_NO_FEATURE = 0x1003;
    private final static int MSG_EVENT_FD_ERROR = 0x1004;
    private final static int MSG_EVENT_FR_ERROR = 0x1005;
    private final static int MSG_EVENT_FILE_NOT_EXIST = 0x1006;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myHandler = new MyHandler(Looper.myLooper(), BatchAddFaceService.this);
        context = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static void BatchAddFace(final String filePath, final String fileName) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AddFaceIssues(filePath, fileName);
            }
        });
    }

    private static void AddFaceIssues(String filePath, String fileName) {
        String absoluteFilePath = filePath + fileName;
        faceImagePath = absoluteFilePath;
        String userName = getUserName(fileName);
        Bitmap mBitmap = Application.decodeImage(absoluteFilePath);
        if (mBitmap == null) {
            Message reg = Message.obtain();
            reg.what = MSG_CODE;
            reg.arg1 = MSG_EVENT_FILE_NOT_EXIST;
            Bundle bundle = new Bundle();
            bundle.putString("username", userName);
            myHandler.sendMessage(reg);
            return;
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
        List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>(); //检测到的人脸(矩形面部区域和角度)
        //初始化人脸检测引擎（appid,sdkkey...人脸数）
        AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
        Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + err.getCode());
        if (err.getCode() != AFD_FSDKError.MOK) {
            Message reg = Message.obtain();
            reg.what = MSG_CODE;
            reg.arg1 = MSG_EVENT_FD_ERROR;
            reg.arg2 = err.getCode();
            myHandler.sendMessage(reg);
        }
        err = engine.AFD_FSDK_GetVersion(version);//获取人脸检测库版本
        Log.d(TAG, "AFD_FSDK_GetVersion =" + version.toString() + ", " + err.getCode());
        //AFD_FSDK_StillImageFaceDetection方法实现人脸检测,result返回检测到的人脸（矩形面部区域和角度）
        err = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
        Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());
        if (!result.isEmpty()) {
            AFR_FSDKVersion version1 = new AFR_FSDKVersion(); //人脸识别库版本
            AFR_FSDKEngine engine1 = new AFR_FSDKEngine(); //人脸识别引擎
            AFR_FSDKFace result1 = new AFR_FSDKFace(); //人脸信息(特征值和字节大小)
            AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);//初始化人脸识别引擎
            Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error1.getCode());
            if (error1.getCode() != AFD_FSDKError.MOK) {
                Message reg = Message.obtain();
                reg.what = MSG_CODE;
                reg.arg1 = MSG_EVENT_FR_ERROR;
                reg.arg2 = error1.getCode();
                myHandler.sendMessage(reg);
            }
            error1 = engine1.AFR_FSDK_GetVersion(version1);
            Log.d("com.arcsoft", "FR=" + version.toString() + "," + error1.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
            //人脸信息特征提取数函数,result1返回人脸特征信息
            //输入的data数据为NV21格式（如Camera里NV21格式的preview数据;)
            // 人脸坐标一般使用人脸检测返回的Rect传入；
            // 人脸角度请按照人脸检测引擎返回的值传入。
            //这里只提取了第一个人脸
            error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
            Log.d("com.arcsoft", "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
            if (error1.getCode() == error1.MOK) {
                AFR_FSDKFace mAFR_FSDKFace = result1.clone(); //人脸信息
                Message reg = Message.obtain();
                reg.what = MSG_CODE;
                reg.arg1 = MSG_EVENT_REG;
                reg.obj = mAFR_FSDKFace;
                Bundle bundle = new Bundle();
                bundle.putString("username", userName);
                bundle.putString("filepath",absoluteFilePath);
                reg.setData(bundle);
                myHandler.sendMessage(reg);
                Log.i(TAG, "we have checked face");
            } else {
                Message reg = Message.obtain();
                reg.what = MSG_CODE;
                reg.arg1 = MSG_EVENT_NO_FEATURE;
                Bundle bundle = new Bundle();
                bundle.putString("username", userName);
                reg.setData(bundle);
                myHandler.sendMessage(reg);
            }
            error1 = engine1.AFR_FSDK_UninitialEngine();
            Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine : " + error1.getCode());
        } else {
            Message reg = Message.obtain();
            reg.what = MSG_CODE;
            reg.arg1 = MSG_EVENT_NO_FACE;
            Bundle bundle = new Bundle();
            bundle.putString("username", userName);
            reg.setData(bundle);
            myHandler.sendMessage(reg);
        }
    }


    public static String getUserName(String fileName) {
        if (fileName.trim().endsWith("jpg")) {
            String[] split = fileName.split(".jpg");
            if (split[0] != null) {
                return split[0];
            }
        } else if (fileName.trim().endsWith("png")) {
            String[] split = fileName.split(".png");
            if (split[0] != null) {
                return split[0];
            }
        } else if (fileName.trim().endsWith("JPG")) {
            String[] split = fileName.split(".JPG");
            if (split[0] != null) {
                return split[0];
            }
        } else if (fileName.trim().endsWith("PNG")) {
            String[] split = fileName.split(".PNG");
            if (split[0] != null) {
                return split[0];
            }
        }
        return "";
    }



    /* 消息处理类 */
    class MyHandler extends Handler {
        private Context context;

        public MyHandler(Looper looper, Context c) {
            super(looper);
            this.context = c;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CODE) {
                switch (msg.arg1) {
                    case MSG_EVENT_FD_ERROR:
                        Toast.makeText(context, "FD初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_EVENT_FILE_NOT_EXIST:
                        Toast.makeText(context, "找不到图片" + msg.getData().getString("username"), Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_EVENT_FR_ERROR:
                        Toast.makeText(context, "FR初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_EVENT_REG:
                        Toast.makeText(context, "图片" + msg.getData().getString("username") + "检测到人脸", Toast.LENGTH_SHORT).show();
                        Application.getFaceDB().addFace(msg.getData().getString("username"), (AFR_FSDKFace) msg.obj);//添加人脸信息
                        TodoFaceListDBIssues(msg.getData().getString("username"), msg.getData().getString("filepath"));
                        break;
                    case MSG_EVENT_NO_FEATURE:
                        Toast.makeText(context, "图片" + msg.getData().getString("username") + "没有检测到特征值", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_EVENT_NO_FACE:
                        Toast.makeText(context, msg.getData().getString("username") + "没有检测到人脸，请换一张图片", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void TodoFaceListDBIssues(String username, String  filePath) {
        UserFaceInfo userFaceInfo = new UserFaceInfo();
        userFaceInfo.setName(username);
        userFaceInfo.setCollectionDateTime(System.currentTimeMillis());
        userFaceInfo.setStatus(0);
        if (!Application.getFaceListDB().isNameExist(username)) {
            Application.getFaceListDB().addMemberFace(userFaceInfo);
        } else {
            Application.getFaceListDB().updateMemberFaceByName_CollectionTimeAndStatus(userFaceInfo);
        }
        FuncUtil.copyFile( filePath, Application.getImagePath(), username + ".jpg");
    }

}
