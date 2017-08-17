package com.rethink.mailappnew.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.rethink.mailappnew.app.PreferensHandler;
import com.rethink.mailappnew.controller.db.DBManager;
import com.rethink.mailappnew.controller.db.model.Contact;
import com.rethink.mailappnew.controller.mail.GmailUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.realm.RealmResults;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class ContactListing extends AppCompatActivity {
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private DBManager dbManager;
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {GmailScopes.MAIL_GOOGLE_COM};

    private static final String PREF_ACCOUNT_NAME = "accountName";

    Context c;
    private PreferensHandler pref;

    String name ="shibin";
    String number = "9645646130";
    String email ="shibinazx@gmail.com";
    String FilePath ="LetsConnect.csv";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_listing);
        c = getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(c);
        rv.setLayoutManager(llm);

        List<Contact> mContactList = new DBManager(c).getContactData();

        RVAdapter adapter = new RVAdapter(c, mContactList);
        rv.setAdapter(adapter);
        dbManager = new DBManager(this);


        pref = new PreferensHandler(c);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        getAccess();


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

    private void getAccess() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(ContactListing.this, "No network connection", Toast.LENGTH_SHORT).show();
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
//                showLog("Request account picker");
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
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
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
//                    showLog("Request authorisation");
                    new SendRequest().execute();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_listing_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings1) {
            this.exportToExcel();
            Log.e("Excel", "Excel Exported");
        }
        else if (id == R.id.action_settings2) {
            exportToCsv();
            Log.e("csv", "Csv Export Function Called");
        }
        return super.onOptionsItemSelected(item);
    }



 ///Exporting the DataBase to Local Storage as .xls
    public void exportToExcel() {
        final String fileName = "LetsConnect.xls";

        //Saving file in external storage
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        //file path
        File file = new File(directory, fileName);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("MyContactList", 0);

            try {
                sheet.addCell(new Label(0, 0, "Name",getHeadingFormat(2))); // column and row
                sheet.addCell(new Label(1, 0, "Mobile Number",getHeadingFormat(2)));
                sheet.addCell(new Label(2, 0, "Email",getHeadingFormat(2)));

                RealmResults<Contact> contacts = dbManager.getContactData();

                int i = 1;
                for (Contact contact : contacts) {
                    sheet.addCell(new Label(0, i, contact.getName()));
                    sheet.addCell(new Label(1, i, contact.getNumber()));
                    sheet.addCell(new Label(2, i, contact.getEmail()));
                    i++;
                }

            } catch (RowsExceededException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
            workbook.write();

            Toast.makeText(ContactListing.this, "File saved to - " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            //Toast.makeText(getBaseContext(), "File saved to - " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            try {
                workbook.close();
                new SendRequest().execute(file.getAbsolutePath(),"Contacts.xls");
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void exportToCsv() {

        //File Name
        final String fileName = "LetsConnect.csv";

        //Saving file in external storage
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath());

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        //file path
        File file = new File(directory, fileName);

            // Write the string to the file
            try {

                Log.e("csv", "Try First Line ");

                FileWriter writer = new FileWriter(file.getAbsolutePath());


                try {
                    writer.append("Name");
                    writer.append(",");
                    writer.append("Number");
                    writer.append(",");
                    writer.append("Email");
                    writer.append("\n");

                    RealmResults<Contact> contacts = dbManager.getContactData();

                    for (Contact contact : contacts) {

                        writer.append("" + contact.getName());
                        writer.append(",");
                        writer.append("" + contact.getNumber());
                        writer.append(",");
                        writer.append("" + contact.getEmail());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                writer.flush();
                Toast.makeText(ContactListing.this, "File saved to - " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                try {
                    writer.close();
                    new SendRequest().execute(file.getAbsolutePath(),"Contacts.csv");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Toast.makeText(ContactListing.this, "File  not saved to - ", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }


    public WritableCellFormat getHeadingFormat(int i) throws WriteException {
        WritableFont wfontStatus;
        if (i == 1) {
            wfontStatus = new WritableFont(WritableFont.createFont("Arial"), 14, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
        } else {
            wfontStatus = new WritableFont(WritableFont.createFont("Arial"), 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
        }

        WritableCellFormat fCellstatus = new WritableCellFormat(wfontStatus);
        fCellstatus.setWrap(true);
        fCellstatus.setAlignment(jxl.format.Alignment.CENTRE);
        fCellstatus.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
        // fCellstatus.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.MEDIUM, jxl.format.Colour.BLUE2);
        return fCellstatus;
    }

    private class SendRequest extends AsyncTask<String, String, Void> {

        private com.google.api.services.gmail.Gmail mService;
        private HttpTransport transport = AndroidHttp.newCompatibleTransport();
        private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        private Exception mLastError = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, mCredential).setApplicationName("AndroidGmailAPIexample")
                    .build();
            Toast.makeText(getBaseContext(), "Sending your file", Toast.LENGTH_LONG).show();

        }

        @Override
        protected Void doInBackground(String... params) {

            try {

                GmailUtil.sendMessage(mService, "me",
                        GmailUtil.createEmailWithAttachment(pref.getUserName() + " <" + pref.getUserEmail() + ">", pref.getUserEmail(), "", "Lets Connect-Contact List", "Here is your Contacts List", params[0], params[1]));

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
            Toast.makeText(getBaseContext(), "File Sent successfully.Check your Email", Toast.LENGTH_SHORT).show();
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
                    //  showLog("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                //     showLog("Request cancelled.");
            }
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                ContactListing.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


}
