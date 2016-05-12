//package hanumesh.sjsu.attendancematters;
//
//
//import android.content.BroadcastReceiver;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.util.TimeUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.loopj.android.http.JsonHttpResponseHandler;
//import com.loopj.android.http.RequestParams;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.UnsupportedEncodingException;
//import java.util.concurrent.TimeUnit;
//
//import cz.msebera.android.httpclient.Header;
//import cz.msebera.android.httpclient.HeaderElement;
//import cz.msebera.android.httpclient.ParseException;
//import cz.msebera.android.httpclient.entity.StringEntity;
//
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class Profile extends Fragment {
//    String sessionToken;
//    JSONObject userProfile;
//    EditText password, name, email, phone;
//
//    public Profile() {
//
//        // Required empty public constructor
//    }
//
//    public static Profile newInstance(String sessionToken){
//
//        Profile p = new Profile();
//        Bundle args = new Bundle();
//        args.putString("sessionToken",sessionToken);
//        p.setArguments(args);
//        return p;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        sessionToken = getArguments().getString("sessionToken");
//        getUserProfile();
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View v= inflater.inflate(R.layout.fragment_profile, container, false);
//        TextView idsjsu = (TextView)v.findViewById(R.id.idFromDb);
//        idsjsu.setText(sessionToken);
//
//        Log.d("here1","here");
//        try {
//            name= (EditText) v.findViewById(R.id.name_input);
//            name.setText(userProfile.getString("name"));
//            email= (EditText) v.findViewById(R.id.emailid_input);
//            email.setText(userProfile.getString("email"));
//            //TODO
//            //EditText notify=(EditText) v.findViewById(R.id.name_input);
//            //notify.setText(userProfile.getString("notify"));
//            phone=(EditText) v.findViewById(R.id.phoneno_input);
//            phone.setText(userProfile.getString("phone"));
//
//            password = (EditText) v.findViewById(R.id.password_input);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Button saveButton =(Button)v.findViewById(R.id.save_button);
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("saved","saved");
//                try {
//                    updatePutCall();
//                    //wait(1000);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//                getUserProfile();
//
//                try {
//                    name.setText(userProfile.getString("name"));
//                    email.setText(userProfile.getString("email"));
//                    //TODO
//                    //EditText notify=(EditText) v.findViewById(R.id.name_input);
//                    //notify.setText(userProfile.getString("notify"));
//                    phone.setText(userProfile.getString("phone"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        return v;
//    }
//
//    public synchronized void getUserProfile() {
//
//
//        String url = "studentCollection?where=sjsuId=='" + sessionToken + "'";
//
//        RestClient.get(url, new RequestParams(), new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                try {
//
//                    String s = new String(responseBody);
//                    Log.d("string", s);
//                    JSONObject json = new JSONObject(s);
//
//                    JSONArray items = json.getJSONArray("_items");
//                    Log.d("JSONArray", items.toString());
//
//                    if (items.length() != 0) {
//                        Log.d("here2","here");
//                        userProfile = (JSONObject) items.getJSONObject(0);
//
//
//                    } else {
//                        Toast.makeText(getContext(), "Username entered doesn't exists", Toast.LENGTH_LONG).show();
//                    }
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    //Toast.makeText(getApplicationContext(), "Error Occurred [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                // When Http response code is '404'
//                if (statusCode == 404) {
//                    Toast.makeText(getContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
//                }
//                // When Http response code is '500'
//                else if (statusCode == 500) {
//                    Toast.makeText(getContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
//                }
//                // When Http response code other than 404, 500
//                else {
//                    Toast.makeText(getContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
//                }
//            }
//
//        });
//
//    }
//    private void updatePutCall() throws JSONException, UnsupportedEncodingException, UnsupportedEncodingException {
//        JSONObject json = new JSONObject();
//        json.put("sjsuId", sessionToken);
//        json.put("email", email.getText().toString());
//        json.put("name",name.getText().toString());
//        json.put("phone",phone.getText().toString());
//        if (password.getText().toString().equals("")) {
//            json.put("password", userProfile.getString("password"));
//        } else {
//            json.put("password", password.getText().toString());
//        }
//        json.put("password", password);
//        //json.put("uuid",generateUUID());// need to change
//        json.put("uuid", userProfile.getString("uuid"));
//        json.put("phoneUnique", userProfile.getString("phoneUnique"));
//        json.put("notify", "1");
//
//        StringEntity entity = new StringEntity(json.toString());
//
//        final String eTag = userProfile.getString("_etag");
//
//        Log.d("check uuid", entity.toString());
//
//        Header header = new Header() {
//            @Override
//            public String getName() {
//                return "If-Match";
//            }
//
//            @Override
//            public String getValue() {
//                return eTag;
//            }
//
//            @Override
//            public HeaderElement[] getElements() throws ParseException {
//                return new HeaderElement[0];
//            }
//        };
//
//        Header[] headers = new Header[] {header};
//
//        String url = "studentCollection/".concat(userProfile.getString("_id"));
//
//
//        RestClient.put(this.getContext(), url, headers, entity, "application/json", new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                Log.d("response", response.toString());
//                Toast.makeText(getContext(), "Registration successful !", Toast.LENGTH_LONG).show();
//
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
//                Log.d("response", response.toString());
//                Toast.makeText(getContext(), "Registration Unsuccessful SJSU ID already registered !", Toast.LENGTH_LONG).show();
//            }
//        });
//
//    }
//
//    public void refresh(){
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(this).attach(this).commit();
//    }
//
//
//}
//
