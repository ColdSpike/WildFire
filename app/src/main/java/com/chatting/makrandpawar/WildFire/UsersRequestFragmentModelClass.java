package com.chatting.makrandpawar.WildFire;


public class UsersRequestFragmentModelClass {
    public String name;
    public String image;
    public String message;

    public UsersRequestFragmentModelClass(String displayname, String image, String status) {
        this.name = displayname;
        this.image = image;
        this.message = status;
    }

    public UsersRequestFragmentModelClass() {}

    public String getName() {
        return name;
    }

    public void setName(String displayname) {
        this.name = displayname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String status) {
        this.message = status;
    }
}

