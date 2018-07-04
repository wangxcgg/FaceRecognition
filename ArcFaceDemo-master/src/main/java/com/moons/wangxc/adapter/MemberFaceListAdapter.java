package com.moons.wangxc.adapter;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.arcsoft.sdk_demo.R;
import com.moons.wangxc.UserFaceInfo;
import com.moons.wangxc.util.FuncUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MemberFaceListAdapter extends BaseAdapter {
    private static final String TAG = "MemberFaceListAdapter";
    private List<UserFaceInfo> userfaceinfo_list = new ArrayList<UserFaceInfo>();
    private final LayoutInflater mInflater;
    private Activity mContext;

    public MemberFaceListAdapter(Activity activity, List<UserFaceInfo> userfaceinfo_list) {
        this.mContext = activity;
        this.userfaceinfo_list = userfaceinfo_list;
        this.mInflater = activity.getLayoutInflater();
    }


    public void setList(List<UserFaceInfo> userfaceinfo_list) {
        this.userfaceinfo_list=userfaceinfo_list;
    }

    @Override
    public int getCount() {
        return userfaceinfo_list.size();
    }

    @Override
    public synchronized Object getItem(int position) {
        UserFaceInfo mUserFaceInfo = null;
        try {
            mUserFaceInfo = userfaceinfo_list.get(position);
        } catch (Exception e) {
            Log.e(TAG,
                    "position=" + position + ",ex:" + e.getLocalizedMessage());
        }
        return mUserFaceInfo;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.member_facelist_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textview_id= (TextView) convertView
                    .findViewById(R.id.userid);
            viewHolder.textview_name= (TextView) convertView
                    .findViewById(R.id.name);
            viewHolder.textview_sex= (TextView) convertView
                    .findViewById(R.id.sex);
            viewHolder.textview_age= (TextView) convertView
                    .findViewById(R.id.age);
            viewHolder.textview_department= (TextView) convertView
                    .findViewById(R.id.department);
            viewHolder.textview_collectiontime= (TextView) convertView
                    .findViewById(R.id.collectiontime);
            viewHolder.textview_status= (TextView) convertView
                    .findViewById(R.id.status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag(); // 重新获取viewholder
        }
        UserFaceInfo mUserFaceInfo=userfaceinfo_list.get(position);
        viewHolder.textview_id.setText(String.valueOf(mUserFaceInfo.getUserId()));
        viewHolder.textview_name.setText(mUserFaceInfo.getName());
        viewHolder.textview_sex.setText(mUserFaceInfo.getSex());
        viewHolder.textview_age.setText(String.valueOf(mUserFaceInfo.getAge()));
        viewHolder.textview_department.setText(mUserFaceInfo.getReserve1());
        try {
            viewHolder.textview_collectiontime.setText(FuncUtil.longToString(mUserFaceInfo.getCollectionDateTime(),"yyyy-MM-dd HH:mm"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        viewHolder.textview_status.setText(String.valueOf(mUserFaceInfo.getStatus()));
        return convertView;
    }



    private class ViewHolder {
        public TextView textview_id;
        public TextView textview_name;
        public TextView textview_sex;
        public TextView textview_age;
        public TextView textview_department;
        public TextView textview_collectiontime;
        public TextView textview_status;
    }


}
