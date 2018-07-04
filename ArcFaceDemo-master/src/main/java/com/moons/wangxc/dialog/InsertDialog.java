package com.moons.wangxc.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.R;
import com.moons.wangxc.UserFaceInfo;

public class InsertDialog extends Activity implements View.OnClickListener {
    private static final int INSERT_RESULTCODE = 1;
    private EditText nameView;
    private EditText ageView;
    private EditText sexView;
    private EditText raceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    //去掉标题栏
        setContentView(R.layout.insert_dialog);
        init();
    }

    private void init() {
        findViewById(R.id.insert_confirm).setOnClickListener(this);
        findViewById(R.id.insert_cancel).setOnClickListener(this);
        nameView = (EditText) findViewById(R.id.insert_edit_name);
        ageView = (EditText) findViewById(R.id.insert_edit_age);
        sexView = (EditText) findViewById(R.id.insert_edit_sex);
        raceView = (EditText) findViewById(R.id.insert_edit_race);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.insert_confirm:
                confirm(); // 确认插入
                break;
            case R.id.insert_cancel:
                cancel(); // 取消插入
                break;
        }
    }

    private void confirm() {
        String username = String.valueOf(nameView.getText());
        int age = ageView.getText().toString().equals("") ? 0 : Integer.valueOf(String.valueOf(ageView.getText()));
        String sex = String.valueOf(sexView.getText());
        String race = String.valueOf(raceView.getText());
        String imagePath = Application.getContext().getExternalCacheDir().getPath() + "/image/" + username + ".png";
        String faceFeaPath = Application.getContext().getExternalCacheDir().getPath() + "/" + "wangxc.data";
        UserFaceInfo userFaceInfo = new UserFaceInfo();
        userFaceInfo.setName(username);
        userFaceInfo.setAge(age);
        userFaceInfo.setSex(sex);
        userFaceInfo.setRace(race);
        userFaceInfo.setFaceImage_AbsPath(imagePath);
        userFaceInfo.setFaceFea_AbsPath(faceFeaPath);
        userFaceInfo.setStatus(1);
        Intent intent = new Intent();
        intent.putExtra("userFaceInfo", userFaceInfo);
        setResult(INSERT_RESULTCODE, intent);
        finish();
    }

    private void cancel() {
        setResult(INSERT_RESULTCODE);
        finish();
    }
}
