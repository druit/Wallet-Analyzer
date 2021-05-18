package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.widget.Button;

import java.util.ArrayList;

import gr.ict.wallet_analyzer.activities.ChooseScanActivity;

public class ReceiptBubbleChoice extends BubbleChoicePopup {
    private Button button;

    public ReceiptBubbleChoice(Activity activity, ArrayList<String> allWords) {
        super(activity, allWords);
    }

    public ReceiptBubbleChoice(Activity activity, ArrayList<String> allWords, Button button) {
        super(activity, allWords);
        this.button = button;
        this.title.setText("Select shop name");
    }

    protected void finishButton() {
        if (ChooseScanActivity.cropFlag.equals(ChooseScanActivity.SHOP_NAME_STEP)) {
            ChooseScanActivity.shopName = finalWordTextView.getText().toString();
            ChooseScanActivity.cropFlag = ChooseScanActivity.LOCATION_STEP;
            super.clearButton.performClick();
            this.title.setText("Select shop address");
        } else if (ChooseScanActivity.cropFlag.equals(ChooseScanActivity.LOCATION_STEP)) {
            ChooseScanActivity.shopAddress = finalWordTextView.getText().toString();
            ChooseScanActivity.cropFlag = ChooseScanActivity.PRODUCT_STEP;
            super.finishButton();
//            button.performClick();
        }
    }
}
