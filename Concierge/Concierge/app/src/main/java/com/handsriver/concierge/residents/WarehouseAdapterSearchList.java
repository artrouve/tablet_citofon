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

public class WarehouseAdapterSearchList extends BaseAdapter implements Filterable {
    private Context mContext;
    private ArrayList<Warehouse> mList;
    private ArrayList<Warehouse> mWarehouse;

    public WarehouseAdapterSearchList(Context context, ArrayList<Warehouse> warehouses) {
        mContext = context;
        mList = warehouses;
        mWarehouse = warehouses;
    }

    private WarehouseFilter mWarehouseFilter;

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

        Warehouse warehouse = mList.get(position);

        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_warehouse_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewWarehouseList = (TextView) view.findViewById(R.id.textViewWarehouseList);
        TextView textViewApartmentList = (TextView) view.findViewById(R.id.textViewApartmentList);

        textViewWarehouseList.setText(warehouse.getWarehouseNumber());
        textViewApartmentList.setText(warehouse.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mWarehouseFilter == null)
            mWarehouseFilter = new WarehouseFilter();

        return mWarehouseFilter;
    }

    public class WarehouseFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mWarehouse;
                results.count = mWarehouse.size();
            }
            else{
                ArrayList<Warehouse> filteredWarehouse = new ArrayList<Warehouse>();

                for (Warehouse warehouse : mWarehouse){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(warehouse.getWarehouseNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(warehouse.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredWarehouse.add(warehouse);
                    }
                }

                results.values = filteredWarehouse;
                results.count = filteredWarehouse.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Warehouse>) results.values;
            notifyDataSetChanged();
        }
    }
}
