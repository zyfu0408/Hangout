package com.parse.hangout;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Custom Parse object for event membership. Takes a pointer to a user and a pointer to an event
 */
@ParseClassName("EventMembership")
public class EventMembership extends ParseObject{

    public HangoutEvent getEvent() {
        return (HangoutEvent) getParseObject("hangoutEvent");
    }

    public void setEvent(HangoutEvent hangoutEvent) {
        put("hangoutEvent", hangoutEvent);
    }

    public ParseUser getEventMember() {
        return getParseUser("member");
    }

    public void setEventMember(ParseUser member) {
        put("member", member);
    }
}
