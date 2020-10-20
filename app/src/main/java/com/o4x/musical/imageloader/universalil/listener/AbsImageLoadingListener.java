package com.o4x.musical.imageloader.universalil.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.o4x.musical.imageloader.model.CoverData;

import static com.o4x.musical.util.CoverUtil.createSquareCoverWithText;

public class AbsImageLoadingListener extends SimpleImageLoadingListener {

    public CoverData coverData;
    public boolean isLoadColorSync = false;


    private AsyncTaskLoader<Bitmap> task;
    protected Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        super.onLoadingStarted(imageUri, view);
        if (view != null)
            context = view.getContext();
        if (coverData != null && !isLoadColorSync) {

            task = new AsyncTaskLoader<Bitmap>(context) {
                @Override
                public Bitmap loadInBackground() {
                    return createSquareCoverWithText(
                            context, coverData.text, coverData.id, coverData.size);
                }

                @Override
                public void deliverResult(@Nullable Bitmap data) {
                    if (coverData.image != null)
                    if (coverData.image.getDrawable() != null) return;

                    super.deliverResult(data);
                    onFailedBitmapReady(data);
                }
            };

            task.forceLoad();
        }
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        super.onLoadingCancelled(imageUri, view);
        if (task != null) task.cancelLoad();
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        super.onLoadingComplete(imageUri, view, loadedImage);
        if (task != null) task.cancelLoad();
        if (coverData.image != null) {
            coverData.image.setBackground(null);
        }
    }

    public void onFailedBitmapReady(Bitmap failedBitmap){
        if (coverData.image != null) {
            coverData.image.setBackground(new BitmapDrawable(context.getResources(), failedBitmap));
//            coverData.image.setImageBitmap(failedBitmap);
        }
    }
}
