package com.example.lfs.agv_android_receiver.Model;

/**
 * Created by lfs on 2018/7/16.
 */

public class Task {
    private String agvId;
    private String content;
    private String remark;


    public Task(String agvId, String content) {
        this.agvId = agvId;
        this.content = content;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAgvId() {
        return agvId;
    }

    public void setAgvId(String agvId) {
        this.agvId = agvId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

