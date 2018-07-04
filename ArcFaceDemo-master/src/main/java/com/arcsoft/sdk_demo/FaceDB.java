package com.arcsoft.sdk_demo;

import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.java.ExtInputStream;
import com.guo.android_extend.java.ExtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gqj3375 on 2017/7/11.
 */
//人脸库
//appID and sdkkey was create by husheng
public class FaceDB {
	private final String TAG = this.getClass().getSimpleName().toString();

//	中型网络（1000人内）-人脸检索(1:N) 文件大小： 版本： v1.1
//	APP_Id
//
//			GT21JRfvL9BYXy4iwxegRyb3CxToLLPviMvpq6xtUo3c
//	SDK_key:
//
//	人脸追踪(FT) Key GUJDRbQGtn3QSrFPxF83gEgWbDKgMGGscbLAVgfRWBdE
//
//	人脸检测(FD) Key GUJDRbQGtn3QSrFPxF83gEgdkcaq1KStnHbWe6SLikFW
//
//	人脸识别(FR) Key GUJDRbQGtn3QSrFPxF83gEh8QDdVu7gYFNcTry6hegf8
//
//	年龄识别(Age) Key GUJDRbQGtn3QSrFPxF83gEhNj29vCPhqtFDfgKnQYeie
//
//	性别识别(Gender) Key GUJDRbQGtn3QSrFPxF83gEhVtRR4Xe2mxikqxeqki8vs
//	此处APP_Id与SDK_Key仅适用于ArcFace v1.1， 不适用于其他版本。

	public static String appid = "GT21JRfvL9BYXy4iwxegRyb3CxToLLPviMvpq6xtUo3c";
	public static String ft_key = "GUJDRbQGtn3QSrFPxF83gEgWbDKgMGGscbLAVgfRWBdE";
	public static String fd_key = "GUJDRbQGtn3QSrFPxF83gEgdkcaq1KStnHbWe6SLikFW";
	public static String fr_key = "GUJDRbQGtn3QSrFPxF83gEh8QDdVu7gYFNcTry6hegf8";
	public static String age_key = "GUJDRbQGtn3QSrFPxF83gEhNj29vCPhqtFDfgKnQYeie";
	public static String gender_key = "GUJDRbQGtn3QSrFPxF83gEhVtRR4Xe2mxikqxeqki8vs";

	//default：即不加任何访问修饰符，通常称为“默认访问模式“。该模式下，只允许在同一个包中进行访
	String mDBPath; //数据库保存的路径
	public List<FaceRegist> mRegister; //人脸库用list存储（已注册的人脸）
	AFR_FSDKEngine mFREngine; //人脸识别引擎
	AFR_FSDKVersion mFRVersion; //人脸识别库版本
	boolean mUpgrade; //更新标志
	//已注册的人脸类（人脸信息和姓名关联）
	public class FaceRegist {
		public String mName; //姓名
		public List<AFR_FSDKFace> mFaceList; //AFR_FSDKFace保存了人脸信息，包含特征信息的长度和内容的byte数组

