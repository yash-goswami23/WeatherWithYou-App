package com.example.weatherwithyou;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherwithyou.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Location currentLocation;
    String api_key = "4b34c97f794147dbbff27159ac8fd864";
    FusedLocationProviderClient fusedLocationProviderClient;
    ActivityMainBinding binding;
    String SearchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        binding.myWeatherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Get_Location();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PremissionRequest();
        }
    }
    private void PremissionRequest(){
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                fineLocationGranted = result.getOrDefault(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                            }
                            Boolean coarseLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                coarseLocationGranted = result.getOrDefault(
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            }
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Get_Location();

                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                Get_Location();
                            } else {
                                // No location access granted.
                                PremissionRequest();
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
    private void Get_Location() {
        Log.d("getLocation", "enter in Method");
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
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.d("getLocation", currentLocation.toString());
                    String url = "https://api.openweathermap.org/data/2.5/weather?lat="+currentLocation.getLatitude()+"&lon="+currentLocation.getLongitude()+"&appid=8bd478ec32f02f4d2e79510ca318c6fd&lang={lang}";
                    Log.d("getLocation", url);
                    binding.SearchView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String Url = searchMethod();
                            Log.d("SearchMethod","Link on click : "+ Url);
                            StringRequest stringRequest  = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.e("checkResponse",response);
                                    try {
                                        JSONObject jsonObject =new JSONObject(response);
                                        JSONArray jsonArray = jsonObject.getJSONArray("weather");
                                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                                        String desc = jsonObjectWeather.getString("description");
                                        JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                                        float temp = (float) (jsonObjectMain.getDouble("temp")-273);
                                        float temp_max = (float) (jsonObjectMain.getDouble("temp_max")-273);
                                        float temp_min = (float) (jsonObjectMain.getDouble("temp_min")-273);
                                        float feellike = (float) (jsonObjectMain.getDouble("feels_like")-273);
                                        float airPressure = jsonObjectMain.getInt("pressure");
                                        double humidity =jsonObjectMain.getDouble("humidity");
                                        JSONObject jsonObjectWind = jsonObject.getJSONObject("wind");
                                        String wind = jsonObjectWind.getString("speed");
                                        JSONObject jsonObjectClouds = jsonObject.getJSONObject("clouds");
                                        String clouds = jsonObjectClouds.getString("all");
                                        JSONObject jsonObjectSys = jsonObject.getJSONObject("sys");
                                        String countryName = jsonObjectSys.getString("country");
                                        String cityName = jsonObject.getString("name");
                                        int visibility = jsonObject.getInt("visibility");

                                        float km = convertMeterToKilometer(visibility);

                                        switch (desc){
                                            case "clear sky" :
                                                binding.DescripitionImage.setImageResource(R.drawable.cloudy_sunny);
                                                break;
                                            case "overcast clouds" :
                                                binding.DescripitionImage.setImageResource(R.drawable.cloudy);
                                                break;
                                        }

                                        Log.d("weather_response"," Description "+desc);
                                        Log.d("weather_response"," temp "+temp);
                                        Log.d("weather_response"," temp_max "+temp_max);
                                        Log.d("weather_response"," temp_min "+temp_min);
                                        Log.d("weather_response"," feellike "+feellike);
                                        Log.d("weather_response"," airPressure "+airPressure);
                                        Log.d("weather_response"," visibility "+km);
                                        Log.d("weather_response"," humidity "+humidity);
                                        Log.d("weather_response"," wind "+wind);
                                        Log.d("weather_response"," clouds "+clouds);
                                        Log.d("weather_response"," cityName "+cityName);



                                        binding.descripitionText.setText(desc);
                                        binding.cityText.setText(cityName);
                                        binding.TempMinText.setText(String.valueOf(temp_min)+"°C");
                                        binding.TempMaxText.setText(String.valueOf(temp_max)+"°C");
                                        binding.TempTextView.setText(String.valueOf(temp)+"°C");
                                        binding.feelText.setText(String.valueOf(feellike)+"°");
                                        binding.humidityText.setText(String.valueOf(humidity)+"%");
                                        binding.airpressure.setText(String.valueOf(airPressure)+"hpa");
                                        binding.windText.setText(wind);
                                        binding.visibilityText.setText(String.valueOf(km)+"km");

                                    }catch (Exception e){
                                        Log.d("checkResponse", String.valueOf(e));
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("checkResponse", String.valueOf(error));
                                }
                            });
                            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                            requestQueue.add(stringRequest);
                        }
                    });
                    StringRequest stringRequest  = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.e("checkResponse",response);
                            try {
                                JSONObject jsonObject =new JSONObject(response);
                                JSONArray jsonArray = jsonObject.getJSONArray("weather");
                                JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                                String desc = jsonObjectWeather.getString("description");
                                JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                                float temp = (float) (jsonObjectMain.getDouble("temp")-273);
                                float temp_max = (float) (jsonObjectMain.getDouble("temp_max")-273);
                                float temp_min = (float) (jsonObjectMain.getDouble("temp_min")-273);
                                float feellike = (float) (jsonObjectMain.getDouble("feels_like")-273);
                                float airPressure = jsonObjectMain.getInt("pressure");
                                double humidity =jsonObjectMain.getDouble("humidity");
                                JSONObject jsonObjectWind = jsonObject.getJSONObject("wind");
                                String wind = jsonObjectWind.getString("speed");
                                JSONObject jsonObjectClouds = jsonObject.getJSONObject("clouds");
                                String clouds = jsonObjectClouds.getString("all");
                                JSONObject jsonObjectSys = jsonObject.getJSONObject("sys");
                                String countryName = jsonObjectSys.getString("country");
                                String cityName = jsonObject.getString("name");
                                int visibility = jsonObject.getInt("visibility");

                                float km = convertMeterToKilometer(visibility);

                                Log.d("weather_response"," Description "+desc);
                                Log.d("weather_response"," temp "+temp);
                                Log.d("weather_response"," temp_max "+temp_max);
                                Log.d("weather_response"," temp_min "+temp_min);
                                Log.d("weather_response"," feellike "+feellike);
                                Log.d("weather_response"," airPressure "+airPressure);
                                Log.d("weather_response"," visibility "+km);
                                Log.d("weather_response"," humidity "+humidity);
                                Log.d("weather_response"," wind "+wind);
                                Log.d("weather_response"," clouds "+clouds);
                                Log.d("weather_response"," cityName "+cityName);


                                switch (desc){
                                    case "clear sky" :
                                        binding.DescripitionImage.setImageResource(R.drawable.cloudy_sunny);
                                        break;
                                    case "overcast clouds" :
                                        binding.DescripitionImage.setImageResource(R.drawable.cloudy);
                                        break;
                                }

                                binding.descripitionText.setText(desc);
                                binding.cityText.setText(cityName);
                                binding.TempMinText.setText(String.valueOf(temp_min)+"°C");
                                binding.TempMaxText.setText(String.valueOf(temp_max)+"°C");
                                binding.TempTextView.setText(String.valueOf(temp)+"°C");
                                binding.feelText.setText(String.valueOf(feellike)+"°");
                                binding.humidityText.setText(String.valueOf(humidity)+"%");
                                binding.airpressure.setText(String.valueOf(airPressure)+"hpa");
                                binding.windText.setText(wind);
                                binding.visibilityText.setText(String.valueOf(km)+"km");

                            }catch (Exception e){
                                Log.d("checkResponse", String.valueOf(e));
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("checkResponse", String.valueOf(error));
                        }
                    });
                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    requestQueue.add(stringRequest);
                }
            }
        });
    }

    private String searchMethod(){
        String url;
        Log.d("SearchMethod ","enter method");
        SearchView = binding.SearchView.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(SearchView, 1);
        }catch (IOException e){
            Log.e("SearchMethod","IOException:" + e.getMessage());
        }
        if(list.size() > 0){
            Address address = list.get(0);
            Log.d("SearchMethod","Found a location: "+ address.toString());
            url = "https://api.openweathermap.org/data/2.5/weather?lat="+address.getLatitude()+"&lon="+address.getLongitude()+"&appid=8bd478ec32f02f4d2e79510ca318c6fd&lang={lang}";
            Log.d("SearchMethod","Link : "+ url);
            return url;
        }else
            Toast.makeText(this, "Enter the Right City Name", Toast.LENGTH_SHORT).show();
            return null;
    }

    private float convertMeterToKilometer(float meter) {
        return (float) (meter * 0.001);
    }
}