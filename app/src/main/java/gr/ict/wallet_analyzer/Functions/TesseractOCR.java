package gr.ict.wallet_analyzer.Functions;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class TesseractOCR implements Runnable {
    private TessBaseAPI mTess;

    private Context context;

    public TesseractOCR(Context context, String language) {
        this.context = context;

        try {
            mTess = new TessBaseAPI();

            String appFolder = context.getFilesDir().toString();

            String dataPath = appFolder + "/tesseract/";
            String langPath = dataPath + "tessdata/" + language + ".traineddata/";

            System.out.println(dataPath);
            System.out.println(langPath);

            File f = new File(langPath);

            if (!f.exists()) {
                if (!f.mkdirs()) {
                    System.out.println(langPath + "can't be created! ----------");
                    Toast.makeText(context, langPath + " can't be created.", Toast.LENGTH_SHORT).show();
                }
            }


//            ----------------------------


            mTess.init(dataPath, language); // myDir + "/tessdata/eng.traineddata" must be present


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
                    String srcText = getOCRResult(bitmap);

                    if (srcText != null && !srcText.equals("")) {
                        textView.setText(srcText);
                    }
                    mProgressDialog.dismiss();
                    onDestroy();
                }
            }
        }).start();
    }

    @Override
    public void run() {
//        if (bitmap != null) {
//            String srcText = getOCRResult(bitmap);
//
//            if (srcText != null && !srcText.equals("")) {
//                textView.setText(srcText);
//            }
//            mProgressDialog.dismiss();
//            onDestroy();
//        }
    }
}
