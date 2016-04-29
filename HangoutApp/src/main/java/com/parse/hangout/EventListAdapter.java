package com.parse.hangout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adapter class that binds our HangoutEvents to the list view
 */
public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {

    private List<HangoutEvent> events;
    private final ParseUser user;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        protected TextView titleTextView;
        protected TextView contentTextView;
        protected TextView membersAttending;
        protected TextView startTime;
        protected TextView stopTime;
        protected Button joinButton;
        protected View cardView;

        public ViewHolder(View v) {
            super(v);
            this.cardView = v;
            titleTextView = (TextView) v.findViewById(R.id.event_title);
            contentTextView = (TextView) v.findViewById(R.id.event_description);
            membersAttending = (TextView) v.findViewById(R.id.members_attending);
            joinButton = (Button) v.findViewById(R.id.join_button);
            startTime = (TextView) v.findViewById(R.id.start_time);
            stopTime = (TextView) v.findViewById(R.id.stop_time);
        }

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public EventListAdapter(List<HangoutEvent> events) {
        this.events = events;
        this.user = ParseUser.getCurrentUser();
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
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final HangoutEvent event = events.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.contentTextView.setText(event.getDescription());

        // add start and stop times
        Date startTime = event.getStartTime();
        Date stopTime = event.getStopTime();

        if (startTime != null) {
            holder.startTime.setText("Start Time: " + startTime.toString());
        } else if (startTime == null) {
            holder.startTime.setVisibility(View.GONE);
        }

        if (stopTime != null) {
            holder.stopTime.setText("Stop Time: " + stopTime.toString());
        } else if (stopTime == null) {
            holder.stopTime.setVisibility(View.GONE);
        }


        int numMembers = 0;
        boolean isUserAttending = false;

        ParseQuery<EventMembership> mapQuery = ParseQuery.getQuery(EventMembership.class);
        mapQuery.whereEqualTo("hangoutEvent", event);


        // see who is attending this event
        List<EventMembership> memberships = new ArrayList<EventMembership>();
        try {
            memberships = mapQuery.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (EventMembership membership : memberships) {
            if (membership.getEventMember().getObjectId().equals(user.getObjectId())) {
                isUserAttending = true;
            }
        }
        numMembers = memberships.size();

        holder.membersAttending.setText("Members attending: " + numMembers);

        if (isUserAttending == true) {
            holder.joinButton.setText("Unjoin");
        } else {
            holder.joinButton.setText("Join");
        }


        holder.joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ParseUser user = ParseUser.getCurrentUser();


                ParseQuery<EventMembership> mapQuery = ParseQuery.getQuery(EventMembership.class);
                mapQuery.whereEqualTo("hangoutEvent", event);
                mapQuery.whereEqualTo("member", user);
                mapQuery.include("hangoutEvent");
                mapQuery.findInBackground(
                        new FindCallback<EventMembership>() {
                            @Override
                            public void done(List<EventMembership> objects, ParseException e) {
                                if (objects.size() == 0) {
                                    EventMembership membership = new EventMembership();
                                    membership.setEvent(event);
                                    membership.setEventMember(user);
                                    membership.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });
                                }
                                // leave the event if the user has joined it
                                else if (objects.size() == 1) {
                                    objects.get(0).deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                // refresh info window once deleted
                                                notifyDataSetChanged();

                                            }
                                        }
                                    });

                                }
                            }
                        }
                );
            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return events.size();
    }


}
