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

public class ResidentTempAdapterSearchList extends BaseAdapter implements Filterable {

    private Context mContext;
    private ArrayList<ResidentTemp> mList;
    private ArrayList<ResidentTemp> mResidentTemp;
    private static final String NO_AVAILABLE = "Sin Información";
    private static final String IS_SYNC = "1";
    private static final String NOT_UPDATE = "0";

    public ResidentTempAdapterSearchList(Context context, ArrayList<ResidentTemp> resident) {
        mContext = context;
        mList = resident;
        mResidentTemp = resident;
    }

    private ResidentTempFilter mResidentTempFilter;


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

        final ResidentTemp resident = mList.get(position);

        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_residenttemp_search,parent,false);
        }
        else{
            view = convertView;
        }

        TextView textViewFullName = (TextView) view.findViewById(R.id.textViewfullNameList);
        TextView textViewEmail = (TextView) view.findViewById(R.id.textViewEmailList);
        TextView textViewApartment = (TextView) view.findViewById(R.id.textViewApartmentList);

        TextView textViewPhone = (TextView) view.findViewById(R.id.textViewPhoneList);
        TextView textViewStartDate = (TextView) view.findViewById(R.id.textViewStartDateList);
        TextView textViewEndDate = (TextView) view.findViewById(R.id.textViewEndDateList);

        TextView textViewRut = (TextView) view.findViewById(R.id.textViewRutList);



        Button button = (Button) view.findViewById(R.id.buttonRegisterVisitResident);
        Button buttonEdit = (Button) view.findViewById(R.id.buttonEditResident);
        Button buttonCode = (Button) view.findViewById(R.id.buttonGenerateCodeResident);

        textViewFullName.setText(resident.getFullName());


        if (resident.getPhone() != null && resident.getPhone().length() > 0){
            textViewPhone.setText(resident.getPhone());
        }
        else{
            textViewPhone.setText(NO_AVAILABLE);
        }

        if (resident.getRut() != null && resident.getRut().length() > 0){
            textViewRut.setText(resident.getRut());
        }
        else{
            textViewRut.setText(NO_AVAILABLE);
        }

        if (resident.getEmail() != null && resident.getEmail().length() > 0){
            textViewEmail.setText(resident.getEmail());
        }
        else{
            textViewEmail.setText(NO_AVAILABLE);
        }

        if (resident.getStartDate() != null && resident.getStartDate().length() > 0){
            textViewStartDate.setText(resident.getStartDate());
        }
        else{
            textViewStartDate.setText(NO_AVAILABLE);
        }

        if (resident.getEndDate() != null && resident.getEndDate().length() > 0){
            textViewEndDate.setText(resident.getEndDate());
        }
        else{
            textViewEndDate.setText(NO_AVAILABLE);
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
                String startDate = resident.getStartDate();
                String endDate = resident.getEndDate();
                String email = resident.getEmail();
                if (email == null){
                    email = "";
                }
                else{
                    if (email.equals(NO_AVAILABLE)){
                        email = "";
                    }
                }


                String phone = resident.getPhone();
                if (phone == null){
                    phone = "";
                }
                else{
                    if (phone.equals(NO_AVAILABLE)){
                        phone = "";
                    }
                }

                String rut = resident.getRut();
                if (rut == null){
                    rut = "";
                }
                else{
                    if (rut.equals(NO_AVAILABLE)){
                        rut = "";
                    }
                }

                Bundle args = new Bundle();
                args.putLong("id",resident.getId());
                args.putString("apartment",apartment);
                args.putString("fullName",fullName);
                args.putString("email",email);

                args.putString("rut",rut);
                args.putString("phone",phone);
                args.putString("startDate",startDate);
                args.putString("endDate",endDate);

                FragmentManager fm = ((AppCompatActivity) mContext).getSupportFragmentManager();
                DialogResidentsTempsEdit dialog = new DialogResidentsTempsEdit();
                dialog.setArguments(args);
                dialog.show(fm, "DialogResidentsTempEdit");
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
        if (mResidentTempFilter == null)
            mResidentTempFilter = new ResidentTempFilter();

        return mResidentTempFilter;
    }

    public class ResidentTempFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0){
                results.values = mResidentTemp;
                results.count = mResidentTemp.size();
            }
            else{
                ArrayList<ResidentTemp> filteredResidentTemp = new ArrayList<ResidentTemp>();

                for (ResidentTemp resident : mResidentTemp){
                    Pattern pattern = Pattern.compile("(^|\\s)" + Pattern.quote(Normalizer.normalize(constraint.toString().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")));
                    Matcher matcher = pattern.matcher(Normalizer.normalize(resident.getFullName().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));
                    Matcher matcher1 = pattern.matcher(Normalizer.normalize(resident.getApartmentNumber().toUpperCase(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", ""));

                    if (matcher.find() || matcher1.find()){
                        filteredResidentTemp.add(resident);
                    }
                }

                results.values = filteredResidentTemp;
                results.count = filteredResidentTemp.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (ArrayList<ResidentTemp>) results.values;
            notifyDataSetChanged();
        }
    }
}
