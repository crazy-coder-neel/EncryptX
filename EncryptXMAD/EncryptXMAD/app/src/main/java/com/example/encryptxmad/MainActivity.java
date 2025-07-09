package com.example.encryptxmad;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button encryptBtn = findViewById(R.id.encryptBtn);
        Button decryptBtn = findViewById(R.id.decryptBtn);

        encryptBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EncryptActivity.class);
            startActivity(intent);
        });

        decryptBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DecryptActivity.class);
            startActivity(intent);
        });
        Button encryptImageBtn = findViewById(R.id.encryptImageBtn);
        Button decryptImageBtn = findViewById(R.id.decryptImageBtn);

        encryptImageBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EncryptImageActivity.class));
        });

        decryptImageBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, DecryptImageActivity.class));
        });
    }
}