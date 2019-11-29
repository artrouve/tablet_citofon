package com.handsriver.concierge.vehicles;

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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 08-07-17.
 */

public class VehicleAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Vehicle> mList;
    private ArrayList<Vehicle> mVehicles;
    private int mHours;
    public static final String NO_AVAILABLE = "Sin Información";


    public VehicleAdapterSearchList(Context context,ArrayList<Vehicle> vehicles,int hours) {
        mContext = context;
        mList = vehicles;
        mVehicles = vehicles;
        mHours = hours;
    }

    private VehicleFilter mVehicleFilter;


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

        Vehicle vehicle = mList.get(position);

        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_vehicle_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewEntry = (TextView) view.findViewById(R.id.textViewEntryListVehicle);
        TextView textViewApartment = (TextView) view.findViewById(R.id.textViewApartmentListVehicle);
        TextView textViewLicensePlate = (TextView) view.findViewById(R.id.textViewVehicleList);
        TextView textViewParkingNumber = (TextView) view.findViewById(R.id.textViewParkingNumberList);

        boolean isFined;
        String exitDate = vehicle.getExitDate();
        String fineDate = vehicle.getFineDate();

        isFined = Utility.differenceDateHours(Utility.getHourForServer(),vehicle.getEntry(),mHours);

        if (isFined && fineDate == null && exitDate == null) {
            view.setBackgroundColor(Color.argb(75,255,255,0));
        }else if (exitDate != null){
            view.setBackgroundColor(Color.argb(75,0,255,0));
        }else{
            view.setBackgroundColor(Color.TRANSPARENT);
        }

        if (vehicle.getParkingNumber() == null){
            textViewParkingNumber.setText(NO_AVAILABLE);
        }
        else{
            textViewParkingNumber.setText(vehicle.getParkingNumber());
        }

        textViewLicensePlate.setText(vehicle.getLicensePlate());
        textViewEntry.setText(Utility.changeDateFormat(vehicle.getEntry(),"ENTRY"));
        textViewApartment.setText(vehicle.getApartmentNumber());

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mVehicleFilter == null)
            mVehicleFilter = new VehicleFilter();

        return mVehicleFilter;
    }

    public class VehicleFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mVehicles;
                results.count = mVehicles.size();
            }
            else{
                ArrayList<Vehicle> filteredVehicles = new ArrayList<Vehicle>();

                for (Vehicle vehicle : mVehicles){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(vehicle.getLicensePlate().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(vehicle.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredVehicles.add(vehicle);
                    }
                }

                results.values = filteredVehicles;
                results.count = filteredVehicles.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Vehicle>) results.values;
            notifyDataSetChanged();
        }
    }
}
