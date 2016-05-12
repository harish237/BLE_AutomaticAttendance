package hanumesh.sjsu.attendancematters;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.MissingFormatArgumentException;

import cz.msebera.android.httpclient.Header;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,BeaconConsumer {

    String sessionToken="";
    String FILENAME = "session";
    MarkAttendance mark;
    List<String> enrolledClasses= new ArrayList<>();

    private LinearLayout mainLayout;
    private PieChart mchart;
    //private float[][] yData = {{5,10}, {15,20}};
    private String[] xData = {"Attended", "Total"};
    int present=0;
    int total=0;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //sessionToken=getIntent().getStringExtra("sessionToken");
        FileInputStream fin = null;
        try {
            fin = openFileInput(FILENAME);
            int c;

            while ((c = fin.read()) != -1) {
                sessionToken = sessionToken + Character.toString((char) c);
            }

//string temp contains all the data of the file.
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mark = new MarkAttendance();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        mainLayout = (LinearLayout) findViewById(R.id.mainLinear);

        getValuesForChart();


        float[][] yData = {{present,total-present}};


        for (int i = 0; i < yData.length; i++) {
            mchart = new PieChart(this);

            //mchart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            mchart.setLayoutParams(new LinearLayout.LayoutParams(1000, 1000));

            //mchart.setLayoutParams(new ActionBar.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //adding Pie Chart to main layout

            mainLayout.addView(mchart);
            //mainLayout.setBackgroundColor(Color.DKGRAY);
            //mainLayout.setMinimumWidth(1000);
            //mainLayout.setMinimumHeight(1000);

            //configure pie chart
            mchart.setUsePercentValues(true);
            mchart.setDescription("");


            //enable hole and configure
            mchart.setDrawHoleEnabled(true);
            mchart.setHoleColor(Color.WHITE);
            mchart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
            mchart.setCenterText("Total Attendance");
            //mchart.setHoleColorTransparent(true);

            mchart.setTransparentCircleColor(Color.WHITE);
            //mchart.setTransparentCircleAlpha(50);

            mchart.setHoleRadius(50);
            mchart.setTransparentCircleRadius(50);

            mchart.setDrawCenterText(true);

            //enable rotation of the chart by touch
            mchart.setRotationAngle(0);
            mchart.setRotationEnabled(false);

            mchart.setHighlightPerTapEnabled(false);

            //set a chart value selected listener
        /*mchart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i, Highlight highlight) {
                //Display msg whn value selected
                if(entry == null)
                    return;
                Toast.makeText(MainActivity.this,xData[entry.getXIndex()] + " = " + entry.getVal() + "%",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });*/

            //add data
            addData(yData[i]);

            //Customize legends
            Legend l = mchart.getLegend();
            l.setEnabled(false);
            l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
            l.setXEntrySpace(7);
            l.setYEntrySpace(5);

        }
    }

    private void getValuesForChart() {

        String url = "attendanceCollection?where=sjsuId=='" + sessionToken + "'";

        RestClient.get(url, new RequestParams(), new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                enrolledClasses.clear();
                try {

                    String s = new String(responseBody);
                    Log.d("string", s);
                    JSONObject json = new JSONObject(s);

                    JSONArray items = json.getJSONArray("_items");
                    Log.d("JSONArray", items.toString());
                    if (items.length() != 0) {
                        JSONObject obj;
                        Log.d("here2","here");
                        for(int i=0;i<items.length();i++){
                            obj = (JSONObject) items.getJSONObject(i);
                            present += obj.getJSONArray("dates").length();
                            enrolledClasses.add(obj.getString("course"));
                        }
                        Log.d("enrolled classes",enrolledClasses.toString());
                        getTotal();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // When Http response code is '404'

                    Toast.makeText(getApplicationContext(), "Unable to fetch Real time data", Toast.LENGTH_LONG).show();

            }

        });

    }
    public void getTotal(){

        String url = "classCollection";

        RestClient.get(url, new RequestParams(), new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                try {

                    String s = new String(responseBody);
                    Log.d("string", s);
                    JSONObject json = new JSONObject(s);

                    JSONArray items = json.getJSONArray("_items");
                    Log.d("JSONArray", items.toString());
                    if (items.length() != 0) {
                        JSONObject obj;
                        Log.d("here2","here");
                        for(int i=0;i<items.length();i++){
                            obj = (JSONObject) items.getJSONObject(i);
                            if (enrolledClasses.contains(obj.getString("course")))
                                total += obj.getJSONArray("dates").length();
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    //Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // When Http response code is '404'

                Toast.makeText(getApplicationContext(), "Unable to fetch Real time data", Toast.LENGTH_LONG).show();

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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent= new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            Intent intent = new Intent(this,Home.class);
            intent.putExtra("sessionToken",sessionToken);
            startActivity(intent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this,UserProfile.class);
            intent.putExtra("sessionToken",sessionToken);
            startActivity(intent);

        } else if (id == R.id.enroll) {
            Intent intent = new Intent(this, ClassEnroll.class);
            intent.putExtra("sessionToken", sessionToken);
            startActivity(intent);

        } else if (id == R.id.logoutnav) {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
            return true;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    //EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.d("BLE Data---->", "The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");

                    //if (firstBeacon.getId1().toString().equals("aa6062f0-98ca-4211-8ec4-193eb73cceb6")) {
                    if (firstBeacon.getId1().toString().equals("aa6062f0-98ca-4211-8ec4-193eb73cceb6")) {
                        Log.d("test", "found raspberry pi ");


                        //if(!mark.isAttendenceMarked()) {

                        mark.getAttendanceDocument("CMPE273",sessionToken);

                        //}

                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    private void addData(float[] yax) {
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        for(int i = 0; i < yax.length; i++)
            yVals1.add(new Entry(yax[i], i));

        ArrayList<String> xVals = new ArrayList<String>();

        for(int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);

        //Create Pie Data Set
        PieDataSet dataSet = new PieDataSet(yVals1, "Daily Attendance");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(70);

        //add  many colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        /*for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);*/

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        //Instantiate Pie Data object now
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(10);
        data.setValueTextColor(Color.BLACK);

        mchart.setData(data);

        //undo all highlights
        mchart.highlightValue(null);

        //update pie chart
        mchart.invalidate();

        //now it's time for demo



    }

}
