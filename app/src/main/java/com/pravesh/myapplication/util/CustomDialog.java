package com.pravesh.myapplication.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.pravesh.myapplication.R;

public class CustomDialog {
    Activity activity;
    String title;
    String message;
    String positiveText;
    String negativeText;
    String positiveBackground;
    String negativeBackground;
    int imageView;
    boolean isCancellable;
    CustomDialogListener positiveListener, negativeListener;
    Dialog dialog;

    public CustomDialog(Activity activity) {
        this.activity = activity;
    }

    public CustomDialog(Activity activity, String title, String message, String positiveText, String negativeText,
                        String positiveBackground, String negativeBackground, int imageView, boolean isCancellable,
                        CustomDialogListener positiveListener, CustomDialogListener negativeListener) {
        this.activity = activity;
        this.title = title;
        this.message = message;
        this.positiveText = positiveText;
        this.negativeText = negativeText;
        this.positiveBackground = positiveBackground;
        this.negativeBackground = negativeBackground;
        this.imageView = imageView;
        this.isCancellable = isCancellable;
        this.positiveListener = positiveListener;
        this.negativeListener = negativeListener;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPositiveText(String positiveText) {
        this.positiveText = positiveText;
    }

    public void setNegativeText(String negativeText) {
        this.negativeText = negativeText;
    }

    public void setPositiveBackground(String positiveBackground) {
        this.positiveBackground = positiveBackground;
    }

    public void setNegativeBackground(String negativeBackground) {
        this.negativeBackground = negativeBackground;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public void setCancellable(boolean cancellable) {
        isCancellable = cancellable;
    }

    public void setPositiveListener(CustomDialogListener positiveListener) {
        this.positiveListener = positiveListener;
    }

    public void setNegativeListener(CustomDialogListener negativeListener) {
        this.negativeListener = negativeListener;
    }

    public void show() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.layout_custom_dialog);

        ImageView imageViewDialog = dialog.findViewById(R.id.imageViewDialog);
        TextView txtTitle = dialog.findViewById(R.id.titleDialog);
        TextView txtMsg = dialog.findViewById(R.id.msgDialog);
        TextView txtPositive = dialog.findViewById(R.id.txtPositive);
        TextView txtNegative = dialog.findViewById(R.id.txtNegative);
        CardView positiveCard = dialog.findViewById(R.id.cardPositive);
        CardView negativeCard = dialog.findViewById(R.id.cardNegative);

        if (imageView != 0) {
            imageViewDialog.setImageResource(imageView);
            imageViewDialog.setVisibility(View.VISIBLE);
        }
        if (title != null) {
            txtTitle.setText(title);
        }
        if (message != null) {
            txtMsg.setText(message);
        }
        txtPositive.setText(positiveText);
        txtNegative.setText(negativeText);
        if (positiveBackground != null) {
            int color = Color.parseColor(positiveBackground);
            positiveCard.setCardBackgroundColor(color);
        }
        if (negativeBackground != null) {
            int color = Color.parseColor(negativeBackground);
            negativeCard.setCardBackgroundColor(color);
        }
        if (positiveListener != null) {
            txtPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    positiveListener.onClick();
                    if (dialog != null)
                        dialog.dismiss();
                }
            });
        }
        if (negativeListener != null) {
            txtNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    negativeListener.onClick();
                    if (dialog != null)
                        dialog.dismiss();
                }
            });
            if (isCancellable) {
                dialog.setCancelable(true);
            } else dialog.setCancelable(false);
        }

        dialog.show();

    }

    public void hide() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
