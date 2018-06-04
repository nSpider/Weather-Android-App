package com.nspider.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText location;
    TextView tv;
    TextView temp;
    ImageView icon;
    TextView feels;
    TextView text;

    IconDownloader iconDownloader;
    GetWeather getWeather;

    LocationManager locationManager;
    LocationListener locationListener;

    Location userCurrentLocation;

    TextView searchTextView;
    ImageView weatherImage;
    ImageView searchImage;

    boolean inputState = false;

    public class IconDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            try {
                URL url = new URL("https:" + strings[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                return BitmapFactory.decodeStream(inputStream);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            icon.setImageBitmap(bitmap);
        }
    }

    public class GetWeather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {

                URL url = new URL(strings[0]);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();

                String res = "";

                while (data > -1) {
                    char c = (char) data;
                    res += c;
                    data = inputStreamReader.read();
                }

                return res;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.i("JSON", s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject condition = current.getJSONObject("condition");
                double tempC = current.getDouble("temp_c");
                String tempF = current.getString("temp_f");
                String weather = tempC + " \u00b0C " + tempF + " \u00b0F";
                String loc = (jsonObject.getJSONObject("location")).getString("name");
                if (!(jsonObject.getJSONObject("location")).getString("region").equals("")) {
                    loc += ", " + (jsonObject.getJSONObject("location")).getString("region");
                }
                loc += ", " + (jsonObject.getJSONObject("location")).getString("country");
                Log.i("Temp", weather);
                temp.setText(String.valueOf((int) tempC));
                feels.setText("Feels like " + current.getInt("feelslike_c") + "\u00b0C");
                tv.setVisibility(View.INVISIBLE);
                //tv.setText("Weather at " + loc);
                searchTextView.setText(loc);
                backToViewState();
                Log.i("Condition", condition.getString("icon"));
                text.setText(condition.getString("text"));
                iconDownloader = new IconDownloader();
                iconDownloader.execute(condition.getString("icon"));
            } catch (JSONException e) {
                tv.setVisibility(View.VISIBLE);
                tv.setText("Location not Found !!");
                backToViewState();
                e.printStackTrace();
            }
        }
    }

    public void back (View view){

        if (inputState){

            location.setText("");
            backToViewState();

        } else {

            search(view);

        }

    }

    private void backToViewState() {

        weatherImage.setImageResource(R.drawable.ic_weather);
        searchTextView.setVisibility(View.VISIBLE);
        searchImage.setImageResource(R.drawable.ic_search);
        location.setVisibility(View.GONE);
        inputState = false;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(location.getWindowToken(),0);
        }
    }

    public void clear (View view) {

        if (inputState) {

            location.setText("");

        } else {

            search(view);

        }

    }

    public void search (View view) {

        weatherImage.setImageResource(R.drawable.ic_back);
        searchTextView.setVisibility(View.GONE);
        searchImage.setImageResource(R.drawable.ic_close);
        location.setVisibility(View.VISIBLE);
        inputState = true;
        location.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(location, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        location = findViewById(R.id.editText);
        tv = findViewById(R.id.textView);
        temp = findViewById(R.id.temp);
        icon = findViewById(R.id.icon);
        feels = findViewById(R.id.feels);
        text = findViewById(R.id.text);
        searchTextView = findViewById(R.id.searchTextView);
        weatherImage = findViewById(R.id.iconBackButton);
        searchImage = findViewById(R.id.iconSearchEditText);

//        location.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_RIGHT = 2;
//
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (event.getRawX() >= (location.getRight() - location.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        performSearch();
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

        location.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            if (locationManager != null) {
                userCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currentWeather(userCurrentLocation);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            userCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (userCurrentLocation != null) {
                Log.i("Location", userCurrentLocation.toString());
                currentWeather(userCurrentLocation);
            }
        }
    }

    public void currentWeather (Location loc){
        try {
            getWeather = new GetWeather();
            getWeather.execute("http://api.apixu.com/v1/current.json?key=9d35752f841541c7b3f165914181005&q="+ loc.getLatitude() + "," + loc.getLongitude());
            Log.i("location",loc.getLatitude() + "," + loc.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performSearch() {
        String loc;
        try {
            if (location.getText().toString().equals("")){
                location.setError("Enter a location first");
                return;
            }
            loc = URLEncoder.encode(location.getText().toString(),"UTF-8");
            getWeather = new GetWeather();
            getWeather.execute("http://api.apixu.com/v1/current.json?key=9d35752f841541c7b3f165914181005&q="+loc);
            Log.i("location",loc);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
