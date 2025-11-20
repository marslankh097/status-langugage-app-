package com.example.status.rest;

import com.example.status.response.AVStatusRP;
import com.example.status.response.AboutUsRP;
import com.example.status.response.AccountDetailRP;
import com.example.status.response.AppRP;
import com.example.status.response.CatLanguageRP;
import com.example.status.response.CategoryRP;
import com.example.status.response.CheckOtpRP;
import com.example.status.response.CommentRP;
import com.example.status.response.ContactRP;
import com.example.status.response.DataRP;
import com.example.status.response.FaqRP;
import com.example.status.response.FavouriteRP;
import com.example.status.response.HomeRP;
import com.example.status.response.LanguageRP;
import com.example.status.response.LoginRP;
import com.example.status.response.PaymentModeRP;
import com.example.status.response.PointDetailRP;
import com.example.status.response.PrivacyPolicyRP;
import com.example.status.response.ProfileRP;
import com.example.status.response.ProfileStatusRP;
import com.example.status.response.RegisterRP;
import com.example.status.response.RewardPointRP;
import com.example.status.response.SpinnerRP;
import com.example.status.response.StatusDetailRP;
import com.example.status.response.StatusDownloadRP;
import com.example.status.response.StatusLikeRP;
import com.example.status.response.StatusRP;
import com.example.status.response.SubmitSpinnerRP;
import com.example.status.response.SuspendRP;
import com.example.status.response.TermsConditionsRP;
import com.example.status.response.TransactionDetailRP;
import com.example.status.response.URPListRP;
import com.example.status.response.UploadStatusOptRP;
import com.example.status.response.UserCommentRP;
import com.example.status.response.UserFollowRP;
import com.example.status.response.UserFollowStatusRP;
import com.example.status.response.UserRedeemRP;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {

    //get app data
    @POST("api.php")
    @FormUrlEncoded
    Call<AppRP> getAppData(@Field("data") String data);

    //login
    @POST("api.php")
    @FormUrlEncoded
    Call<LoginRP> getLogin(@Field("data") String data);

    //login check
    @POST("api.php")
    @FormUrlEncoded
    Call<LoginRP> getLoginDetail(@Field("data") String data);

    //check otp on/off
    @POST("api.php")
    @FormUrlEncoded
    Call<CheckOtpRP> getOtpStatus(@Field("data") String data);

    //send otp verification email
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> getVerification(@Field("data") String data);

    //register
    @POST("api.php")
    @FormUrlEncoded
    Call<RegisterRP> getRegisterDetail(@Field("data") String data);

    //submit reference code
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitReferenceCode(@Field("data") String data);

    //forget password
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> getForgetPassword(@Field("data") String data);

    //profile
    @POST("api.php")
    @FormUrlEncoded
    Call<ProfileRP> getProfile(@Field("data") String data);

    //edit profile
    @POST("api.php")
    @Multipart
    Call<DataRP> getEditProfile(@Part("data") RequestBody data, @Part MultipartBody.Part part);

    //update password
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> updatePassword(@Field("data") String data);

    //get language
    @POST("api.php")
    @FormUrlEncoded
    Call<LanguageRP> getLanguage(@Field("data") String data);

    //home page
    @POST("api.php")
    @FormUrlEncoded
    Call<HomeRP> getHome(@Field("data") String data);

    //category
    @POST("api.php")
    @FormUrlEncoded
    Call<CategoryRP> getCategory(@Field("data") String data);

    //get category and language list
    @POST("api.php")
    @FormUrlEncoded
    Call<CatLanguageRP> getCatLanguageRP(@Field("data") String data);

    //get status list
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusRP> getStatusList(@Field("data") String data);

    //get favourite status list
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusRP> getFavStatusList(@Field("data") String data);

    //get search status list
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusRP> getSearchList(@Field("data") String data);

    //status detail
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusDetailRP> getStatusDetail(@Field("data") String data);

    //status like
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusLikeRP> statusLike(@Field("data") String data);

    //status view
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> statusView(@Field("data") String data);

    //status download
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusDownloadRP> statusDownloadCount(@Field("data") String data);

    //Favourite
    @POST("api.php")
    @FormUrlEncoded
    Call<FavouriteRP> isFavouriteStatus(@Field("data") String data);

    //get all comment
    @POST("api.php")
    @FormUrlEncoded
    Call<CommentRP> getAllComment(@Field("data") String data);

    //comment
    @POST("api.php")
    @FormUrlEncoded
    Call<UserCommentRP> submitComment(@Field("data") String data);

    //delete comment
    @POST("api.php")
    @FormUrlEncoded
    Call<UserCommentRP> deleteComment(@Field("data") String data);

    //submit review
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitReview(@Field("data") String data);

    //get user reference code
    @POST("api.php")
    @FormUrlEncoded
    Call<ProfileRP> getUserReferenceCode(@Field("data") String data);

    //user follow and unfollow
    @POST("api.php")
    @FormUrlEncoded
    Call<UserFollowStatusRP> getUserFollowStatus(@Field("data") String data);

    //get user follower, following and user search list
    @POST("api.php")
    @FormUrlEncoded
    Call<UserFollowRP> getUserFollow(@Field("data") String data);

    //get user status list
    @POST("api.php")
    @FormUrlEncoded
    Call<StatusRP> getUserStatusList(@Field("data") String data);

    //user status delete
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> deleteStatus(@Field("data") String data);

    //get upload status option on/off
    @POST("api.php")
    @FormUrlEncoded
    Call<UploadStatusOptRP> getUploadStatusOPT(@Field("data") String data);

    //get user daily upload limit
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> getDailyUploadLimit(@Field("data") String data);

    //upload quotes
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> uploadQuotes(@Field("data") String data);

    //user profile status check
    @POST("api.php")
    @FormUrlEncoded
    Call<ProfileStatusRP> getProfileStatus(@Field("data") String data);

    //account verification request
    @POST("api.php")
    @Multipart
    Call<DataRP> submitAccountVerification(@Part("data") RequestBody data, @Part MultipartBody.Part part);

    //account verification status
    @POST("api.php")
    @FormUrlEncoded
    Call<AVStatusRP> getAVStatus(@Field("data") String data);

    //account detail
    @POST("api.php")
    @FormUrlEncoded
    Call<AccountDetailRP> getAccountDetail(@Field("data") String data);

    //delete account
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> deleteAccount(@Field("data") String data);

    //user account suspend
    @POST("api.php")
    @FormUrlEncoded
    Call<SuspendRP> getSuspend(@Field("data") String data);

    //get spinner data
    @POST("api.php")
    @FormUrlEncoded
    Call<SpinnerRP> getSpinnerData(@Field("data") String data);

    //submit spinner data
    @POST("api.php")
    @FormUrlEncoded
    Call<SubmitSpinnerRP> submitSpinnerData(@Field("data") String data);

    //get payment mode
    @POST("api.php")
    @FormUrlEncoded
    Call<PaymentModeRP> getPaymentMode(@Field("data") String data);

    //submit payment detail
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitPaymentDetail(@Field("data") String data);

    //app point detail
    @POST("api.php")
    @FormUrlEncoded
    Call<PointDetailRP> getPointDetail(@Field("data") String data);

    //get reward point
    @POST("api.php")
    @FormUrlEncoded
    Call<RewardPointRP> getUserRewardPoint(@Field("data") String data);

    //get user reward point list
    @POST("api.php")
    @FormUrlEncoded
    Call<URPListRP> getUserRewardPointList(@Field("data") String data);

    //get user redeem point history
    @POST("api.php")
    @FormUrlEncoded
    Call<UserRedeemRP> getUserRedeemHistory(@Field("data") String data);

    //get user reward point history
    @POST("api.php")
    @FormUrlEncoded
    Call<URPListRP> getURPointHistoryList(@Field("data") String data);

    //transaction detail
    @POST("api.php")
    @FormUrlEncoded
    Call<TransactionDetailRP> getTransactionDetail(@Field("data") String data);

    //get about us
    @POST("api.php")
    @FormUrlEncoded
    Call<AboutUsRP> getAboutUs(@Field("data") String data);

    //get privacy policy
    @POST("api.php")
    @FormUrlEncoded
    Call<PrivacyPolicyRP> getPrivacyPolicy(@Field("data") String data);

    //get terms condition
    @POST("api.php")
    @FormUrlEncoded
    Call<TermsConditionsRP> getTermsCondition(@Field("data") String data);

    //get faq
    @POST("api.php")
    @FormUrlEncoded
    Call<FaqRP> getFaq(@Field("data") String data);

    //get contact us list
    @POST("api.php")
    @FormUrlEncoded
    Call<ContactRP> getContactSub(@Field("data") String data);

    //Submit contact
    @POST("api.php")
    @FormUrlEncoded
    Call<DataRP> submitContact(@Field("data") String data);

}
