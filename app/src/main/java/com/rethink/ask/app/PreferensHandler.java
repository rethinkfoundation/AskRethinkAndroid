package com.rethink.ask.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Shibin.co on 31/05/17.
 */

public class PreferensHandler {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context c;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "settings_pref";
    final String user_name = "user_name";
    final String user_email = "user_email";
    final String user_college = "user_college";
    final String user_branch = "user_branck";
    final String user_year = "user_year";
    final String first_open = "first_open";
    final String show_hint = "show_hint";


    @SuppressLint("CommitPrefEdits")
    public PreferensHandler(Context context) {
        this.c = context;
        pref = c.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setUserName(String var) {
        editor.putString(user_name, var);
        editor.commit();
    }

    public String getUserName() {
        return pref.getString(user_name, "");
    }


    public void setUserEmail(String var) {
        editor.putString(user_email, var);
        editor.commit();
    }

    public String getUserEmail() {
        return pref.getString(user_email, "");
    }


    public void setUserCollege(String var) {
        editor.putString(user_college, var);
        editor.commit();
    }

    public String getUserCollege() {
        return pref.getString(user_college, "");
    }


    public void setUserBranch(String var) {
        editor.putString(user_branch, var);
        editor.commit();
    }

    public String getUserBranch() {
        return pref.getString(user_branch, "");
    }


    public void setUserYear(String var) {
        editor.putString(user_year, var);
        editor.commit();
    }

    public String getUserYear() {
        return pref.getString(user_year, "");
    }

    public void setFirstOpen() {
        editor.putBoolean(first_open, true);
        editor.commit();
    }

    public boolean getFirstOpen() {
        return pref.getBoolean(first_open, false);
    }

    public void setShowHint() {
        editor.putBoolean(show_hint, true);
        editor.commit();
    }

    public boolean getShowHint() {
        return pref.getBoolean(show_hint, false);
    }


}
