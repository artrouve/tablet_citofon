package com.handsriver.concierge.vehicles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handsriver.concierge.R;
import com.handsriver.concierge.visits.DialogVisitsRegister;
import com.handsriver.concierge.visits.Visit;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Created by alain_r._trouve_silva after 13-02-17.
 */

public class VehiclePlateAdapter extends ArrayAdapter<VehiclePlateDetected> {
    private int focusedPosition = -1;
    private ArrayList<VehiclePlateDetected> vehiclesDetected;
    ImageButton buttonDeletePlateDetected;
    ImageButton buttonViewPlateDetected;
    VehiclePlateDetected vehicleSelected;
    private String urlBase = "https://salpr.citofon.cl/";

    public void setFocusedPosition(int focusedPosition) {
        this.focusedPosition = focusedPosition;
    }
    public int getFocusedPosition(){
        return this.focusedPosition;
    }

    public VehiclePlateAdapter(Context context, ArrayList<VehiclePlateDetected> vehiclesDetected) {
        super(context,0,vehiclesDetected);
        this.vehiclesDetected = vehiclesDetected;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final VehiclePlateDetected vehicleDetected = getItem(position);
        vehicleSelected = vehicleDetected;
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_plate_detected,parent,false);
        }


        if (position == focusedPosition) {
            //convertView.setBackgroundColor(getContext().getResources().getColor(R.color.red));
            //convertView.setBackgroundColor(Color.RED);

        }


        ImageView imagePlate = (ImageView) convertView.findViewById(R.id.plateImageView);
        Picasso.get().load(urlBase+vehicleDetected.getUrlImage()).networkPolicy(NetworkPolicy.NO_CACHE).memoryPolicy(MemoryPolicy.NO_CACHE).into(imagePlate);

        //TextView textViewFullName = (TextView) convertView.findViewById(R.id.textViewfullName);
        TextView textViewPlate = (TextView) convertView.findViewById(R.id.textViewPLATE);
        textViewPlate.setText(vehicleDetected.getLicensePlate());



        buttonDeletePlateDetected = (ImageButton) convertView.findViewById(R.id.buttonDeletePlateDetectedList);
        buttonViewPlateDetected = (ImageButton) convertView.findViewById(R.id.buttonViewPlateDetectedList);
        //textViewFullName.setText(visit.getFullName());
        //textViewRUT.setText(visit.getDocumentNumber());

        buttonDeletePlateDetected.setTag(position);

        buttonDeletePlateDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vehiclesDetected.remove(position);
                notifyDataSetChanged();

                //SE DEBE ELIMINAR LA PATENTE DETECTADA DEL TOTAL DE REGISTROS
                VehiclePlateDetectedOper DetectedPlate = new VehiclePlateDetectedOper(getContext());
                DetectedPlate.DeleteFromSharedPreferences(vehicleDetected);

            }
        });

        buttonViewPlateDetected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SE DEBE MOSTRAR LA PATENTE DETECTADA, HACER UN ZOOM
                Bundle args = new Bundle();
                VehiclePlateDetected vehicleDetected = getItem(position);

                args.putString("urlBase",urlBase + vehicleDetected.getUrlImage());

                FragmentManager manager = ((AppCompatActivity)getContext()).getSupportFragmentManager();

                DialogVehiclePlateDetected dialog = new DialogVehiclePlateDetected();
                dialog.setStyle(R.style.DialogThemePlate,R.style.DialogThemePlate);
                dialog.setArguments(args);
                dialog.show(manager, "dialog");



            }
        });



        return convertView;

    }
    public void refreshEvents(ArrayList<VehiclePlateDetected> vehiclesDetecteds) {


        this.vehiclesDetected.clear();

        if (vehiclesDetecteds != null){

            for (VehiclePlateDetected object : vehiclesDetecteds) {

                this.vehiclesDetected.add(object);
            }
        }

        this.notifyDataSetChanged();

/*


        int size = this.vehiclesDetected.size();
        //this.vehiclesDetected.removeAll(this.vehiclesDetected);

        this.clear();
        this.vehiclesDetected = vehiclesDetecteds;
        this.notifyDataSetChanged();



        //this.vehiclesDetected.addAll(vehiclesDetected);

        //this.notifyDataSetChanged();

 */
    }

}
