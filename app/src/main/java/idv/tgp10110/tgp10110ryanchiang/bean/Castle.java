package idv.tgp10110.tgp10110ryanchiang.bean;

import java.util.Date;

public class Castle {
    private String castleId; // Firebase的UID
    private Integer castleNumber; // 百名城編號
    private String castleImagePath; // 圖片路徑
    private String castleName; // 百名城名稱
    private boolean isStamped; // 是否已蓋章(上傳城章圖片)
    private boolean isFavorite; // 是否加入我的最愛
    private Date stampedDate; // 蓋章日期
    private String strStampedDate; // 日期顯示用字串

    public Castle() {
    }

    public Castle(Integer castleNumber, String castleName) {
        this.castleNumber = castleNumber;
        this.castleName = castleName;
    }

    public String getCastleId() {
        return castleId;
    }

    public void setCastleId(String castleId) {
        this.castleId = castleId;
    }

    public Integer getCastleNumber() {
        return castleNumber;
    }

    public void setCastleNumber(Integer castleNumber) {
        this.castleNumber = castleNumber;
    }

    public String getCastleImagePath() {
        return castleImagePath;
    }

    public void setCastleImagePath(String castleImagePath) {
        this.castleImagePath = castleImagePath;
    }

    public String getCastleName() {
        return castleName;
    }

    public void setCastleName(String castleName) {
        this.castleName = castleName;
    }

    public boolean isStamped() {
        return isStamped;
    }

    public void setStamped(boolean stamped) {
        isStamped = stamped;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Date getStampedDate() {
        return stampedDate;
    }

    public void setStampedDate(Date stampedDate) {
        this.stampedDate = stampedDate;
    }

    public String getStrStampedDate() {
        return strStampedDate;
    }

    public void setStrStampedDate(String strStampedDate) {
        this.strStampedDate = strStampedDate;
    }
}
