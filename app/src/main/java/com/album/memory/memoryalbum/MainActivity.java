package com.album.memory.memoryalbum;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.album.memory.memoryalbum.Helper.Analyzer;
import com.album.memory.memoryalbum.Helper.ImageHelper;
import com.album.memory.memoryalbum.Helper.ImageModel;
import com.album.memory.memoryalbum.Helper.ListViewAdapter;
import com.album.memory.memoryalbum.Helper.MyAxisValueFormatter;
import com.album.memory.memoryalbum.Helper.MyValueFormatter;
import com.album.memory.memoryalbum.Helper.SQLiteManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.github.mikephil.charting.charts.Chart.LOG_TAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnChartGestureListener, OnChartValueSelectedListener  {

    String userChoosenTask = "";

    private final static int REQUEST_CAMERA = 1;
    private final static int SELECT_FILES = 2;

    ViewFlipper vf;
    FloatingActionButton fab;

    // The URI of the image selected to detect.
    private Uri mUriImage;

    // The image selected to detect.

    private EmotionServiceClient client;
    private SQLiteManager dbManager = null;

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private ImageView imageView;

    private RadioButton recentRadio, datetimeRadio;

    private Spinner emotionSpinner;
    private ArrayList<ImageModel> filtered_data;
    private List<String> sList = new ArrayList<String>();

    private int ImgWaiting = 0;
    private int ImgDone = 0;

    private ListViewAdapter lvAdapter;
    private int ORDER_BY = 0, mCategory = Analyzer.CONV_KEYS.length, gCategory = Analyzer.CONV_KEYS.length;

    private ArrayAdapter<String> dataAdapter;
    private ListView listView;
    private int deletePos;

    // Chart 1
    private Spinner mGraphSpinner;
    private BarChart mChart1;

    // Chart 2
    private LineChart mChart2;
    private boolean mCBox[];
    private CheckBox aCB, fCB, nCB, hCB, sCB;
    // Chart 3
    private LineChart mChart3;

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    /////////////////
    class RecordButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
            setHeight(100);
            setWidth(400);
            setGravity(Gravity.CENTER);
            setBackgroundColor(Color.rgb(220, 170, 170));
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
            setHeight(100);
            setWidth(400);
            setGravity(Gravity.CENTER);
            setBackgroundColor(Color.rgb(170, 220, 170));
        }
    }


    private static String mFileName = null;
    boolean mStartRecording = true;
    boolean mStartPlaying = true;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        vf = (ViewFlipper)findViewById(R.id.vf);

        // MS Emotion API
        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        dbManager = new SQLiteManager(this);

        mTextView = (TextView) findViewById(R.id.textResult);;
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);;
        imageView = (ImageView) findViewById(R.id.selectedImage);

        /////////////////////////
        listView = (ListView) findViewById(R.id.list_item);
        emotionSpinner = (Spinner) findViewById(R.id.emotion);
        recentRadio = (RadioButton) findViewById(R.id.recent);
        datetimeRadio = (RadioButton) findViewById(R.id.datetime);
        recentRadio.setChecked(true);

        recentRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ORDER_BY = 0;
                resetFilterList(false);
                listView.setSelection(0);
            }
        });
        datetimeRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ORDER_BY = 1;
                resetFilterList(false);
                listView.setSelection(0);
            }
        });

        //Bitmap bm = BitmapFactory.decodeFile("/storage/emulated/0/test.png");
        filtered_data = new ArrayList();
        lvAdapter=new ListViewAdapter(this,R.layout.list_view_layout, filtered_data);
        listView.setAdapter(lvAdapter);


        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //listView.addView()

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                Log.v("long clicked","pos: " + pos);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Edit Post");
                alertDialog.setMessage("Do you want to delete this post?");

                ImageView imageView = new ImageView(getApplicationContext());
                Bitmap bitmap = BitmapFactory.decodeFile(filtered_data.get(pos).FILE_PATH);
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                // Toast.makeText(this, width + " , " + height, Toast.LENGTH_SHORT).show();
                Bitmap resized = null;
                while (height > 600) {
                    resized = Bitmap.createScaledBitmap(bitmap, (width * 600) / height, 600, true);
                    height = resized.getHeight();
                    width = resized.getWidth();
                }

                imageView.setImageBitmap(resized);
                imageView.setPadding(30, 30, 30, 30);


                mFileName = getExternalCacheDir().getAbsolutePath() + "/" + filtered_data.get(pos).DATE + ".3gp";
                LinearLayout ll2 = new LinearLayout(getApplicationContext());
                ll2.setOrientation(LinearLayout.VERTICAL);
                LinearLayout ll = new LinearLayout(getApplicationContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);
                //ll.addView(imageView);
                RecordButton mRecordButton = new RecordButton(getApplicationContext());
                ll.addView(mRecordButton,
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                0));
                PlayButton mPlayButton = new PlayButton(getApplicationContext());
                ll.addView(mPlayButton,
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                0));
                //setContentView(ll);
                ll.setGravity(Gravity.CENTER);
                ll2.addView(imageView);
                ll2.addView(ll);

                //alertDialog.setView(imageView);
                alertDialog.setView(ll2);

                deletePos = pos;
                //alertDialog.setIcon(d);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "LEAVE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DELETE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dbManager.deleteImage(filtered_data.get(deletePos).URI_PATH);
                                resetFilterList(false);
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


                /*
                Dialog builder = new Dialog(MainActivity.this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //nothing;
                    }
                });

                ImageView imageView = new ImageView(this);


                Bitmap bt = BitmapFactory.decodeFile(filtered_data.get(idx).FILE_PATH);
                imageView.setImageBitmap(bt);
                builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                builder.show();
                */

                return true;
            }
        });


        for(String k : Analyzer.CONV_KEYS){
            sList.add(k);
        }
        sList.add("all");
        sList.add("max");

        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, sList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        emotionSpinner.setAdapter(dataAdapter);
        emotionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                mCategory = position;
                resetFilterList(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        emotionSpinner.setSelection(Analyzer.CONV_KEYS.length);
        resetFilterList(false);

        // MP Android Chart

        mGraphSpinner = (Spinner) findViewById(R.id.graphSpinner1);

        mGraphSpinner.setAdapter(dataAdapter);
        mGraphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                gCategory = position;
                refreshBarGraph();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        mChart1 = (BarChart) findViewById(R.id.chart1);
        mChart1.setOnChartValueSelectedListener(this);

        mChart1.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart1.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        mChart1.setPinchZoom(false);

        mChart1.setDrawGridBackground(false);
        mChart1.setDrawBarShadow(false);

        mChart1.setDrawValueAboveBar(false);
        mChart1.setHighlightPerDragEnabled(false);
        mChart1.setHighlightFullBarEnabled(true);
        mChart1.zoom(5f, 1f, 0f, 0f);

        // change the position of the y-labels
        YAxis leftAxis = mChart1.getAxisLeft();
        leftAxis.setValueFormatter(new MyAxisValueFormatter());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        mChart1.getAxisRight().setEnabled(false);
        mChart1.setDoubleTapToZoomEnabled(false);

        XAxis xLabels = mChart1.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.TOP);


        // mChart1.setDrawXLabels(false);
        // mChart1.setDrawYLabels(false);

        // setting data

        Legend l = mChart1.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(10f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        // mChart1.setDrawLegend(false);

        // MP Android Chart 2
        mChart2 = (LineChart) findViewById(R.id.chart2);

        aCB = (CheckBox) findViewById(R.id.aCB);
        fCB = (CheckBox) findViewById(R.id.fCB);
        nCB = (CheckBox) findViewById(R.id.nCB);
        hCB = (CheckBox) findViewById(R.id.hCB);
        sCB = (CheckBox) findViewById(R.id.sCB);

        mCBox = new boolean[5];
        for(int i = 0; i < 5; i++){
            mCBox[i] = true;
        }
        aCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCBox[0] = aCB.isChecked();
                refreshMultiLineGraph();
            }
        });
        fCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCBox[1] = fCB.isChecked();
                refreshMultiLineGraph();
            }
        });
        nCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCBox[2] = nCB.isChecked();
                refreshMultiLineGraph();
            }
        });
        hCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCBox[3] = hCB.isChecked();
                refreshMultiLineGraph();
            }
        });
        sCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCBox[4] = sCB.isChecked();
                refreshMultiLineGraph();
            }
        });

        mChart2.setOnChartValueSelectedListener(this);

        mChart2.setDrawGridBackground(false);
        mChart2.getDescription().setEnabled(false);
        mChart2.setDrawBorders(false);

        mChart2.getAxisLeft().setEnabled(false);
        mChart2.getAxisRight().setDrawAxisLine(false);
        mChart2.getAxisRight().setDrawGridLines(false);
        mChart2.getXAxis().setDrawAxisLine(true);
        mChart2.getXAxis().setDrawGridLines(true);
        mChart2.zoom(5f, 1f, 0f, 0f);

        Legend l2 = mChart2.getLegend();
        l2.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l2.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l2.setOrientation(Legend.LegendOrientation.VERTICAL);
        l2.setDrawInside(false);

        // enable touch gestures
        mChart2.setTouchEnabled(true);

        // enable scaling and dragging
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart2.setPinchZoom(false);


        mChart3 = (LineChart) findViewById(R.id.chart3);
        mChart3.setOnChartGestureListener(this);
        mChart3.setOnChartValueSelectedListener(this);
        mChart3.setDrawGridBackground(false);
        mChart3.setHighlightPerDragEnabled(false);
        mChart3.zoom(5f, 1f, 0f, 0f);

        // no description text
        mChart3.getDescription().setEnabled(false);

        // enable touch gestures
        mChart3.setTouchEnabled(true);

        // enable scaling and dragging
        mChart3.setDragEnabled(true);
        mChart3.setScaleEnabled(true);
        // mChart3.setScaleXEnabled(true);
        // mChart3.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart3.setPinchZoom(false);

        // set an alternative background color
        // mChart3.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        /*
        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart3); // For bounds control
        mChart3.setMarker(mv); // Set the marker to the chart
        */

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart3.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


        //Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        /*
        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        //ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        //ll2.setTypeface(tf);
        */

        YAxis leftAxis2 = mChart3.getAxisLeft();
        leftAxis2.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis2.addLimitLine(ll1);
        //leftAxis2.addLimitLine(ll2);
        leftAxis2.setAxisMaximum(120f);
        leftAxis2.setAxisMinimum(-120f);
        //leftAxis.setYOffset(20f);
        leftAxis2.enableGridDashedLine(10f, 10f, 0f);
        leftAxis2.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis2.setDrawLimitLinesBehindData(true);

        mChart3.getAxisRight().setEnabled(false);
        mChart3.setDoubleTapToZoomEnabled(false);

        //mChart3.getViewPortHandler().setMaximumScaleY(2f);
        //mChart3.getViewPortHandler().setMaximumScaleX(2f);

        // add data

