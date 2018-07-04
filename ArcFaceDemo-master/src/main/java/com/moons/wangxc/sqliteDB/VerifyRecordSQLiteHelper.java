package com.moons.wangxc.sqliteDB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.arcsoft.sdk_demo.Application;
import com.moons.wangxc.CheckInInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class VerifyRecordSQLiteHelper extends SQLiteOpenHelper {
    private String TAG = this.getClass().getSimpleName().toString();
    private static String DB_PATH = Application.getContext().getExternalCacheDir().getPath(); //数据库默认保存路径
    private String DB_NAME;
    //storage/emulated/0/android/data/com.arcsoft.sdk_demo/cache目录下
    private String DB_PATH_BACKUP = Application.getContext().getExternalCacheDir().getPath(); //数据库备份路径
    private Context mContext;

    public static final String CREATE_FACELIST = "create table verifyrecord(" +
            "  recordID integer primary key Autoincrement," +
            "  verifyResult char(20)," +
            "  name char(50)," +
            "  faceid char(64)," +
            "  reserve1 char(50)," +
            "  reserve2 char(50)," +
            "  faceimage char(64)," +
            "  verifyDateTime  char(64)," +
            "  strDateTime char(64)," +
            "  reportstatus integer not null" +
            ");";

    //第一个参数context,第二个参数数据库名,第三个参数一般传入null,第四个参数数据库版本号
    public VerifyRecordSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        this.DB_NAME = name;
        Log.i(TAG, "--VerifyRecordSQLiteHelper--");
    }


    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (dbExist) {
            //数据库已存在
        } else {
            this.getReadableDatabase();//获取一个用于操作数据库的SQLiteDatabase实例
            try {
                copyDataBase();//拷贝备份的数据库
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //数据库不存在
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }


    private void copyDataBase() throws IOException {
        File file = new File(DB_PATH_BACKUP + "/" + DB_NAME); //备份数据库文件
        if (!file.exists()) {
            Log.i(TAG, "backup verifyrecordDB isn't exist");
            return;
        }
        InputStream myInput = new FileInputStream(file);
        String outFileName = DB_PATH + "/" + DB_NAME; //默认数据库文件
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }


    // 创建数据库
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e(TAG, "--create datebasse--");
        String sql = CREATE_FACELIST; //创建表;
        db.execSQL(sql);
    }

    // 数据库更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "--upgrade datebasse--");
    }


    public void addCheckIn(CheckInInfo checkInInfo) {
        Log.e(TAG, "--addCheckIn--");
        SQLiteDatabase db = getWritableDatabase(); //以读写的形式打开数据库
        db.execSQL("insert into verifyrecord(recordID,verifyResult,name,faceid,reserve1,reserve2," +
                "faceimage,verifyDateTime,strDateTime,reportstatus) values("
                + null + ","
                + String.format("'%s'", checkInInfo.getVerifyResult()) + ","
                + String.format("'%s'", checkInInfo.getName()) + ","
                + null + ","
                + String.format("'%s'", checkInInfo.getReserve1()) + ","
                + String.format("'%s'", checkInInfo.getReserve2()) + ","
                + String.format("'%s'", checkInInfo.getFaceImage_AbsPath()) + ","
                + String.format("'%s'", checkInInfo.getVerifyDateTime()) + ","
                + String.format("'%s'", checkInInfo.getStrDateTime()) + ","
                + checkInInfo.getReportstatus() +
                ");"
        ); // 插入数据库
        db.close(); // 关闭数据库连接
    }


    private long getTimeMillis(String strTime) {
        long returnMillis = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date d = null;
        try {
            d = sdf.parse(strTime);
            returnMillis = d.getTime();
        } catch (ParseException e) {
        }
        return returnMillis;
    }

    private String getTimeStartString(String endTime, String expendTime) {
        //传入字串类型 end:2016/06/28 08:30 expend: 03:00
        endTime = "2018-03-23" + " " + endTime;
        long longEnd = getTimeMillis(endTime);
        String[] expendTimes = expendTime.split(":");   //截取出小时数和分钟数
        long longExpend = Long.parseLong(expendTimes[0]) * 60 * 60 * 1000 + Long.parseLong(expendTimes[1]) * 60 * 1000;
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        return sdfTime.format(new Date(longEnd - longExpend));
    }

    private String getTimeEndString(String startTime, String expendTime) {
        //传入字串类型 end:2016/06/28 08:30 expend: 03:OO
        startTime = "2018-03-23" + " " + startTime;
        long longStart = getTimeMillis(startTime);
        String[] expendTimes = expendTime.split(":");   //截取出小时数和分钟数
        long longExpend = Long.parseLong(expendTimes[0]) * 60 * 60 * 1000 + Long.parseLong(expendTimes[1]) * 60 * 1000;
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
        return sdfTime.format(new Date(longStart + longExpend));
    }

    public List<CheckInInfo> queryAllCheckIn() {
        List<CheckInInfo> list = new ArrayList<CheckInInfo>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String sql = "select * from verifyrecord;";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            CheckInInfo checkInInfo = new CheckInInfo();
            checkInInfo.setRecordId(cursor.getInt(cursor.getColumnIndex("recordID")));
            checkInInfo.setVerifyResult(cursor.getString(cursor.getColumnIndex("verifyResult")));
            checkInInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
            checkInInfo.setFaceId(cursor.getInt(cursor.getColumnIndex("faceid")));
            checkInInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            checkInInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            checkInInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            checkInInfo.setVerifyDateTime(cursor.getLong(cursor.getColumnIndex("verifyDateTime")));
            checkInInfo.setStrDateTime(cursor.getString(cursor.getColumnIndex("strDateTime")));
            checkInInfo.setReportStatus(cursor.getInt(cursor.getColumnIndex("reportstatus")));
            list.add(checkInInfo); // 添加到数组
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        return list;
    }

    public List<CheckInInfo> queryAllCheckIn(String startWorkTime, String endWorkTime, HashMap<String, Boolean> weeks) {
        List<CheckInInfo> list = new ArrayList<CheckInInfo>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        //设置第一次最早打卡时间和第二次最晚打卡时间
        String firstStartTime = getTimeStartString(startWorkTime, "03:00");
        String secondEndTime = getTimeEndString(endWorkTime, "03:00");
        Log.i(TAG, "firstStartTime" + firstStartTime + "secondEndTime" + secondEndTime);
        String strSql = "select * from verifyrecord where ";
        String str1 = "strftime('%H:%M',strDateTime) between time('" + firstStartTime + "') and time('" +
                startWorkTime + "')";
        String str2 = " or strftime('%H:%M',strDateTime) between time('" + endWorkTime + "') and time('" +
                secondEndTime + "')";
        String str3 = "(" + str1 + str2 + ")";
        String str4 = " and(";
        boolean startflag = false;
        if (weeks.get("mon")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='1'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='1'";
                startflag = true;
            }
        }
        if (weeks.get("tues")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='2'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='2'";
                startflag = true;
            }
        }
        if (weeks.get("wed")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='3'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='3'";
                startflag = true;
            }
        }
        if (weeks.get("thur")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='4'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='4'";
                startflag = true;
            }
        }
        if (weeks.get("fri")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='5'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='5'";
                startflag = true;
            }
        }
        if (weeks.get("sat")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='6'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='6'";
                startflag = true;
            }
        }
        if (weeks.get("sun")) {
            if (startflag) {
                str4 = str4 + " or strftime('%w',strDateTime)='7'";
            } else {
                str4 = str4 + " strftime('%w',strDateTime)='7'";
                startflag = true;
            }
        }
        str4 = str4 + ");";

        strSql = strSql + str3 + str4;

        if (!startflag) {
            return list;
        }
        Log.i(TAG, "strSql is " + strSql);
        Cursor cursor = db.rawQuery(strSql, null);
        while (cursor.moveToNext()) {
            CheckInInfo checkInInfo = new CheckInInfo();
            checkInInfo.setRecordId(cursor.getInt(cursor.getColumnIndex("recordID")));
            checkInInfo.setVerifyResult(cursor.getString(cursor.getColumnIndex("verifyResult")));
            checkInInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
            checkInInfo.setFaceId(cursor.getInt(cursor.getColumnIndex("faceid")));
            checkInInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            checkInInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            checkInInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            checkInInfo.setVerifyDateTime(cursor.getLong(cursor.getColumnIndex("verifyDateTime")));
            checkInInfo.setStrDateTime(cursor.getString(cursor.getColumnIndex("strDateTime")));
            checkInInfo.setReportStatus(cursor.getInt(cursor.getColumnIndex("reportstatus")));
            list.add(checkInInfo); // 添加到数组
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        Log.i(TAG, "list size is" + list.size());
        return list;
    }


    public List<CheckInInfo> queryAllCheckIn(String startExportTime, String endExportTime) {

        List<CheckInInfo> list = new ArrayList<CheckInInfo>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String strSql="select * from verifyrecord where ";
        String str1="strDateTime >='"+startExportTime+"'";
        String str2="and strDateTime <= date('"+endExportTime+"','+1 day')";
        strSql=strSql+str1+str2;
        Log.i(TAG, "select str is" + strSql);
        Cursor cursor = db.rawQuery(strSql, null);
        while (cursor.moveToNext()) {
            CheckInInfo checkInInfo = new CheckInInfo();
            checkInInfo.setRecordId(cursor.getInt(cursor.getColumnIndex("recordID")));
            checkInInfo.setVerifyResult(cursor.getString(cursor.getColumnIndex("verifyResult")));
            checkInInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
            checkInInfo.setFaceId(cursor.getInt(cursor.getColumnIndex("faceid")));
            checkInInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            checkInInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            checkInInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            checkInInfo.setVerifyDateTime(cursor.getLong(cursor.getColumnIndex("verifyDateTime")));
            checkInInfo.setStrDateTime(cursor.getString(cursor.getColumnIndex("strDateTime")));
            checkInInfo.setReportStatus(cursor.getInt(cursor.getColumnIndex("reportstatus")));
            list.add(checkInInfo); // 添加到数组
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        Log.i(TAG, "list size is" + list.size());
        return list;

    }











}













