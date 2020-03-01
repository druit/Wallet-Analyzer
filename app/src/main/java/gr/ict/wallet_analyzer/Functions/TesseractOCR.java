package gr.ict.wallet_analyzer.Functions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class TesseractOCR implements Runnable {
    private final TessBaseAPI mTess;

    private Context context;
    private Bitmap bitmap;
    private ProgressDialog mProgressDialog;
    private TextView textView;

    public TesseractOCR(Context context, String language) {
        this.context = context;

        mTess = new TessBaseAPI();
        boolean fileExistFlag = false;

        AssetManager assetManager = context.getAssets();

        String dstPathDir = "/tesseract/tessdata/";

        String srcFile = language + ".traineddata";
        InputStream inFile = null;

        dstPathDir = context.getFilesDir() + dstPathDir;
        String dstInitPathDir = context.getFilesDir() + "/tesseract";
        String dstPathFile = dstPathDir + srcFile;
        FileOutputStream outFile = null;

        try {
            inFile = assetManager.open(srcFile);

            File f = new File(dstPathDir);

            if (!f.exists()) {
                if (!f.mkdirs()) {
                    Toast.makeText(context, srcFile + " can't be created.", Toast.LENGTH_SHORT).show();
                }
                outFile = new FileOutputStream(new File(dstPathFile));
            } else {
                fileExistFlag = true;
            }

        } catch (Exception ex) {
            Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
        } finally {
            if (fileExistFlag) {
                try {
                    System.out.println("here?");
                    inFile.close();
                    mTess.init(dstInitPathDir, language);
                } catch (Exception ex) {
                    Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                }
            }

            if (inFile != null && outFile != null) {
                try {
                    //copy file
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inFile.read(buf)) != -1) {
                        outFile.write(buf, 0, len);
                    }
                    inFile.close();
                    outFile.close();
                    mTess.init(dstInitPathDir, language);
                } catch (Exception ex) {
                    Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                }
            } else {
                Toast.makeText(context, srcFile + " can't be read.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getOCRResult(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    public void onDestroy() {
        if (mTess != null) mTess.end();
    }

    public void doOCR(Bitmap bitmap, TextView textView) {
        this.bitmap = bitmap;
        this.textView = textView;

        if (mProgressDialog == null) {
            String DIALOG_MESSAGE = "Doing OCR...";
            String DIALOG_TITLE = "Processing";
            mProgressDialog = ProgressDialog.show(context, DIALOG_TITLE, DIALOG_MESSAGE, true);
        } else {
            mProgressDialog.show();
        }

        new Thread(this).start();
    }

    @Override
    public void run() {
        if (bitmap != null) {
            String srcText = getOCRResult(bitmap);

            if (srcText != null && !srcText.equals("")) {
                textView.setText(srcText);
            }
            mProgressDialog.dismiss();
        }
    }
}
