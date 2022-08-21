package idv.tgp10110.tgp10110ryanchiang.bean;

import java.io.Serializable;
import java.util.List;

public class Diary implements Serializable {
    private String diaryId; // Firebase的UID
    private String diaryName; // 日記名稱
    private String diaryDate; // 待調整
    private String diaryDetail; // 日記內容
    private String diaryRemark; // 附註
    private String diaryImagePath; //日記圖片路徑

    public Diary() {

    }

    public Diary(String diaryId, String diaryName, String diaryDate, String diaryDetail, String diaryRemark, String diaryImagePath) {
        this.diaryId = diaryId;
        this.diaryName = diaryName;
        this.diaryDate = diaryDate;
        this.diaryDetail = diaryDetail;
        this.diaryRemark = diaryRemark;
        this.diaryImagePath = diaryImagePath;
    }

    public String getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(String diaryId) {
        this.diaryId = diaryId;
    }

    public String getDiaryName() {
        return diaryName;
    }

    public void setDiaryName(String diaryName) {
        this.diaryName = diaryName;
    }

    public String getDiaryDate() {
        return diaryDate;
    }

    public void setDiaryDate(String diaryDate) {
        this.diaryDate = diaryDate;
    }

    public String getDiaryDetail() {
        return diaryDetail;
    }

    public void setDiaryDetail(String diaryDetail) {
        this.diaryDetail = diaryDetail;
    }

    public String getDiaryRemark() {
        return diaryRemark;
    }

    public void setDiaryRemark(String diaryRemark) {
        this.diaryRemark = diaryRemark;
    }

    public String getDiaryImagePath() {
        return diaryImagePath;
    }

    public void setDiaryImagePath(String diaryImagePath) {
        this.diaryImagePath = diaryImagePath;
    }
}