package com.moons.wangxc.adapter;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arcsoft.sdk_demo.R;
import com.moons.wangxc.CheckInInfo;
import com.moons.wangxc.util.FuncUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class VerifyRecordAdapter extends BaseAdapter {
    private static final String TAG = "VerifyRecordAdapter";
    private List<CheckInInfo> checkInInfo_list = new ArrayList<CheckInInfo>();
    private final LayoutInflater mInflater;
    private Activity mContext;

    public VerifyRecordAdapter(Activity activity, List<CheckInInfo> checkInInfo_list) {
        this.mContext = activity;
        this.checkInInfo_list = checkInInfo_list;
        this.mInflater = activity.getLayoutInflater();
    }

    public void setList(List<CheckInInfo> checkInInfo_list) {
        this.checkInInfo_list = checkInInfo_list;
    }

    @Override
    public int getCount() {
        return checkInInfo_list.size();
    }

    @Override
    public synchronized Object getItem(int position) {
        CheckInInfo mCheckInInfo = null;
        try {
            mCheckInInfo = checkInInfo_list.get(position);
        } catch (Exception e) {
            Log.e(TAG,
                    "position=" + position + ",ex:" + e.getLocalizedMessage());
        }
        return mCheckInInfo;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.verify_record_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textview_recordId = (TextView) convertView
                    .findViewById(R.id.recordID);
            viewHolder.textview_name = (TextView) convertView
                    .findViewById(R.id.name);
            viewHolder.textview_userId = (TextView) convertView
                    .findViewById(R.id.userID);
            viewHolder.textview_verifyTime = (TextView) convertView
                    .findViewById(R.id.verify_time);
            viewHolder.textview_VerifyResult = (TextView) convertView
                    .findViewById(R.id.verify_result);
            viewHolder.textview_verifyStatus = (TextView) convertView
                    .findViewById(R.id.verify_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(); // 重新获取viewholder
        }
        CheckInInfo mCheckInInfo = checkInInfo_list.get(position);
        viewHolder.textview_recordId.setText(String.valueOf(mCheckInInfo.getRecordId()));
        viewHolder.textview_name.setText(mCheckInInfo.getName());
        viewHolder.textview_userId.setText(String.valueOf(mCheckInInfo.getFaceId()));
        try {
            viewHolder.textview_verifyTime.setText(FuncUtil.longToString(mCheckInInfo.getVerifyDateTime(),"yyyy-MM-dd HH:mm"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewHolder.textview_VerifyResult.setText(mCheckInInfo.getVerifyResult());
        viewHolder.textview_verifyStatus.setText(String.valueOf(mCheckInInfo.getReportstatus()));
        return convertView;
    }


    private class ViewHolder {
        public TextView textview_recordId;
        public TextView textview_name;
        public TextView textview_userId;
        public TextView textview_verifyTime;
        public TextView textview_VerifyResult;
        public TextView textview_verifyStatus;
    }


}
