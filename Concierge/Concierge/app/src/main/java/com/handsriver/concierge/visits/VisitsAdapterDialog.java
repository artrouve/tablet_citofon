package com.handsriver.concierge.visits;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.handsriver.concierge.R;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 13-02-17.
 */

public class VisitsAdapterDialog extends ArrayAdapter<Visit> {

    private ArrayList<Visit> visits;

    public VisitsAdapterDialog(Context context, ArrayList<Visit> visits) {
        super(context,0,visits);
        this.visits = visits;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        Visit visit = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_visit_dialog,parent,false);
        }

        TextView textViewFullName = (TextView) convertView.findViewById(R.id.textViewfullName);
        TextView textViewRUT = (TextView) convertView.findViewById(R.id.textViewRUT);

        textViewFullName.setText(visit.getFullName());
        textViewRUT.setText(visit.getDocumentNumber());

        return convertView;
    }
}
