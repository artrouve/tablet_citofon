package com.handsriver.concierge.residents;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.handsriver.concierge.R;
import com.handsriver.concierge.visits.VisitsRegisterFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Created by alain_r._trouve_silva after 09-07-17.
 */

public class ResidentVehicleAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<ResidentVehicle> mList;
    private ArrayList<ResidentVehicle> mResidentVehicle;
    private static final String NO_AVAILABLE = "Sin Información";
    private static final String ACTIVE = "Activo";
    private static final String NO_ACTIVE = "No Activo";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public ResidentVehicleAdapterSearchList(Context context, ArrayList<ResidentVehicle> residentvehicle) {
        mContext = context;
        mList = residentvehicle;
        mResidentVehicle = residentvehicle;
    }

    private ResidentVehicleFilter mResidentVehicleFilter;


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

        final ResidentVehicle residentvehicle = mList.get(position);

        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_residentvehicle_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewPlate = (TextView) view.findViewById(R.id.textViewplateList);
        TextView textViewApartment = (TextView) view.findViewById(R.id.textViewApartmentList);


        Button buttonEdit = (Button) view.findViewById(R.id.buttonEditResident);


        textViewPlate.setText(residentvehicle.getPlate());
        textViewApartment.setText(residentvehicle.getApartmentNumber());



        buttonEdit.setTag(position);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apartment = residentvehicle.getApartmentNumber();
                String plate = residentvehicle.getPlate();
                String active = residentvehicle.getActive();

                Bundle args = new Bundle();
                args.putLong("id",residentvehicle.getId());
                args.putString("apartment",apartment);
                args.putString("plate",plate);
                args.putString("active",active);


                FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                DialogResidentsVehiclesEdit dialog = new DialogResidentsVehiclesEdit();
                dialog.setArguments(args);
                dialog.show(fm, "DialogResidentsVehiclesEdit");
            }
        });


        return view;
    }

    @Override
    public Filter getFilter() {
        if (mResidentVehicleFilter == null)
            mResidentVehicleFilter = new ResidentVehicleFilter();

        return mResidentVehicleFilter;
    }

    public class ResidentVehicleFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mResidentVehicle;
                results.count = mResidentVehicle.size();
            }
            else{
                ArrayList<ResidentVehicle> filteredResidentVehicle = new ArrayList<ResidentVehicle>();

                for (ResidentVehicle residentvehicle : mResidentVehicle){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(residentvehicle.getPlate().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(residentvehicle.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredResidentVehicle.add(residentvehicle);
                    }
                }

                results.values = filteredResidentVehicle;
                results.count = filteredResidentVehicle.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<ResidentVehicle>) results.values;
            notifyDataSetChanged();
        }
    }
}
