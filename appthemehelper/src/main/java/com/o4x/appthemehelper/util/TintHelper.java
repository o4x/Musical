package com.o4x.appthemehelper.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.AttrRes;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.o4x.appthemehelper.R;
import java.lang.reflect.Field;

/** @author afollestad, plusCubed */
public final class TintHelper {

  @SuppressWarnings("JavaReflectionMemberAccess")
  public static void colorHandles(@NonNull TextView view, int color) {
    try {
      Field editorField = TextView.class.getDeclaredField("mEditor");
      if (!editorField.isAccessible()) {
        editorField.setAccessible(true);
      }

      Object editor = editorField.get(view);
      Class<?> editorClass = editor.getClass();

      String[] handleNames = {"mSelectHandleLeft", "mSelectHandleRight", "mSelectHandleCenter"};
      String[] resNames = {
        "mTextSelectHandleLeftRes", "mTextSelectHandleRightRes", "mTextSelectHandleRes"
      };

      for (int i = 0; i < handleNames.length; i++) {
        Field handleField = editorClass.getDeclaredField(handleNames[i]);
        if (!handleField.isAccessible()) {
          handleField.setAccessible(true);
        }

        Drawable handleDrawable = (Drawable) handleField.get(editor);

        if (handleDrawable == null) {
          Field resField = TextView.class.getDeclaredField(resNames[i]);
          if (!resField.isAccessible()) {
            resField.setAccessible(true);
          }
          int resId = resField.getInt(view);
          handleDrawable = view.getResources().getDrawable(resId);
        }

        if (handleDrawable != null) {
          Drawable drawable = handleDrawable.mutate();
          drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
          handleField.set(editor, drawable);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @CheckResult
  @NonNull
  public static Drawable createTintedDrawable(
      Context context, @DrawableRes int res, @ColorInt int color) {
    Drawable drawable = ContextCompat.getDrawable(context, res);
    return createTintedDrawable(drawable, color);
  }

  @CheckResult
  @NonNull
  public static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
    if (drawable == null) {
      return null;
    }
    drawable = DrawableCompat.wrap(drawable.mutate());
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
    DrawableCompat.setTint(drawable, color);
    return drawable;
  }

  @CheckResult
  @Nullable
  public static Drawable createTintedDrawable(
      @Nullable Drawable drawable, @NonNull ColorStateList sl) {
    if (drawable == null) {
      return null;
    }
    Drawable temp = DrawableCompat.wrap(drawable.mutate());
    DrawableCompat.setTintList(temp, sl);
    return temp;
  }

  public static void setCursorTint(@NonNull EditText editText, @ColorInt int color) {
    try {
      Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
      fCursorDrawableRes.setAccessible(true);
      int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
      Field fEditor = TextView.class.getDeclaredField("mEditor");
      fEditor.setAccessible(true);
      Object editor = fEditor.get(editText);
      Class<?> clazz = editor.getClass();
      Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
      fCursorDrawable.setAccessible(true);
      Drawable[] drawables = new Drawable[2];
      drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
      drawables[0] = createTintedDrawable(drawables[0], color);
      drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
      drawables[1] = createTintedDrawable(drawables[1], color);
      fCursorDrawable.set(editor, drawables);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setTint(@NonNull RadioButton radioButton, @ColorInt int color, boolean useDarker) {
    ColorStateList sl =
        new ColorStateList(
            new int[][] {
              new int[] {-android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
            },
            new int[] {
              ColorUtil.INSTANCE.stripAlpha(
                  ContextCompat.getColor(
                      radioButton.getContext(),
                      useDarker
                          ? R.color.ate_control_disabled_dark
                          : R.color.ate_control_disabled_light)),
              ContextCompat.getColor(
                  radioButton.getContext(),
                  useDarker ? R.color.ate_control_normal_dark : R.color.ate_control_normal_light),
              color
            });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      radioButton.setButtonTintList(sl);
    } else {
      TypedValue value = new TypedValue();
      radioButton.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorSingle, value, true);
      Drawable d = createTintedDrawable(ContextCompat.getDrawable(radioButton.getContext(), value.resourceId), sl);
      radioButton.setButtonDrawable(d);
    }
  }

  public static void setTint(@NonNull SeekBar seekBar, @ColorInt int color, boolean useDarker) {
    final ColorStateList s1 =
        getDisabledColorStateList(
            color,
            ContextCompat.getColor(
                seekBar.getContext(),
                useDarker
                    ? R.color.ate_control_disabled_dark
                    : R.color.ate_control_disabled_light));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      seekBar.setThumbTintList(s1);
      seekBar.setProgressTintList(s1);
    } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
      Drawable progressDrawable = createTintedDrawable(seekBar.getProgressDrawable(), s1);
      seekBar.setProgressDrawable(progressDrawable);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        Drawable thumbDrawable = createTintedDrawable(seekBar.getThumb(), s1);
        seekBar.setThumb(thumbDrawable);
      }
    } else {
      PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
        mode = PorterDuff.Mode.MULTIPLY;
      }
      if (seekBar.getIndeterminateDrawable() != null) {
        seekBar.getIndeterminateDrawable().setColorFilter(color, mode);
      }
      if (seekBar.getProgressDrawable() != null) {
        seekBar.getProgressDrawable().setColorFilter(color, mode);
      }
    }
  }

