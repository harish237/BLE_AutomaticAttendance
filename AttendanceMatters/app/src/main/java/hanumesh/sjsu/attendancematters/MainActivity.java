package hanumesh.sjsu.attendancematters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    EditText sjsuid, password;
    String sessionToken = "";
    String FILENAME = "session";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     /*   final int REQUEST_CODE = 0x11;

        String[] permissions = {"android.permission.READ_PHONE_STATE"};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);*/


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);



        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                    123);
        }
        int hasCoarseLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }



        TextView register = (TextView)findViewById(R.id.reg_click);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegistrationActivity.class);
                startActivity(intent);
            }
        });

        sjsuid = (EditText) findViewById(R.id.sjsuid_input);
        password = (EditText) findViewById(R.id.password_input);

        Button login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginValidation();
            }
        });

    }


    public void loginValidation() {

        final String username = sjsuid.getText().toString();
        final String pwd = password.getText().toString();

        String url = "studentCollection?where=sjsuId=='" + username + "'";

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
                        if (pwd.equals(items.getJSONObject(0).getString("password"))) {
                            Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                            sessionToken = username;
                            // Navigate to Home screen
                            callHome();
                        } else {
                            Toast.makeText(getApplicationContext(), "Password Wrong :(", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Username entered doesn't exists", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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


    public void callHome() throws IOException {
        Intent intent = new Intent(MainActivity.this, Home.class);
        //intent.putExtra("sessionToken",sessionToken);

        FileOutputStream fos = openFileOutput("session", Context.MODE_PRIVATE);
        fos.write(sessionToken.getBytes());
        fos.close();

        /*FileOutputStream fos1 = openFileOutput("isMarked", Context.MODE_PRIVATE);
        fos1.write("".getBytes());
        fos1.close();*/

       startActivity(intent);
    }
}
