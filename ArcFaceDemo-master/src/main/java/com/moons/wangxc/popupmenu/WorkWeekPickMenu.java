package com.moons.wangxc.popupmenu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;

import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moons.wangxc.util.SharedPreferencesUtil;

import java.util.HashMap;
import java.util.Iterator;

import static android.R.attr.x;

public class WorkWeekPickMenu extends PopupWindow {
    private static final String TAG = "WorkTimePickMenu";
    private Context context_;
    private View contentView;
    private static WorkWeekPickMenu mWorkWeekPickMenu = null;
    private Handler mHandler;
    private HashMap<String, Boolean> weeks = null;
    private HashMap<String, CheckBox> checkboxs = null;

    private WorkWeekPickMenu() {
        // 构造函数私有
    }

    // Double Check Lock(DCL)实现单例
    public static WorkWeekPickMenu getInstance() {
        if (mWorkWeekPickMenu == null) {
            synchronized (WorkWeekPickMenu.class) {
                if (mWorkWeekPickMenu == null) {
                    mWorkWeekPickMenu = new WorkWeekPickMenu();
                }
            }
        }
        // 静态方法返回单例类对象
        return mWorkWeekPickMenu;
    }

    public void initialize(Context context, Handler dateTimeHandler) {
        context_ = context;
        initView(context);
        mHandler = dateTimeHandler;
        initWeekConfig();
        setEventListener();

    }

    private void initWeekConfig() {
        weeks = new HashMap<String, Boolean>();
        weeks.clear();
        Log.i(TAG, "init week");
        String strJson = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "workweek", "config", "none");
        if (strJson.contains("none")) {
            weeks.put("mon", false);
            weeks.put("tues", false);
            weeks.put("wed", false);
            weeks.put("thur", false);
            weeks.put("fri", false);
            weeks.put("sat", false);
            weeks.put("sun", false);
        } else {
            Gson gson = new Gson();
            weeks = gson.fromJson(strJson, new TypeToken<HashMap<String, Boolean>>() {
            }.getType());
        }
        checkboxs.get("mon").setChecked(weeks.get("mon"));
        checkboxs.get("tues").setChecked(weeks.get("tues"));
        checkboxs.get("wed").setChecked(weeks.get("wed"));
        checkboxs.get("thur").setChecked(weeks.get("thur"));
        checkboxs.get("fri").setChecked(weeks.get("fri"));
        checkboxs.get("sat").setChecked(weeks.get("sat"));
        checkboxs.get("sun").setChecked(weeks.get("sun"));

    }


    private void setEventListener() {
        Iterator<String> it = checkboxs.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            checkboxs.get(key).setOnCheckedChangeListener(cb);
        }

    }

    private CompoundButton.OnCheckedChangeListener cb = new CompoundButton.OnCheckedChangeListener() { //实例化一个cb
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()) {
                case R.id.check_Monday:
                    weeks.put("mon",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Tuesday:
                    weeks.put("tues",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Wednesday:
                    weeks.put("wed",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Thursday:
                    weeks.put("thur",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Friday:
                    weeks.put("fri",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Saturday:
                    weeks.put("sat",isChecked);
                    saveWeekConfig();
                    break;
                case R.id.check_Sunday:
                    weeks.put("sun",isChecked);
                    saveWeekConfig();
                    break;
                default:
                    break;
            }

        }
    };

    private void saveWeekConfig() {
        Gson gson = new Gson();
        String strJson = gson.toJson(weeks);
        SharedPreferencesUtil.saveData(Application.getContext(), Application.getCachePath(), "workweek", "config", strJson);
    }

    private void initView(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.work_week_menu, null);
        checkboxs = new HashMap<String, CheckBox>();
        checkboxs.clear();
        checkboxs.put("mon", ((CheckBox) contentView.findViewById(R.id.check_Monday)));
        checkboxs.put("tues", ((CheckBox) contentView.findViewById(R.id.check_Tuesday)));
        checkboxs.put("wed", ((CheckBox) contentView.findViewById(R.id.check_Wednesday)));
        checkboxs.put("thur", ((CheckBox) contentView.findViewById(R.id.check_Thursday)));
        checkboxs.put("fri", ((CheckBox) contentView.findViewById(R.id.check_Friday)));
        checkboxs.put("sat", ((CheckBox) contentView.findViewById(R.id.check_Saturday)));
        checkboxs.put("sun", ((CheckBox) contentView.findViewById(R.id.check_Sunday)));
        // 设置SelectPicPopupWindow的View
        this.setContentView(contentView);
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
        mWorkWeekPickMenu = this; // 保存例
        if (!this.isShowing()) {
            // 相对于父控件的位置（例如正中央Gravity.CENTER，下方Gravity.BOTTOM等），可以设置偏移或无偏移
            this.showAsDropDown(parent, x, 0);
        } else {
            this.dismiss();
        }
    }

}
