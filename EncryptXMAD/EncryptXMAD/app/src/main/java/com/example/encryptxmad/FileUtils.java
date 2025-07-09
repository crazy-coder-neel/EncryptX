package com.example.encryptxmad;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static boolean saveToDownloads(Context context, byte[] data, String fileName) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();

            MediaScannerConnection.scanFile(context,
                    new String[]{file.getAbsolutePath()},
                    null,
                    (path, uri) -> {});

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File getTempFile(Context context, String prefix, String extension) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, prefix + "_" + System.currentTimeMillis() + extension);
    }
}