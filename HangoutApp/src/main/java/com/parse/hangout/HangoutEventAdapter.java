package com.parse.hangout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.eventful_android.data.User;

import java.util.ArrayList;
import java.util.List;

public class HangoutEventAdapter extends ArrayAdapter<HangoutEvent> {
    public HangoutEventAdapter(Context context, List<HangoutEvent> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        HangoutEvent event = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_hangoutevent, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.title);
        TextView tvHome = (TextView) convertView.findViewById(R.id.description);
        // Populate the data into the template view using the data object
        tvName.setText(event.getTitle());
        tvHome.setText(event.getDescription());
        // Return the completed view to render on screen
        return convertView;
    }
}