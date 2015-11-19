package uk.co.feixie.mynote.model;

import java.io.Serializable;

/**
 * Created by Fei on 09/11/2015.
 */
public class Note implements Serializable {

    private int id;
    private String title;
    private String content;
    private String imagePath;
    private String videoPath;
    private String voicePath;
    private String time;
    private String latitude;
    private String longitude;



    public Note() {
    }

    public Note(int id, String title, String content, String imagePath, String videoPath, String voicePath, String time, String latitude, String longitude) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.voicePath = voicePath;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

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

    @Override
    public String toString() {
        return "Note{" +

                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", videoPath='" + videoPath + '\'' +
                ", voicePath='" + voicePath + '\'' +
                ", time='" + time + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
