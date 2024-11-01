package com.handsriver.concierge.suppliers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.utilities.Utility;
import com.handsriver.concierge.vehicles.Vehicle;
import com.handsriver.concierge.vehicles.VehicleAdapterSearchList;
import com.handsriver.concierge.visits.Visit;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class SupplierAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<SupplierVisit> mList;
    private ArrayList<SupplierVisit> mVisits;

    public SupplierAdapterSearchList(Context context,ArrayList<SupplierVisit> visits) {
        mContext = context;
        mList = visits;
        mVisits = visits;
    }

    private SupplierVisitFilter mSupplierVisitFilter;


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

        SupplierVisit visit = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_supplier_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewEntryList = (TextView) view.findViewById(R.id.textViewEntryList);
        TextView textViewNameSupplierList = (TextView) view.findViewById(R.id.textViewNameSupplierList);
        textViewEntryList.setText(Utility.changeDateFormat(visit.getEntrySupplier(),"ENTRY"));
        textViewNameSupplierList.setText(visit.getNameSupplier());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mSupplierVisitFilter == null)
            mSupplierVisitFilter = new SupplierVisitFilter();

        return mSupplierVisitFilter;
    }

    public class SupplierVisitFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mVisits;
                results.count = mVisits.size();
            }
            else{
                ArrayList<SupplierVisit> filteredVisit = new ArrayList<SupplierVisit>();

                for (SupplierVisit visit : mVisits){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(visit.getNameSupplier().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find()){
                        filteredVisit.add(visit);
                    }
                }

                results.values = filteredVisit;
                results.count = filteredVisit.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<SupplierVisit>) results.values;
            notifyDataSetChanged();
        }
    }
}
