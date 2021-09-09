package gr.ict.wallet_analyzer.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import gr.ict.wallet_analyzer.R;

public class CategoryIconSelector {
    Context context;

    public CategoryIconSelector(Context context) {
        this.context = context;
    }

    public Drawable getDrawableIcon(String category) {
        Drawable drawable;
        switch (category) {
            case "gas station":
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_11);
                break;
            case "shoes":
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_6);
                break;
            case "clothes":
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_5);
                break;
            case "supermarket":
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_4);
                break;
            case "jewelry":
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_7);
                break;
            default:
                drawable = ContextCompat.getDrawable(context, R.drawable.asset_4);
                break;
        }
        return drawable;
    }
}
