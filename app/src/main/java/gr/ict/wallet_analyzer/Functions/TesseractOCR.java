package gr.ict.wallet_analyzer.Functions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class TesseractOCR {
    private TessBaseAPI mTess;

    private Context context;

    public TesseractOCR(Context context, String language) {
        this.context = context;

        try {
            mTess = new TessBaseAPI();

            String dataPath = context.getFilesDir().toString() + "/tesseract/tessdata/";

            String traineddataName = language + ".traineddata";
            File endFile = copyAssets("tessdata/", traineddataName, dataPath);

            String lastString = Objects.requireNonNull(endFile.getParent()).replace("tessdata", "");
            mTess.init(lastString, language); // myDir + "/tessdata/eng.traineddata" must be present


        } catch (Exception exception) {
            System.out.print("failed with message: ");
            System.out.println(exception.getMessage());
        }
    }

    private String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    private void onDestroy() {
        if (mTess != null) mTess.end();
    }

    public void doOCR(final Bitmap bitmap, final TextView textView) {
        String DIALOG_MESSAGE = "Doing OCR...";
        String DIALOG_TITLE = "Processing";

        final ProgressDialog mProgressDialog = ProgressDialog
                .show(context, DIALOG_TITLE, DIALOG_MESSAGE, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    try {
                        String srcText = getOCRResult(bitmap);

                        if (srcText != null && !srcText.equals("")) {
                            textView.setText(srcText);
                        }
                    } catch (Exception e) {
                        Log.i("OCR ERROR", Objects.requireNonNull(e.getMessage()));
                    }

                    mProgressDialog.dismiss();
                    onDestroy();
                }
            }
        }).start();
    }

    private File copyAssets(String dataPath, String fileToCopy, String externalPath) {
        File theFile = null;
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(dataPath);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            if (filename.equals(fileToCopy)) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(dataPath + filename);
                    File outFile = new File(context.getExternalFilesDir(externalPath), filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);

                    theFile = outFile;
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
        }
        return theFile;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
