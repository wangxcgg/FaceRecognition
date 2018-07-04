package com.moons.wangxc;

import java.io.Serializable;

public class CheckInInfo implements Serializable {
    //序列化版本号
    private static final long serialVersionUID = 2L;

    private int recordId; //每条记录的ID
    private String verifyResult; //识别记录结果(sucess,fail)
    private String name; //姓名
    private int faceId; //例如员工ID
    private String reserve1; //保留字段1
    private String reserve2; //保留字段2
    private String faceImage_AbsPath; //人脸图片路径
    private long verifyDateTime; //签到时间long型
    private String strDateTime; //签到时间字符串型
    private int reportStatus; //状态（0：已上报,1:未上报）


    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFaceId() {
        return faceId;
    }

    public void setFaceId(int faceId) {
        this.faceId = faceId;
    }

    public String getVerifyResult() {
        if (verifyResult == null) {
            return "";
        }
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult) {
        this.verifyResult = verifyResult;
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


    public String getFaceImage_AbsPath() {

        if (faceImage_AbsPath == null) return "";
        return faceImage_AbsPath;
    }

    public void setFaceImage_AbsPath(String faceImage_absPath) {
        this.faceImage_AbsPath = faceImage_absPath;
    }


    public long getVerifyDateTime() {
        return verifyDateTime;
    }

    public void setVerifyDateTime(long verifyDateTime) {
        this.verifyDateTime = verifyDateTime;
    }


    public String getStrDateTime() {
        if (strDateTime == null) return "";
        return strDateTime;
    }

    public void setStrDateTime(String strDateTime) {
        this.strDateTime= strDateTime;
    }

    public int getReportstatus() {
        return reportStatus;
    }

    public void setReportStatus(int reportStatus) {
        this.reportStatus = reportStatus;
    }


}
