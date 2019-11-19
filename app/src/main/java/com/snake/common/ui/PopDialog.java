package com.snake.common.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.snake.common.R;


public class PopDialog extends Dialog {
    private int dialogResult = 0;
    private Handler mHandler;

    public PopDialog(Activity context) {
        super(context);
        setOwnerActivity(context);
        onCreate();
        TextView popText = (TextView) findViewById(R.id.popText);
        popText.setText("您的贪吃蛇已死亡，点击重新开始");
    }

    public int getDialogResult() {
        return dialogResult;
    }

    private void setDialogResult(int dialogResult) {
        this.dialogResult = dialogResult;
    }

    private void onCreate() {
        setContentView(R.layout.popup_window);
        findViewById(R.id.confirmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endDialog(1);
            }
        });
    }

    private void endDialog(int result) {
        dismiss();
        setDialogResult(result);
        Message m = mHandler.obtainMessage();
        mHandler.sendMessage(m);
    }

    public int showDialog() {
        super.show();
        try {
            Looper.getMainLooper().loop();
        } catch (RuntimeException e2) {
        }
        return dialogResult;
    }
}
