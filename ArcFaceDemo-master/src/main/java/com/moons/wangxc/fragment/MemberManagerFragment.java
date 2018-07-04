package com.moons.wangxc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.sdk_demo.Application;
import com.arcsoft.sdk_demo.R;
import com.arcsoft.sdk_demo.RegisterActivity;
import com.moons.wangxc.UserFaceInfo;
import com.moons.wangxc.adapter.MemberFaceListAdapter;
import com.moons.wangxc.dialog.DeleteDialog;
import com.moons.wangxc.dialog.InsertDialog;
import com.moons.wangxc.dialog.UpdateDialog;
import com.moons.wangxc.popupmenu.TimePickMenu;
import com.moons.wangxc.service.BatchAddFaceService;
import com.moons.wangxc.sqliteDB.FaceListSQLiteHelper;
import com.moons.wangxc.util.FuncUtil;
import com.moons.wangxc.util.UriToPathUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;


public class MemberManagerFragment extends Fragment {
    private static final String TAG = "MemberManagerFragment";
    private FaceListSQLiteHelper mFaceListDB;
    private Context mContext;
    private MemberFaceListAdapter mMemberFaceListAdapter;
    private View mView;
    private ListView memberfacelist_listview;
    private List<UserFaceInfo> userfaceinfo_list = new ArrayList<UserFaceInfo>();
    private Button btn_importMemberNameList;
    private Button btn_importMemberFaceImage;
    private Button btn_add;
    private Button btn_delete;
    private Button btn_cameraImage;
    private Button btn_fileImage;
    private ImageView imageView_member;
    private CheckBox check_name;
    private EditText editText_name;
    private EditText editText_id;
    private EditText editText_status;
    private CheckBox check_id;
    private CheckBox check_status;
    private LinearLayout linearLayout_begin_time;
    private LinearLayout linearLayout_end_time;
    private CheckBox check_tiem_begin;
    private TextView time_text_begin;
    private CheckBox check_tiem_end;
    private TextView time_text_end;
    private Button btn_query;
    private Boolean isNameCheck;
    private Boolean isIdCheck;
    private Boolean isStatusCheck;
    private Boolean isBeginTimeCheck;
    private Boolean isEndTimeCheck;
    private long startTime;
    private long endTime;
    private int status;
    private TimePickMenu mTimePickMenu;
    private String beginDateStr;
    private String beginTimeStr;
    private String endDateStr;
    private String endTimeStr;

    private static final int INSERT_REQUESTCODE = 1;
    private static final int DELETE_REQUESTCODE = 2;
    private static final int UPDATE_REQUESTCODE = 3;
    private static final int INSERT_RESULTCODE = 1;
    private static final int DELETE_RESULTCODE = 2;
    private static final int UPDATE_RESULTCODE = 3;


