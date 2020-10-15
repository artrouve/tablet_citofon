package com.handsriver.concierge.residents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handsriver.concierge.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 10-07-17.
 */

public class ParkingAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Parking> mList;
    private ArrayList<Parking> mParking;

    public ParkingAdapterSearchList(Context context,ArrayList<Parking> parking) {
        mContext = context;
        mList = parking;
        mParking = parking;
    }

    private ParkingFilter mParkingFilter;

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

        Parking parking = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_parking_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewParkingNumberList = (TextView) view.findViewById(R.id.textViewParkingNumberList);
        TextView textViewApartmentList = (TextView) view.findViewById(R.id.textViewApartmentList);

        textViewParkingNumberList.setText(parking.getParkingNumber());
        textViewApartmentList.setText(parking.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mParkingFilter == null)
            mParkingFilter = new ParkingFilter();

        return mParkingFilter;
    }

    public class ParkingFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mParking;
                results.count = mParking.size();
            }
            else{
                ArrayList<Parking> filteredParking = new ArrayList<Parking>();

                for (Parking parking : mParking){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(parking.getParkingNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(parking.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredParking.add(parking);
                    }
                }

                results.values = filteredParking;
                results.count = filteredParking.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Parking>) results.values;
            notifyDataSetChanged();
        }
    }
}
