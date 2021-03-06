/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.hangout;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Activity for the map screen
 */
public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback, ResultCallback<LocationSettingsResult> {

    private GoogleApiClient mApiClient;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private Marker marker;
    private final int LOCATION_REQUEST_INTERVAL = 500000;

    protected static final String TAG = "MapActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private final Map<Marker, HangoutEvent> markerHangoutEventMap = new HashMap<Marker, HangoutEvent>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        try {
            new EventfulService().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        createLocationRequest();
        mApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // create our map if it already isn't created
        if (map == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }

        checkSelfPermission();
        LocationServices.FusedLocationApi.getLastLocation(mApiClient);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mApiClient.disconnect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
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

    @Override
    public void onConnected(Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mApiClient,
                        builder.build());

        result.setResultCallback(this);
        createLocationRequest();

        checkSelfPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
    }

    //the call back of location setting result
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to " +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    // the call back of status.startResolutionForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                // the case for location change
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation != null) {
            mCurrentLocation = mLastLocation;
        }
        mCurrentLocation = location;

        //remove previous current location Marker
        if (marker != null) {
            marker.remove();
        }

        double dLatitude = mCurrentLocation.getLatitude();
        double dLongitude = mCurrentLocation.getLongitude();
        marker = map.addMarker(new MarkerOptions().position(new LatLng(dLatitude, dLongitude))
                .title("My Location").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // show the info window for your location and center camera around it
        marker.showInfoWindow();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 16.0f));

        // update our map when the user's location changes
        doMapQuery();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Sets up our map
     */
    @Override
    public void onMapReady(GoogleMap retMap) {

        map = retMap;

        setUpMap();

    }

    /**
     * Helper called by onMapReady callback
     */
    public void setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
        }
        map.setMyLocationEnabled(true);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(CameraPosition position) {
                doMapQuery();
            }
        });

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {

                final ParseUser user = ParseUser.getCurrentUser();
                final HangoutEvent event = markerHangoutEventMap.get(marker);

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                TextView title = (TextView) v.findViewById(R.id.event_title);
                title.setText(marker.getTitle());

                final TextView membersAttending = (TextView) v.findViewById(R.id.members_attending);
                Button joinButton = (Button) v.findViewById(R.id.join_button);
                TextView startTimeEditText = (TextView) v.findViewById(R.id.start_time);
                TextView stopTimeEditText = (TextView) v.findViewById(R.id.stop_time);


                // remove irrelevant ui components on marker for the "my location" marker
                if (marker.getTitle().equals("My Location")) {
                    membersAttending.setVisibility(View.GONE);
                    joinButton.setVisibility(View.GONE);
                    startTimeEditText.setVisibility(View.GONE);
                    stopTimeEditText.setVisibility(View.GONE);
                    return v;
                }

                // add start and stop times
                Date startTime = event.getStartTime();
                Date stopTime = event.getStopTime();

                if (startTime != null) {
                    startTimeEditText.setText("Start Time: " + startTime.toString());
                } else if (startTime == null) {
                    startTimeEditText.setVisibility(View.GONE);
                }

                if (stopTime != null) {
                    stopTimeEditText.setText("Stop Time: " + stopTime.toString());
                } else if (stopTime == null) {
                    stopTimeEditText.setVisibility(View.GONE);
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


                membersAttending.setText("Members attending: " + numMembers);

                if (isUserAttending == true) {
                    joinButton.setText("Unjoin");
                } else {
                    joinButton.setText("Join");
                }

                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        // listener that handles joining/leaving an event on the map
        map.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(final Marker marker) {

                        View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                        final ParseUser user = ParseUser.getCurrentUser();
                        final HangoutEvent event = markerHangoutEventMap.get(marker);


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
                                                        // once the EventMembership is saved,
                                                        // refresh the info window to show this user is attending the event
                                                        marker.showInfoWindow();
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
                                                        marker.showInfoWindow();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                }
                        );
                        //test sendNotification() method
                        //sendNotification();
                    }
                }
        );
    }

    /**
     * Adds map markers for all of the events nearby the current user
     */
    private void doMapQuery() {
        Location myLoc = mCurrentLocation;
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }

        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        ParseQuery<HangoutEvent> mapQuery = ParseQuery.getQuery(HangoutEvent.class);
//        mapQuery.whereWithinKilometers("location", myPoint, 100);
//        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");

        mapQuery.findInBackground(new FindCallback<HangoutEvent>() {
            @Override
            public void done(List<HangoutEvent> objects, ParseException e) {

                Set<String> toKeep = new HashSet<String>();

                for (HangoutEvent event : objects) {

                    toKeep.add(event.getObjectId());

                    Marker oldMarker = mapMarkers.get(event.getObjectId());

                    // if it already exists on the map, dont add another one
                    if (oldMarker != null) {
                        continue;
                    } else {

                        // add the marker to the map
                        MarkerOptions markerOpts =
                                new MarkerOptions().position(new LatLng(event.getLocation().getLatitude(), event
                                        .getLocation().getLongitude()))
                                        .title(event.getTitle())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                        Marker marker = map.addMarker(markerOpts);
                        mapMarkers.put(event.getObjectId(), marker);
                        markerHangoutEventMap.put(marker, event);

                    }
                }

                cleanUpMarkers(toKeep);
            }
        });
    }

    /**
     * Helper method to convert a Location to ParseGeoPoint
     */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }


    /**
     * Method to remove old markers, and keeps the markers we provide
     */
    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }

    /**
     * Method to build a notification and send it to the creator of the event
     * if the creator exists in the database
     */
    private void sendNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Hangout Notification")
                        .setContentText("A new member have added your event!");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 1;
        int numMessage = 0;
        mBuilder.setNumber(++numMessage);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notifyID, mBuilder.build());
    }
}
