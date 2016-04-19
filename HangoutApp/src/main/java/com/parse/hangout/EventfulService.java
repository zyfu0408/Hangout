package com.parse.hangout;

import android.os.AsyncTask;

import com.parse.eventful_android.APIConfiguration;
import com.parse.eventful_android.EVDBAPIException;
import com.parse.eventful_android.EVDBRuntimeException;
import com.parse.eventful_android.data.Event;
import com.parse.eventful_android.data.SearchResult;
import com.parse.eventful_android.data.request.EventSearchRequest;
import com.parse.eventful_android.operations.EventOperations;

import java.util.List;

public class EventfulService extends AsyncTask<Void, Void, List<Event>> {

    private static final String API_KEY = "4PkxLvVPCTrPpxHX";
    private static final String APP_USERNAME = "test";
    private static final String APP_PASSWORD = "eventsearch";

    public static List<Event> fetch(String query, String location, int limit) {

        System.out.println("Setting configuration");

        APIConfiguration.setEvdbUser(APP_USERNAME);
        APIConfiguration.setEvdbPassword(APP_PASSWORD);
        APIConfiguration.setApiKey(API_KEY);

        EventOperations eo = new EventOperations();

        //create the search request for music events in San Diego
        EventSearchRequest esr = new EventSearchRequest();
        esr.setPageSize(100);
        esr.setLocation("Worcester, MA");

        System.out.println("Starting initial request.");

        SearchResult sr = null;
        try {
            sr = eo.search(esr);
        } catch (EVDBRuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (EVDBAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<Event> events = sr.getEventsList();
        return events;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        return fetch(null, null, 1);
    }
}