		public FaceRegist(String name) {
			mName = name;
			mFaceList = new ArrayList<>();
		}
	}
	//人脸库构造函数
	public FaceDB(String path) {
		mDBPath = path; //人脸库保存的路径
		mRegister = new ArrayList<>(); //人脸库list
		mFRVersion = new AFR_FSDKVersion(); //人脸识别库版本
		mUpgrade = false; //更新标记
		mFREngine = new AFR_FSDKEngine(); //人脸识别引擎
		//初始化人脸识别引擎，使用时请替换申请的 APPID 和 SDKKEY
		AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
		if (error.getCode() != AFR_FSDKError.MOK) {
			Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code :" + error.getCode());
		} else {
			mFREngine.AFR_FSDK_GetVersion(mFRVersion);
			Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
		}
	}
	//释放引擎占用的系统资源
	public void destroy() {
		if (mFREngine != null) {
			mFREngine.AFR_FSDK_UninitialEngine();
		}
	}
	//保存人脸识别库版本和FeatureLevel
	private boolean saveInfo() {
		try {
			FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt");
			ExtOutputStream bos = new ExtOutputStream(fs);
			bos.writeString(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel()); //写入人脸识别库版本和FeatureLevel
			bos.close();
			fs.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	//从文件中读取人脸库list(仅读取了人名索引）
	private boolean loadInfo() {
		if (!mRegister.isEmpty()) {
			return false; //人脸库list没有任何数据
		}
		try {
			FileInputStream fs = new FileInputStream(mDBPath + "/face.txt");
			ExtInputStream bos = new ExtInputStream(fs);
			//load version
			String version_saved = bos.readString(); //读取人脸识别库版本和FeatureLevel信息
			if (version_saved.equals(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel())) {
				mUpgrade = true; //face.txt文件中的人脸识别库版本信息与当前人脸库的人脸识别库版本信息一致
			}
			//load all regist name.
			if (version_saved != null) {
				for (String name = bos.readString(); name != null; name = bos.readString()){
					if (new File(mDBPath + "/" + name + ".data").exists()) {
						mRegister.add(new FaceRegist(new String(name))); //确保人名和特征值文件存在，获取人脸库List
					}
				}
			}
			bos.close();
			fs.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	//从文件中读取人脸信息（读取特征值信息）
	public boolean loadFaces(){
		if (loadInfo()) { //确认人名索引已读取
			try {
				for (FaceRegist face : mRegister) {
					Log.d(TAG, "load name:" + face.mName + "'s face feature data.");
					FileInputStream fs = new FileInputStream(mDBPath + "/" + face.mName + ".data");
					ExtInputStream bos = new ExtInputStream(fs); //输入流
					AFR_FSDKFace afr = null; //特征值信息
					//do-while先循环再判断条件
					do {
						if (afr != null) {
							if (mUpgrade) {
								//upgrade data.
							}
							face.mFaceList.add(afr); //读取特征值
						}
						afr = new AFR_FSDKFace();
					} while (bos.readBytes(afr.getFeatureData()));//读取特征值
					bos.close();
					fs.close();
					Log.d(TAG, "load name: size = " + face.mFaceList.size());
				}
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	//将待注册的人脸信息添加到人脸库中
	public	void addFace(String name, AFR_FSDKFace face) {
		try {
			//check if already registered.
			boolean add = true;
			for (FaceRegist frface : mRegister) {
				if (frface.mName.equals(name)) {
					frface.mFaceList.add(face); //如果姓名已注册，就添加人脸特征值
					add = false;
					break;
				}
			}
			if (add) { // not registered.
				FaceRegist frface = new FaceRegist(name); //新建需要注册的人脸
				frface.mFaceList.add(face); //添加人脸特征值
				mRegister.add(frface);  //添加到人脸库list
			}

			if (saveInfo()) {
				//update all names
				FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
				ExtOutputStream bos = new ExtOutputStream(fs);
				for (FaceRegist frface : mRegister) {
					bos.writeString(frface.mName); //保存人名索引文件
				}
				bos.close();
				fs.close();

				//save new feature
				fs = new FileOutputStream(mDBPath + "/" + name + ".data", true);
				bos = new ExtOutputStream(fs);
				bos.writeBytes(face.getFeatureData()); //保存人脸特征值文件
				bos.close();
				fs.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
  //删除姓名name对应人脸库中的人脸信息
	public boolean delete(String name) {
		try {
			//check if already registered.
			boolean find = false;
			for (FaceRegist frface : mRegister) {
				if (frface.mName.equals(name)) {
					File delfile = new File(mDBPath + "/" + name + ".data");
					if (delfile.exists()) {
						delfile.delete(); //删除特征值
					}
					mRegister.remove(frface); //从人脸库list中删除
					find = true;
					break;
				}
			}

			if (find) {
				if (saveInfo()) {
					//update all names
					FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
					ExtOutputStream bos = new ExtOutputStream(fs);
					for (FaceRegist frface : mRegister) {
						bos.writeString(frface.mName); //保存人名索引文件
					}
					bos.close();
					fs.close();
				}
			}
			return find;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean upgrade() {
		return false;
	}
}
