package hanumesh.sjsu.attendancematters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.util.UUID;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        Button register = (Button)findViewById(R.id.register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    validation();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void validation() throws JSONException, UnsupportedEncodingException {
        EditText tName = (EditText)findViewById(R.id.name_input);
        String name= tName.getText().toString();
        EditText tsjsuid = (EditText)findViewById(R.id.sjsuid_inputreg);
        String sjsuid = tsjsuid.getText().toString();
        EditText temail = (EditText)findViewById(R.id.emailid_input);
        String email = temail.getText().toString();
        EditText tphone = (EditText)findViewById(R.id.phoneno_input);
        String phone = tphone.getText().toString();
        EditText tpassword = (EditText)findViewById(R.id.password_input);
        String password = tpassword.getText().toString();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.d("Emi::", ">" + telephonyManager.getDeviceId());
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
        registerPostCall(name,sjsuid,email,phone,password,telephonyManager.getDeviceId().toString(), notify);
    }
    //need to change
    private String generateUUID() {
        Random random = new Random();
        int val = random.nextInt();
        String Hex = new String();
        Hex = Integer.toHexString(val);
        return Hex;
    }

    private void registerPostCall(String name, String sjsuid, String email, String phone, String password, String phoneUnique, String notify) throws JSONException, UnsupportedEncodingException {
        JSONObject json = new JSONObject();
        json.put("sjsuId", sjsuid);
        json.put("email", email);
        json.put("name",name);
        json.put("phone",phone);
        json.put("password",password);
        //json.put("uuid",generateUUID());// need to change
        json.put("uuid", String.valueOf(UUID.randomUUID()));
        json.put("phoneUnique",phoneUnique);
        json.put("notify", notify);

        StringEntity entity = new StringEntity(json.toString());

        Log.d("check uuid", entity.toString());

        RestClient.post(this.getApplicationContext(), "studentCollection", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("response", response.toString());
                Toast.makeText(getApplicationContext(),"Registration successful !",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("response", response.toString());
                Toast.makeText(getApplicationContext(),"Registration Unsuccessful SJSU ID already registered !",Toast.LENGTH_LONG).show();
            }
        });

    }

}
