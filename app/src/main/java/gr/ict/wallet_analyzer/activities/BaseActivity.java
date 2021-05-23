package gr.ict.wallet_analyzer.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import androidx.appcompat.app.AppCompatActivity;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
//        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = Prefs.getString("My_Lang", "en");
        Context newContext = wrap(newBase, new Locale(language));
        super.attachBaseContext(newContext);
    }

    private ContextWrapper wrap(Context mContext, Locale newLocale) {
        Configuration configuration = mContext.getResources().getConfiguration();
        Locale.setDefault(newLocale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);

            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            mContext = mContext.createConfigurationContext(configuration);
            mContext.getResources().getConfiguration().setLocale(newLocale);

        } else {
            configuration.setLocale(newLocale);
            mContext = mContext.createConfigurationContext(configuration);
            mContext.getResources().getConfiguration().setLocale(newLocale);
        }

        return new ContextWrapper(mContext);
    }
}
