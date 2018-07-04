package com.moons.wangxc.sqliteDB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.arcsoft.sdk_demo.Application;
import com.moons.wangxc.UserFaceInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class FaceListSQLiteHelper extends SQLiteOpenHelper {
    private String TAG = this.getClass().getSimpleName().toString();
    private static String DB_PATH = Application.getContext().getExternalCacheDir().getPath(); //数据库默认保存路径
    private String DB_NAME;
    //storage/emulated/0/android/data/com.arcsoft.sdk_demo/cache目录下
    private String DB_PATH_BACKUP = Application.getContext().getExternalCacheDir().getPath(); //数据库备份路径
    private Context mContext;

    public static final String CREATE_FACELIST = "create table facelist(" +
            "  userid integer primary key Autoincrement," +
            "  username char(50) not null," +
            "  sex char(20)," +
            "  age integer," +
            "  race char(64)," +
            "  reserve1 char(50)," +
            "  reserve2 char(50)," +
            "  reserve3 char(50)," +
            "  reserve4 char(50)," +
            "  reserve5 char(50)," +
            "  faceimage char(64)," +
            "  facefea char(64)," +
            "  collectionDateTime  char(64)," +
            "  status integer not null" +
            ");";

    //第一个参数context,第二个参数数据库名,第三个参数一般传入null,第四个参数数据库版本号
    public FaceListSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
        this.DB_NAME = name;
        Log.i(TAG, "--FaceListSQLiteHelper--");
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
            Log.i(TAG, "backup facelistDB isn't exist");
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


    public void addMemberFace(UserFaceInfo userFaceInfo) {
        Log.e(TAG, "--addMemberFace--");
        SQLiteDatabase db = getWritableDatabase(); //以读写的形式打开数据库
        db.execSQL("insert into facelist(userid,username,sex,age,race,reserve1,reserve2,reserve3,"
                + "reserve4,reserve5,faceimage,facefea,collectionDateTime,status) values("
                + null + ","
                + String.format("'%s'", userFaceInfo.getName()) + ","
                + String.format("'%s'", userFaceInfo.getSex()) + ","
                + userFaceInfo.getAge() + ","
                + String.format("'%s'", userFaceInfo.getRace()) + ","
                + String.format("'%s'", userFaceInfo.getReserve1()) + ","
                + String.format("'%s'", userFaceInfo.getReserve2()) + ","
                + String.format("'%s'", userFaceInfo.getReserve3()) + ","
                + String.format("'%s'", userFaceInfo.getReserve4()) + ","
                + String.format("'%s'", userFaceInfo.getReserve5()) + ","
                + String.format("'%s'", userFaceInfo.getFaceImage_AbsPath()) + ","
                + String.format("'%s'", userFaceInfo.getFaceFea_AbsPath()) + ","
                + String.format("'%s'", userFaceInfo.getCollectionDateTime()) + ","
                + userFaceInfo.getStatus() +
                ");"
        ); // 插入数据库
        db.close(); // 关闭数据库连接
    }


    public void deleteMemberFace(String username) {
        Log.e(TAG, "--deleteMemberFace--");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "username = ?";
        String wheres[] = {String.valueOf(username)};
        db.delete("facelist", sql, wheres); // 数据库删除
        db.close(); // 关闭数据库
    }

    public void deleteMemberFace(int userid) {
        Log.e(TAG, "--deleteMemberFace--");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "userid = ?";
        String wheres[] = {String.valueOf(userid)};
        db.delete("facelist", sql, wheres); // 数据库删除
        db.close(); // 关闭数据库
    }



    //更新基本信息(除了图像采集时间和状态）
    public void updateMemberFaceByName_ExceptCollectionTimeAndStatus(UserFaceInfo userFaceInfo) {
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "update facelist set sex="
                + String.format("'%s'", userFaceInfo.getSex())
                + ",age=" + userFaceInfo.getAge()
                + ",race=" + String.format("'%s'", userFaceInfo.getRace())
                + ",reserve1=" + String.format("'%s'", userFaceInfo.getReserve1())
                + ",reserve2=" + String.format("'%s'", userFaceInfo.getReserve2())
                + ",reserve3=" + String.format("'%s'", userFaceInfo.getReserve3())
                + ",reserve4=" + String.format("'%s'", userFaceInfo.getReserve4())
                + ",reserve5=" + String.format("'%s'", userFaceInfo.getReserve5())
                + ",faceimage=" + String.format("'%s'", userFaceInfo.getFaceImage_AbsPath())
                + ",facefea=" + String.format("'%s'", userFaceInfo.getFaceFea_AbsPath())
                + " where username=" + String.format("'%s'", userFaceInfo.getName());
        db.execSQL(sql); // 更新数据库
        db.close(); // 关闭数据库连接
    }

    //更新图像采集时间和状态
    public void updateMemberFaceByName_CollectionTimeAndStatus(UserFaceInfo userFaceInfo) {
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "update facelist set collectionDateTime="
                + String.format("'%s'", userFaceInfo.getCollectionDateTime())
                + ",status=" + userFaceInfo.getStatus()
                + " where username=" + String.format("'%s'", userFaceInfo.getName());
        db.execSQL(sql); // 更新数据库
        db.close(); // 关闭数据库连接
    }


    public void updateMemberFace(UserFaceInfo userFaceInfo) {
        Log.e(TAG, "--updateMemberFace--");
        SQLiteDatabase db = getWritableDatabase(); // 以读写的形式打开数据库
        String sql = "update facelist set username="
                + String.format("'%s'", userFaceInfo.getName())
                + ",sex=" + String.format("'%s'", userFaceInfo.getSex())
                + ",age=" + userFaceInfo.getAge()
                + ",race=" + String.format("'%s'", userFaceInfo.getRace())
                + ",reserve1=" + String.format("'%s'", userFaceInfo.getReserve1())
                + ",reserve2=" + String.format("'%s'", userFaceInfo.getReserve2())
                + ",reserve3=" + String.format("'%s'", userFaceInfo.getReserve3())
                + ",reserve4=" + String.format("'%s'", userFaceInfo.getReserve4())
                + ",reserve5=" + String.format("'%s'", userFaceInfo.getReserve5())
                + ",faceimage=" + String.format("'%s'", userFaceInfo.getFaceImage_AbsPath())
                + ",facefea=" + String.format("'%s'", userFaceInfo.getFaceFea_AbsPath())
                + ",collectionDateTime=" + String.format("'%s'", userFaceInfo.getCollectionDateTime())
                + ",status=" + userFaceInfo.getStatus()
                + " where userid=" + userFaceInfo.getUserId();
        db.execSQL(sql); // 更新数据库
        db.close(); // 关闭数据库连接
    }


    public List<UserFaceInfo> queryAllPerson() {
        List<UserFaceInfo> list = new ArrayList<UserFaceInfo>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读的方式打开数据库
        String sql = "select * from facelist;";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            UserFaceInfo userFaceInfo = new UserFaceInfo();
            userFaceInfo.setUserId(cursor.getInt(cursor.getColumnIndex("userid")));
            userFaceInfo.setName(cursor.getString(cursor.getColumnIndex("username")));
            userFaceInfo.setSex(cursor.getString(cursor.getColumnIndex("sex")));
            userFaceInfo.setAge(cursor.getInt(cursor.getColumnIndex("age")));
            userFaceInfo.setRace(cursor.getString(cursor.getColumnIndex("race")));
            userFaceInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            userFaceInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            userFaceInfo.setReserve3(cursor.getString(cursor.getColumnIndex("reserve3")));
            userFaceInfo.setReserve4(cursor.getString(cursor.getColumnIndex("reserve4")));
            userFaceInfo.setReserve5(cursor.getString(cursor.getColumnIndex("reserve5")));
            userFaceInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            userFaceInfo.setFaceFea_AbsPath(cursor.getString(cursor.getColumnIndex("facefea")));
            userFaceInfo.setCollectionDateTime(cursor.getLong(cursor.getColumnIndex("collectionDateTime")));
            userFaceInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            list.add(userFaceInfo); // 添加到数组
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库
        return list;
    }


    public Boolean isNameExist(String username) {
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
        String[] columns = {"userid", "username", "sex", "age", "race", "reserve1", "reserve2", "reserve3",
                "reserve4", "reserve5", "faceimage", "facefea", "collectionDateTime", "status"}; //你想要的数据
        String selection = "username=?";  //条件字段
        String[] selectionArgs = {username}; //具体的条件,对应条件字段
        Cursor cursor = db.query("facelist", columns, selection, selectionArgs,
                null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }


    public Boolean isIdExist(int userid) {
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
        String[] columns = {"userid", "username", "sex", "age", "race", "reserve1", "reserve2", "reserve3",
                "reserve4", "reserve5", "faceimage", "facefea", "collectionDateTime", "status"}; //你想要的数据
        String selection = "userid=?";  //条件字段
        String[] selectionArgs = {String.valueOf(userid)}; //具体的条件,对应条件字段
        Cursor cursor = db.query("facelist", columns, selection, selectionArgs,
                null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }

    public UserFaceInfo queryUserFaceInfoById(int userid) {
        UserFaceInfo userFaceInfo = null;
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
        String[] columns = {"userid", "username", "sex", "age", "race", "reserve1", "reserve2", "reserve3",
                "reserve4", "reserve5", "faceimage", "facefea", "collectionDateTime", "status"}; //你想要的数据
        String selection = "userid=?";  //条件字段
        String[] selectionArgs = {String.valueOf(userid)}; //具体的条件,对应条件字段
        Cursor cursor = db.query("facelist", columns, selection, selectionArgs,
                null, null, null);
        if (cursor.moveToNext()) {
            userFaceInfo = new UserFaceInfo();
            userFaceInfo.setUserId(cursor.getInt(cursor.getColumnIndex("userid")));
            userFaceInfo.setName(cursor.getString(cursor.getColumnIndex("username")));
            userFaceInfo.setSex(cursor.getString(cursor.getColumnIndex("sex")));
            userFaceInfo.setAge(cursor.getInt(cursor.getColumnIndex("age")));
            userFaceInfo.setRace(cursor.getString(cursor.getColumnIndex("race")));
            userFaceInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            userFaceInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            userFaceInfo.setReserve3(cursor.getString(cursor.getColumnIndex("reserve3")));
            userFaceInfo.setReserve4(cursor.getString(cursor.getColumnIndex("reserve4")));
            userFaceInfo.setReserve5(cursor.getString(cursor.getColumnIndex("reserve5")));
            userFaceInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            userFaceInfo.setFaceFea_AbsPath(cursor.getString(cursor.getColumnIndex("facefea")));
            userFaceInfo.setCollectionDateTime(cursor.getLong(cursor.getColumnIndex("collectionDateTime")));
            userFaceInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        }
        return userFaceInfo;
    }


    public List<UserFaceInfo> queryUserFaceInfoFree(String username, int userid, int status, Long startTime, Long endTime, boolean isNameCheck,
                                                    boolean isIDCheck, boolean isStatusCheck, boolean isBeginTimeCheck, boolean isEndTimeCheck) {
        List<UserFaceInfo> list = new ArrayList<UserFaceInfo>();
        SQLiteDatabase db = getReadableDatabase(); // 以只读方式打开数据库
        String[] columns = {"userid", "username", "sex", "age", "race", "reserve1", "reserve2", "reserve3",
                "reserve4", "reserve5", "faceimage", "facefea", "collectionDateTime", "status"}; //你想要的数据
        String selection = "";//条件字段
        List<String> strList = new ArrayList<String>();
        strList.clear();
        boolean start_Flag = false;
        if (isNameCheck) {
            selection = selection + "username=?";
            start_Flag = true;
            strList.add(username);
        }
        if (isIDCheck) {
            if (start_Flag) {
                selection = selection + "and userid=?";
            } else {
                selection = selection + "userid=?";
                start_Flag = true;
            }
            strList.add(String.valueOf(userid));
        }

        if (isStatusCheck) {
            if (start_Flag) {
                selection = selection + "and status=?";
            } else {
                selection = selection + "status=?";
                start_Flag = true;
            }
            strList.add(String.valueOf(status));
        }

        if (isBeginTimeCheck) {
            if (start_Flag) {
                selection = selection + "and collectionDateTime >=?";
            } else {
                selection = selection + "collectionDateTime >=?";
                start_Flag = true;
            }
            strList.add(String.valueOf(startTime));
        }

        if (isEndTimeCheck) {
            if (start_Flag) {
                selection = selection + "and collectionDateTime <=?";
            } else {
                selection = selection + "collectionDateTime <=?";
                start_Flag = true;
            }
            strList.add(String.valueOf(endTime));
        }

        String[] selectionArgs = strList.toArray(new String[strList.size()]); //具体的条件,对应条件字段
        Log.i(TAG, "selection is " + selection);
        for (int i = 0; i < selectionArgs.length; i++) {
            Log.i(TAG, "selection data is " + selectionArgs[i]);
        }

        if (selection.equals("")) {
            return list;
        }
        Cursor cursor = db.query("facelist", columns, selection, selectionArgs,
                null, null, null);
        while (cursor.moveToNext()) {
            UserFaceInfo userFaceInfo = new UserFaceInfo();
            userFaceInfo.setUserId(cursor.getInt(cursor.getColumnIndex("userid")));
            userFaceInfo.setName(cursor.getString(cursor.getColumnIndex("username")));
            userFaceInfo.setSex(cursor.getString(cursor.getColumnIndex("sex")));
            userFaceInfo.setAge(cursor.getInt(cursor.getColumnIndex("age")));
            userFaceInfo.setRace(cursor.getString(cursor.getColumnIndex("race")));
            userFaceInfo.setReserve1(cursor.getString(cursor.getColumnIndex("reserve1")));
            userFaceInfo.setReserve2(cursor.getString(cursor.getColumnIndex("reserve2")));
            userFaceInfo.setReserve3(cursor.getString(cursor.getColumnIndex("reserve3")));
            userFaceInfo.setReserve4(cursor.getString(cursor.getColumnIndex("reserve4")));
            userFaceInfo.setReserve5(cursor.getString(cursor.getColumnIndex("reserve5")));
            userFaceInfo.setFaceImage_AbsPath(cursor.getString(cursor.getColumnIndex("faceimage")));
            userFaceInfo.setFaceFea_AbsPath(cursor.getString(cursor.getColumnIndex("facefea")));
            userFaceInfo.setCollectionDateTime(cursor.getLong(cursor.getColumnIndex("collectionDateTime")));
            userFaceInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
            list.add(userFaceInfo); // 添加到数组
        }
        return list;
    }


}