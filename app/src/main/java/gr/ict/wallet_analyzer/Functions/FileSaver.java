package gr.ict.wallet_analyzer.Functions;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileSaver {
    private Context context;

    public FileSaver(Context context) {
        this.context = context;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = String.valueOf(new java.util.Date().getTime());

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
