package com.handsriver.concierge.utilities;

import android.text.Editable;

/**
 * Created by Created by alain_r._trouve_silva after 22-02-17.
 */

public class RutFormat {

    public static void rutFormatRealTime (Editable s){

        String replaced = s.toString().replace("-","").replace(".","");
        String replaced2 = replaced.replace(".","");
        int leng = replaced2.length();

        if (leng == 1)
        {
            s.clear();
            s.append(replaced);
        }else if (leng == 2) {
            if (Character.isDigit(s.charAt(leng-1)) || Character.toString(s.charAt(leng-1)).equals("K") || Character.toString(s.charAt(leng-1)).equals("-") || Character.toString(s.charAt(leng-1)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
            }
            else
            {
                s.clear();
                s.append(replaced);
            }
        }else if(leng == 3){
            if (Character.isDigit(s.charAt(leng)) || Character.toString(s.charAt(leng)).equals("K") || Character.toString(s.charAt(leng)).equals("-") || Character.toString(s.charAt(leng)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
            }
            else {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
            }
        }else if(leng == 4){
            if (Character.isDigit(s.charAt(leng)) || Character.toString(s.charAt(leng)).equals("K") || Character.toString(s.charAt(leng)).equals("-") || Character.toString(s.charAt(leng)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
            }
            else {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
            }
        }else if(leng == 5){
            if (Character.isDigit(s.charAt(leng)) || Character.toString(s.charAt(leng)).equals("K") || Character.toString(s.charAt(leng)).equals("-") || Character.toString(s.charAt(leng)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
            }
            else{
                s.clear();
                s.append(replaced);
                s.insert(leng-4, "-");
            }
        }else if(leng == 6){
            if (Character.isDigit(s.charAt(leng + 1)) || Character.toString(s.charAt(leng+1)).equals("K") || Character.toString(s.charAt(leng+1)).equals("-") || Character.toString(s.charAt(leng+1)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
            }
            else{
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
            }
        }else if(leng == 7){
            if (Character.isDigit(s.charAt(leng + 1)) || Character.toString(s.charAt(leng+1)).equals("K") || Character.toString(s.charAt(leng+1)).equals("-") || Character.toString(s.charAt(leng+1)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
            }
            else{
                s.clear();
                s.append(replaced);
                s.insert(leng-4, "-");
            }
        }else if(leng == 8){
            if (Character.isDigit(s.charAt(leng + 1)) || Character.toString(s.charAt(leng+1)).equals("K") || Character.toString(s.charAt(leng+1)).equals("-") || Character.toString(s.charAt(leng+1)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
                s.insert(leng-7, ".");

            }
            else{
                s.clear();
                s.append(replaced);
                s.insert(leng-3, "-");
                s.insert(leng-6, ".");
            }
        }else if(leng == 9){
            if (Character.isDigit(s.charAt(leng + 2)) || Character.toString(s.charAt(leng+2)).equals("K") || Character.toString(s.charAt(leng+2)).equals("-") || Character.toString(s.charAt(leng+2)).equals("."))
            {
                s.clear();
                s.append(replaced);
                s.insert(leng-1, "-");
                s.insert(leng-4, ".");
                s.insert(leng-7, ".");

            }
        }
    }

    public static boolean checkRutDv (String rut){

        String rutNumber = rut.replace(".","").replace("-","");
        int lenghtRut = rutNumber.length();

        if (lenghtRut == 0)
        {
            return false;
        }
        String rutWithoutDv = rutNumber.substring(0,lenghtRut-1);
        StringBuilder stringRutInverter = new StringBuilder(rutWithoutDv);
        String rutInverter = stringRutInverter.reverse().toString();
        String dv = rutNumber.substring(lenghtRut-1);
        int sum = 0;

        if (rutWithoutDv.contains("K"))
        {
            return false;
        }

        for(int i = 0; i < rutInverter.length(); i++) {
            String a = Character.toString(rutInverter.charAt(i)) ;
            sum = sum + Integer.parseInt(a) * weighting(i);
        }

        if (dv.equals("K")) {
            dv = "10";
        }
        if (dv.equals("0")){
            dv = "11";
        }

        if(11 - sum%11 == Integer.parseInt(dv)) {
            return true;
        }
        else{
            return false;
        }
    }

    private static int weighting (int i) {
        switch (i%6){
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 5;
            case 4:
                return 6;
            case 5:
                return 7;
            default:
                return 0;
        }
    }
}
