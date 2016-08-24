package fi.qvik.demo.realtrans.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * Created by jja on 24/08/16.
 */
public class RecentImagesFragment extends ImageListFragment {
    public RecentImagesFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_uploads_query]
        // Last 40 uploads, these are automatically the 40 most recent
        // due to sorting by push() keys
        Query recentUploadsQuery = databaseReference.child("uploads")
                .limitToFirst(40);
        // [END recent_uploads_query]

        return recentUploadsQuery ;
    }
}
