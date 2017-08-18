package com.rethink.ask.controller.db.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Model class for contact details.
 * Created by zac on 05/07/17.
 */

public class Contact extends RealmObject {
    @PrimaryKey
    String email;
    String name;
    String number;
    Boolean emailSent;
    Boolean smsSent;
    Long syncedTimeinMillis;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public Boolean getSmsSent() {
        return smsSent;
    }

    public void setSmsSent(Boolean smsSent) {
        this.smsSent = smsSent;
    }

    public Long getSyncedTimeinMillis() {
        return syncedTimeinMillis;
    }

    public void setSyncedTimeinMillis(Long syncedTimeinMillis) {
        this.syncedTimeinMillis = syncedTimeinMillis;
    }
}
