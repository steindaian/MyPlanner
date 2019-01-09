package upt.myplanner.calendar;

import java.io.Serializable;

public class MyEvent implements Serializable {
    String name;
    String description;
    String start_time;
    String end_time;
    String year;
    String month;
    String day;
    String timestamp;
    String uid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public MyEvent() {

    }
    public MyEvent(String name, String description, String start_time, String end_time, String year, String month, String day) {
        this.name = name;
        this.description = description;
        this.start_time = start_time;
        this.end_time = end_time;
        this.year = year;
        this.month = month;
        this.day = day;
    }

}
