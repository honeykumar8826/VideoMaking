package com.videoMaking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.videoMaking.R;
import com.videoMaking.modal.ImageModal;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageLoadAdapter extends RecyclerView.Adapter<ImageLoadAdapter.ImageViewHolder> {
    private Context context;
    private List<ImageModal> imageModalList;

    public ImageLoadAdapter(Context context, List<ImageModal> imageModalList) {
        this.context = context;
        this.imageModalList = imageModalList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontal_image_layout, viewGroup, false);
        return new ImageLoadAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        final ImageModal newsInfoModal = imageModalList.get(i);
        final String imgUrl = newsInfoModal.getImgUrl();

        Glide.with(context)
                .asBitmap()
                .load(imgUrl)
                .placeholder(R.drawable.placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(imageViewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageModalList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_load)
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

