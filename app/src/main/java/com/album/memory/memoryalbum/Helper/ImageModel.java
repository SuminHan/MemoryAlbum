package com.album.memory.memoryalbum.Helper;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user on 2017-11-23.
 */

public class ImageModel {
    public long DATE;
    public String URI_PATH;
    public String FILE_PATH;
    public String FACE_DATA;
    public double EXIF_LAT;
    public double EXIF_LNG;
    public String EXIF_DATE;
    public String EXIF_AREA;
    public double FACE_ANGER;
    public double FACE_FEAR;
    public double FACE_NEUTRAL;
    public double FACE_HAPPINESS;
    public double FACE_SURPRISE;
    public int CATEGORY;

    public ImageModel(){};

    public ImageModel(long DATE, String URI_PATH, String FILE_PATH, String FACE_DATA, String EXIF_DATA){
        this.DATE = DATE;


        this.URI_PATH = URI_PATH;
        this.FILE_PATH = FILE_PATH;
        this.FACE_DATA = FACE_DATA;

        Analyzer an = new Analyzer(FACE_DATA);
        double m[] = an.getMean();
        double k = 0.0;
        int tar = -1;
        for (int i = 0; i < m.length; i++){
            if (k < m[i]){
                tar = i;
                k = m[i];
            }
            switch(i){
                case 0:
                    this.FACE_ANGER = m[i];
                    break;
                case 1:
                    this.FACE_FEAR = m[i];
                    break;
                case 2:
                    this.FACE_NEUTRAL = m[i];
                    break;
                case 3:
                    this.FACE_HAPPINESS = m[i];
                    break;
                case 4:
                    this.FACE_SURPRISE = m[i];
                    break;
            }
        }
        this.CATEGORY = tar;

        try {
            if(EXIF_DATA != null){
                Log.d("EXIF", EXIF_DATA);
                JSONObject exif = new JSONObject(EXIF_DATA);

                this.EXIF_LAT = exif.getDouble("lat");
                this.EXIF_LNG = exif.getDouble("lng");
                this.EXIF_DATE = exif.getString("datetime");
                this.EXIF_AREA = exif.getString("area");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
