package com.rethink.ask.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.rethink.ama.R;
import com.rethink.ask.app.PreferensHandler;
import com.rethink.ask.app.RethinkApplication;
import com.rethink.ask.controller.db.DBManager;
import com.rethink.ask.controller.mail.GmailUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by zac on 13/02/18.
 */

public class DiscoveryActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private String data;
    private String to = "volunteers@rethinkfoundation.in";

    //private EditText queryEditText;
    private Button sendButton, clearButton, testButton;
    private EditText studentNameEditText, studentEmailEditText, studentCollegeEditText, studentBranchEditText, studentYearEditText;

    private static final String TAG = "RethinkAMA";
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {GmailScopes.MAIL_GOOGLE_COM};
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private DBManager dbManager;

    private String studentName;
    private String studentEmailId;
    private String studentCollege;
    private String studentBranch;
    private String studentYear;

    PreferensHandler pref;
    Context c;

    private Tracker mTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discoverycontainer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Student Discovery");

        c = getApplicationContext();
        pref = new PreferensHandler(c);

        RethinkApplication application = (RethinkApplication) getApplication();
        mTracker = application.getDefaultTracker();

        //queryEditText = (EditText) findViewById(R.id.queryedittext);
        studentNameEditText = (EditText) findViewById(R.id.studentName);
        studentEmailEditText = (EditText) findViewById(R.id.studentEmail);
        studentCollegeEditText = (EditText) findViewById(R.id.studentCollege);
        studentBranchEditText = (EditText) findViewById(R.id.studentBranch);
        studentYearEditText = (EditText) findViewById(R.id.studentYear);


        sendButton = (Button) findViewById(R.id.submitButton);
        dbManager = new DBManager(DiscoveryActivity.this);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (studentNameEditText.getText().toString().equals("") && studentEmailEditText.getText().toString().equals("") && studentCollegeEditText.getText().toString().equals("")) {
                    showDialog("Please fill minimum details..");
                } else {

                    if (isNetworkAvailable()) {
                        studentName = studentNameEditText.getText().toString();
                        studentEmailId = studentEmailEditText.getText().toString();
                        studentCollege = studentCollegeEditText.getText().toString();
                        studentBranch = studentBranchEditText.getText().toString();
                        studentYear = studentYearEditText.getText().toString();

                        new SendRequest().execute();
                        hideKeyboard();
                    } else {
                        new AlertDialog.Builder(DiscoveryActivity.this)
                                .setTitle("No Internet Connection")
                                .setMessage("Please turn on your mobile data or connect to a wifi to send email.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }

                //dbManager.saveDetails(name, email, number);
            }
        });


       /* clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFields();
            }
        });*/


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getAccess();

        //Code to insert
        //dbManager.saveDetails("Zac", "zacflys@gmail.com", "9633963626"); // for inserting data


        //Code to fetch
        /*RealmResults<Contact> contacts = dbManager.getContactData();
        for (Contact contact : contacts) {
            Log.d("Database", contact.getEmail() + "    " + contact.getName() + "    " + contact.getNumber() + "    " + contact.getEmailSent() + "    " + contact.getSmsSent() + "    " + contact.getSyncedTimeinMillis());
        }*/

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showHint() {
        showLog("Call on show hint");
        new MaterialTapTargetPrompt.Builder(this)
                .setPrimaryText("You can update your profile here")
                .setBackgroundColour(getResources().getColor(R.color.Pink))
                .setIconDrawableColourFilter(getResources().getColor(R.color.Pink))

                //         .setSecondaryText("Edit your Credential in Email")
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setMaxTextWidth(R.dimen.tap_target_menu_max_width)
                .setIcon(R.drawable.ic_edit_pref)
                .setTarget(R.id.option_preference).setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
            @Override
            public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                Log.e("TAg", "on Hide prompt");

            }

            @Override
            public void onHidePromptComplete() {
                Log.e("TAg", "on Hide prompt");
            }
        }).show();
        pref.setShowHint();
    }

    private void getAccess() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(DiscoveryActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
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

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                DiscoveryActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
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
                showLog("Request account picker");
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    showLog("Account name - " + accountName);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();

                        new PreferensHandler(c).setUserEmail(accountName);
                        mCredential.setSelectedAccountName(accountName);

                        if (!pref.getShowHint()) {
                            showHint();
                        }
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    showLog("Request authorisation");
                    new DiscoveryActivity.SendRequest().execute();
                }
                break;
        }
    }

    private class SendRequest extends AsyncTask<Void, Void, Void> {

        private com.google.api.services.gmail.Gmail mService;
        private HttpTransport transport = AndroidHttp.newCompatibleTransport();
        private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        private Exception mLastError = null;

        @Override
        protected void onPreExecute() {
            showLog("On Pre Execute");
            super.onPreExecute();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential).setApplicationName("AndroidGmailAPIexample")
                    .build();
            Toast.makeText(getBaseContext(), "Sending email", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            showLog("sendRequest - DoinBackground");
            showLog("sendRequest - " + to);
            String subject = "Query from " + pref.getUserName();


            try {

                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());

                String message = "Query from " + pref.getUserName() + " on " + date;
                message = message + "\n______________________________________________\n\nQuery : \n\n";
                message = message + data;
                message = message + "\n______________________________________________\n";
                message = message + pref.getUserName().trim() + "\n" + pref.getUserCollege().trim() + "\n" + pref.getUserBranch().trim() + ", " + pref.getUserYear().trim();

                GmailUtil.sendMessage(mService, "me",
                        GmailUtil.createEmail(pref.getUserName() + " <" + pref.getUserEmail() + ">", to, pref.getUserEmail(), subject, message));


            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showLog("sendRequest - onPostExecute");
            Toast.makeText(getBaseContext(), "Mail sent successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        }


        @Override
        protected void onCancelled() {
            System.out.println("onCancelled");
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    showLog("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                showLog("Request cancelled.");
            }
        }
    }

    private void clearFields() {
        studentNameEditText.setText("");
        studentEmailEditText.setText("");
        studentCollegeEditText.setText("");
        studentBranchEditText.setText("");
        studentYearEditText.setText("");
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

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.primary_activity_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.option_preference) {
            Intent prefActivity = new Intent(DiscoveryActivity.this, ProfileActivity.class);
            startActivity(prefActivity);
        }
        return super.onOptionsItemSelected(item);
    }
}
