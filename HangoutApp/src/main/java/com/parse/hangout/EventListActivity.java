package com.parse.hangout;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventListActivity extends AppCompatActivity {

    private List<HangoutEvent> events;
    private ListView eventsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventslist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            events = new EventfulService().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // now map the events
        HangoutEventAdapter adapter = new HangoutEventAdapter(this, events);
        eventsListView = (ListView) findViewById(R.id.events_listview);
        eventsListView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_screen:
                startActivity(new Intent(this, MapActivity.class));
                return true;
            case R.id.list_screen:
                startActivity(new Intent(this, EventListActivity.class));
                return true;
            case R.id.post_screen:
                startActivity(new Intent(this, PostEventActivity.class));
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
