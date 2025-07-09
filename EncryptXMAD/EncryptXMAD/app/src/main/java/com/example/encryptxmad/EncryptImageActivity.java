package com.example.encryptxmad;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private ImageView imagePreview;
    private Button selectImageBtn, encryptBtn, saveBtn;
    private ProgressBar progressBar;
    private Bitmap originalBitmap;
    private byte[] encryptedBytes;
    private File originalImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt_image);

        imagePreview = findViewById(R.id.imagePreview);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        encryptBtn = findViewById(R.id.encryptBtn);
        saveBtn = findViewById(R.id.saveBtn);
        progressBar = findViewById(R.id.progressBar);

        selectImageBtn.setOnClickListener(v -> checkPermissionsAndOpenGallery());
        encryptBtn.setOnClickListener(v -> encryptImage());
        saveBtn.setOnClickListener(v -> saveEncryptedImage());
    }

    private void checkPermissionsAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            handleImageSelection(imageUri);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void encryptImage() {
        if (originalBitmap == null) {
            return;
        }

        new AsyncTask<Void, Void, byte[]>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                encryptBtn.setEnabled(false);
                saveBtn.setEnabled(false);
            }

            @Override
            protected byte[] doInBackground(Void... voids) {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] imageBytes = stream.toByteArray();
                    byte[] key = ("EncryptxNJ").getBytes("UTF-8");
                    MessageDigest sha = MessageDigest.getInstance("SHA-256");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key, 16);
                    SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    return cipher.doFinal(imageBytes);
                } catch (Exception e) {
                    Log.e("EncryptImage", "Encryption error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(byte[] result) {
                progressBar.setVisibility(View.GONE);
                if (result != null) {
                    encryptedBytes = result;
                    saveBtn.setEnabled(true);
                    Toast.makeText(EncryptImageActivity.this, "Image encrypted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EncryptImageActivity.this, "Encryption failed", Toast.LENGTH_SHORT).show();
                    encryptBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    private void saveEncryptedImage() {
        if (encryptedBytes == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
            return;
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "encrypted_" + System.currentTimeMillis() + ".dat";
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(encryptedBytes);
            fos.close();

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    null,
                    (path, uri) -> {
                    });

            Toast.makeText(this, "Saved to Downloads/" + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("EncryptImage", "File save error", e);
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "temp_img_" + System.currentTimeMillis() + ".tmp");

            InputStream in = getContentResolver().openInputStream(imageUri);
            FileOutputStream out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();

            originalImageFile = tempFile;
            originalBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
            imagePreview.setImageBitmap(originalBitmap);
            encryptBtn.setEnabled(true);
        } catch (IOException e) {
            Log.e("EncryptImage", "Image loading error", e);
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (originalBitmap == null) {
                    openGallery();
                } else {
                    saveEncryptedImage();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (originalImageFile != null && originalImageFile.exists()) {
            originalImageFile.delete();
        }
        super.onDestroy();
    }
}
