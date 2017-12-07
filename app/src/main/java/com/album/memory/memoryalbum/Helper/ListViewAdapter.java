package com.album.memory.memoryalbum.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.album.memory.memoryalbum.R;

import java.util.ArrayList;
import java.util.Date;

public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<ImageModel> data;
    private int layout;

    public ListViewAdapter(Context context, int layout, ArrayList<ImageModel> data) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
        this.layout = layout;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public ImageModel getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }
        ImageModel item = data.get(position);
        ImageView icon = (ImageView) convertView.findViewById(R.id.item_image_view);

        Log.d("BITMAP", item.FILE_PATH);
        //File imgFile = new File(item.FILE_PATH);
        //icon.setImageURI(Uri.parse(item.PATH));
        Analyzer an = new Analyzer(item.FACE_DATA);

        Bitmap bt = BitmapFactory.decodeFile(item.FILE_PATH);
        Bitmap tempBitmap = Bitmap.createScaledBitmap(bt, bt.getWidth(), bt.getHeight(), true);
        tempBitmap = tempBitmap.copy(Bitmap.Config.ARGB_8888,true);

        Canvas canvas = new Canvas(tempBitmap);
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);
        p.setStrokeWidth(6);
        p.setColor(Color.RED);

        Paint pnt = new Paint();
        pnt.setAntiAlias(true);
        pnt.setTextSize(40);
        pnt.setColor(Color.WHITE);
        for (int i = 0; i < an.face_count; i++){
            int x1, y1, x2, y2;
            x1 = an.posList.get(i)[0];
            y1 = an.posList.get(i)[1];
            x2 = x1 + an.posList.get(i)[2];
            y2 = y1 + an.posList.get(i)[3];

            int perc = (int)(an.varList.get(i) * 100);
            if (perc < 30){
                p.setColor(Color.BLUE);
            }
            else if (perc < 50){
                p.setColor(Color.YELLOW);
            }
            else{
                p.setColor(Color.RED);
            }
            canvas.drawText("[" + i + "] " + perc + " %", x1, y1 - 25, pnt);
            canvas.drawLine(x1, y1, x2, y1, p);//up
            canvas.drawLine(x1, y1, x1, y2, p);//left
            canvas.drawLine(x1, y2, x2, y2, p);//down
            canvas.drawLine(x2, y1, x2, y2, p);
        }


        // rect ...
        //canvas.drawRect(/*all of my end coordinates*/, p);

        icon.setImageBitmap(tempBitmap);

        TextView name = (TextView) convertView.findViewById(R.id.item_text_view);


        //convertView.setBackgroundColor(ColorTemplate.MATERIAL_COLORS[item.CATEGORY]);
        int clr = Analyzer.EMOTION_COLORS[item.CATEGORY];
        double dist = an.deviation;
        int r = Color.red(clr), g = Color.green(clr), b = Color.blue(clr);

        int nr = (int)(r*(1-dist) + 255*dist);
        int ng = (int)(g*(1-dist) + 255*dist);
        int nb = (int)(b*(1-dist) + 255*dist);

        int newClr = Color.argb(220, nr, ng, nb);
        convertView.setBackgroundColor(newClr);
        //convertView.setBackgroundResource(R.drawable.filmshape);

        Date d = new Date(item.DATE);

        String content = "포스팅 시간: " + d.toString() + "\n" + an.toString() + "\n"
                + "촬영 시각: " + item.EXIF_DATE + "\n"
                + "촬영 장소: " + item.EXIF_AREA + "\n"
                + Analyzer.CONV_KEYS[item.CATEGORY];
        name.setText(content);


        return convertView;
    }

}