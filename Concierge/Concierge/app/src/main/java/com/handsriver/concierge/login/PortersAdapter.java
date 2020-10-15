package com.handsriver.concierge.login;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handsriver.concierge.R;

import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 12-03-18.
 */

public class PortersAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Porter> mList;
    private int selectedIndex;
    private int selectedColor = Color.parseColor("#d3d3d3");


    public void setSelectedIndex(int ind)
    {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public PortersAdapter(Context context, ArrayList<Porter> porters) {
        super();
        mList = porters;
        mContext = context;
        selectedIndex = -1;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        else{
            return mList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        Porter porter = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_user,parent,false);
        }
        else{
            view = convertView;
        }

        if(selectedIndex!= -1 && position == selectedIndex)
        {
            view.setBackgroundColor(selectedColor);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        TextView textViewFullName = (TextView) view.findViewById(R.id.textViewFullName);

        textViewFullName.setText(porter.getFullName());

        return view;
    }
}
