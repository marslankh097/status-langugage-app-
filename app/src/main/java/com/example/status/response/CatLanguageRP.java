package com.example.status.response;

import com.example.status.item.CategoryList;
import com.example.status.item.LanguageList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CatLanguageRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("category_list")
    private List<CategoryList> categoryLists;

    @SerializedName("language_list")
    private List<LanguageList> languageLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CategoryList> getCategoryLists() {
        return categoryLists;
    }

    public List<LanguageList> getLanguageLists() {
        return languageLists;
    }
}