  public static void setTint(@NonNull ProgressBar progressBar, @ColorInt int color) {
    setTint(progressBar, color, false);
  }

  public static void setTint(
      @NonNull ProgressBar progressBar, @ColorInt int color, boolean skipIndeterminate) {
    ColorStateList sl = ColorStateList.valueOf(color);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      progressBar.setProgressTintList(sl);
      progressBar.setSecondaryProgressTintList(sl);
      if (!skipIndeterminate) {
        progressBar.setIndeterminateTintList(sl);
      }
    } else {
      PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
      if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
        mode = PorterDuff.Mode.MULTIPLY;
      }
      if (!skipIndeterminate && progressBar.getIndeterminateDrawable() != null) {
        progressBar.getIndeterminateDrawable().setColorFilter(color, mode);
      }
      if (progressBar.getProgressDrawable() != null) {
        progressBar.getProgressDrawable().setColorFilter(color, mode);
      }
    }
  }

  @SuppressLint("RestrictedApi")
  public static void setTint(@NonNull EditText editText, @ColorInt int color, boolean useDarker) {
    final ColorStateList editTextColorStateList =
        new ColorStateList(
            new int[][] {
              new int[] {-android.R.attr.state_enabled},
              new int[] {
                android.R.attr.state_enabled,
                -android.R.attr.state_pressed,
                -android.R.attr.state_focused
              },
              new int[] {}
            },
            new int[] {
              ContextCompat.getColor(
                  editText.getContext(),
                  useDarker ? R.color.ate_text_disabled_dark : R.color.ate_text_disabled_light),
              ContextCompat.getColor(
                  editText.getContext(),
                  useDarker ? R.color.ate_control_normal_dark : R.color.ate_control_normal_light),
              color
            });
    if (editText instanceof AppCompatEditText) {
      ((AppCompatEditText) editText).setSupportBackgroundTintList(editTextColorStateList);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      editText.setBackgroundTintList(editTextColorStateList);
    }
    setCursorTint(editText, color);
  }

  public static void setTint(@NonNull CheckBox box, @ColorInt int color, boolean useDarker) {
    ColorStateList sl =
        new ColorStateList(
            new int[][] {
              new int[] {-android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
            },
            new int[] {
              ContextCompat.getColor(
                  box.getContext(),
                  useDarker
                      ? R.color.ate_control_disabled_dark
                      : R.color.ate_control_disabled_light),
              ContextCompat.getColor(
                  box.getContext(),
                  useDarker ? R.color.ate_control_normal_dark : R.color.ate_control_normal_light),
              color
            });
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      box.setButtonTintList(sl);
    } else {
        TypedValue value = new TypedValue();
        box.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
        Drawable drawable = createTintedDrawable(ContextCompat.getDrawable(box.getContext(), value.resourceId), sl);
        box.setButtonDrawable(drawable);
    }
  }

  public static void setTint(@NonNull ImageView image, @ColorInt int color) {
    image.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
  }

  public static void setTint(@NonNull Switch switchView, @ColorInt int color, boolean useDarker) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
      return;
    }
    // Not sure how to tint this
    //    if (switchView.getTrackDrawable() != null) {
    //      switchView.setTrackDrawable(
    //          modifySwitchDrawable(
    //              switchView.getContext(),
    // switchView.getTrackDrawable(), color, false, false, useDarker));
    //    }
    //    if (switchView.getThumbDrawable() != null) {
    //      switchView.setThumbDrawable(
    //          modifySwitchDrawable(
    //              switchView.getContext(),
    // switchView.getThumbDrawable(), color, true, false, useDarker));
    //    }
  }

  public static void setTint(@NonNull SwitchCompat switchView, @ColorInt int color, boolean useDarker) {
    if (switchView.getTrackDrawable() != null) {
      switchView.setTrackDrawable(
          modifySwitchDrawable(
              switchView.getContext(),
              switchView.getTrackDrawable(),
              color,
              false,
              false,
              useDarker));
    }
    if (switchView.getThumbDrawable() != null) {
      switchView.setThumbDrawable(
          modifySwitchDrawable(
              switchView.getContext(),
              switchView.getThumbDrawable(),
              color,
              true,
              false,
              useDarker));
    }
  }

  public static void setTintAuto(
      @NonNull View view, @ColorInt int color, boolean background, boolean useDarker) {
    if (view instanceof ProgressBar) {
      setTint(((ProgressBar) view), color, useDarker);
    } else if (view instanceof EditText) {
      setTint(((EditText) view), color, useDarker);
    } else if (view instanceof CheckBox) {
      setTint(((CheckBox) view), color, useDarker);
    } else if (view instanceof RadioButton) {
      setTint(((RadioButton) view), color, useDarker);
    } else if (view instanceof SeekBar) {
      setTint(((SeekBar) view), color, useDarker);
    } else if (view instanceof Switch) {
      setTint(((Switch) view), color, useDarker);
    } else if (view instanceof SwitchCompat) {
      setTint(((SwitchCompat) view), color, useDarker);
    } else if (view instanceof ImageView) {
      if (!background) {
        setTint(((ImageView) view), color);
      } else {
        setBackgroundTint(view, color);
      }
    } else if (view instanceof Button) {
      setTint(((Button) view), color, useDarker);
    } else if (view instanceof FloatingActionButton) {
      setTint(((FloatingActionButton) view), color, useDarker);
    } else if (background) {
      setBackgroundTint(view, color);
    }
  }

  public static void setTint(@NonNull Button button, @ColorInt int color, boolean useDarker) {
    ColorStateList sl = new ColorStateList(new int[][] {new int[] {android.R.attr.state_enabled}}, new int[] {color});
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && button.getBackground() instanceof RippleDrawable) {
      RippleDrawable rd = (RippleDrawable) button.getBackground();
      rd.setColor(sl);
    } else {
      setBackgroundTint(button, color);
    }
  }

  public static void setTint(FloatingActionButton fab, @ColorInt int color, boolean useDarker) {
    ColorStateList sl = new ColorStateList(new int[][] {new int[] {android.R.attr.state_enabled}}, new int[] {color});
    fab.setBackgroundTintList(sl);
  }

  private static Drawable modifySwitchDrawable(
      @NonNull Context context,
      @NonNull Drawable from,
      @ColorInt int tint,
      boolean thumb,
      boolean compatSwitch,
      boolean useDarker) {
    ColorStateList sl =
        new ColorStateList(
            new int[][] {
              new int[] {-android.R.attr.state_enabled},
              new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked},
              new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}
            },
            new int[] {
              ContextCompat.getColor(
                  context,
                  useDarker
                      ? R.color.ate_control_disabled_dark
                      : R.color.ate_control_disabled_light),
              ContextCompat.getColor(
                  context,
                  useDarker
                      ? (thumb
                          ? R.color.ate_control_normal_dark
                          : R.color.ate_control_normal_dark)
                      : (thumb
                          ? R.color.ate_control_normal_light
                          : R.color.ate_control_normal_light)),
              tint
            });
    return createTintedDrawable(from, sl);
  }

  private static void setBackgroundTint(View view, @ColorInt int color) {
    if (view == null) {
      return;
    }
    Drawable d = createTintedDrawable(view.getBackground(), color);
    view.setBackground(d);
  }

  @CheckResult
  @NonNull
  private static ColorStateList getDisabledColorStateList(
      @ColorInt int normal, @ColorInt int disabled) {
    return new ColorStateList(
        new int[][] {new int[] {-android.R.attr.state_enabled}, new int[] {android.R.attr.state_enabled}},
        new int[] {disabled, normal});
  }
}