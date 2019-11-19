package com.snake.common.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.snake.common.R;
import com.snake.common.base.BaseAvtivity;

public class StartActivity extends BaseAvtivity {
    private static final String TAG = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.start_layout);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.d(TAG, "onCreate: Width:"+dm.widthPixels);
        Log.d(TAG, "onCreate: Height:"+dm.heightPixels);
        Button startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, Game.class);
                startActivity(intent);
            }
        });
    }


}