    private static final int REQUEST_CODE_IMAGE_CAMERA = 4;
    private static final int REQUEST_CODE_IMAGE_OP = 5;
    private static final int REQUEST_CODE_OP = 6;
    private static final int REQUEST_CODE_CSV = 7;
    private static final int REQUEST_CODE_IMPORT_IMAGE = 8;


    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach...");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate...");
        mContext = this.getActivity();
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INSERT_REQUESTCODE:
                if (resultCode == INSERT_RESULTCODE) {
                    if (data != null) {
                        mFaceListDB.addMemberFace((UserFaceInfo) data.getSerializableExtra("userFaceInfo"));
                        showMessage("插入成功!");
                    } else {
                        showMessage("取消插入!");
                    }
                }
                break;
            case DELETE_REQUESTCODE:
                if (resultCode == DELETE_RESULTCODE) {
                    if (data != null) {
                        mFaceListDB.deleteMemberFace(data.getStringExtra("username"));
                        showMessage("删除成功!");
                    } else {
                        showMessage("取消操作!");
                    }
                }
                break;
            case UPDATE_REQUESTCODE:
                if (resultCode == UPDATE_RESULTCODE) {
                    if (data != null) {
                        mFaceListDB.updateMemberFace((UserFaceInfo) data.getSerializableExtra("userFaceInfo"));
                        showMessage("更新成功!");
                    } else {
                        showMessage("取消操作!");
                    }
                } else if (resultCode == DELETE_RESULTCODE) {
                    if (data != null) {
                        mFaceListDB.deleteMemberFace(data.getStringExtra("username"));
                        showMessage("删除成功!");
                    } else {
                        showMessage("取消操作!");
                    }
                }
                break;
            case REQUEST_CODE_IMAGE_OP:
                if (resultCode == RESULT_OK) {
                    Uri mPath = data.getData();
                    String file = UriToPathUtil.getImageAbsolutePath(mContext, mPath); //获取图片文件的路径
                    showMessage(file);
                    Bitmap bmp = Application.decodeImage(file);//解析路径下的图片文件，获取Bitmap
                    if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
                        Log.e(TAG, "error");
                    } else {
                        Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("imagepath", file);
                    Message message = new Message();
                    message.setData(bundle);
                    mImageHandler.sendMessage(message);
                    message.what = 0;
                    startRegister(bmp, file);
                }
                break;
            case REQUEST_CODE_IMAGE_CAMERA:
                if (resultCode == RESULT_OK) {
                    Uri mPath = ((Application) mContext.getApplicationContext()).getCaptureImage();
                    String file = UriToPathUtil.getImageAbsolutePath(mContext, mPath);
                    Bitmap bmp = Application.decodeImage(file); //解析路径下的图片文件，获取Bitmap
                    Bundle bundle = new Bundle();
                    bundle.putString("imagepath", file);
                    Message message = new Message();
                    message.setData(bundle);
                    mImageHandler.sendMessage(message);
                    message.what = 0;
                    startRegister(bmp, file);
                }
                break;
            case REQUEST_CODE_CSV:
                if (data != null) {
                    Uri uri = data.getData();
                    String file = UriToPathUtil.getImageAbsolutePath(mContext, uri);
                    if (checkCSVFile(file)) {
                        Toast.makeText(mContext, file, Toast.LENGTH_SHORT).show();
                        new ReadCSVThread(file).start();
                    }
                }
            case REQUEST_CODE_IMPORT_IMAGE:
                if (data != null) {
                    Uri uri = data.getData();
                    String file = UriToPathUtil.getImageAbsolutePath(mContext, uri);
                    if (!getImageLocationPath(file).equals("")) {
                        Toast.makeText(mContext, getImageLocationPath(file), Toast.LENGTH_SHORT).show();
                        new ImportMemberFaceThread(getImageLocationPath(file)).start();
                    }

                }

        }
    }


    public String getImageLocationPath(String file) {
        if (file.toString().endsWith("location.txt")) {
            String[] split = file.split("location.txt"); //不忽略空值
            if (split[0] != null) {
                return split[0];
            }
        }
        return "";
    }


    boolean checkCSVFile(String file) {
        if (file.toString().endsWith(".csv")) {
            return true;
        }
        return false;

    }

    public List<UserFaceInfo> parseCSVFile(String file) {
        Log.i(TAG, "to parse CSV");
        List<UserFaceInfo> datalist = new ArrayList<UserFaceInfo>();
        List<String> lists = new ArrayList<String>(); //行数
        if (datalist != null) {
            datalist.clear();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            int i = 0;
            while ((line = br.readLine()) != null) {//读行
                // 把一行数据分割成多个字段
                String[] split = line.split(",", -1); //不忽略空值
                Log.i(TAG, "size is" + split.length);
                for (String string : split) {
                    lists.add(string);
                }
                if (lists.size() > 13) {//从第二行开始读数据
                    ///username|sex|age|race|reserve1|reserve2|reserve3|reserve4|reserve5|faceimage|facefea|collectionDateTime|status
                    UserFaceInfo mUserFaceInfo = new UserFaceInfo();
                    mUserFaceInfo.setName(FuncUtil.isEmpty(lists.get(13 + i)) ? "" : lists.get(13 + i));
                    mUserFaceInfo.setSex(FuncUtil.isEmpty(lists.get(14 + i)) ? "" : lists.get(14 + i));
                    mUserFaceInfo.setAge(FuncUtil.isEmpty(lists.get(15 + i)) ? 0 : Integer.valueOf(lists.get(15 + i)));
                    mUserFaceInfo.setRace(FuncUtil.isEmpty(lists.get(16 + i)) ? "" : lists.get(16 + i));
                    mUserFaceInfo.setReserve1(FuncUtil.isEmpty(lists.get(17 + i)) ? "" : lists.get(17 + i));
                    mUserFaceInfo.setReserve2(FuncUtil.isEmpty(lists.get(18 + i)) ? "" : lists.get(18 + i));
                    mUserFaceInfo.setReserve3(FuncUtil.isEmpty(lists.get(19 + i)) ? "" : lists.get(19 + i));
                    mUserFaceInfo.setReserve4(FuncUtil.isEmpty(lists.get(20 + i)) ? "" : lists.get(20 + i));
                    mUserFaceInfo.setReserve5(FuncUtil.isEmpty(lists.get(21 + i)) ? "" : lists.get(21 + i));
                    mUserFaceInfo.setFaceImage_AbsPath(FuncUtil.isEmpty(lists.get(22 + i)) ? "" : lists.get(22 + i));
                    mUserFaceInfo.setFaceFea_AbsPath(FuncUtil.isEmpty(lists.get(23 + i)) ? "" : lists.get(23 + i));
                    mUserFaceInfo.setCollectionDateTime(FuncUtil.isEmpty(lists.get(24 + i)) ? 0L : Long.parseLong(lists.get(24 + i)));
                    mUserFaceInfo.setStatus(FuncUtil.isEmpty(lists.get(25 + i)) ? 1 : Integer.valueOf(lists.get(25 + i)));
                    datalist.add(mUserFaceInfo);
                    i = i + 13;
                }
            }
            return datalist;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return datalist;
    }


    private void startRegister(Bitmap mBitmap, String file) {
        Intent it = new Intent(mContext, RegisterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("imagePath", file);
        it.putExtras(bundle);
        startActivityForResult(it, REQUEST_CODE_OP);
    }


    private void showMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.member_manager_fragment, null, false);
        initView();
        initDB();
        initConfig();
        setEventListener();
        return mView;
    }

    private void initConfig() {
        isNameCheck = false;
        isIdCheck = false;
        isStatusCheck = false;
        isBeginTimeCheck = false;
        isEndTimeCheck = false;
        check_name.setChecked(false);
        check_id.setChecked(false);
        check_status.setChecked(false);
        check_tiem_begin.setChecked(false);
        check_tiem_end.setChecked(false);
        status = 1;
        startTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis();

        //获取日历的一个对象
        Calendar calendar = Calendar.getInstance();
        //获取年月日时分的信息
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        time_text_begin.setText(String.format("%d-%02d-%02d %02d-%02d",year,month,day,hour,minute));
        time_text_end.setText(String.format("%d-%02d-%02d %02d-%02d",year,month,day,hour,minute));
        Intent intent = new Intent(mContext, BatchAddFaceService.class);
        mContext.startService(intent);
    }


    class ImportMemberFaceThread extends Thread {
        String imagePath;
        Vector<String> namelist;

        public ImportMemberFaceThread(String imagePath) {
            this.imagePath = imagePath;
        }

        @Override
        public void run() {
            super.run();
            try {
                namelist = FuncUtil.GetImageFileName(imagePath);
                for (String name : namelist) {
                    BatchAddFaceService.BatchAddFace(imagePath, name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class ReadCSVThread extends Thread {
        String file;

        public ReadCSVThread(String file) {
            this.file = file;
        }

        @Override
        public void run() {
            super.run();
            try {
                List<UserFaceInfo> list = parseCSVFile(file);
                for (int i = 0; i < list.size(); i++) {
                    Log.i(TAG, "name is" + list.get(i).getName().toString());
                    Log.i(TAG, "age is " + list.get(i).getAge());
                    if (!mFaceListDB.isNameExist(list.get(i).getName())) {
                        mFaceListDB.addMemberFace(list.get(i));
                    } else {
                        mFaceListDB.updateMemberFaceByName_ExceptCollectionTimeAndStatus(list.get(i));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    Handler mImageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.i(TAG, "call image fun");
                    Bundle bd = msg.getData();
                    String imagePath = bd.getString("imagepath");
                    imageView_member.setImageBitmap(Application.decodeImage(imagePath));
                    break;
                default:
                    break;
            }
        }
    };


    Handler mBeginHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Bundle bd = msg.getData();
                    beginDateStr = bd.getString("dateStr");
                    beginTimeStr = bd.getString("timeStr");
                    String beginStr = beginDateStr + " " + beginTimeStr;
                    time_text_begin.setText(beginDateStr + " " + beginTimeStr);
                    try {
                        startTime = FuncUtil.stringToLong(beginStr, "yyyy-MM-dd HH:mm");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "startTime is" + startTime);
                    break;
                default:
                    break;
            }
        }
    };

    Handler mEndHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Bundle bd = msg.getData();
                    endDateStr = bd.getString("dateStr");
                    endTimeStr = bd.getString("timeStr");
                    time_text_end.setText(endDateStr + " " + endTimeStr);
                    String endStr = endDateStr + " " + endTimeStr;
                    try {
                        endTime = FuncUtil.stringToLong(endStr, "yyyy-MM-dd HH:mm");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener cb = new CompoundButton.OnCheckedChangeListener() { //实例化一个cb
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            switch (buttonView.getId()) {
                case R.id.check_name:
                    isNameCheck = isChecked;
                    break;
                case R.id.check_id:
                    isIdCheck = isChecked;
                    break;
                case R.id.check_status:
                    isStatusCheck = isChecked;
                    break;
                case R.id.check_time_begin:
                    isBeginTimeCheck = isChecked;
                    break;
                case R.id.check_time_end:
                    isEndTimeCheck = isChecked;
                default:
                    break;
            }

        }
    };

    private void setEventListener() {
        check_name.setOnCheckedChangeListener(cb);
        check_id.setOnCheckedChangeListener(cb);
        check_status.setOnCheckedChangeListener(cb);
        check_tiem_begin.setOnCheckedChangeListener(cb);
        check_tiem_end.setOnCheckedChangeListener(cb);

        editText_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "," + start + "," + count + "," + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "," + start + "," + before + "," + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "onTextChanged: " + s);
            }
        });


        editText_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "," + start + "," + count + "," + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "," + start + "," + before + "," + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "onTextChanged: " + s);
            }
        });

        editText_status.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: " + s + "," + start + "," + count + "," + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: " + s + "," + start + "," + before + "," + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "onTextChanged: " + s);
            }
        });


        btn_query.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                int userid = editText_id.getText().toString().equals("") ? 0 : Integer.parseInt(editText_id.getText().toString());
                int status = editText_status.getText().toString().equals("") ? 0 : Integer.parseInt(editText_status.getText().toString());
                userfaceinfo_list.clear();
                userfaceinfo_list = mFaceListDB.queryUserFaceInfoFree(editText_name.getText().toString(),
                        userid, status, startTime, endTime,
                        isNameCheck, isIdCheck, isStatusCheck, isBeginTimeCheck, isEndTimeCheck);
                updateQueryResult(userfaceinfo_list);
            }
        });

        linearLayout_begin_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showBeginTimePickMenu(v);
            }
        });

        linearLayout_end_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showEndTimePickMenu(v);
            }
        });


        btn_add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, InsertDialog.class);
                startActivityForResult(intent, INSERT_REQUESTCODE);
            }
        });


        btn_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DeleteDialog.class);
                startActivityForResult(intent, DELETE_RESULTCODE);
            }
        });

        btn_fileImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);//打开图片浏览intent
                getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
                getImageByalbum.setType("image/jpeg");
                startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
            }
        });

        btn_cameraImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                Uri uri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                ((Application) mContext.getApplicationContext()).setCaptureImage(uri);//存储在Application中的uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAMERA);
            }
        });
        btn_importMemberNameList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType(“image/*”);//选择图片
                //intent.setType(“audio/*”); //选择音频
                //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
                intent.setType("text/csv");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_CSV);
            }
        });

        btn_importMemberFaceImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType(“image/*”);//选择图片
                //intent.setType(“audio/*”); //选择音频
                //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
                //intent.setType(“video/*;image/*”);//同时选择视频和图片
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_IMPORT_IMAGE);
            }
        });


    }


    private void showBeginTimePickMenu(View v) {
        mTimePickMenu = TimePickMenu.getInstance();
        mTimePickMenu.initialize(mContext, mBeginHandler);
        mTimePickMenu.showPopupWindow(v);
    }

    private void showEndTimePickMenu(View v) {
        mTimePickMenu = TimePickMenu.getInstance();
        mTimePickMenu.initialize(mContext, mEndHandler);
        mTimePickMenu.showPopupWindow(v);
    }

    private void initDB() {
        mFaceListDB = Application.getFaceListDB();
        try {
            mFaceListDB.createDataBase();
//            mFaceListDB.dataInitTest();
        } catch (IOException e) {
            Log.i(TAG, "crteate facelistDB fail");
        }
    }

    private void initView() {
        memberfacelist_listview = (ListView) mView.findViewById(R.id.facelist_listView);
        btn_importMemberNameList = (Button) mView.findViewById(R.id.btn_importMemberNameList);
        btn_importMemberFaceImage = (Button) mView.findViewById(R.id.btn_importMemberFaceImage);
        btn_add = (Button) mView.findViewById(R.id.btn_add);
        btn_delete = (Button) mView.findViewById(R.id.btn_delete);
        btn_cameraImage = (Button) mView.findViewById(R.id.btn_cameraImage);
        btn_fileImage = (Button) mView.findViewById(R.id.btn_fileImage);
        imageView_member = (ImageView) mView.findViewById(R.id.imageView_member);
        check_name = (CheckBox) mView.findViewById(R.id.check_name);
        editText_name = (EditText) mView.findViewById(R.id.editText_name);
        editText_id = (EditText) mView.findViewById(R.id.editText_id);
        editText_status = (EditText) mView.findViewById(R.id.editText_status);
        check_id = (CheckBox) mView.findViewById(R.id.check_id);
        check_status = (CheckBox) mView.findViewById(R.id.check_status);
        linearLayout_begin_time=(LinearLayout)mView.findViewById(R.id.linearLayout_time_begin);
        linearLayout_end_time=(LinearLayout)mView.findViewById(R.id.linearLayout_time_end);
        check_tiem_begin = (CheckBox) mView.findViewById(R.id.check_time_begin);
        time_text_begin = (TextView) mView.findViewById(R.id.time_text_begin);
        check_tiem_end = (CheckBox) mView.findViewById(R.id.check_time_end);
        time_text_end = (TextView) mView.findViewById(R.id.time_text_end);
        btn_query = (Button) mView.findViewById(R.id.btn_query);
        time_text_begin = (TextView) mView.findViewById(R.id.time_text_begin);
        time_text_end = (TextView) mView.findViewById(R.id.time_text_end);
    }

    private void updateQueryResult(final List<UserFaceInfo> userfaceinfo_list) {
        if (mMemberFaceListAdapter == null) {
            mMemberFaceListAdapter = new MemberFaceListAdapter(this.getActivity(), userfaceinfo_list);
            memberfacelist_listview.setAdapter(mMemberFaceListAdapter);
        }
        mMemberFaceListAdapter.setList(userfaceinfo_list);
        mMemberFaceListAdapter.notifyDataSetChanged(); // 刷新数据
        memberfacelist_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(mContext, UpdateDialog.class);
                intent.putExtra("userfaceinfo", userfaceinfo_list.get(position));//对象必须实现序列化接口
                startActivityForResult(intent, UPDATE_REQUESTCODE);
            }
        });
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy...");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach...");
        super.onDetach();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause...");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop...");
        super.onStop();
    }

}
