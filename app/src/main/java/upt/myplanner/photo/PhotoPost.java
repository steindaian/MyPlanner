package upt.myplanner.photo;

import android.location.Location;
import android.os.Parcelable;

import java.io.Serializable;

public class PhotoPost implements Serializable{
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getDownloadImgPath() {
        return downloadImgPath;
    }

    public void setDownloadImgPath(String downloadImgPath) {
        this.downloadImgPath = downloadImgPath;
    }

    String uid;
    String description;
    String timestamp;
    Location location;
    String imgPath;
    String downloadImgPath;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    String latitude;
    String longitude;
    public PhotoPost() {

    }
}
