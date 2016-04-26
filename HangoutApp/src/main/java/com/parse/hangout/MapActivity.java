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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class MapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private GoogleApiClient mApiClient;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private Marker marker;
    private final int LOCATION_REQUEST_INTERVAL = 500000;

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

        mApiClient.connect();

        try {
            new EventfulService().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mApiClient == null || !mApiClient.isConnected()) {
//            buildGoogleApiClient();
//            mApiClient.connect();
        }

        // create our map if it already isn't created
        if (map == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
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
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

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
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 16.0f));

        doMapQuery();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

                Button joinButton = (Button) v.findViewById(R.id.join_button);

                if (isUserAttending == true) {
                    joinButton.setText("Joined!");
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
                    }
                }
        );
    }

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
//                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(event.getLocation().getLatitude(), event
//                                .getLocation().getLongitude()), 16.0f));
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
}
