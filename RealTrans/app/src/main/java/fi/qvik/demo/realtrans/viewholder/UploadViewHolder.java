package fi.qvik.demo.realtrans.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fi.qvik.demo.realtrans.R;
import fi.qvik.demo.realtrans.models.Upload;

/**
 * Created by jja on 24/08/16.
 */
public class UploadViewHolder extends RecyclerView.ViewHolder {
    public TextView nicknameView;
    public ImageView thumbView;

    public UploadViewHolder(View itemView) {
        super(itemView);

        nicknameView = (TextView) itemView.findViewById(R.id.nickname);
        thumbView = (ImageView) itemView.findViewById(R.id.image);
    }

    public void bindToUpload(Context context, Upload upload) {
        nicknameView.setText(upload.nickname);
        Picasso.with(context).load(upload.dUrl).into(thumbView);
    }
}
