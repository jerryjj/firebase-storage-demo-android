package fi.qvik.demo.realtrans.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import fi.qvik.demo.realtrans.R;
import fi.qvik.demo.realtrans.models.Upload;
import fi.qvik.demo.realtrans.viewholder.UploadViewHolder;

/**
 * Created by jja on 24/08/16.
 */
public abstract class ImageListFragment extends Fragment {
    private static final String TAG = "ImageListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Upload, UploadViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public ImageListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_images, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.images_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        final Context context = this.getContext();

        // Set up FirebaseRecyclerAdapter with the Query
        Query uploadsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Upload, UploadViewHolder>(Upload.class, R.layout.item_image,
                UploadViewHolder.class, uploadsQuery) {
            @Override
            protected void populateViewHolder(final UploadViewHolder viewHolder, final Upload model, final int position) {
                final DatabaseReference upsRef = getRef(position);

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToUpload(context, model);
            }
        };

        // Scroll to top on new uploads
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mManager.smoothScrollToPosition(mRecycler, null, mAdapter.getItemCount());
            }
        });

        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
