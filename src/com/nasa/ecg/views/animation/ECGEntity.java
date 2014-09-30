package com.nasa.ecg.views.animation;

import java.util.Date;
import java.util.List;

public class ECGEntity {

    private List<Float> points;
    private String fileName;
    private Date born;
    private Date lastEcg;
    private String code;

    public List<Float> getPoints() {
        return points;
    }

    public void setPoints(List<Float> points) {
        this.points = points;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getBorn() {
        return born;
    }

    public void setBorn(Date born) {
        this.born = born;
    }

    public Date getLastEcg() {
        return lastEcg;
    }

    public void setLastEcg(Date lastEcg) {
        this.lastEcg = lastEcg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
