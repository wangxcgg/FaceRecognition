package com.moons.wangxc;

import java.io.Serializable;


public class UserFaceInfo implements Serializable {
    //序列化版本号
    private static final long serialVersionUID = 1L;

    private int userId; //用户ID
    private String name; //用户姓名
    private String sex; //性别
    private int age; //年龄
    private String race; //人种
    private String reserve1; //保留字段1
    private String reserve2; //保留字段2
    private String reserve3; //保留字段3
    private String reserve4; //保留字段4
    private String reserve5; //保留字段5
    private String faceImage_AbsPath; //人脸图片路径
    private String faceFea_AbsPath; //人脸信息存储路径
    private long collectionDateTime; //上传时间
    private int status; //状态（0：已采集:,1：未采集）


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        if (sex == null) return "";
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getRace() {
        if (race == null) return "";
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getReserve1() {
        if (reserve1 == null) return "";
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1;
    }

    public String getReserve2() {
        if (reserve2 == null) return "";
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2;
    }

    public String getReserve3() {

        if (reserve3 == null) return "";
        return reserve3;
    }

    public void setReserve3(String reserve3) {
        this.reserve3 = reserve3;
    }

    public String getReserve4() {

        if (reserve4 == null) return "";
        return reserve4;
    }

    public void setReserve4(String reserve4) {
        this.reserve4 = reserve4;
    }

    public String getReserve5() {
        if (reserve5 == null) return "";
        return reserve5;
    }

    public void setReserve5(String reserve5) {
        this.reserve5 = reserve5;
    }

    public String getFaceImage_AbsPath() {

        if (faceImage_AbsPath == null) return "";
        return faceImage_AbsPath;
    }

    public void setFaceImage_AbsPath(String faceImage_absPath) {
        this.faceImage_AbsPath =faceImage_absPath;
    }

    public String getFaceFea_AbsPath() {

        if (faceFea_AbsPath == null) return "";
        return faceFea_AbsPath;
    }

    public void setFaceFea_AbsPath(String faceFea_AbsPath) {
        this.faceFea_AbsPath = faceFea_AbsPath;
    }

    public long getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(long collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class CheckInInfo implements Serializable {
        //序列化版本号
        private static final long serialVersionUID = 1L;

        private int userId; //用户ID
        private String name; //用户姓名
        private String sex; //性别
        private int age; //年龄
        private String race; //人种
        private String reserve1; //保留字段1
        private String reserve2; //保留字段2
        private String reserve3; //保留字段3
        private String reserve4; //保留字段4
        private String reserve5; //保留字段5
        private String faceImage_AbsPath; //人脸图片路径
        private String faceFea_AbsPath; //人脸信息存储路径
        private long collectionDateTime; //上传时间
        private int status; //状态（0：已采集:,1：未采集）


        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            if (sex == null) return "";
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getRace() {
            if (race == null) return "";
            return race;
        }

        public void setRace(String race) {
            this.race = race;
        }

        public String getReserve1() {
            if (reserve1 == null) return "";
            return reserve1;
        }

        public void setReserve1(String reserve1) {
            this.reserve1 = reserve1;
        }

        public String getReserve2() {
            if (reserve2 == null) return "";
            return reserve2;
        }

        public void setReserve2(String reserve2) {
            this.reserve2 = reserve2;
        }

        public String getReserve3() {

            if (reserve3 == null) return "";
            return reserve3;
        }

        public void setReserve3(String reserve3) {
            this.reserve3 = reserve3;
        }

        public String getReserve4() {

            if (reserve4 == null) return "";
            return reserve4;
        }

        public void setReserve4(String reserve4) {
            this.reserve4 = reserve4;
        }

        public String getReserve5() {
            if (reserve5 == null) return "";
            return reserve5;
        }

        public void setReserve5(String reserve5) {
            this.reserve5 = reserve5;
        }

        public String getFaceImage_AbsPath() {

            if (faceImage_AbsPath == null) return "";
            return faceImage_AbsPath;
        }

        public void setFaceImage_AbsPath(String faceImage_absPath) {
            this.faceImage_AbsPath =faceImage_absPath;
        }

        public String getFaceFea_AbsPath() {

            if (faceFea_AbsPath == null) return "";
            return faceFea_AbsPath;
        }

        public void setFaceFea_AbsPath(String faceFea_AbsPath) {
            this.faceFea_AbsPath = faceFea_AbsPath;
        }

        public long getCollectionDateTime() {
            return collectionDateTime;
        }

        public void setCollectionDateTime(long collectionDateTime) {
            this.collectionDateTime = collectionDateTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

    }
}