//        mChart3.setVisibleXRange(20);
//        mChart3.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart3.centerViewTo(20, 50, AxisDependency.LEFT);

        //mChart3.animateX(2500);
        //mChart3.invalidate();

        // get the legend (only possible after setting data)
        Legend l3 = mChart3.getLegend();

        // modify the legend ...
        l3.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart3.invalidate();
    }

    private void refreshLineGraph() {

        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < filtered_data.size(); i++) {
            ImageModel im = filtered_data.get(i);

            float val = (float)(im.FACE_ANGER * -100 + im.FACE_FEAR * -50 + im.FACE_NEUTRAL * 0
                                + im.FACE_HAPPINESS * 50 + im.FACE_SURPRISE * 100);
            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
        }

        LineDataSet set1;

        if (mChart3.getData() != null &&
                mChart3.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart3.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart3.getData().notifyDataChanged();
            mChart3.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "Mental State");

            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.1f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart3.setData(data);
        }

        // redraw
        mChart3.invalidate();
    }

    private void refreshMultiLineGraph() {
        mChart2.resetTracking();
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        for (int z = 0; z < Analyzer.CONV_KEYS.length; z++){
            if(!mCBox[z]) continue;
            ArrayList<Entry> values = new ArrayList<Entry>();

            for (int i = 0; i < filtered_data.size(); i++){
                ImageModel im = filtered_data.get(i);
                switch(z){
                    case 0:
                        values.add(new Entry(i, (float) im.FACE_ANGER));
                        break;
                    case 1:
                        values.add(new Entry(i, (float) im.FACE_FEAR));
                        break;
                    case 2:
                        values.add(new Entry(i, (float) im.FACE_NEUTRAL));
                        break;
                    case 3:
                        values.add(new Entry(i, (float) im.FACE_HAPPINESS));
                        break;
                    case 4:
                        values.add(new Entry(i, (float) im.FACE_SURPRISE));
                        break;
                }
            }

            LineDataSet d = new LineDataSet(values, Analyzer.CONV_KEYS[z]);
            d.setLineWidth(2.5f);
            d.setCircleRadius(4f);
            d.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            d.setCubicIntensity(0.001f);

            int color = Analyzer.EMOTION_COLORS[z];
            d.setColor(color);
            d.setCircleColor(color);
            dataSets.add(d);

        }

        LineData data = new LineData(dataSets);
        mChart2.setData(data);
        mChart2.invalidate();
    }

    private void refreshBarGraph(){

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for (int i = 0; i < filtered_data.size(); i++){
            ImageModel im = filtered_data.get(i);
            //float mult = (mSeekBarY1.getProgress() + 1);

            float tot = (float)(im.FACE_ANGER + im.FACE_FEAR + im.FACE_NEUTRAL + im.FACE_HAPPINESS + im.FACE_SURPRISE);

            float val1 = (float) im.FACE_ANGER / tot * 100;
            float val2 = (float) im.FACE_FEAR / tot * 100;
            float val3 = (float) im.FACE_NEUTRAL / tot * 100;
            float val4 = (float) im.FACE_HAPPINESS / tot * 100;
            float val5 = (float) im.FACE_SURPRISE / tot * 100;

            if(0 <= gCategory && gCategory < Analyzer.CONV_KEYS.length || gCategory == 6){
                Log.d("CAT", "Category : " + gCategory);
                int zeroing;
                if(gCategory == 6){
                    float v = 0f;
                    int midx = 0;
                    int idx = 0;
                    for (float w : new float[]{val1, val2, val3, val4, val5}){
                        if (v < w){
                            v = w;
                            midx = idx;
                        }
                        idx++;
                    }
                    zeroing = midx;
                }
                else{
                    zeroing = gCategory;
                }

                switch(zeroing){
                    case 0:
                        val2 = val3 = val4 = val5 = 0;
                        break;
                    case 1:
                        val1 = val3 = val4 = val5 = 0;
                        break;
                    case 2:
                        val1 = val2 = val4 = val5 = 0;
                        break;
                    case 3:
                        val1 = val2 = val3 = val5 = 0;
                        break;
                    case 4:
                        val1 = val2 = val3 = val4 = 0;
                        break;

                }
            }

            yVals1.add(new BarEntry(
                    i,
                    new float[]{val1, val2, val3, val4, val5},
                    getResources().getDrawable(R.drawable.star)));
        }

        BarDataSet set1;

        if (mChart1.getData() != null &&
                mChart1.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart1.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart1.getData().notifyDataChanged();
            mChart1.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "(Emotion)");
            set1.setDrawIcons(false);
            set1.setColors(getColors());
            set1.setStackLabels(Analyzer.CONV_KEYS);
            //set1.setHighLightAlpha(0);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new MyValueFormatter());
            data.setValueTextColor(Color.WHITE);

            mChart1.setData(data);
        }

        mChart1.setFitBars(true);
        mChart1.invalidate();
    }

    private int[] getColors() {

        int stacksize = 5;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = Analyzer.EMOTION_COLORS[i];
        }

        return colors;
    }

    private void resetFilterList(boolean dir)
    {
        ArrayList<ImageModel> list_data = dbManager.getAllImages();
        filtered_data.clear();
        for(ImageModel im : list_data){
            if(im.CATEGORY == mCategory || mCategory < 0 || mCategory >= Analyzer.CONV_KEYS.length)
                filtered_data.add(im);
        }
        if(dir){
            sortList();
        }
        else
            reverseSortList();

        lvAdapter.notifyDataSetChanged();
    }

    private void sortList(){
        Collections.sort(filtered_data, new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
                ImageModel a = (ImageModel)o;
                ImageModel b = (ImageModel)t1;

                if(ORDER_BY == 0){
                    return a.DATE > b.DATE ? 1 : -1;
                }
                else {
                    if (a.EXIF_DATE == null)
                        a.EXIF_DATE = "";
                    if (b.EXIF_DATE == null)
                        b.EXIF_DATE = "";

                    return a.EXIF_DATE.compareTo(b.EXIF_DATE);
                }
            }
        });
    }
    private void reverseSortList(){
        Collections.sort(filtered_data, new Comparator() {
            @Override
            public int compare(Object o, Object t1) {
                ImageModel a = (ImageModel)o;
                ImageModel b = (ImageModel)t1;

                if(ORDER_BY == 0){
                    return a.DATE < b.DATE ? 1 : -1;
                }
                else {
                    if (a.EXIF_DATE == null)
                        a.EXIF_DATE = "";
                    if (b.EXIF_DATE == null)
                        b.EXIF_DATE = "";

                    return b.EXIF_DATE.compareTo(a.EXIF_DATE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings: {
                break;
            }
            case R.id.animateX: {
                mChart1.animateX(3000);
                mChart3.animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart1.animateY(3000);
                mChart3.animateY(3000);
                break;
            }
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            fab.setVisibility(View.VISIBLE);
            vf.setDisplayedChild(0);
        } else if (id == R.id.nav_gallery) {
            recentRadio.setVisibility(View.VISIBLE);
            datetimeRadio.setVisibility(View.VISIBLE);
            //emotionSpinner.setVisibility(View.VISIBLE);
            emotionSpinner.setVisibility(View.GONE);

            mCategory = Analyzer.CONV_KEYS.length;

            ORDER_BY = 0;
            //mCategory = Analyzer.CONV_KEYS.length;
            emotionSpinner.setSelection(mCategory);
            resetFilterList(false);
            listView.setSelection(0);
            recentRadio.setChecked(true);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_graph) {
            ORDER_BY = 1;
            mCategory = Analyzer.CONV_KEYS.length;
            gCategory = Analyzer.CONV_KEYS.length + 1;
            mGraphSpinner.setSelection(gCategory);
            resetFilterList(true);
            refreshBarGraph();
            mChart1.moveViewTo(filtered_data.size(), 0, YAxis.AxisDependency.RIGHT);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(2);
        } else if (id == R.id.nav_graph_2) {
            ORDER_BY = 1;
            mCategory = Analyzer.CONV_KEYS.length;
            mGraphSpinner.setSelection(gCategory);
            resetFilterList(true);
            refreshMultiLineGraph();
            mChart2.moveViewTo(filtered_data.size(), 0, YAxis.AxisDependency.RIGHT);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(3);
        } else if (id == R.id.nav_graph_line) {
            ORDER_BY = 1;

            mCategory = Analyzer.CONV_KEYS.length;
            resetFilterList(true);
            refreshLineGraph();
            mChart3.moveViewTo(filtered_data.size(), 0, YAxis.AxisDependency.RIGHT);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(4);
        }
        else if (id == R.id.nav_angry) {
            recentRadio.setVisibility(View.GONE);
            datetimeRadio.setVisibility(View.GONE);
            emotionSpinner.setVisibility(View.GONE);

            ORDER_BY = 0;
            mCategory = 0;
            resetFilterList(false);
            listView.setSelection(0);
            recentRadio.setSelected(true);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_sadness) {
            recentRadio.setVisibility(View.GONE);
            datetimeRadio.setVisibility(View.GONE);
            emotionSpinner.setVisibility(View.GONE);

            ORDER_BY = 0;
            mCategory = 1;
            resetFilterList(false);
            listView.setSelection(0);
            recentRadio.setSelected(true);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_neutral) {
            recentRadio.setVisibility(View.GONE);
            datetimeRadio.setVisibility(View.GONE);
            emotionSpinner.setVisibility(View.GONE);

            ORDER_BY = 0;
            mCategory = 2;
            resetFilterList(false);
            listView.setSelection(0);
            recentRadio.setSelected(true);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_happiness) {
            recentRadio.setVisibility(View.GONE);
            datetimeRadio.setVisibility(View.GONE);
            emotionSpinner.setVisibility(View.GONE);

            ORDER_BY = 0;
            mCategory = 3;
            resetFilterList(false);
            listView.setSelection(0);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_surprise) {
            recentRadio.setVisibility(View.GONE);
            datetimeRadio.setVisibility(View.GONE);
            emotionSpinner.setVisibility(View.GONE);

            ORDER_BY = 0;
            mCategory = 4;
            resetFilterList(false);
            listView.setSelection(0);
            recentRadio.setSelected(true);
            fab.setVisibility(View.GONE);
            vf.setDisplayedChild(1);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile("IMG_", ".jpg", storageDir);
            } catch (IOException ex) {
                // Error occurred while creating the File
                //...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);

                mUriImage = photoURI;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {

                    ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.CAMERA  },
                            Utility.MY_PERMISSIONS_REQUEST_CAMERA );


                }
                else {
                    startActivityForResult(intent, REQUEST_CAMERA);
                }

            }
        }

        //startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILES);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILES)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA) {
                if (data == null || data.getData() == null) {
                } else {
                    mUriImage = data.getData();
                }
                onCaptureImageResult(data);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if(data.getData()!=null){
            Uri mImageUri=data.getData();
            Log.d("TAG", "Only one " + mImageUri.toString());
            fab.setEnabled(false);
            imageView.setImageResource(0);
            mTextView.setText("STARTING PROCESS");
            ImgWaiting = 1;
            ImgDone = 0;
            recognizeImage(mImageUri);
        }
        else if (data.getClipData() != null) {
            ClipData mClipData = data.getClipData();
            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
            ImgWaiting = mClipData.getItemCount();
            ImgDone = 0;
            fab.setEnabled(false);
            mTextView.setText("STARTING PROCESS");
            for (int i = 0; i < mClipData.getItemCount(); i++)
            {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();
                mArrayUri.add(uri);
                Log.d("TAG", "Multiple" + uri.toString());
                recognizeImage(uri);
            }
        }
    }


    private void onCaptureImageResult(Intent data) {
        //ivImage.setImageBitmap(thumbnail);
        //Log.d("TAG", "GOGO " + data.getData());
        if(mUriImage  != null) {
            fab.setEnabled(false);
            mTextView.setText("STARTING PROCESS");
            ImgWaiting = 1;
            ImgDone = 0;
            recognizeImage(mUriImage);
        }

    }

    public void doRecognize(Bitmap myBitmap, Uri imgUri, String exif_data) {

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            //mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new doRequest(true, myBitmap, imgUri, exif_data).execute();
            } catch (Exception e) {
                mTextView.setText("Error encountered. Exception is: " + e.toString());
            }
        }
    }

    public void recognizeImage(Uri imgUri){
        // If image is selected successfully, set the image URI and bitmap.

        Bitmap mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                imgUri, getContentResolver());

        if(dbManager.imageAlreadyExist(imgUri.toString())){
            ImgDone ++;

            imageView.setImageBitmap(mBitmap);
            mTextView.setText("(PROCESSED ALREADY)  " + ImgDone + " / " + ImgWaiting);
            mProgressBar.setProgress((int)(ImgDone * 100.0 / ImgWaiting));
            if(ImgDone == ImgWaiting) {
                fab.setEnabled(true);
            }
        }
        else {

            Geocoder geoCoder = new Geocoder(this, Locale.getDefault()); //it is Geocoder
            String exif_data = ImageHelper.getExifInfo(imgUri, getContentResolver(), geoCoder);

            //Log.d("TAG", ImageHelper.getImageExif(mImageUri, getContentResolver()));

            if (mBitmap != null) {
                doRecognize(mBitmap, imgUri, exif_data);
            }
        }
    }

    public void uploadImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MainActivity.this);
                if (items[item].equals("Take Photo")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private List<RecognizeResult> processWithAutoFaceDetection(Bitmap mBitmap) throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles(Bitmap mBitmap) throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart3.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }


    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        int idx = (int)e.getX();
        Dialog builder = new Dialog(MainActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);


        Bitmap bt = BitmapFactory.decodeFile(filtered_data.get(idx).FILE_PATH);
        imageView.setImageBitmap(bt);

        mFileName = getExternalCacheDir().getAbsolutePath() + "/" + filtered_data.get(idx).DATE + ".3gp";
        LinearLayout ll2 = new LinearLayout(getApplicationContext());
        ll2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        //ll.addView(imageView);
        RecordButton mRecordButton = new RecordButton(getApplicationContext());
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        PlayButton mPlayButton = new PlayButton(getApplicationContext());
        ll.addView(mPlayButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        //setContentView(ll);
        ll.setGravity(Gravity.CENTER);
        ll2.addView(imageView);
        ll2.addView(ll);

        //alertDialog.setView(imageView);
        //alertDialog.setView(ll);

        builder.addContentView(ll2, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();

        //Log.i("LOWHIGH", "low: " + mChart3.getLowestVisibleX() + ", high: " + mChart3.getHighestVisibleX());
        //Log.i("MIN MAX", "xmin: " + mChart3.getXChartMin() + ", xmax: " + mChart3.getXChartMax() + ", ymin: " + mChart3.getYChartMin() + ", ymax: " + mChart3.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;
        private Bitmap myBitmap;
        private Uri imgUri;
        private String exif_data;

        public doRequest(boolean useFaceRectangles, Bitmap myBitmap, Uri imgUri, String exif_data) {
            this.useFaceRectangles = useFaceRectangles;
            this.myBitmap = myBitmap;
            this.imgUri = imgUri;
            this.exif_data = exif_data;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection(myBitmap);
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles(myBitmap);
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            int faceFound = 0;

            if (this.useFaceRectangles == false) {
                mTextView.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                mTextView.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
                mTextView.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mTextView.append("No emotion detected :(");
                } else {
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = myBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(myBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(50);
                    paint.setColor(Color.RED);

                    String face_data = "[";
                    boolean is_first = true;
                    faceFound = result.size();
                    for (RecognizeResult r : result) {
                        if (is_first){
                            is_first = false;
                            face_data += "{";
                        }
                        else  face_data += ",{";
                        face_data += String.format("anger:%1$.5f,", r.scores.anger);
                        face_data += String.format("contempt:%1$.5f,", r.scores.contempt);
                        face_data += String.format("disgust:%1$.5f,", r.scores.disgust);
                        face_data += String.format("fear:%1$.5f,", r.scores.fear);
                        face_data += String.format("happiness:%1$.5f,", r.scores.happiness);
                        face_data += String.format("neutral:%1$.5f,", r.scores.neutral);
                        face_data += String.format("sadness:%1$.5f,", r.scores.sadness);
                        face_data += String.format("surprise:%1$.5f,    ", r.scores.surprise);
                        face_data += String.format("left:%d, top:%d, width:%d, height:%d"
                                , r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height);
                        face_data += "}";

                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                    }

                    face_data += "]";
                    Log.d("TAG", face_data);


                    //mImageUri.getPath();
                    //getPathFromUri(mImageUri)
                    //dbManager.addImage(new ImageModel(new Date().getTime(), mImageUri.toString(), face_data));

                    try {
                        String filePath = saveBitmap(myBitmap);
                        dbManager.addImage(new ImageModel(new Date().getTime(), imgUri.toString(), filePath, face_data, exif_data));
                        imageView.setImageDrawable(new BitmapDrawable(getResources(), myBitmap));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            ImgDone ++;
            imageView.setImageBitmap(myBitmap);
            if(faceFound > 0) {
                mTextView.setText("(" + faceFound + " FACE" + (faceFound>1?"S":"")+ " FOUND)  " + ImgDone + " / " + ImgWaiting);
            }
            else {
                mTextView.setText("(NO FACE FOUND)  " + ImgDone + " / " + ImgWaiting);
            }
            mProgressBar.setProgress((int)(ImgDone * 100.0 / ImgWaiting));
            if(ImgDone == ImgWaiting) {
                fab.setEnabled(true);
            }
        }
    }

    public String saveBitmap(Bitmap bm) throws IOException {
        //create a file to write bitmap data
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = File.createTempFile("IMG_", ".jpg", storageDir);

        //Convert bitmap to byte array
        Bitmap bitmap = bm;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        return f.getAbsolutePath();
    }
}
