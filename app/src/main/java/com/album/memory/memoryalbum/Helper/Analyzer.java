package com.album.memory.memoryalbum.Helper;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user on 2017-11-28.
 */

public class Analyzer {
    public static final String ORIG_KEYS[] = {"anger", "contempt", "disgust", "fear", "sadness", "neutral", "happiness", "surprise"};
    public static final String CONV_KEYS[] = {"anger", "fear", "neutral", "happiness", "surprise"};
    public static final int[] EMOTION_COLORS = {
            Color.rgb(217, 80, 138), Color.rgb(254, 149, 7), Color.rgb(106, 167, 134),
            Color.rgb(254, 247, 120), Color.rgb(53, 194, 209)
    };

    public static final String pkeys[] = {"left", "top", "width", "height"};
    public ArrayList<double[]> bagList = null;
    public ArrayList<int[]> posList = null;
    public ArrayList<Double> varList = null;
    public double deviation = 0;
    public double[] mean;
    public int face_count = 0;

    public Analyzer(String face_data){
        int i, j;
        try {
            JSONArray arr = new JSONArray(face_data);
            bagList = new ArrayList();
            posList = new ArrayList();
            varList = new ArrayList();
            mean = new double[CONV_KEYS.length];
            deviation = 0.0;
            for (j = 0; j < mean.length; j++) mean[j] = 0.0;

            for (i = 0; i < arr.length(); i++){
                face_count ++;
                JSONObject face = arr.getJSONObject(i);
                double bag[] = new double[CONV_KEYS.length];
                for (j = 0; j < bag.length; j++) bag[j] = 0;
                for (j = 0; j < ORIG_KEYS.length; j++){
                    if(j == 0 || j == 1 || j == 2) // anger and contempt disgust
                        bag[0] += face.getDouble(ORIG_KEYS[j]);
                    if(j == 3 || j == 4) // fear and sadness
                        bag[1] += face.getDouble(ORIG_KEYS[j]);
                    if(j == 5) // neutral
                        bag[2] += face.getDouble(ORIG_KEYS[j]);
                    if(j == 6) // happiness
                        bag[3] += face.getDouble(ORIG_KEYS[j]);
                    if(j == 7) // surprise
                        bag[4] += face.getDouble(ORIG_KEYS[j]);
                }
                normalize(bag);
                for (j = 0; j < CONV_KEYS.length; j++){
                    mean[j] += bag[j];
                }
                bagList.add(bag);

                int pos[] = new int[pkeys.length];
                for (j = 0; j < pkeys.length; j++){
                    pos[j] = face.getInt(pkeys[j]);
                }
                posList.add(pos);
            }

            for (j = 0; j < mean.length; j++) mean[j] /= face_count;

            // calculate deviation
            if(face_count == 1){
                varList.add(0.0);
                deviation = 0;
            }
            else {
                for (i = 0; i < face_count; i++){
                    double rsum = 0.0;
                    double[] r = new double[CONV_KEYS.length];
                    double cosval = 0.0;
                    for (j = 0; j < r.length; j++){
                        r[j] = bagList.get(i)[j] - mean[j];
                        cosval += bagList.get(i)[j] * mean[j];
                    }
                    for (j = 0; j < r.length; j++) rsum += r[j]*r[j];
                    varList.add(Math.acos(cosval)/Math.PI);
                    deviation += rsum;
                }
                deviation /= face_count;//(face_count - 1);
                deviation = Math.sqrt(deviation);
            }

            Log.d("SIZE", posList.size() + " : " + varList.size());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void normalize(double bag[]){
        double len = 0.0;
        for(int i = 0; i < bag.length; i++){
            len += bag[i]*bag[i];
        }
        len = Math.sqrt(len);
        for(int i = 0; i < bag.length; i++){
            bag[i] /= len;
        }
    }

    public double[] getMean(){
        return mean;
    }

    public String toString(){
        String rtn = "";
        for(int i = 0; i < face_count; i++){
            rtn += "[" + i + "] (" + posList.get(i)[0] + ", "  + posList.get(i)[1] + ", "
                    + posList.get(i)[2] + ", " + posList.get(i)[3] + ") : " + (int)(varList.get(i) * 100) + " %\n";
        }
        rtn += "친밀도: " + (int)(100 - deviation * 100) + " %";
        return rtn;
    }

    public static Double convertToDegree(String stringDMS){
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double DoubleD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double DoubleM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double DoubleS = S0/S1;

        result = new Double(DoubleD + (DoubleM/60) + (DoubleS/3600));

        return result;
    };
}
