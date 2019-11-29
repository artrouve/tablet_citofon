package com.handsriver.concierge.timekeeping;

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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class TimekeepingAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Timekeeping> mList;
    private ArrayList<Timekeeping> mTimekeeping;
    public static final String NO_AVAILABLE = "Sin Información";

    public TimekeepingAdapterSearchList(Context context,ArrayList<Timekeeping> timekeeping) {
        mContext = context;
        mList = timekeeping;
        mTimekeeping = timekeeping;
    }

    private TimekeepingFilter mTimekeepingFilter;


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

        Timekeeping timekeeping = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_timekeeping_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewRutList = (TextView) view.findViewById(R.id.textViewRutList);
        TextView textViewEntryList = (TextView) view.findViewById(R.id.textViewEntryList);
        TextView textViewExitList = (TextView) view.findViewById(R.id.textViewExitList);
        textViewRutList.setText(timekeeping.getRut());
        textViewEntryList.setText(Utility.changeDateFormat(timekeeping.getEntryPorter(),"ENTRY"));

        if (timekeeping.getExitPorter() == null){
            textViewExitList.setText(NO_AVAILABLE);
        }
        else{
            textViewExitList.setText(Utility.changeDateFormat(timekeeping.getExitPorter(),"ENTRY"));
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mTimekeepingFilter == null)
            mTimekeepingFilter = new TimekeepingFilter();

        return mTimekeepingFilter;
    }

    public class TimekeepingFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mTimekeeping;
                results.count = mTimekeeping.size();
            }
            else{
                ArrayList<Timekeeping> filteredTimekeeping = new ArrayList<Timekeeping>();

                for (Timekeeping timekeeping : mTimekeeping){
                    ParsePosition position = new ParsePosition(0);

                    String[] value = constraint.toString().split(" ");
                    String year = value[1];

                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(inputFormat.parse(value[0],position));
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MM");

                    String month = outputFormat.format(cal.getTime());

                    String argDate = year+"-"+month;
                    Pattern pattern = Pattern.compile("(^|\\s)" + argDate);
                    Matcher matcher = pattern.matcher(timekeeping.getEntryPorter());

                    if (matcher.find()){
                        filteredTimekeeping.add(timekeeping);
                    }
            }

                results.values = filteredTimekeeping;
                results.count = filteredTimekeeping.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Timekeeping>) results.values;
            notifyDataSetChanged();
        }
    }
}
