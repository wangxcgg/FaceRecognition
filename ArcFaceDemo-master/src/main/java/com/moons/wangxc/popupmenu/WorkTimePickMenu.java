package com.moons.wangxc.popupmenu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.R;
import com.moons.wangxc.util.SharedPreferencesUtil;
import com.nineoldandroids.view.ViewHelper;

import java.util.Calendar;

import static android.R.attr.x;

public class WorkTimePickMenu extends PopupWindow {
    private static final String TAG = "WorkTimePickMenu";
    private Context context_;
    private View conentView;
    private static WorkTimePickMenu mWorkTimePickMenu = null;
    private TimePicker mTimePickerStartWork;
    private TimePicker mTimePickerEndWork;
    private String timeStrStartWork;
    private String timeStrEndWork;
    private Handler mHandler;

    private WorkTimePickMenu() {
        // 构造函数私有
    }

    // Double Check Lock(DCL)实现单例
    public static WorkTimePickMenu getInstance() {
        if (mWorkTimePickMenu == null) {
            synchronized (TimePickMenu.class) {
                if (mWorkTimePickMenu == null) {
                    mWorkTimePickMenu = new WorkTimePickMenu();
                }
            }
        }
        // 静态方法返回单例类对象
        return mWorkTimePickMenu;
    }

    public void initialize(Context context, Handler dateTimeHandler) {
        context_ = context;
        iniView(context);
        mHandler = dateTimeHandler;
    }

    private void iniView(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.work_time_menu, null);
        mTimePickerStartWork = (TimePicker) conentView.findViewById(R.id.timePicker_startwork);
        mTimePickerEndWork = (TimePicker) conentView.findViewById(R.id.timePicker_endwork);
        ViewHelper.setScaleX(mTimePickerStartWork, 0.8f);//可以随意指定缩小百分比
        ViewHelper.setScaleY(mTimePickerEndWork, 0.8f);
        String strStartWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "startwork", "none");
        String strEndWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "endwork", "none");
        int startHour;
        int startMinute;
        int endHour;
        int endMinute;
        if (!strStartWork.contains("none")) {
            String[] split = strStartWork.split(":");
            startHour = Integer.valueOf(split[0]);
            startMinute = Integer.valueOf(split[1]);
        } else {
            Calendar calendar = Calendar.getInstance();
            startHour = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute = calendar.get(Calendar.MINUTE);
        }
        if (!strEndWork.contains("none")) {
            String[] split = strEndWork.split(":");
            endHour = Integer.valueOf(split[0]);
            endMinute = Integer.valueOf(split[1]);
        } else {
            Calendar calendar = Calendar.getInstance();
            endHour = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute = calendar.get(Calendar.MINUTE);
        }
        timeStrStartWork = startHour + ":" + startMinute;
        timeStrEndWork = endHour + ":" + endMinute;
        mTimePickerStartWork.setHour(startHour);
        mTimePickerStartWork.setMinute(startMinute);
        mTimePickerStartWork.setIs24HourView(true);
        mTimePickerStartWork.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeStrStartWork = hourOfDay + ":" + minute;
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("timeStrStartWork", timeStrStartWork);
                data.putString("timeStrEndWork", timeStrEndWork);
                msg.setData(data);
                msg.what = 0;
                mHandler.sendMessage(msg);
            }
        });

        mTimePickerEndWork.setHour(endHour);
        mTimePickerEndWork.setMinute(endMinute);
        mTimePickerEndWork.setIs24HourView(true);
        mTimePickerEndWork.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timeStrEndWork = hourOfDay + ":" + minute;
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("timeStrStartWork", timeStrStartWork);
                data.putString("timeStrEndWork", timeStrEndWork);
                msg.setData(data);
                msg.what = 0;
                mHandler.sendMessage(msg);
            }
        });


        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 点击空白处让窗体消失
        this.setOutsideTouchable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
        // 刷新状态
        this.update();
    }


    // 重写了showPopupWindow函数
    public void showPopupWindow(View parent) {
        mWorkTimePickMenu = this; // 保存弹出的实例
        if (!this.isShowing()) {
            // 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
            this.showAsDropDown(parent, x, 0);
        } else {
            this.dismiss();
        }
    }

}
