package com.parse.hangout;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;


@ParseClassName("Events")
public class HangoutEvent extends ParseObject {

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String title) {
        put("title", title);
    }

    public String getDescription() {
        return getString("description");
    }

    public void setDescription(String description) {
        put("description", description);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser value) {
        put("user", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String address) {
        put("address", address);
    }

    public Date getStartTime() {
        return getDate("startTime");
    }

    public void setStartTime(Date startTime) {
        put("startTime", startTime);
    }

    public Date getStopTime() {
        return getDate("endTime");
    }

    public void setStopTime(Date endTime) {
        put("endTime", endTime);
    }

    public static ParseQuery<HangoutEvent> getQuery() {
        return ParseQuery.getQuery(HangoutEvent.class);
    }
}
