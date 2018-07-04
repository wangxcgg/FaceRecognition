package com.moons.wangxc.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.arcsoft.sdk_demo.R;

public class DeleteDialog extends Activity implements View.OnClickListener {
    private static final int DELETE_RESULTCODE = 2;
    private EditText nameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);    //去掉标题栏
        setContentView(R.layout.delete_dialog);
        init();
    }

    private void init() {
        findViewById(R.id.delete_confirm).setOnClickListener(this);
        findViewById(R.id.delete_cancel).setOnClickListener(this);
        nameView = (EditText) findViewById(R.id.delete_edit_name);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.delete_confirm:
                confirm(); // 确认删除
                break;
            case R.id.delete_cancel:
                cancel(); // 取消删除
                break;
        }
    }


    private void confirm() {
        String username = String.valueOf(nameView.getText());
        Intent intent = new Intent();
        intent.putExtra("username", username);
        setResult(DELETE_RESULTCODE, intent);
        finish();
    }


    private void cancel() {
        setResult(DELETE_RESULTCODE);
        finish();
    }
}
