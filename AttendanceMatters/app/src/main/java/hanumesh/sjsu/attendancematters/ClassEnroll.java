package hanumesh.sjsu.attendancematters;

import android.content.Intent;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ClassEnroll extends AppCompatActivity implements BeaconConsumer {
    String sessionToken;
    String FILENAME = "session";
    private BeaconManager beaconManager;
    boolean checkErolled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_enroll);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        FileInputStream fin = null;
        sessionToken="";
        try {
            fin = openFileInput(FILENAME);
            int c;

            while( (c = fin.read()) != -1){
                sessionToken = sessionToken + Character.toString((char)c);
            }

//string temp contains all the data of the file.
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // sessionToken=getIntent().getStringExtra("sessionToken");

        addItemsToEnrollSpinner();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        Button enrollButton = (Button)findViewById(R.id.enroll_button);
        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) findViewById(R.id.spinnerForEnroll);
                String classSelectedToEnroll = String.valueOf(spinner.getSelectedItem());
                checkEnrollment(classSelectedToEnroll);
                if(checkErolled)
                    Toast.makeText(getApplicationContext(), "Already Enrolled", Toast.LENGTH_LONG).show();
                else
                {
                    try {
                        enroll(classSelectedToEnroll);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void addItemsToEnrollSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinnerForEnroll);
        final List<String> list = new ArrayList<String>();
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
                        Log.d("here2","here");
                        for( int i =0; i<items.length();i++) {
                            JSONObject listOfClasses = (JSONObject) items.getJSONObject(i);
                            list.add(listOfClasses.getString("course"));
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "No Classes to Enroll Currently", Toast.LENGTH_LONG).show();
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
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        });

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


    }

    private void enroll(String classSelectedToEnroll) throws JSONException, UnsupportedEncodingException {

        JSONObject json = new JSONObject();
        json.put("sjsuId", sessionToken);
        json.put("course", classSelectedToEnroll);
        json.put("dates",new JSONArray());

        StringEntity entity = new StringEntity(json.toString());

        RestClient.post(this.getApplicationContext(), "attendanceCollection", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("response", response.toString());
                Toast.makeText(getApplicationContext(),"Enrollment successful !",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("response", response.toString());
                Toast.makeText(getApplicationContext(),"Enrollment Unsuccessful !",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void checkEnrollment(final String classSelectedToEnroll) {

        String url = "attendanceCollection?where=sjsuId=='" + sessionToken + "'";
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
                        Log.d("here2","here");
                        for (int i=0;i<items.length();i++){
                            JSONObject classesEnrolled = (JSONObject) items.getJSONObject(i);
                            if (classSelectedToEnroll.equalsIgnoreCase(classesEnrolled.getString("course"))){
                                checkErolled=true;
                                break;
                            }
                            else
                                checkErolled=false;
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
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        });
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

                    if (firstBeacon.getId1().toString().equals("abcdefab-cdef-abcd-efab-cdefabcdefab")) {
                        Log.d("test", "found raspberry pi ");


                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

}
