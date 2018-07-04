package com.moons.wangxc.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.moons.wangxc.CheckInInfo;
import com.moons.wangxc.adapter.VerifyRecordAdapter;
import com.moons.wangxc.popupmenu.WorkTimePickMenu;
import com.moons.wangxc.popupmenu.WorkWeekPickMenu;
import com.moons.wangxc.sqliteDB.VerifyRecordSQLiteHelper;
import com.moons.wangxc.util.FuncUtil;
import com.moons.wangxc.util.SharedPreferencesUtil;
import com.moons.wangxc.widget.DoubleDatePickerDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class RecognitionRecordFragment extends Fragment {
    private static String TAG = "RecognitionRecordFragment";
    private VerifyRecordSQLiteHelper mVerifyRecordDB;
    private VerifyRecordAdapter mVerifyRecordAdapter;
    private List<CheckInInfo> checkInInfo_list = new ArrayList<CheckInInfo>();
    private Context mContext;
    private ListView verifyRecordListView;
    private Button btn_allRecord;
    private Button btn_ruleRecord;
    private Button btn_exportCurrentRecord;
    private Button btn_exportDateRecord;
    private View mView;
    private TextView textView_CheckInTime;
    private TextView textView_CheckInWeek;
    private TextView textView_exportTime;
    private LinearLayout mLinearLayout_CheckInTime;
    private LinearLayout mLinearLayout_CheckInWeek;
    private LinearLayout mLinearLayout_ExportTime;
    private WorkTimePickMenu mWorkTimePickMenu;
    private WorkWeekPickMenu mWorkWeekPickMenu;

    Handler mWorkTimePickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Bundle bd = msg.getData();
                    String timeStrStartWork = bd.getString("timeStrStartWork");
                    String timeStrEndWork = bd.getString("timeStrEndWork");
                    saveTimePreferences(ZeroizeTime(timeStrStartWork), ZeroizeTime(timeStrEndWork));
                    textView_CheckInTime.setText(timeStrStartWork + "-" + timeStrEndWork + " " +
                            getTimeExpend(timeStrStartWork, timeStrEndWork));
                    break;
                default:
                    break;
            }
        }
    };

    Handler mWorkWeekPickHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Bundle bd = msg.getData();
                    break;
                default:
                    break;
            }
        }
    };


    private void saveTimePreferences(String strStart, String strEnd) {
        SharedPreferencesUtil.saveData(Application.getContext(), Application.getCachePath(), "worktime", "startwork", strStart);
        SharedPreferencesUtil.saveData(Application.getContext(), Application.getCachePath(), "worktime", "endwork", strEnd);
    }


    public String ZeroizeTime(String timeStr) {
        try {
            String[] split = timeStr.split(":");
            return FuncUtil.Zeroize(split[0]) + ":" + FuncUtil.Zeroize(split[1]);
        } catch (Exception e) {
            return timeStr;
        }
    }


    private String getTimeExpend(String startTime, String endTime) {
        //传入字串类型 2018-03-23 08:30
        startTime = "2018-03-23" + ' ' + startTime;
        endTime = "2018-03-23" + ' ' + endTime;
        long longStart = FuncUtil.getTimeMillis(startTime, "yyyy-MM-dd HH:mm"); //获取开始时间毫秒数
        long longEnd = FuncUtil.getTimeMillis(endTime, "yyyy-MM-dd HH:mm");  //获取结束时间毫秒数
        long longExpend = longEnd - longStart;  //获取时间差
        long longHours = longExpend / (60 * 60 * 1000); //根据时间差来计算小时数
        long longMinutes = (longExpend - longHours * (60 * 60 * 1000)) / (60 * 1000);   //根据时间差来计算分钟数
//        return longHours + ":" + longMinutes;
        return longHours + "小时" + longMinutes + "分";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        Log.i(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.verify_record_fragment, null, false);
        initView();
        initTextViewWorkTime();
        initDB();
        setEventListener();

        return mView;
    }

    private void initTextViewWorkTime() {
        String strStartWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "startwork", "none");
        String strEndWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "endwork", "none");

        if(strStartWork.contains("none")||strEndWork.contains("none")){
            Calendar calendar = Calendar.getInstance();
            strStartWork= String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            strEndWork= String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        }

        textView_CheckInTime.setText(strStartWork + "-" + strEndWork + " " +
                getTimeExpend(strStartWork, strEndWork));
        String startExportTime = getStartExportTime();
        String endExportTime = getEndExportTime();
        if (startExportTime.contains("none") || endExportTime.contains("none")) {
            Calendar c = Calendar.getInstance();
            startExportTime = String.format("%02d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE));
            endExportTime = String.format("%02d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DATE));
            saveExportTime(startExportTime, endExportTime);
        }
        textView_exportTime.setText(startExportTime + "至" + endExportTime);
    }

    private void initDB() {
        mVerifyRecordDB = Application.getVerifyRecordDB();
        try {
            mVerifyRecordDB.createDataBase();
        } catch (IOException e) {
            Log.i(TAG, "create verifyRecordDB fail");
        }
    }


    private void initView() {
        verifyRecordListView = (ListView) mView.findViewById(R.id.verifyRecord_listView);
        btn_allRecord = (Button) mView.findViewById(R.id.btn_allRecord);
        btn_ruleRecord = (Button) mView.findViewById(R.id.btn_ruleRecord);
        btn_exportCurrentRecord = (Button) mView.findViewById(R.id.btn_exportCurrentRecord);
        btn_exportDateRecord=(Button) mView.findViewById(R.id.btn_exportDateRecord);
        mLinearLayout_CheckInTime = (LinearLayout) mView.findViewById(R.id.linearLayout_checkInTime);
        mLinearLayout_CheckInWeek = (LinearLayout) mView.findViewById(R.id.linearLayout_checkInWeek);
        mLinearLayout_ExportTime = (LinearLayout) mView.findViewById(R.id.linearLayout_exportTime);
        textView_CheckInTime = (TextView) mView.findViewById(R.id.textView_checkInTime);
        textView_CheckInWeek = (TextView) mView.findViewById(R.id.textView_checkInWeek);
        textView_exportTime = (TextView) mView.findViewById(R.id.textView_exportTime);
    }


    private void setEventListener() {
        mLinearLayout_CheckInTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                showWorkTimePickMenu(paramView);
            }
        });
        mLinearLayout_CheckInWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                showWorkWeekPickMenu(paramView);

            }
        });


        mLinearLayout_ExportTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                Calendar c = Calendar.getInstance();
                new DoubleDatePickerDialog(mContext, 0, new DoubleDatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker startDatePicker, int startYear, int startMonthOfYear,
                                          int startDayOfMonth, DatePicker endDatePicker, int endYear, int endMonthOfYear,
                                          int endDayOfMonth) {
                        String beginExportTime = String.format("%02d-%02d-%02d", startYear, startMonthOfYear + 1, startDayOfMonth);
                        String endExportTime = String.format("%02d-%02d-%02d", endYear, endMonthOfYear + 1, endDayOfMonth);
                        saveExportTime(beginExportTime, endExportTime);
                        textView_exportTime.setText(beginExportTime + "至" + endExportTime);
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), true).show();
            }
        });

        btn_allRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                checkInInfo_list.clear();
                checkInInfo_list = mVerifyRecordDB.queryAllCheckIn();
                updateQueryResult(checkInInfo_list);
            }
        });

        btn_ruleRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                checkInInfo_list.clear();
                checkInInfo_list = mVerifyRecordDB.queryAllCheckIn(getStartWorkTime(), getEndWorkTime(), getWeekConfig());
                updateQueryResult(checkInInfo_list);
            }
        });

        btn_exportCurrentRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                ExportToCSV(checkInInfo_list,"record.csv");
            }
        });
        btn_exportDateRecord.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                String fileName=getStartExportTime()+"--"+getEndExportTime()+".csv";
                checkInInfo_list.clear();
                checkInInfo_list = mVerifyRecordDB.queryAllCheckIn(getStartExportTime(), getEndExportTime());
                updateQueryResult(checkInInfo_list);
                ExportToCSV(checkInInfo_list,fileName);
            }
        });

    }


    private void saveExportTime(String beginExportTime, String endExportTime) {
        SharedPreferencesUtil.saveData(Application.getContext(), Application.getCachePath(), "exporttime", "startexport", beginExportTime);
        SharedPreferencesUtil.saveData(Application.getContext(), Application.getCachePath(), "exporttime", "endexport", endExportTime);
    }


    private String getStartExportTime() {
        String startExportTime = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "exporttime", "startexport", "none");
        return startExportTime;
    }

    private String getEndExportTime() {
        String endExportTime = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "exporttime", "endexport", "none");
        return endExportTime;
    }


    private String getStartWorkTime() {
        String strStartWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "startwork", "none");
        return strStartWork;
    }

    private String getEndWorkTime() {
        String strEndWork = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "worktime", "endwork", "none");
        return strEndWork;
    }

    private HashMap<String, Boolean> getWeekConfig() {
        HashMap<String, Boolean> weeks = new HashMap<String, Boolean>();
        weeks.clear();
        String strJson = (String) SharedPreferencesUtil.getData(Application.getContext(),
                Application.getCachePath(), "workweek", "config", "none");
        if (!strJson.contains("none")) {
            Gson gson = new Gson();
            weeks = gson.fromJson(strJson, new TypeToken<HashMap<String, Boolean>>() {
            }.getType());
        }
        return weeks;
    }


    public void ExportToCSV(List<CheckInInfo> checkInInfo_list,String fileName) {
        FileWriter fw;
        BufferedWriter bfw;
        File sdCardDir=new File(Application.getCachePath());
        File saveFile = new File(sdCardDir, fileName);
        String[] contents = {"RECORDID", "姓名", "用户ID", "签到时间", "结果", "状态"};
        try {
            fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(fw);
            bfw.write(0xFEFF);
            for (int i = 0; i < contents.length; i++) {
                bfw.write(contents[i] + ",");
            }
            // 写好表头后换行
            bfw.newLine();
            // 写入数据
            for (CheckInInfo checkIninfo : checkInInfo_list) {
                // 写好每条记录后换行
                String recordID = String.valueOf(checkIninfo.getRecordId());
                String userName = checkIninfo.getName();
                String userID = String.valueOf(checkIninfo.getFaceId());
                String verifyTime = checkIninfo.getStrDateTime();
                String result = checkIninfo.getVerifyResult();
                String status = String.valueOf(checkIninfo.getReportstatus());
                String arrStr[] = {recordID == null ? "" : recordID,
                        "".equals(userName) ? "" : userName,
                        "".equals(userID) ? "" : userID,
                        "".equals(verifyTime) ? "" : verifyTime,
                        "".equals(result) ? "" : result,
                        status == null ? "" : status};
                for (int i = 0; i < arrStr.length; i++) {
                    if (i != arrStr.length - 1)
                        bfw.write(arrStr[i] + ",");
                    else
                        bfw.write(arrStr[i]);
                }
                bfw.newLine();
            }
            bfw.flush();
            bfw.close();
        } catch (IOException e) {
            Log.i(TAG, "write csv exception");
            e.printStackTrace();
        }
    }


    private void showWorkTimePickMenu(View v) {
        mWorkTimePickMenu = WorkTimePickMenu.getInstance();
        mWorkTimePickMenu.initialize(mContext, mWorkTimePickHandler);
        mWorkTimePickMenu.showPopupWindow(v);
    }

    private void showWorkWeekPickMenu(View v) {
        mWorkWeekPickMenu = WorkWeekPickMenu.getInstance();
        mWorkWeekPickMenu.initialize(mContext, mWorkWeekPickHandler);
        mWorkWeekPickMenu.showPopupWindow(v);
    }

    private void updateQueryResult(final List<CheckInInfo> checkInInfo_list) {
        if (mVerifyRecordAdapter == null) {
            mVerifyRecordAdapter = new VerifyRecordAdapter(this.getActivity(), checkInInfo_list);
            verifyRecordListView.setAdapter(mVerifyRecordAdapter);
        }
        mVerifyRecordAdapter.setList(checkInInfo_list);
        mVerifyRecordAdapter.notifyDataSetChanged(); // 刷新数据
        verifyRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });
    }


}
