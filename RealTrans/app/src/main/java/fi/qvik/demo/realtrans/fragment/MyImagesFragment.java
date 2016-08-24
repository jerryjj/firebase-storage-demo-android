package fi.qvik.demo.realtrans.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by jja on 24/08/16.
 */
public class MyImagesFragment extends ImageListFragment {
    public MyImagesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_images_query]
        // My top images
        String myUserId = getUid();
        Query myTopPostsQuery = databaseReference.child("user-uploads").child(myUserId)
                .orderByChild("starCount");
        // [END my_images_query]

        return myTopPostsQuery;
    }
}
