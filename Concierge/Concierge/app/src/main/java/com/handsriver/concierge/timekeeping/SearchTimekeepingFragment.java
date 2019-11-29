package com.handsriver.concierge.timekeeping;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;

/**
 * Created by Created by alain_r._trouve_silva after 21-03-17.
 */

public class SearchTimekeepingFragment extends Fragment {
    View rootView;
    LinearLayout linearLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_timekeeping, container, false);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.linearSearchTimekeeping);
        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Utility.hideKeyboard(v,getContext());
                return false;
            }
        });
        return rootView;
    }
}