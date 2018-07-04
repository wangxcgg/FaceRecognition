package com.arcsoft.sdk_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import com.moons.wangxc.sqliteDB.DatabaseContext;
import com.moons.wangxc.sqliteDB.FaceListSQLiteHelper;
import com.moons.wangxc.sqliteDB.VerifyRecordSQLiteHelper;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class Application extends android.app.Application {
    private final String TAG = this.getClass().toString();
    public static FaceDB mFaceDB; //人脸库
    Uri mImage; //统一资源标识符
    private static Context mContext = null;
    private static FaceListSQLiteHelper mFaceListDB;
    private static VerifyRecordSQLiteHelper mVerifyRecordDB;
    private static String imagePath;
    private static String cachePath;
    @Override
    public void onCreate() {
        super.onCreate();
        mFaceDB = new FaceDB(this.getExternalCacheDir().getPath());//storage/emulated/0/android/data/com.arcsoft.sdk_demo/cache目录下
        mImage = null;
        mContext = getApplicationContext();
        initDB();
    }

    private void initDB() {
        imagePath = this.getExternalCacheDir().getPath() + "/" + "image/";
        cachePath=this.getExternalCacheDir().getPath()+"/";
        DatabaseContext dbContext = new DatabaseContext(getApplicationContext()); //自定义database context更改路径
        mFaceListDB = new FaceListSQLiteHelper(dbContext, "facelist.db", null, 1);
        mVerifyRecordDB = new VerifyRecordSQLiteHelper(dbContext, "verifyRecord.db", null, 1);
    }


    public static String getCachePath() {
        return cachePath;
    }

    public static String getImagePath() {
        return imagePath;
    }

    public static FaceListSQLiteHelper getFaceListDB() {
        return mFaceListDB;
    }

    public static VerifyRecordSQLiteHelper getVerifyRecordDB() {
        return mVerifyRecordDB;
    }

    public static FaceDB getFaceDB() {
        return mFaceDB;
    }

    public static Context getContext() {
        return mContext;
    }

    public void setCaptureImage(Uri uri) {
        mImage = uri;
    }

    public Uri getCaptureImage() {
        return mImage;
    }

    /**
     * @param path
     * @return
     */
    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
