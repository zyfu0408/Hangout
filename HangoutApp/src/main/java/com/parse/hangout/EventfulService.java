package com.parse.hangout;

import android.os.AsyncTask;

import com.parse.eventful_android.APIConfiguration;
import com.parse.eventful_android.EVDBAPIException;
import com.parse.eventful_android.EVDBRuntimeException;
import com.parse.eventful_android.data.Event;
import com.parse.eventful_android.data.SearchResult;
import com.parse.eventful_android.data.request.EventSearchRequest;
import com.parse.eventful_android.operations.EventOperations;


import java.util.Hashtable;
import java.util.List;

public class EventfulService extends AsyncTask<Void, Void, List<Event>> {

    private static final String APP_KEY = "tn5Rmwfp2zzBxfwX";
    private static final String APP_USERNAME = "GorjanZ";
    private static final String APP_PASSWORD = "EventFul@123";

    public static List<Event> fetch(String query, String location, int limit) {

        System.out.println("Setting configuration");

        APIConfiguration.setEvdbUser("adam");
        APIConfiguration.setEvdbPassword("eventsearch");
        APIConfiguration.setApiKey("4PkxLvVPCTrPpxHX");

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


        Hashtable<String, Event> eventfulListe = sr.getEvents();

        return null;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        return fetch(null, null, 1);
    }
}
