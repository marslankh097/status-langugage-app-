package com.example.status.util;

public class Events {

    // Event used to update view and like
    public static class InfoUpdate {

        private String id, type, status_layout, status_type, view, total_like, already_like;
        private int position;

        public InfoUpdate(String id, String type, String status_layout, String status_type, String view, String total_like, String already_like, int position) {
            this.id = id;
            this.type = type;
            this.status_layout = status_layout;
            this.status_type = status_type;
            this.view = view;
            this.total_like = total_like;
            this.already_like = already_like;
            this.position = position;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getStatus_layout() {
            return status_layout;
        }

        public String getStatus_type() {
            return status_type;
        }

        public String getView() {
            return view;
        }

        public String getTotal_like() {
            return total_like;
        }

        public String getAlready_like() {
            return already_like;
        }

        public int getPosition() {
            return position;
        }
    }

    //Event used to download update
    public static class DownloadUpdate {

        private String id, status_type, download_count;

        public DownloadUpdate(String id, String status_type, String download_count) {
            this.id = id;
            this.status_type = status_type;
            this.download_count = download_count;
        }

        public String getId() {
            return id;
        }

        public String getStatus_type() {
            return status_type;
        }

        public String getDownload_count() {
            return download_count;
        }
    }

    // Event used to send message from stop player.
    public static class StopPlay {
        private String play;

        public StopPlay(String play) {
            this.play = play;
        }

        public String getPlay() {
            return play;
        }
    }

    // Event used to favourite or not check notify.
    public static class FavouriteNotify {
        private String id, status_layout, is_favourite, status_type;

        public FavouriteNotify(String id, String status_layout, String is_favourite, String status_type) {
            this.id = id;
            this.status_layout = status_layout;
            this.is_favourite = is_favourite;
            this.status_type = status_type;
        }

        public String getId() {
            return id;
        }

        public String getStatus_layout() {
            return status_layout;
        }

        public String getIs_favourite() {
            return is_favourite;
        }

        public String getStatus_type() {
            return status_type;
        }
    }

    // Event used to send message from reward data notify.
    public static class RewardNotify {
        private String reward;

        public RewardNotify(String reward) {
            this.reward = reward;
        }

        public String getReward() {
            return reward;
        }
    }

    // Event used to comment notify
    public static class Comment {

        private String status, deleteCommentId, commentId, userId, userName, userImage, postId, statusType, commentText, commentDate, totalComment,type;

        public Comment(String status, String deleteCommentId, String commentId, String userId, String userName, String userImage, String postId, String statusType, String commentText, String commentDate, String totalComment, String type) {
            this.status = status;
            this.deleteCommentId = deleteCommentId;
            this.commentId = commentId;
            this.userId = userId;
            this.userName = userName;
            this.userImage = userImage;
            this.postId = postId;
            this.statusType = statusType;
            this.commentText = commentText;
            this.commentDate = commentDate;
            this.totalComment = totalComment;
            this.type = type;
        }

        public String getStatus() {
            return status;
        }

        public String getDeleteCommentId() {
            return deleteCommentId;
        }

        public String getCommentId() {
            return commentId;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserImage() {
            return userImage;
        }

        public String getPostId() {
            return postId;
        }

        public String getStatusType() {
            return statusType;
        }

        public String getCommentText() {
            return commentText;
        }

        public String getCommentDate() {
            return commentDate;
        }

        public String getTotalComment() {
            return totalComment;
        }

        public String getType() {
            return type;
        }
    }

    // Event used to send message from login notify.
    public static class Login {
        private String login;

        public Login(String login) {
            this.login = login;
        }

        public String getLogin() {
            return login;
        }
    }

    // Event used to image status notify.
    public static class ImageStatusNotify {
        private String imageNotify;

        public ImageStatusNotify(String imageNotify) {
            this.imageNotify = imageNotify;
        }

        public String getImageNotify() {
            return imageNotify;
        }

    }

    // Event used to video status notify.
    public static class VideoStatusNotify {
        private String videoNotify;

        public VideoStatusNotify(String videoNotify) {
            this.videoNotify = videoNotify;
        }

        public String getVideoNotify() {
            return videoNotify;
        }

    }

    // Event used to full screen notify.
    public static class FullScreenNotify {
        private boolean fullscreen;

        public FullScreenNotify(boolean fullscreen) {
            this.fullscreen = fullscreen;
        }

        public boolean isFullscreen() {
            return fullscreen;
        }
    }

    //Event used to select category notify.
    public static class Select {

        private String string;

        public Select(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    //Event image,gif and video upload notify
    public static class UploadFinish {

        private String upload;

        public UploadFinish(String upload) {
            this.upload = upload;
        }

        public String getUpload() {
            return upload;
        }
    }

    //Event use to language selection notify
    public static class Language {

        private String type;

        public Language(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    //Event used to update profile
    public static class ProfileUpdate {

        private String string;

        public ProfileUpdate(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    //Event used to update remove and update image
    public static class ProImage {

        private String string, imagePath;
        private boolean isProfile, isRemove;

        public ProImage(String string, String imagePath, boolean isProfile, boolean isRemove) {
            this.string = string;
            this.imagePath = imagePath;
            this.isProfile = isProfile;
            this.isRemove = isRemove;
        }

        public String getString() {
            return string;
        }

        public String getImagePath() {
            return imagePath;
        }

        public boolean isProfile() {
            return isProfile;
        }

        public boolean isRemove() {
            return isRemove;
        }
    }
    public static class CategoryHome{

    }
}
