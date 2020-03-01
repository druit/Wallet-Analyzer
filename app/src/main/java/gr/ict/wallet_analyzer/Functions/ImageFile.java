package gr.ict.wallet_analyzer.Functions;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class ImageFile {
    private Context context;

    public ImageFile(Context context) {
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
