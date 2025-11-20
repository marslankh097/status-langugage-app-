package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ContactList implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("subject")
    private String subject;

    public ContactList(String id, String subject) {
        this.id = id;
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
