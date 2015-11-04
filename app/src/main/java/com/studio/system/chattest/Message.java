package com.studio.system.chattest;

/**
 * Created by N1cK0 on 15/03/13.
 */
public class Message {
    private String message;
    private boolean user;

    public Message(String message, boolean user){
        this.message = message;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public boolean getUser(){
        return user;
    }
}
