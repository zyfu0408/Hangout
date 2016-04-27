package com.parse.hangout;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.AwesomeTextView;
import com.beardedhen.androidbootstrap.BootstrapButton;
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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;

public class PostEventActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult> {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    protected static final String TAG = "PostEventActivity";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private EditText post_subject;
    private EditText post_content;
    private Button post_button;
    private String address;
    private ParseGeoPoint event_location;
    private BootstrapButton event_start_date;
    private BootstrapButton event_start_time;
    private BootstrapButton event_end_date;
    private BootstrapButton event_end_time;
    private static AwesomeTextView start_time_text;
    private static AwesomeTextView end_time_text;

    private static int event_start_year;
    private static int event_start_month;
    private static int event_start_day;
    private static int event_start_hour;
    private static int event_start_minute;

    private static int event_end_year;
    private static int event_end_month;
    private static int event_end_day;
    private static int event_end_hour;
    private static int event_end_minute;

    private static AwesomeTextView show_event_start_date;
    private static AwesomeTextView show_event_start_time;
    private static AwesomeTextView show_event_end_date;
    private static AwesomeTextView show_event_end_time;

    private static boolean event_start_date_isSet = false;
    private static boolean event_start_time_isSet = false;
    private static boolean event_end_date_isSet = false;
    private static boolean event_end_time_isSet = false;

    private HangoutEvent hangoutEvent;
    private ParseGeoPoint parseGeoPoint;

    private Location mLastLocation;

    private PlaceAutocompleteFragment autocompleteFragment;
    private AutocompleteFilter typeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        post_subject = (EditText) findViewById(R.id.post_subject);
        post_content = (EditText) findViewById(R.id.post_content);
        post_button = (Button) findViewById(R.id.post_button);

        event_start_date = (BootstrapButton) findViewById(R.id.event_start_date);
        event_start_time = (BootstrapButton) findViewById(R.id.event_start_time);
        event_end_date = (BootstrapButton) findViewById(R.id.event_end_date);
        event_end_time = (BootstrapButton) findViewById(R.id.event_end_time);

        show_event_start_date = (AwesomeTextView) findViewById(R.id.show_event_start_date);
        show_event_start_time = (AwesomeTextView) findViewById(R.id.show_event_start_time);
        show_event_end_date = (AwesomeTextView) findViewById(R.id.show_event_end_date);
        show_event_end_time = (AwesomeTextView) findViewById(R.id.show_event_end_time);

        start_time_text = (AwesomeTextView) findViewById(R.id.start_time_text);
        end_time_text = (AwesomeTextView) findViewById(R.id.end_time_text);

