package com.parse.hangout;

import com.parse.ParseUser;

public class AccountUtilities {

    // utility method for user signout
    public static void signout() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            ParseUser.logOut();
        }
    }
}
