package gr.ict.wallet_analyzer.helpers;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyXAxisValueFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float value) {

        // Convert float value to date string
        // Convert from seconds back to milliseconds to format time  to show to the user
        long emissionsMilliSince1970Time = (long) value;

        // Show time in local version
        Date timeMilliseconds = new Date(emissionsMilliSince1970Time);
//        DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");
        String finalString = dt.format(timeMilliseconds);
        return finalString;
    }
}