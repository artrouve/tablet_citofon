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

public class ResidentAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<Resident> mList;
    private ArrayList<Resident> mResident;
    private static final String NO_AVAILABLE = "Sin Información";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public ResidentAdapterSearchList(Context context,ArrayList<Resident> resident) {
        mContext = context;
        mList = resident;
        mResident = resident;
    }

    private ResidentFilter mResidentFilter;


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

        final Resident resident = mList.get(position);

        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_resident_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewFullName = (TextView) view.findViewById(R.id.textViewfullNameList);
        TextView textViewEmail = (TextView) view.findViewById(R.id.textViewEmailList);
        TextView textViewApartment = (TextView) view.findViewById(R.id.textViewApartmentList);
        Button button = (Button) view.findViewById(R.id.buttonRegisterVisitResident);
        Button buttonEdit = (Button) view.findViewById(R.id.buttonEditResident);
        Button buttonCode = (Button) view.findViewById(R.id.buttonGenerateCodeResident);

        textViewFullName.setText(resident.getFullName());

        if (resident.getEmail() != null && resident.getEmail().length() > 0){
            textViewEmail.setText(resident.getEmail());
        }
        else{
            textViewEmail.setText(NO_AVAILABLE);
        }

        textViewApartment.setText(resident.getApartmentNumber());

        button.setTag(position);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String var = resident.getApartmentNumber();
                Bundle args = new Bundle();
                args.putString("apartmentNumber",var);

                FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                VisitsRegisterFragment fragmentRegisterVisits = new VisitsRegisterFragment();
                fragmentRegisterVisits.setArguments(args);
                fm.beginTransaction().replace(R.id.frame_main,fragmentRegisterVisits).commit();
                NavigationView navigationView = (NavigationView)((AppCompatActivity) mContext).findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.register_visits);
                ((AppCompatActivity) mContext).getSupportActionBar().setTitle("Registrar Visitas");
            }
        });

        buttonEdit.setTag(position);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apartment = resident.getApartmentNumber();
                String fullName = resident.getFullName();
                String email = resident.getEmail();

                if (email == null){
                    email = "";
                }
                else{
                    if (email.equals(NO_AVAILABLE)){
                        email = "";
                    }
                }


                Bundle args = new Bundle();
                args.putLong("id",resident.getId());
                args.putString("apartment",apartment);
                args.putString("fullName",fullName);
                args.putString("email",email);

                FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                DialogResidentsEdit dialog = new DialogResidentsEdit();
                dialog.setArguments(args);
                dialog.show(fm, "DialogResidentsEdit");
            }
        });

        buttonCode.setTag(position);

        buttonCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (resident.getIsUpdate().equals(NOT_UPDATE) && resident.getIsSync().equals(IS_SYNC) && resident.getEmail() != null && resident.getEmail().length() > 0){

                    String apartment = resident.getApartmentNumber();
                    String fullName = resident.getFullName();
                    String email = resident.getEmail();

                    Bundle args = new Bundle();
                    args.putLong("id",resident.getId());
                    args.putString("apartment",apartment);
                    args.putString("fullName",fullName);
                    args.putString("email",email);

                    FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                    DialogCodeAuthResident dialog = new DialogCodeAuthResident();
                    dialog.setArguments(args);
                    dialog.show(fm, "DialogCodeAuthResident");

                }
                else {
                    if (resident.getEmail() != null && resident.getEmail().length() == 0){
                        Toast.makeText(mContext.getApplicationContext(), "Falta ingresar el correo electrónico", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(mContext.getApplicationContext(), "Vuelva a intentarlo dentro de unos instantes", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public Filter getFilter() {
        if (mResidentFilter == null)
            mResidentFilter = new ResidentFilter();

        return mResidentFilter;
    }

    public class ResidentFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mResident;
                results.count = mResident.size();
            }
            else{
                ArrayList<Resident> filteredResident = new ArrayList<Resident>();

                for (Resident resident : mResident){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(resident.getFullName().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(resident.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredResident.add(resident);
                    }
                }

                results.values = filteredResident;
                results.count = filteredResident.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<Resident>) results.values;
            notifyDataSetChanged();
        }
    }
}