package com.parse.hangout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class EventListActivity extends AppCompatActivity {
    private List<HangoutEvent> events;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_eventslist);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // specify an adapter (see also next example)
        ParseQuery<HangoutEvent> query = ParseQuery.getQuery(HangoutEvent.class);
        query.findInBackground(new FindCallback<HangoutEvent>() {
            @Override
            public void done(List<HangoutEvent> objects, ParseException e) {
                if (e == null) {
                    mAdapter = new EventListAdapter(objects);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


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
                return true;
            case R.id.signout:
                AccountUtilities.signout();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
