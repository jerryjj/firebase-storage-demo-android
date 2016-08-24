package fi.qvik.demo.realtrans.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jja on 23/08/16.
 */
@IgnoreExtraProperties
public class Upload {
    public String dUrl;
    public String uid;
    public String nickname;
    public Long createdAt;

    public Upload() {
        // Default constructor required for calls to DataSnapshot.getValue(Upload.class)
    }

    public Upload(String dUrl, String uid, String nickname) {
        this.dUrl = dUrl;
        this.uid = uid;
        this.nickname = nickname;
    }

    @Exclude
    public Long getCreatedAtLong() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("nickname", nickname);
        result.put("dUrl", dUrl);
        result.put("createdAt", ServerValue.TIMESTAMP);

        return result;
    }
}
