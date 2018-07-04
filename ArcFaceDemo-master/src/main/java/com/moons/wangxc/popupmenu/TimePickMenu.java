package com.moons.wangxc.popupmenu;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TimePicker;

import com.arcsoft.sdk_demo.R;
import com.nineoldandroids.view.ViewHelper;

import java.util.Calendar;

import static android.R.attr.x;

public class TimePickMenu extends PopupWindow {
    private static final String TAG = "TimePickMenu";
    private Context context_;
    private View conentView;
    private static TimePickMenu sMTimePickMenu = null;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private String timeStr;
    private String dateStr;
    private Handler mHandler;

    private TimePickMenu() {
        // 构造函数私有
    }

    // Double Check Lock(DCL)实现单例
    public static TimePickMenu getInstance() {
        if (sMTimePickMenu == null) {
            synchronized (TimePickMenu.class) {
                if (sMTimePickMenu == null) {
                    sMTimePickMenu = new TimePickMenu();
                }
            }
        }
        // 静态方法返回单例类对象
        return sMTimePickMenu;
    }

    public void initialize(Context context, Handler dateTimeHandler) {
        context_ = context;
        iniView(context);
        mHandler=dateTimeHandler;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void iniView(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.time_menu, null);
        mDatePicker = (DatePicker) conentView.findViewById(R.id.datePicker);
        mTimePicker = (TimePicker) conentView.findViewById(R.id.timePicker);
        ViewHelper.setScaleX(mDatePicker, 0.8f);//可以随意指定缩小百分比
        ViewHelper.setScaleY(mDatePicker, 0.8f);
        ViewHelper.setScaleX(mTimePicker, 0.8f);//可以随意指定缩小百分比
        ViewHelper.setScaleY(mTimePicker, 0.8f);


        //获取日历的一个对象
        Calendar calendar = Calendar.getInstance();
        //获取年月日时分的信息
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
//        dateStr=year + "-" + (month + 1) + "-" + day;
        dateStr=String.format("%d-%02d-%02d",year,month+1,day);
//        timeStr=hour+":"+minute;
        timeStr=String.format("%02d:%02d",hour,minute);
        //初始化为系统时间
        mDatePicker.init(year,calendar.get(Calendar.MONTH), day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                dateStr = (year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                dateStr=String.format("%d-%02d-%02d",year,monthOfYear + 1,dayOfMonth);
//                Toast.makeText(context, dateStr, Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("dateStr", dateStr);
                data.putString("timeStr", timeStr);
                msg.setData(data);
                msg.what = 0;
                mHandler.sendMessage(msg);
            }
        });
        mTimePicker.setHour(hour);
        mTimePicker.setMinute(minute);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                timeStr = hourOfDay + ":" + minute;
                timeStr=String.format("%02d:%02d",hourOfDay,minute);
//                Toast.makeText(context, timeStr, Toast.LENGTH_SHORT).show();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("dateStr", dateStr);
                data.putString("timeStr", timeStr);
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
        sMTimePickMenu = this; // 保存弹出的实例
        if (!this.isShowing()) {
            // 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
            this.showAsDropDown(parent, x, 0);
        } else {
            this.dismiss();
        }
    }

}
