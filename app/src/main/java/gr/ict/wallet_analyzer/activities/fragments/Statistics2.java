package gr.ict.wallet_analyzer.activities.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import gr.ict.wallet_analyzer.R;

public class Statistics2 extends Fragment {

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistics2_fragment,container,false);
//        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
