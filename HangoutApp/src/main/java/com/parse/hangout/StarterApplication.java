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

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
        
        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                        .applicationId("hangout_id")
                        .clientKey("hangout_key")
                        .server("http://192.168.29.100:1337/parse/") // The trailing slash is important.
                        .build()
        );

        ParseObject.registerSubclass(HangoutEvent.class);
        ParseObject.registerSubclass(EventMembership.class);
    }
}
