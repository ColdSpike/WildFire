package com.chatting.makrandpawar.WildFire;

public class AllUsersModelClass {
    public String displayname;
    public String image;
    public String status;
    public String thumb_image;

    public AllUsersModelClass(String displayname, String image, String status, String thumb_image) {
        this.displayname = displayname;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public AllUsersModelClass() {
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public void setThumbImage(String thumbImage) {
        this.thumb_image = thumbImage;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
