/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.hangout;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//    // Enable Local Datastore.
//    Parse.enableLocalDatastore(this);
//
//    // Add your initialization code here
//    Parse.initialize(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId("hangout_id")
                        .clientKey("hangout_key")
                        .server("http://10.169.46.155:1337/parse/") // The trailing slash is important.
                        .build()
        );

        ParseObject.registerSubclass(HangoutEvent.class);


//        ParseUser.enableAutomaticUser();
//        ParseACL defaultACL = new ParseACL();
//        // Optionally enable public read access.
//        // defaultACL.setPublicReadAccess(true);
//        ParseACL.setDefaultACL(defaultACL, true);
//
//        ParseObject parseObject = new ParseObject("Objects");
//        parseObject.put("key", "value");
//        parseObject.put("key2", "value2");
//        parseObject.saveInBackground();
//
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Objects");
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> objects, ParseException e) {
//                if (e == null) {
//                Log.d("test", "Retrieved " + objects.size() + " scores");
//            } else {
//                Log.d("test", "Error: " + e.getMessage());
//            }
//            }
//        });
    }
}
