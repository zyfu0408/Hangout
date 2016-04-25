package com.parse.hangout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {
    //private String[] mDataset;
    List<HangoutEvent> events;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView titleTextView;
        protected TextView contentTextView;

        public ViewHolder(View v) {
            super(v);
            titleTextView = (TextView) v.findViewById(R.id.event_title);
            contentTextView = (TextView) v.findViewById(R.id.event_description);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public EventListAdapter(List<HangoutEvent> events) {
        this.events = events;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EventListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hangoutevents, parent, false);
        // set the view's size, margins, paddings and layout parameters
        v.getLayoutParams().width = parent.getWidth();

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        HangoutEvent event = events.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.contentTextView.setText(event.getDescription());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return events.size();
    }
}
