package upt.myplanner.calendar;

import java.io.Serializable;

public class MyEvent implements Serializable {
    private String Name;
    private String Description;
    private String Start_time;
    private String End_time;
    private String Year;
    private String Month;
    private String Day;
    String uid;
    public MyEvent() {

    }
    public MyEvent(String name, String description, String start_time, String end_time, String year, String month, String day) {
        Name = name;
        Description = description;
        Start_time = start_time;
        End_time = end_time;
        Year = year;
        Month = month;
        Day = day;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getStart_time() {
        return Start_time;
    }

    public void setStart_time(String start_time) {
        Start_time = start_time;
    }

    public String getEnd_time() {
        return End_time;
    }

    public void setEnd_time(String end_time) {
        End_time = end_time;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getMonth() {
        return Month;
    }

    public void setMonth(String month) {
        Month = month;
    }

    public String getDay() {
        return Day;
    }

    public void setDay(String day) {
        Day = day;
    }
}
