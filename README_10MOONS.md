# 人脸识别考勤demo

>工程概述
  基于ArcFaceDemo修改的人脸识别考勤系统，appid、sdkkey和lib已申请并配置好，关于ArcFaceDemo更多细节，详见《README_ArcFaceDemo.md》。
  本工程作为人脸识别应用于实际的范例，更多详细介绍参见《阅人脸识别考勤系统介绍.pdf》

>工程如何使用?
1. 下载代码:
    git clone https://github.com/wangxcgg/FaceRecognition.git 或者直接下载压缩包

2. Android Studio3.0 中直接打开或者导入Project,编译运行即可。

> demo如何使用?    

> 点击第三个按钮，涵盖了人脸识别考勤系统的所有功能。

> demo中两个数据库的使用?
　1. 用户名单数据库:
     FaceListSQLiteHelper类封装了用户名单数据库操作细节，Application.getFaceListDB()获取程序初始化时new的FaceListSQLiteHelper实例，
     通过实例调用用户名单数据库的操作方法。
  2. 识别记录是数据库：
     VerifyRecordSQLiteHelper类封装了识别记录数据库，Application.getVerifyRecordDB()获取程序初始化时new的VerifyRecordSQLiteHelper实例，
     通过实例调用识别记录数据库的操作方法。

> 导入人员名单

　 人员名单数据格式csv,parseCSVFile方法根据表头有序读入数据。如需变更csv格式，就要同时修改parseCSVFile方法。

> 批量导入头像
  批量图片以姓名+后缀.jpg放入同一个文件夹，文件夹放入location.txt文件，在点击打开这个文件时即获取文件夹所在的路径。遍历路径下的文件，批量导入姓名关联的
  人脸图片。BatchAddFaceService.BatchAddFace完成所有批量导入图片所要做的后续工作。

> 导出打卡记录
  导出当前打卡记录，可选全部记录和规则内打卡记录，文件名为record.csv。
  导出日期打卡记录，按日期段导出记录，文件名为日期段组成的字符串.csv，如2018-06-01--2018-06-30.csv。

> 工程的所有数据库、人脸图片、打卡记录、配置等文件所在目录
  以上所有文件均保存在外置SD卡对应包名cache目录下，如/storage/emulated/0/android/data/com.arcsoft.sdk_demo/cache
