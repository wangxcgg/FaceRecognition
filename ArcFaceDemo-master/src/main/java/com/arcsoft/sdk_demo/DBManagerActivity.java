package com.arcsoft.sdk_demo;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.moons.wangxc.fragment.MemberManagerFragment;
import com.moons.wangxc.fragment.RecognitionRecordFragment;

import java.util.ArrayList;

public class DBManagerActivity extends Activity {
    private final String TAG = this.getClass().getSimpleName().toString();
    private RadioGroup itemGroup;
    private RadioButton member_manager;
    private RadioButton face_recognition;
    private RadioButton verify_record;
    private RadioButton system_setting;
    private ArrayList<RadioButton> mRadioButtonList_ = new ArrayList<RadioButton>();
    private MemberManagerFragment mMemberManagerFragment;
    private RecognitionRecordFragment mRecogniseRecordFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.db_manager_main);
        loadMemberManagerFragment();
        init();
        mRadioButtonList_.clear();
        mRadioButtonList_.add(member_manager);
        mRadioButtonList_.add(face_recognition);
        mRadioButtonList_.add(verify_record);
        mRadioButtonList_.add(system_setting);
        itemGroup.check(R.id.member_manager);
        itemGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.member_manager:
                        Toast.makeText(getApplicationContext(), "选中名单管理", Toast.LENGTH_SHORT).show();
                        loadMemberManagerFragment();
                        break;
                    case R.id.face_recognition:
                        Toast.makeText(getApplicationContext(), "选中人脸识别", Toast.LENGTH_SHORT).show();
                        Intent it = new Intent(DBManagerActivity.this, DetecterActivity.class);
                        it.putExtra("Camera", 0);
                        startActivity(it);
                        break;
                    case R.id.verify_record:
                        Toast.makeText(getApplicationContext(), "选中识别记录", Toast.LENGTH_SHORT).show();
                        loadRecogniseRecordFragment();
                        break;
                    case R.id.system_setting:
                        Toast.makeText(getApplicationContext(), "选中系统设置", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "未选中任何选项", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

    }


    private void init() {
        itemGroup = (RadioGroup) findViewById(R.id.main_menu);
        member_manager = (RadioButton) findViewById(R.id.member_manager);
        face_recognition = (RadioButton) findViewById(R.id.face_recognition);
        verify_record = (RadioButton) findViewById(R.id.verify_record);
        system_setting = (RadioButton) findViewById(R.id.system_setting);
    }

    private synchronized boolean loadRecogniseRecordFragment() {
        if (mRecogniseRecordFragment == null) {
            mRecogniseRecordFragment = new RecognitionRecordFragment();
        }

        if (showFragment(mRecogniseRecordFragment)) {
            return true;
        }
        return false;

    }

    private synchronized boolean loadMemberManagerFragment() {
        if (mMemberManagerFragment == null) {
            mMemberManagerFragment = new MemberManagerFragment();
        }

        if (showFragment(mMemberManagerFragment)) {
            return true;
        }
        return false;
    }


    //显示fragement
    private boolean showFragment(Fragment fragment) {
        try {
            FragmentManager fm = getFragmentManager();//获取fragment管理器
            FragmentTransaction ft = fm.beginTransaction();//打开事务
            ft.replace(R.id.fragment_content, fragment, fragment.getTag());
            ft.commit();//提交
        } catch (Exception e) {
            Log.e(TAG, "show fragment ex:" + e.getLocalizedMessage());
            return false;
        }
        return true;
    }


}


