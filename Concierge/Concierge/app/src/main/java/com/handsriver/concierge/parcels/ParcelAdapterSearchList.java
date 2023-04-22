package com.handsriver.concierge.parcels;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.visits.Visit;
import com.handsriver.concierge.visits.VisitAdapterSearchList;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class ParcelAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Parcel> mList;
    private ArrayList<Parcel> mParcels;

    public ParcelAdapterSearchList(Context context,ArrayList<Parcel> parcels) {
        mContext = context;
        mList = parcels;
        mParcels = parcels;
    }

    private ParcelFilter mParcelsFilter;


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

        Parcel parcel = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_parcel_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewEntryListParcel = (TextView) view.findViewById(R.id.textViewEntryListParcel);
        TextView textViewParcelList = (TextView) view.findViewById(R.id.textViewParcelList);
        TextView textViewApartmentListParcel = (TextView) view.findViewById(R.id.textViewApartmentListParcel);

        String exitParcel = parcel.getExitParcel();

        if (exitParcel != null) {
            view.setBackgroundColor(Color.argb(75,0,255,0));
        }else{
            view.setBackgroundColor(Color.TRANSPARENT);
        }


        textViewEntryListParcel.setText(Utility.changeDateFormat(parcel.getEntryParcel(),"ENTRY"));
        textViewParcelList.setText(parcel.getUniqueId());
        textViewApartmentListParcel.setText(parcel.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mParcelsFilter == null)
            mParcelsFilter = new ParcelFilter();

        return mParcelsFilter;
    }

    public class ParcelFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mParcels;
                results.count = mParcels.size();
            }
            else{
                ArrayList<Parcel> filteredParcels = new ArrayList<Parcel>();
                if(!(constraint.toString().toUpperCase()).equals("PEN")){
                    for (Parcel parcel : mParcels){
                        Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                        Matcher matcher = pattern.matcher(Normalizer.normalize(parcel.getUniqueId().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                        Matcher matcher1 = pattern.matcher(Normalizer.normalize(parcel.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                        if (matcher.find() || matcher1.find()){
                            filteredParcels.add(parcel);
                        }
                    }
                }
                else{
                    //SEARCH PENDING TO RESIDENT RECEPT
                    for (Parcel parcel : mParcels){
                        if (parcel.getExitParcel() == null){
                            filteredParcels.add(parcel);
                        }
                    }
                }

                results.values = filteredParcels;
                results.count = filteredParcels.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Parcel>) results.values;
            notifyDataSetChanged();
        }
    }
}
