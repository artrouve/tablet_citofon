package com.handsriver.concierge.visits;

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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 05-07-17.
 */

public class VisitAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Visit> mList;
    private ArrayList<Visit> mVisits;
    public static final String NO_AVAILABLE = "Sin Información";


    public VisitAdapterSearchList(Context context,ArrayList<Visit> visits) {
        mContext = context;
        mList = visits;
        mVisits = visits;
    }

    private VisitFilter mVisitsFilter;

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

        Visit visit = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_visit_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewEntryList = (TextView) view.findViewById(R.id.textViewEntryList);
        TextView textViewfullNameList = (TextView) view.findViewById(R.id.textViewfullNameList);
        TextView textViewApartmentList = (TextView) view.findViewById(R.id.textViewApartmentList);
        textViewEntryList.setText(Utility.changeDateFormat(visit.getEntry(),"ENTRY"));
        if (visit.getFullName() == null){
            textViewfullNameList.setText(NO_AVAILABLE);
        }
        else{
            textViewfullNameList.setText(visit.getFullName());
        }
        textViewApartmentList.setText(visit.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mVisitsFilter == null)
            mVisitsFilter = new VisitFilter();

        return mVisitsFilter;
    }

    public class VisitFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mVisits;
                results.count = mVisits.size();
            }
            else{
                ArrayList<Visit> filteredVisits = new ArrayList<Visit>();

                for (Visit visit : mVisits){
                    if(visit.getFullName() != null){
                        Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")) );
                        Matcher matcher = pattern.matcher(Normalizer.normalize(visit.getFullName().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                        Matcher matcher1 = pattern.matcher(Normalizer.normalize(visit.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                        if (matcher.find() || matcher1.find()){
                            filteredVisits.add(visit);
                        }
                    }
                    else{
                        Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                        Matcher matcher1 = pattern.matcher(Normalizer.normalize(visit.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                        if (matcher1.find()){
                            filteredVisits.add(visit);
                        }
                    }

                }

                results.values = filteredVisits;
                results.count = filteredVisits.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Visit>) results.values;
            notifyDataSetChanged();
        }
    }
}