        event_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePickerDialog(v);
            }
        });

        event_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimePickerDialog(v);
            }
        });

        event_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePickerDialog(v);
            }
        });

        event_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePickerDialog(v);
            }
        });

        hangoutEvent = new HangoutEvent();

        post_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //updatePostButtonState();
            }
        });


        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post();
            }
        });

        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                Log.i(TAG, place.getLatLng().toString());

                address = place.getName().toString();
                event_location = new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        autocompleteFragment.setHint("type your event location");

        typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        autocompleteFragment.setFilter(typeFilter);
    }

    private void post() {
        if (!checkTitleAddressExist()) {
            Toast.makeText(this, "At least you should type in event title and choose event location",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!checkTimeValid()) {
            Toast.makeText(this, "The event start time should be earlier than the end time",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final ParseUser user = ParseUser.getCurrentUser();

        hangoutEvent.setUser(user);

        parseGeoPoint = new ParseGeoPoint(event_location.getLatitude(), event_location.getLongitude());
        hangoutEvent.setLocation(parseGeoPoint);

        hangoutEvent.setTitle(getPostTitle());

        hangoutEvent.setDescription(getPostContent());

        hangoutEvent.setAddress(address);

        //data in the database is not correct, and should detect start time is smaller than stop time
        if (event_start_date_isSet && event_start_time_isSet) {
            Date start_time = new Date(event_start_year, event_start_month, event_start_day, event_start_hour, event_start_minute);
            hangoutEvent.setStartTime(start_time);
        }
        if (event_end_date_isSet && event_end_time_isSet) {
            Date end_time = new Date(event_end_year, event_end_month, event_end_day, event_end_hour, event_end_minute);
            hangoutEvent.setStopTime(end_time);
        }

        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        hangoutEvent.setACL(acl);

        hangoutEvent.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    EventMembership membership = new EventMembership();
                    membership.setEvent(hangoutEvent);
                    membership.setEventMember(user);
                    membership.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                            }
                        }
                    });
                }

            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void checkSelfPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
    }

    @Override
    protected void onStart() {
        createLocationRequest();
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkSelfPermission();
        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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

    // implement google api client connection callback
    @Override
    public void onConnected(Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(this);

        checkSelfPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
                    status.startResolutionForResult(PostEventActivity.this, REQUEST_CHECK_SETTINGS);
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
        mGoogleApiClient.connect();
    }

    // implement google api client connection failed
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    //implement android location listener
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    private String getPostTitle() {
        return post_subject.getText().toString().trim();
    }

    private String getPostContent() {
        return post_content.getText().toString().trim();
    }

    private boolean checkTitleAddressExist() {

        boolean titleState = getPostTitle().length() != 0;
        boolean addressState = (address != null && event_location != null);

        return  (titleState && addressState) ;
    }
    
    private boolean checkTimeValid() {
        Date start_time = null;
        Date end_time = null;

        if (event_start_date_isSet && event_start_time_isSet && event_end_date_isSet && event_end_time_isSet) {
            start_time = new Date(event_start_year, event_start_month, event_start_day, event_start_hour, event_start_minute);
            end_time = new Date(event_end_year, event_end_month, event_end_day, event_end_hour, event_end_minute);
        }

        int i = start_time.compareTo(end_time);
        return i == -1;
    }

    public static class StartDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            event_start_date_isSet = false;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            event_start_year =  year - 1900;
            event_start_month = month;
            event_start_day = day;
            event_start_date_isSet = true;

            String date = String.format("%d/%d/%d", month, day, year);
            show_event_start_date.setText(date);

            if (event_start_date_isSet && event_start_time_isSet) {
                String time = String.format("%d/%d/%d %d:%d:00", event_start_month, event_start_day, event_start_year, event_start_hour, event_start_minute);
                start_time_text.setText("Your event start time: \n" + time);
            }
        }
    }

    public static class EndDatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            event_end_date_isSet = false;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            event_end_year = year - 1900;
            event_end_month = month;
            event_end_day = day;
            event_end_date_isSet = true;

            String date = String.format("%d/%d/%d", month, day, year);
            show_event_end_date.setText(date);

            if (event_end_date_isSet && event_end_time_isSet) {
                String time = String.format("%d/%d/%d  %d:%d:00", event_end_month, event_end_day, event_end_year, event_end_hour, event_end_minute);
                end_time_text.setText("Your event end time: \n" + time);
            }

        }
    }

    public static class StartTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            event_start_time_isSet = false;

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            event_start_hour = hourOfDay;
            event_start_minute = minute;
            event_start_time_isSet = true;

            String time = String.format("%d:%d:00", hourOfDay, minute);
            show_event_start_time.setText(time);

            if (event_start_date_isSet && event_start_time_isSet) {
                String event_time = String.format("%d/%d/%d  %d:%d:00", event_start_month, event_start_day, event_start_year, event_start_hour, event_start_minute);
                start_time_text.setText("Your event start time: \n" + event_time);
            }

        }

    }

    public static class EndTimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            event_end_time_isSet = false;

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            event_end_hour = hourOfDay;
            event_end_minute = minute;
            event_end_time_isSet = true;

            String time = String.format("%d:%d:00", hourOfDay, minute);
            show_event_end_time.setText(time);

            if (event_end_date_isSet && event_end_time_isSet) {
                String event_time = String.format("%d/%d/%d  %d:%d:00", event_end_month, event_end_day, event_end_year, event_end_hour, event_end_minute);
                end_time_text.setText("Your event end time: \n" + event_time);
            }
        }

    }

    public void showStartTimePickerDialog(View v) {
        DialogFragment newFragment = new StartTimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showStartDatePickerDialog(View v) {
        DialogFragment newFragment = new StartDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showEndTimePickerDialog(View v) {
        DialogFragment newFragment = new EndTimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showEndDatePickerDialog(View v) {
        DialogFragment newFragment = new EndDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

}
