package com.example.makrandpawar.chatla;

class UsersFriendsFragmentModelClass {
    String name;
    String image;
    int chatactive;
    String chatroom;

    public UsersFriendsFragmentModelClass(String name, String image, int chatactive, String chatroom) {
        this.name = name;
        this.image = image;
        this.chatroom = chatroom;
        this.chatactive = chatactive;
    }

    public String getChatroom() {
        return chatroom;
    }

    public void setChatroom(String chatroom) {
        this.chatroom = chatroom;
    }

    public int getChatactive() {
        return chatactive;
    }

    public void setChatactive(int chatactive) {
        this.chatactive = chatactive;
    }

    public String getName() {return name;}

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public UsersFriendsFragmentModelClass() {

    }
}
