package hanumesh.sjsu.attendancematters;

import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import java.util.Collection;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class UserProfile extends AppCompatActivity implements BeaconConsumer{
    String sessionToken;
    JSONObject userProfile;
    EditText password, name, email, phone;
    RadioButton radioAlertButton;
    String FILENAME = "session";

    private BeaconManager beaconManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
       // sessionToken=getIntent().getStringExtra("sessionToken");
        sessionToken="";
        FileInputStream fin = null;
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

            getUserProfile();
        try {
            name= (EditText)findViewById(R.id.name_input);
            name.setText(userProfile.getString("name"));
            email= (EditText)findViewById(R.id.emailid_input);
            email.setText(userProfile.getString("email"));
            TextView idsjsu = (TextView)findViewById(R.id.sjsuid);
            idsjsu.setText(sessionToken);
            phone=(EditText)findViewById(R.id.phoneno_input);
            phone.setText(userProfile.getString("phone"));

            password = (EditText)findViewById(R.id.password_input);
            Log.d("notify in user profile",userProfile.getString("notify"));
            //RadioGroup radioAlertGroup = (RadioGroup) findViewById(R.id.radioAlert);
            /*
            if(userProfile.getString("notify").equalsIgnoreCase("1")) {
                radioAlertButton = (RadioButton) findViewById(2131492980);
                radioAlertButton.setChecked(true);
                radioAlertButton = (RadioButton) findViewById(2131492981);
                radioAlertButton.setChecked(false);
            }
            else{
                radioAlertButton = (RadioButton) findViewById(2131492980);
                radioAlertButton.setChecked(false);
                radioAlertButton = (RadioButton) findViewById(2131492981);
                radioAlertButton.setChecked(true);

            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Button saveButton =(Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("saved", "saved");
                try {
                    updatePutCall();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }
    public void getUserProfile() {


        String url = "studentCollection?where=sjsuId=='" + sessionToken + "'";

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
                        userProfile = (JSONObject) items.getJSONObject(0);


                    } else {
                        Toast.makeText(getApplicationContext(), "Username entered doesn't exists", Toast.LENGTH_LONG).show();
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

            private void updatePutCall() throws JSONException, UnsupportedEncodingException, UnsupportedEncodingException {
                JSONObject json = new JSONObject();
                json.put("sjsuId", sessionToken);
                json.put("email", email.getText().toString());
                json.put("name",name.getText().toString());
                json.put("phone",phone.getText().toString());
                Log.d("1234",password.getText().toString());
                if (password.getText().toString().equals("")||password.getText()==(null)) {
                    json.put("password", userProfile.getString("password"));
                } else {
                    json.put("password", password.getText().toString());
                }

                Log.d("12345",password.getText().toString());
                //json.put("password", password);
                //json.put("uuid",generateUUID());// need to change
                json.put("uuid", userProfile.getString("uuid"));
                json.put("phoneUnique", userProfile.getString("phoneUnique"));
                RadioGroup radioAlertGroup = (RadioGroup) findViewById(R.id.radioAlert);
                int selectedId = radioAlertGroup.getCheckedRadioButtonId();
                Log.d("selected id", Integer.toString(selectedId));
                // find the radiobutton by returned id
                RadioButton radioAlertButton = (RadioButton) findViewById(selectedId);
                String notify = radioAlertButton.getText().toString();
                if (notify.equalsIgnoreCase("yes"))
                    notify="1";
                else
                    notify="0";
                json.put("notify", notify);

                StringEntity entity = new StringEntity(json.toString());

                Log.d("Query Data", entity.toString());

                final String eTag = userProfile.getString("_etag");

                Log.d("check uuid", entity.toString());

                Header header = new Header() {
                    @Override
                    public String getName() {
                        return "If-Match";
                    }

                    @Override
                    public String getValue() {
                        return eTag;
                    }

                    @Override
                    public HeaderElement[] getElements() throws ParseException {
                        return new HeaderElement[0];
                    }
                };

                Header[] headers = new Header[] {header};

                String url = "studentCollection/".concat(userProfile.getString("_id"));


                RestClient.put(this.getApplicationContext(), url, headers, entity, "application/json", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d("response", response.toString());
                        Toast.makeText(getApplicationContext(), "Update successful !", Toast.LENGTH_LONG).show();


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                        Log.d("response", response.toString());
                        Toast.makeText(getApplicationContext(), "Update Unsuccessful SJSU ID already registered !", Toast.LENGTH_LONG).show();
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
