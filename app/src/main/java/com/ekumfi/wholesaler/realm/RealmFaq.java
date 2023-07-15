package com.ekumfi.wholesaler.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by 2CLearning on 12/16/2017.
 */

public class RealmFaq extends RealmObject {

    private int id;
    @PrimaryKey
    private String faq_id;
    private String title;
    private String description;

    public RealmFaq() {

    }

    public RealmFaq(String faq_id, String title, String description) {
        this.faq_id = faq_id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFaq_id() {
        return faq_id;
    }

    public void setFaq_id(String faq_id) {
        this.faq_id = faq_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
