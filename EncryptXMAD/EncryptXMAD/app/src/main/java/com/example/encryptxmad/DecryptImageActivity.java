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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DecryptImageActivity extends AppCompatActivity {

    private static final int PICK_FILE = 200;
    private static final int STORAGE_PERMISSION_CODE = 102;

    private ImageView imagePreview;
    private Button selectFileBtn, decryptBtn, saveBtn;
    private ProgressBar progressBar;
    private byte[] encryptedBytes;
    private Bitmap decryptedBitmap;
    private File encryptedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt_image);

        imagePreview = findViewById(R.id.imagePreview);
        selectFileBtn = findViewById(R.id.selectFileBtn);
        decryptBtn = findViewById(R.id.decryptBtn);
        saveBtn = findViewById(R.id.saveBtn);
        progressBar = findViewById(R.id.progressBar);

        selectFileBtn.setOnClickListener(v -> checkPermissionsAndOpenFilePicker());
        decryptBtn.setOnClickListener(v -> decryptImage());
        saveBtn.setOnClickListener(v -> saveDecryptedImage());
    }

    private void checkPermissionsAndOpenFilePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_FILE && data != null) {
            Uri fileUri = data.getData();
            handleFileSelection(fileUri);
        }
    }

    private void handleFileSelection(Uri fileUri) {
        try {
            encryptedFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "encrypted_temp_" + System.currentTimeMillis() + ".dat");
            InputStream in = getContentResolver().openInputStream(fileUri);
            FileOutputStream out = new FileOutputStream(encryptedFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            FileInputStream fis = new FileInputStream(encryptedFile);
            encryptedBytes = new byte[(int) encryptedFile.length()];
            fis.read(encryptedBytes);
            fis.close();
            decryptBtn.setEnabled(true);
            Toast.makeText(this, "File loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("DecryptImage", "File loading error", e);
            Toast.makeText(this, "Error loading file", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void decryptImage() {
        if (encryptedBytes == null) {
            return;
        }
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
                decryptBtn.setEnabled(false);
                saveBtn.setEnabled(false);
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                try {
                    byte[] key = ("EncryptxNJ").getBytes("UTF-8");
                    MessageDigest sha = MessageDigest.getInstance("SHA-256");
                    key = sha.digest(key);
                    key = Arrays.copyOf(key, 16);
                    SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
                    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey);
                    byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                    return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.length);
                } catch (Exception e) {
                    Log.e("DecryptImage", "Decryption error", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                progressBar.setVisibility(View.GONE);
                if (result != null) {
                    decryptedBitmap = result;
                    imagePreview.setImageBitmap(decryptedBitmap);
                    saveBtn.setEnabled(true);
                    Toast.makeText(DecryptImageActivity.this, "Image decrypted successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DecryptImageActivity.this,
                            "Decryption failed - Invalid file or key", Toast.LENGTH_LONG).show();
                    decryptBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    private void saveDecryptedImage() {
        if (decryptedBitmap == null) {
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
            String fileName = "decrypted_" + System.currentTimeMillis() + ".png";
            File file = new File(downloadsDir, fileName);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            decryptedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.close();
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/png"},
                    (path, uri) -> {
                    });
            Toast.makeText(this, "Saved to Downloads/" + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("DecryptImage", "File save error", e);
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (encryptedBytes == null) {
                    openFilePicker();
                } else {
                    saveDecryptedImage();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (encryptedFile != null && encryptedFile.exists()) {
            encryptedFile.delete();
        }
        if (decryptedBitmap != null && !decryptedBitmap.isRecycled()) {
            decryptedBitmap.recycle();
        }
        super.onDestroy();
    }
}
