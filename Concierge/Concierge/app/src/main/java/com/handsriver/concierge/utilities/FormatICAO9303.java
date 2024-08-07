package com.handsriver.concierge.utilities;


import com.handsriver.concierge.visits.Visit;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Created by alain_r._trouve_silva after 20-02-17.
 */

public class FormatICAO9303 {

    private static final int A = 10;
    private static final int B = 11;
    private static final int C = 12;
    private static final int D = 13;
    private static final int E = 14;
    private static final int F = 15;
    private static final int G = 16;
    private static final int H = 17;
    private static final int I = 18;
    private static final int J = 19;
    private static final int K = 20;
    private static final int L = 21;
    private static final int M = 22;
    private static final int N = 23;
    private static final int O = 24;
    private static final int P = 25;
    private static final int Q = 26;
    private static final int R = 27;
    private static final int S = 28;
    private static final int T = 29;
    private static final int U = 30;
    private static final int V = 31;
    private static final int W = 32;
    private static final int X = 33;
    private static final int Y = 34;
    private static final int Z = 35;
    private static final int fillCharacter = 0;


    public static Visit formatDocument(String ocrEntry_) {

        try {

            String ocrEntry = ocrEntry_.replace("+","<");
            //ocrEntry = ocrEntry.replace("/","<");

            String ocr = ocrEntry.replace("\n", "");
            Integer lenghtOcr = ocr.length();
            String typeDocument = ocr.substring(0, 1);
            String typeDocumentCHL = ocr.substring(0, 5);


            if (lenghtOcr == 72) {
                if (typeDocument.equals("V")) {
                    return formatVisa72(ocr);
                } else if (typeDocument.equals("I")) {
                    return formatTravelDocument72(ocr);
                } else {
                    return null;
                }
            } else if (lenghtOcr == 88) {
                if (typeDocument.equals("P")) {
                    return formatPassport(ocr);
                } else if (typeDocument.equals("V")) {
                    return formatVisa88(ocr);
                } else {
                    return null;
                }
            } else if (lenghtOcr == 90) {
                if (typeDocumentCHL.equals("INCHL") || typeDocumentCHL.equals("IECHL")) {
                    return formatTravelDocumentChileNew(ocr);
                } else if (typeDocumentCHL.equals("IDCHL")) {
                    return formatTravelDocumentChileOld(ocr);
                } else if (typeDocument.equals("I")) {
                    return formatTravelDocument90(ocr);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatPassport (String ocr){
        try {


            String fullName = ocr.substring(5, 44).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(44, 53).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(53, 54));
            String nacionality = ocr.substring(54, 57);
            String birthdate = ocr.substring(57, 63);
            int checkBirthdate = Integer.parseInt(ocr.substring(63, 64));
            String gender = ocr.substring(64, 65);
            String secondLine = ocr.substring(44, 54).concat(ocr.substring(57, 64)).concat(ocr.substring(65, 87));
            int checkSecondLine = Integer.parseInt(ocr.substring(87, 88));
            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;
            Boolean isCheckSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);
            isCheckSecondLine = checkInfo(secondLine, checkSecondLine);

            if (isCheckDocumentNumber && isCheckBirthdate && isCheckSecondLine) {
                Visit visit = new Visit();
                visit.setDocumentNumber(documentNumber);
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatVisa88(String ocr){
        try {
            String fullName = ocr.substring(5, 44).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(44, 53).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(53, 54));
            String nacionality = ocr.substring(54, 57);
            String birthdate = ocr.substring(57, 63);
            int checkBirthdate = Integer.parseInt(ocr.substring(63, 64));
            String gender = ocr.substring(64, 65);
            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);

            if (isCheckDocumentNumber && isCheckBirthdate) {
                Visit visit = new Visit();
                visit.setDocumentNumber(documentNumber);
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatVisa72(String ocr){
        try {

            String fullName = ocr.substring(5, 36).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(36, 45).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(45, 46));
            String nacionality = ocr.substring(46, 49);
            String birthdate = ocr.substring(49, 55);
            int checkBirthdate = Integer.parseInt(ocr.substring(55, 56));
            String gender = ocr.substring(56, 57);
            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);

            if (isCheckDocumentNumber && isCheckBirthdate) {
                Visit visit = new Visit();
                visit.setDocumentNumber(documentNumber);
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatTravelDocument72(String ocr){
        try {


            String fullName = ocr.substring(5, 36).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(36, 45).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(45, 46));
            String nacionality = ocr.substring(46, 49);
            String birthdate = ocr.substring(49, 55);
            int checkBirthdate = Integer.parseInt(ocr.substring(55, 56));
            String gender = ocr.substring(56, 57);
            String secondLine = ocr.substring(36, 46).concat(ocr.substring(49, 56)).concat(ocr.substring(57, 71));
            int checkSecondLine = Integer.parseInt(ocr.substring(71, 72));
            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;
            Boolean isCheckSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);
            isCheckSecondLine = checkInfo(secondLine, checkSecondLine);

            if (isCheckDocumentNumber && isCheckBirthdate && isCheckSecondLine) {
                Visit visit = new Visit();
                visit.setDocumentNumber(documentNumber);
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }

        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatTravelDocument90(String ocr){
        try {


            String fullName = ocr.substring(60, 90).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(5, 14).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(14, 15));

            String nacionality = ocr.substring(45, 48);

            String birthdate = ocr.substring(30, 36);
            int checkBirthdate = Integer.parseInt(ocr.substring(36, 37));

            String gender = ocr.substring(37, 38);

            String firstAndSecondLine = ocr.substring(5, 37).concat(ocr.substring(38, 45)).concat(ocr.substring(48, 59));
            int checkFirstAndSecondLine = Integer.parseInt(ocr.substring(59, 60));

            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;
            Boolean isCheckFirstAndSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);
            isCheckFirstAndSecondLine = checkInfo(firstAndSecondLine, checkFirstAndSecondLine);

            if (isCheckDocumentNumber && isCheckBirthdate && isCheckFirstAndSecondLine) {
                Visit visit = new Visit();
                visit.setDocumentNumber(documentNumber);
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatTravelDocumentChileNew(String ocr){
        try {


            String fullName = ocr.substring(60, 90).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(5, 14).replace("<", "");
            String rut = ocr.substring(48, 59).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(14, 15));

            String nacionality = ocr.substring(45, 48);

            String birthdate = ocr.substring(30, 36);
            int checkBirthdate = Integer.parseInt(ocr.substring(36, 37));

            String gender = ocr.substring(37, 38);

            String firstAndSecondLine = ocr.substring(5, 37).concat(ocr.substring(38, 45)).concat(ocr.substring(48, 59));
            int checkFirstAndSecondLine = Integer.parseInt(ocr.substring(59, 60));

            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;
            Boolean isCheckFirstAndSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);
            isCheckFirstAndSecondLine = checkInfo(firstAndSecondLine, checkFirstAndSecondLine);

            if (isCheckDocumentNumber && isCheckBirthdate && isCheckFirstAndSecondLine) {
                Visit visit = new Visit();
                visit.setDocumentNumber(rutFormat(rut));
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static Visit formatTravelDocumentChileOld(String ocr){
        try {


            String fullName = ocr.substring(60, 90).replace("<", " ").trim().replaceAll("\\s+", " ");
            String documentNumber = ocr.substring(5, 14).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(14, 15));

            String nacionality = ocr.substring(45, 48);

            String birthdate = ocr.substring(30, 36);
            int checkBirthdate = Integer.parseInt(ocr.substring(36, 37));

            String gender = ocr.substring(37, 38);

            String firstAndSecondLine = ocr.substring(5, 37).concat(ocr.substring(38, 45)).concat(ocr.substring(48, 59));
            int checkFirstAndSecondLine = Integer.parseInt(ocr.substring(59, 60));

            Boolean isCheckDocumentNumber;
            Boolean isCheckBirthdate;
            Boolean isCheckFirstAndSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckBirthdate = checkInfo(birthdate, checkBirthdate);
            isCheckFirstAndSecondLine = checkInfo(firstAndSecondLine, checkFirstAndSecondLine);

            if (isCheckDocumentNumber && isCheckBirthdate && isCheckFirstAndSecondLine) {

                Visit visit = new Visit();
                visit.setDocumentNumber(rutFormat(documentNumber));
                visit.setFullName(fullName);
                visit.setNationality(nacionality);
                visit.setBirthdate(birthdateFormat(birthdate));
                visit.setGender(gender);

                return visit;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }

    }

    private static boolean checkInfo(String value, int check)
    {
        int sum = 0;

        for(int i = 0; i < value.length(); i++) {
            String a = Character.toString(value.charAt(i)) ;
            switch (a){
                case "A":
                    sum = sum + A * weighting(i);
                    break;
                case "B":
                    sum = sum + B * weighting(i);
                    break;
                case "C":
                    sum = sum + C * weighting(i);
                    break;
                case "D":
                    sum = sum + D * weighting(i);
                    break;
                case "E":
                    sum = sum + E * weighting(i);
                    break;
                case "F":
                    sum = sum + F * weighting(i);
                    break;
                case "G":
                    sum = sum + G * weighting(i);
                    break;
                case "H":
                    sum = sum + H * weighting(i);
                    break;
                case "I":
                    sum = sum + I * weighting(i);
                    break;
                case "J":
                    sum = sum + J * weighting(i);
                    break;
                case "K":
                    sum = sum + K * weighting(i);
                    break;
                case "L":
                    sum = sum + L * weighting(i);
                    break;
                case "M":
                    sum = sum + M * weighting(i);
                    break;
                case "N":
                    sum = sum + N * weighting(i);
                    break;
                case "O":
                    sum = sum + O * weighting(i);
                    break;
                case "P":
                    sum = sum + P * weighting(i);
                    break;
                case "Q":
                    sum = sum + Q * weighting(i);
                    break;
                case "R":
                    sum = sum + R * weighting(i);
                    break;
                case "S":
                    sum = sum + S * weighting(i);
                    break;
                case "T":
                    sum = sum + T * weighting(i);
                    break;
                case "U":
                    sum = sum + U * weighting(i);
                    break;
                case "V":
                    sum = sum + V * weighting(i);
                    break;
                case "W":
                    sum = sum + W * weighting(i);
                    break;
                case "X":
                    sum = sum + X * weighting(i);
                    break;
                case "Y":
                    sum = sum + Y * weighting(i);
                    break;
                case "Z":
                    sum = sum + Z * weighting(i);
                    break;
                case "<":
                    sum = sum + fillCharacter * weighting(i);
                    break;
                default:
                    sum = sum + Integer.parseInt(a) * weighting(i);
                    break;
            }
        }

        if(sum%10 == check) {
            return true;
        }
        else{
            return false;
        }
    }

    private static int weighting (int i)
    {
        switch (i%3){
            case 0:
                return 7;
            case 1:
                return 3;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    public static String rutFormat (String rut){
        String formatRut;

        if (rut.length() == 8) {
            formatRut = rut.substring(0,1) + "." + rut.substring(1,4) + "." + rut.substring(4,7) + "-" + rut.substring(7,8);
            return formatRut;
        }
        else if (rut.length() == 9){
            formatRut = rut.substring(0,2) + "." + rut.substring(2,5) + "." + rut.substring(5,8) + "-" + rut.substring(8,9);
            return formatRut;
        }
        else{
            return rut;
        }
    }

    private static String birthdateFormat (String birthdate){
        String newDate;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -100);
        SimpleDateFormat oldPattern, newPattern;
        ParsePosition position = new ParsePosition(0);

        oldPattern = new SimpleDateFormat("yyMMdd");
        oldPattern.set2DigitYearStart(cal.getTime());
        Date date1 = oldPattern.parse(birthdate,position);
        newPattern = new SimpleDateFormat("yyyy-MM-dd");
        newDate = newPattern.format(date1);

        return newDate;
    }

    public static String returnRut(String ocrEntry){
        String INVALID = "INVALID";

        try{

            String ocr = ocrEntry.replace("\n","");
            Integer lenghtOcr = ocr.length();
            String typeDocumentCHL = ocr.substring(0,5);


            if(lenghtOcr == 90) {
                if(typeDocumentCHL.equals("INCHL") || typeDocumentCHL.equals("IECHL")){
                    return returnRUTNew(ocr);
                }
                else if(typeDocumentCHL.equals("IDCHL")) {
                    return returnRUTOld(ocr);
                }
                else{
                    return INVALID;
                }
            }
            else {
                return INVALID;
            }

        }
        catch (Exception e){
            return INVALID;
        }
    }

    private static String returnRUTNew(String ocr){

        try {
            String rut = ocr.substring(48, 59).replace("<", "");

            String firstAndSecondLine = ocr.substring(5, 37).concat(ocr.substring(38, 45)).concat(ocr.substring(48, 59));
            int checkFirstAndSecondLine = Integer.parseInt(ocr.substring(59, 60));

            Boolean isCheckFirstAndSecondLine;
            isCheckFirstAndSecondLine = checkInfo(firstAndSecondLine, checkFirstAndSecondLine);

            if (isCheckFirstAndSecondLine) {
                return rut;
            } else {
                return null;
            }
        }
        catch (Exception e){
            return null;
        }
    }

    private static String returnRUTOld(String ocr){

        try {


            String documentNumber = ocr.substring(5, 14).replace("<", "");
            int checkDocumentNumber = Integer.parseInt(ocr.substring(14, 15));

            String firstAndSecondLine = ocr.substring(5, 37).concat(ocr.substring(38, 45)).concat(ocr.substring(48, 59));
            int checkFirstAndSecondLine = Integer.parseInt(ocr.substring(59, 60));

            Boolean isCheckDocumentNumber;
            Boolean isCheckFirstAndSecondLine;

            isCheckDocumentNumber = checkInfo(documentNumber, checkDocumentNumber);
            isCheckFirstAndSecondLine = checkInfo(firstAndSecondLine, checkFirstAndSecondLine);

            if (isCheckDocumentNumber && isCheckFirstAndSecondLine) {
                return documentNumber;
            } else {
                return null;
            }

        }
        catch (Exception e){
            return null;
        }
    }
}
