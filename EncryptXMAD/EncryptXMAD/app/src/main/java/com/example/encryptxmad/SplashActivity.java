package com.example.encryptxmad;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
            splashScreen.setKeepOnScreenCondition(() -> true);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView logo = findViewById(R.id.splash_logo);
        TextView appName = findViewById(R.id.splash_app_name);
        TextView slogan = findViewById(R.id.splash_slogan);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        logo.startAnimation(pulse);
        appName.setAlpha(0f);
        appName.animate().alpha(1f).setDuration(1200).setStartDelay(300).start();
        slogan.setAlpha(0f);
        slogan.animate().alpha(0.85f).setDuration(800).setStartDelay(500).start();
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            finish();
        }, SPLASH_DURATION);
    }
    @Override
    protected void onDestroy() {
        ImageView logo = findViewById(R.id.splash_logo);
        if (logo != null) {
            logo.clearAnimation();
        }
        super.onDestroy();
    }
}