package fi.qvik.demo.realtrans.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by jja on 23/08/16.
 */
@IgnoreExtraProperties
public class User {

    public String nickname;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String nickname) {
        this.nickname = nickname;
    }
}
