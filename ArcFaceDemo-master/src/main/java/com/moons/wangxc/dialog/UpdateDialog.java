package com.moons.wangxc.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.arcsoft.sdk_demo.R;
import com.moons.wangxc.UserFaceInfo;

public class UpdateDialog extends Activity implements View.OnClickListener {
    private static final int DELETE_RESULTCODE = 2;
    private static final int UPDATE_RESULTCODE = 3;
    private EditText idView;
    private EditText nameView;
    private EditText ageView;
    private EditText sexView;
    private EditText raceView;
    private UserFaceInfo mUserFaceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    //去掉标题栏
        setContentView(R.layout.update_dialog);
        Intent intent = getIntent();
        if (intent != null) {
            mUserFaceInfo = (UserFaceInfo) intent.getSerializableExtra("userfaceinfo");
        }
        init();
    }

    private void init() {
        findViewById(R.id.update_confirm).setOnClickListener(this);
        findViewById(R.id.update_cancel).setOnClickListener(this);
        findViewById(R.id.update_delete).setOnClickListener(this);
        idView = (EditText) findViewById(R.id.update_edit_id);
        nameView = (EditText) findViewById(R.id.update_edit_name);
        ageView = (EditText) findViewById(R.id.update_edit_age);
        sexView = (EditText) findViewById(R.id.update_edit_sex);
        raceView = (EditText) findViewById(R.id.update_edit_race);
        idView.setText(String.valueOf(mUserFaceInfo.getUserId()));
        nameView.setText(mUserFaceInfo.getName());
        ageView.setText(String.valueOf(mUserFaceInfo.getAge()));
        sexView.setText(mUserFaceInfo.getSex());
        raceView.setText(mUserFaceInfo.getRace());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.update_confirm:
                confirm(); // 确认修改
                break;
            case R.id.update_cancel:
                cancel(); // 取消修改
                break;
            case R.id.update_delete:
                delete();
        }
    }

    private void delete() {
        String username = String.valueOf(nameView.getText());
        Intent intent = new Intent();
        intent.putExtra("username", username);
        setResult(DELETE_RESULTCODE, intent);
        finish();
    }

    private void confirm() {
        int userid = Integer.valueOf(String.valueOf(idView.getText()));
        String name = String.valueOf(nameView.getText());
        int age = Integer.valueOf(String.valueOf(ageView.getText()));
        String sex = String.valueOf(sexView.getText());
        String race = String.valueOf(raceView.getText());
        UserFaceInfo userFaceInfo = new UserFaceInfo();
        userFaceInfo.setUserId(userid);
        userFaceInfo.setAge(age);
        userFaceInfo.setName(name);
        userFaceInfo.setSex(sex);
        userFaceInfo.setRace(race);
        Intent intent = new Intent();
        intent.putExtra("userFaceInfo", userFaceInfo);
        setResult(UPDATE_RESULTCODE, intent);
        finish();
    }

    private void cancel() {
        setResult(UPDATE_RESULTCODE);
        finish();
    }
}
