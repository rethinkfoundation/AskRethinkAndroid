package com.rethink.ask.controller.db;

import android.content.Context;
import android.util.Log;
import com.rethink.ask.controller.db.model.Contact;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

/**
 * Manager class for Database operations
 * Created by zac on 05/07/17.
 */
public class DBManager {
    private Realm realm;
    private long counter = 1;

    /**
     * Constructor for class DBManager
     *
     * @param context App context from the UI
     */
    public DBManager(Context context) {
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    /**
     * This public function has access from outside package and class.
     * It saves the contact details to Database. If email is already there in database,
     * it updates the existing data row without adding new row to the table
     *
     * @param name   Name of the contact
     * @param email  email of the contact
     * @param number contact number of the contact
     */
    public void saveDetails(String name, String email, String number) {

        try {
            realm.beginTransaction();
            Contact contact = realm.createObject(Contact.class, email);
            contact.setName(name);
            contact.setNumber(number);
            contact.setEmailSent(true);
            contact.setSmsSent(false);
            contact.setSyncedTimeinMillis(counter++);
            realm.insertOrUpdate(contact);
            realm.commitTransaction();
        } catch (RealmPrimaryKeyConstraintException realmPrimaryKeyConstraintException) {
            Log.d("Database", "Primary key exception occurred");
            realm.cancelTransaction();
            updateDetails(name, email, number);

        }
    }
    /**
     * Private function calls when saveDetails function is failed with realmPrimaryKeyConstraintException.
     * It updates the data corresponding to the email
     *
     * @param name   Name of the contact
     * @param email  Email of the contact
     * @param number contact number of the contact
     */
    private void updateDetails(String name, String email, String number) {
        Log.d("Database", "Update called");

        Contact contact = realm.where(Contact.class).equalTo("email", email).findFirst();
        realm.beginTransaction();
        contact.setName(name);
        contact.setNumber(number);
        contact.setEmailSent(true);
        contact.setSmsSent(false);
        contact.setSyncedTimeinMillis(counter++);
        realm.insertOrUpdate(contact);
        realm.commitTransaction();
    }
    /**
     * Public method to get all the rows from the table Contacts
     *
     * @return full contact data in the table
     */
    public RealmResults<Contact> getContactData() {
        final RealmResults<Contact> contacts = realm.where(Contact.class).findAll();
        for (Contact contact : contacts) {
            Log.d("Database", contact.getEmail() + "    " + contact.getName() + "    " + contact.getNumber() + "    " + contact.getEmailSent() + "    " + contact.getSmsSent() + "    " + contact.getSyncedTimeinMillis());
        }
        return contacts;
    }
}
