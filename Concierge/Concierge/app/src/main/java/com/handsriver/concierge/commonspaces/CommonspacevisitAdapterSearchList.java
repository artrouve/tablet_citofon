package com.handsriver.concierge.commonspaces;

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
import com.handsriver.concierge.commonspaces.CommonspaceVisit;
import com.handsriver.concierge.utilities.Utility;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class CommonspacevisitAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<CommonspaceVisit> mList;
    private ArrayList<CommonspaceVisit> mVisits;
    public static final String NO_AVAILABLE = "Sin Información";

    public CommonspacevisitAdapterSearchList(Context context, ArrayList<CommonspaceVisit> visits) {
        mContext = context;
        mList = visits;
        mVisits = visits;
    }

    private CommonspaceVisitFilter mCommonspaceVisitFilter;


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

        CommonspaceVisit visit = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_commonspacevisit_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewEntryList = (TextView) view.findViewById(R.id.textViewEntryListCommonspacevisit);
        textViewEntryList.setText(Utility.changeDateFormat(visit.getEntry(),"ENTRY"));

        TextView textViewNameCommonspaceVisitList = (TextView) view.findViewById(R.id.textViewCommonspaceList);
        textViewNameCommonspaceVisitList.setText(visit.getCommonspaceName());

        TextView textViewApartmentListCommonspacevisit = (TextView) view.findViewById(R.id.textViewApartmentListCommonspacevisit);
        textViewApartmentListCommonspacevisit.setText(visit.getApartmentNumber());

        TextView textViewNameCommonspaceVisit = (TextView) view.findViewById(R.id.textViewCommonspacevisitList);
        if(visit.getFullName() == null){
            textViewNameCommonspaceVisit.setText(NO_AVAILABLE);
        }
        else{
            textViewNameCommonspaceVisit.setText(visit.getFullName());
        }

        TextView textViewDocumentCommonspaceVisit = (TextView) view.findViewById(R.id.textViewDocumentCommonspacevisit);
        if(visit.getDocumentNumber() == null){
            textViewDocumentCommonspaceVisit.setText(NO_AVAILABLE);
        }
        else{
            textViewDocumentCommonspaceVisit.setText(visit.getDocumentNumber());
        }

        if (visit.getExitDate() != null){
            view.setBackgroundColor(Color.argb(75,0,255,0));
        }else{
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mCommonspaceVisitFilter == null)
            mCommonspaceVisitFilter = new CommonspaceVisitFilter();

        return mCommonspaceVisitFilter;
    }

    public class CommonspaceVisitFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mVisits;
                results.count = mVisits.size();
            }
            else{
                ArrayList<CommonspaceVisit> filteredVisit = new ArrayList<CommonspaceVisit>();

                for (CommonspaceVisit visit : mVisits){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(visit.getFullName().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

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
            mList = (ArrayList<CommonspaceVisit>) results.values;
            notifyDataSetChanged();
        }
    }
}
