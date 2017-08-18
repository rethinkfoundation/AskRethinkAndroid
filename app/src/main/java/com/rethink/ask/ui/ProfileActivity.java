package com.rethink.ask.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.rethink.ama.R;
import com.rethink.ask.app.PreferensHandler;

import java.util.Arrays;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ProfileActivity extends AppCompatActivity {

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {GmailScopes.MAIL_GOOGLE_COM};
    private static final String PREF_ACCOUNT_NAME = "accountName";

    EditText edtName;
    EditText edtEmail;
    EditText edtCollege;
    EditText edtBranch;
    EditText edtYear;


    Button btnSave;
    PreferensHandler pref;
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        c = getApplicationContext();
        pref = new PreferensHandler(c);


        //Binding java variable to xml
        edtName = (EditText) findViewById(R.id.your_name);
        edtEmail = (EditText) findViewById(R.id.your_email);
        edtCollege = (EditText) findViewById(R.id.college);
        edtBranch = (EditText) findViewById(R.id.branch);
        edtYear = (EditText) findViewById(R.id.year);


        //Binding save button (xml--Java)
        btnSave = (Button) findViewById(R.id.save_button);


        //set cache value to edit text
        edtName.setText(pref.getUserName());
        edtEmail.setText(pref.getUserEmail());
        edtCollege.setText(pref.getUserCollege());
        edtBranch.setText(pref.getUserBranch());
        edtYear.setText(pref.getUserYear());

        Log.e("PreferenceActivity", "username = " + pref.getUserName());
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = 0;

                if (!edtName.getText().toString().equals("")) {
                    pref.setUserName(edtName.getText().toString());
                    count++;
                }

                if (!edtEmail.getText().toString().equals("")) {
                   // pref.setUserEmail(edtEmail.getText().toString());
                }

                if (!edtCollege.getText().toString().equals("")) {
                    pref.setUserCollege(edtCollege.getText().toString());
                    count++;
                }

                if (!edtBranch.getText().toString().equals("")) {
                    pref.setUserBranch(edtBranch.getText().toString());
                    count++;
                }

                if (!edtYear.getText().toString().equals("")) {
                    pref.setUserYear(edtYear.getText().toString());
                    count++;
                }

                if(count == 4) {
                    if (!pref.getFirstOpen()) {
                        pref.setFirstOpen();
                        Intent i = new Intent(ProfileActivity.this, PrimaryActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    showDialog("Please complete your details!!!");
                }
            }


        });

        if (!pref.getFirstOpen()) {
            btnSave.setText("Next");

            TextInputLayout textInputLayout = (TextInputLayout) findViewById(R.id.input_layout_email);
            textInputLayout.setVisibility(View.GONE);

            mCredential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
            //getAccess();
        }
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        //builder.setTitle("AppCompatDialog");
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        // builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void getAccess() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(ProfileActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ProfileActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                //getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    //todo: show a dialog -> Need google play service
                }
                break;
            case REQUEST_ACCOUNT_PICKER:

                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    String userName = data.getStringExtra(AccountManager.KEY_ACCOUNTS);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();

                        new PreferensHandler(c).setUserEmail(accountName);

                        mCredential.setSelectedAccountName(accountName);
                    }
                }
                break;

        }
    }
}