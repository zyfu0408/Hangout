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

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Application class that connects to the parse server
 */
public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();

        // initializes the server with the provided URL
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId("hangout_id")
                        .clientKey("hangout_key")
                        .server("http://130.215.218.22:1337/parse/") // The trailing slash is important.
                        .build()
        );

        // registers our two hangout objects
        ParseObject.registerSubclass(HangoutEvent.class);
        ParseObject.registerSubclass(EventMembership.class);
    }
}
