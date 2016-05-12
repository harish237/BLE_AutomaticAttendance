package hanumesh.sjsu.attendancematters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MarkAttendance extends AppCompatActivity {
    String FILENAME = "isMarked";
    String SESSIONFILE = "session";
    String emailID;


    /*public Boolean isAttendenceMarked() {

        FileInputStream fin = null;
        String isMarked = "";
        try {
            fin = openFileInput(FILENAME);
            int c;

            while ((c = fin.read()) != -1) {
                isMarked = isMarked + Character.toString((char) c);
            }

            Log.d("ismarked",isMarked);
            Log.d("check",new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception d) {

        }

        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        Log.d("system date", date);
        return date.equalsIgnoreCase(isMarked);
    }*/


    public void getAttendanceDocument(final String classToMark, String sessionToken) {

        String url = "attendanceCollection?where=sjsuId=='" + sessionToken + "' and course=='" + classToMark + "'";
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
                        Log.d("here2", "here");
                        JSONObject attendanceObject = items.getJSONObject(0);

                        JSONArray a = attendanceObject.getJSONArray("dates");

                        int mark = 1;
                        for (int i = 0; i<a.length(); i++ ) {
                            if (a.getString(i).equals(new SimpleDateFormat("MM-dd-yyyy").format(new Date()))){
                                mark = 0;
                            }
                        }

                        if (mark == 1) {
                            putAttendance(attendanceObject.getString("_id"), attendanceObject.getString("_etag"), attendanceObject.getString("sjsuId"), attendanceObject.getString("course"), attendanceObject.getJSONArray("dates"));
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
                Log.d("error", new String(responseBody));
                if (statusCode == 404) {
                    //Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    //Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    //Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        });

    }

    public void putAttendance(String id, final String eTag, final String sjsuId, final String classToMark, JSONArray attendence) throws JSONException, UnsupportedEncodingException {


        Log.d("data", id+eTag+sjsuId + classToMark + attendence.toString());
        JSONObject json = new JSONObject();
        json.put("sjsuId", sjsuId);
        json.put("course", classToMark);



        attendence.put(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));



        json.put("dates", attendence);

        Log.d("data", json.toString());

        StringEntity entity = new StringEntity(json.toString());

        /*
        RestClient.post(this.getApplicationContext(), "attendanceCollection/" + id, entity, "application/json", new JsonHttpResponseHandler() {
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
        });*/


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

        String url = "attendanceCollection/".concat(id);

        Log.d("etag", eTag);

        Log.d("Query Data", entity.toString());


        RestClient.put(this, url, headers, entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("response", response.toString());
                /*String todaysDate=new SimpleDateFormat("MM-dd-yyyy").format(new Date());
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(todaysDate.getBytes());
                    fos.close();
                    Log.d("write","successful");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                getEmailFromSjduID(sjsuId);
                SendEmail sm = new SendEmail(emailID, classToMark+"  Attendance Marked ", "Your Attendance for the day has been marked. Happy Studying !! --Admin");

                //Executing sendmail to send email
                sm.execute();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("response", response.toString());
            }
        });

    }

    private void getEmailFromSjduID(String sjsuID) {
        String url = "studentCollection?where=sjsuId=='" + sjsuID + "'";


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
                        JSONObject obj = (JSONObject) items.getJSONObject(0);
                        emailID = obj.getString("email");

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
                    //Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                   // Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    //Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

        });

    }
}
