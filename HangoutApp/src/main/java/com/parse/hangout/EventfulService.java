package com.parse.hangout;

import android.os.AsyncTask;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.eventful_android.APIConfiguration;
import com.parse.eventful_android.EVDBAPIException;
import com.parse.eventful_android.EVDBRuntimeException;
import com.parse.eventful_android.data.Event;
import com.parse.eventful_android.data.SearchResult;
import com.parse.eventful_android.data.request.EventSearchRequest;
import com.parse.eventful_android.operations.EventOperations;

import java.util.ArrayList;
import java.util.List;

public class EventfulService extends AsyncTask<Void, Void, List<HangoutEvent>> {

    private static final String API_KEY = "4PkxLvVPCTrPpxHX";
    private static final String APP_USERNAME = "test";
    private static final String APP_PASSWORD = "eventsearch";

    public static List<HangoutEvent> fetch(String query, String location, int limit) {

        System.out.println("Setting configuration");

        APIConfiguration.setEvdbUser(APP_USERNAME);
        APIConfiguration.setEvdbPassword(APP_PASSWORD);
        APIConfiguration.setApiKey(API_KEY);

        EventOperations eo = new EventOperations();

        //create the search request for events in Worcester
        EventSearchRequest esr = new EventSearchRequest();
        esr.setPageSize(100);
        esr.setLocation("Worcester, MA");

        SearchResult sr = null;
        try {
            sr = eo.search(esr);
        } catch (EVDBRuntimeException e) {
            e.printStackTrace();
        } catch (EVDBAPIException e) {
            e.printStackTrace();
        }

        List<Event> events = sr.getEventsList();
        List<HangoutEvent> hangoutEvents = convertEventListToHangoutEventList(events);
        return hangoutEvents;
    }

    @Override
    protected List<HangoutEvent> doInBackground(Void... params) {
        return fetch(null, null, 1);
    }

    private static List<HangoutEvent> convertEventListToHangoutEventList(List<Event> events) {

        List<HangoutEvent> hangoutEvents = new ArrayList<HangoutEvent>();
        HangoutEvent hangoutEvent;
        ParseGeoPoint parseGeoPoint;
        String description;

        ParseUser user = ParseUser.getCurrentUser();

        for (Event event : events) {
            parseGeoPoint = new ParseGeoPoint(event.getVenueLatitude(), event.getVenueLongitude());

            hangoutEvent = new HangoutEvent();
            hangoutEvent.setTitle(event.getTitle());
            hangoutEvent.setUser(user);
            hangoutEvent.setLocation(parseGeoPoint);

            // loads description as HTML, as some of the descriptions use <p> tags and others
            description = android.text.Html.fromHtml(event.getDescription()).toString();
            hangoutEvent.setDescription(description);

            // add the event to the list to be displayed
            hangoutEvents.add(hangoutEvent);


            // run a query in the background, checking to see if the current event exists in the DB or not
            ParseQuery<HangoutEvent> query = ParseQuery.getQuery(HangoutEvent.class);
            query.whereEqualTo("title", hangoutEvent.getTitle());
            query.whereEqualTo("description", hangoutEvent.getDescription());
            final HangoutEvent finalHangoutEvent = hangoutEvent;
            query.findInBackground(new FindCallback<HangoutEvent>() {
                @Override
                public void done(List<HangoutEvent> objects, ParseException e) {
                    if (e == null) {
                        if (objects == null || objects.size() == 0) {
                            finalHangoutEvent.saveInBackground();
                        } else if (objects.size() == 1) {
                            // do nothing, the event we are looking for has been created
                        }
                    }
                }
            });

        }
        return hangoutEvents;
    }
}
