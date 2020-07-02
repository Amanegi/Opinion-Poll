package com.pravesh.myapplication.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.pravesh.myapplication.R;
import com.pravesh.myapplication.admin.activities.AdminDashboardActivity;

public class SplashActivity extends AppCompatActivity {
    private LottieAnimationView loadingAnimation;
    int splashTime = 1;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        loadingAnimation = findViewById(R.id.animation_view);
        loadingAnimation.playAnimation();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        final String phone = sharedPreferences.getString("phone", "");
        final String type = sharedPreferences.getString("type", "");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (phone.equals("")) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                if (!phone.equals("") && type.equals("admin")) {
                    startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                }
                if (!phone.equals("") && type.equals("user")) {
                    Intent i = new Intent(SplashActivity.this, VoteActivity.class);
                    i.putExtra("type","userType");
                    startActivity(i);
                }
                finish();
            }
        }, 1000 * splashTime);


    }
}
