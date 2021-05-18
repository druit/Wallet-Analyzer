package gr.ict.wallet_analyzer.helpers;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

import gr.ict.wallet_analyzer.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class BubbleChoicePopup {

    protected final Activity activity;
    protected ArrayList<String> allWords;
    protected FlexboxLayout keywordFlexBox;
    protected TextView finalWordTextView;
    protected TextView title;
    protected PopupWindow bubbleChoicePopup;
    protected Button clearButton;

    public BubbleChoicePopup(Activity activity, ArrayList<String> allWords) {
        this.activity = activity;
        this.allWords = allWords;
        this.showBubblePicker(activity.findViewById(R.id.iv_crop));
    }

    // TODO
    private void comboWords() {

    }

    private void showBubblePicker(View viewToShowAt) {
        // inflate the layout of the popup window`
        LayoutInflater genericInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View bubbleChoiceView = genericInflater.inflate(R.layout.bubble_choice_popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        bubbleChoicePopup = new PopupWindow(bubbleChoiceView, width, height, true);

        keywordFlexBox = bubbleChoiceView.findViewById(R.id.keywords_flex_box);
        finalWordTextView = bubbleChoiceView.findViewById(R.id.final_word_text_view);
        title = bubbleChoiceView.findViewById(R.id.bubble_choice_title);

        // clear button
        clearButton = bubbleChoiceView.findViewById(R.id.clear_final_word_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalWordTextView.setText("");
                keywordFlexBox.removeAllViews();
                populateWords();
            }
        });

        // finish button
        Button finishButton = bubbleChoiceView.findViewById(R.id.finish_choice_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishButton();
            }
        });

        // show the bubble window
        bubbleChoicePopup.showAtLocation(viewToShowAt, Gravity.CENTER, 0, 0);

        populateWords();
    }

    protected void finishButton() {
        bubbleChoicePopup.dismiss();
    }

    private void populateWords() {
        for (String word : allWords) {
            addTextView(word);
        }
    }

    private void addTextView(String word) {
        final TextView keywordTextView = (TextView) LayoutInflater.from(activity).inflate(R.layout.keyword_text_view, null);
        keywordTextView.setText(word);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 10, 20);
        keywordTextView.setLayoutParams(params);

        keywordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = ((TextView) v).getText().toString();
                finalWordTextView.append(" " + word);
                keywordFlexBox.removeView(v);
            }
        });

        keywordFlexBox.addView(keywordTextView);
    }
}
