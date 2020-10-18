package com.o4x.musical.imageloader.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.o4x.musical.imageloader.model.CoverData;
import com.o4x.musical.util.CoverUtil;

public class CustomCoverUtil {

    public static Bitmap createCustomCover(CoverData coverData) {
        if (coverData.context == null) return null;

        return CoverUtil.createSquareCoverWithText(
                        coverData.context,
                        coverData.text,
                        coverData.id,
                        coverData.size
                );
    }
}
