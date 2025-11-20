package com.example.status.response;

import com.example.status.item.RewardPointList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class URPListRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ANDROID_REWARDS_APP")
    private List<RewardPointList> rewardPointLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<RewardPointList> getRewardPointLists() {
        return rewardPointLists;
    }
}
