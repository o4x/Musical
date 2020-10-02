package com.o4x.musical.imageloader.universalil.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import static com.o4x.musical.util.CoverUtil.createSquareCoverWithText;

public class AbsImageLoadingListener extends SimpleImageLoadingListener {

    private CoverData coverData;
    private AsyncTaskLoader<Bitmap> task;

    public void setCoverData(CoverData coverData) {
        this.coverData = coverData;
    }

    protected Context context;

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        super.onLoadingStarted(imageUri, view);
        context = view.getContext();
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        if (coverData != null) {

            task = new AsyncTaskLoader<Bitmap>(context) {
                @Override
                public Bitmap loadInBackground() {
                    return createSquareCoverWithText(
                            context, coverData.text, coverData.id, coverData.size);
                }

                @Override
                public void deliverResult(@Nullable Bitmap data) {
                    super.deliverResult(data);
                    onFailedBitmapReady(data);
                }
            };

            task.forceLoad();
        }
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        if (task != null) task.cancelLoad();
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
    }

    public void onFailedBitmapReady(Bitmap failedBitmap){
        coverData.image.setImageBitmap(failedBitmap);
    }

    public static class CoverData {
        final long id;
        final ImageView image;
        final String text;
        final int size;

        public CoverData(long id, ImageView image, String text, int size) {
            this.id = id;
            this.image = image;
            this.text = text;
            this.size = size;
        }
    }
}
