package ru.simpleGBD.App.Logic.DataModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by km on 28.11.2015.
 */
public class TocItem {

    @JsonProperty("Title") private String title;
    @JsonProperty("Pid") private String pid;
    @JsonProperty("PgNum") private String pgNum;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getPgNum() {
        return pgNum;
    }

    public void setPgNum(String pgNum) {
        this.pgNum = pgNum;
    }
}
