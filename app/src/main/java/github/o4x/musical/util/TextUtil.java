package github.o4x.musical.util;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.NonNull;

public class TextUtil {
    public static Spanned makeTextWithTitle(@NonNull Context context, int titleResId, String text) {
        return Html.fromHtml("<b>" + context.getResources().getString(titleResId) + ": " + "</b>" + text);
    }
}
