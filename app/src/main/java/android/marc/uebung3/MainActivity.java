package android.marc.uebung3;
//tid 45
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.TimeUnit;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaCas;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.EditText;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    int sensorDelayopt;
    TextView tvLocationManager;
    //TextView tvLocProvClient;
    TextView tvCarrera;
    LocationManager locationManager;
    LocationListener locationListener;
    SensorEventListener eventListener1;

    SensorManager sensorManager;
    Geocoder geocoder;
    Switch switchLoc;
    Switch switchGyr;
    Switch switchAcc;
    Switch logging;
    Switch switchAccLin;
    Switch activateAPP;
    int sessionId = 126;

    public LocationRequest locationRequest;

    public SessionConnection sConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        switchAccLin =findViewById(R.id.switch4);
        switchAcc = findViewById(R.id.switch3);
        switchGyr = findViewById(R.id.switch2);
        switchLoc = findViewById(R.id.switch1);
        logging = findViewById(R.id.logging);
        activateAPP=findViewById(R.id.switch5);

        tvLocationManager = findViewById(R.id.tvLocationManager);
        //tvLocProvClient = findViewById(R.id.tvLocProvClient);
        tvCarrera =findViewById(R.id.tvLocProvClient);

        sConn = new SessionConnection();
        sConn.execute();


        ((EditText)findViewById(R.id.freqEdit)).addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String x = s.toString();
                try
                {
                    int delayOpt = Integer.parseInt(x);
                    sensorDelayopt = delayOpt;

                    sensorManager.unregisterListener(eventListener1, sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION));                                                                     ///////////
                    sensorManager.unregisterListener(eventListener1, sensorManager.getDefaultSensor(TYPE_GYROSCOPE));
                    sensorManager.unregisterListener(eventListener1, sensorManager.getDefaultSensor(TYPE_ACCELEROMETER));
                    sensorManager.registerListener(eventListener1, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), delayOpt);
                    sensorManager.registerListener(eventListener1, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delayOpt);
                }
                catch(Exception e){
                    sensorDelayopt = 0;
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
// TODO Auto-generated method stub

            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
// TODO Auto-generated method stub

            }

        });

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            startApplikation();
        }

        this.eventListener1 = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        accelerometer(sensorEvent);
                        break;

                    case Sensor.TYPE_GYROSCOPE:
                        gyroscope(sensorEvent);
                        break;

                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        linearaccelerometer(sensorEvent);
                        break;
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }

        };
        sensorManager.registerListener(eventListener1, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(eventListener1, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(eventListener1, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void startApplikation() {
        positionierungMitLocationManager();
        positionierungMitLocationProvider();
        //sensor2();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (alleBerechtigungenErteilt(grantResults)) {
            startApplikation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    float[] values = new float[9];
    public void collectSensorValues(float gyrosX,float gyrosY, float gyrosZ, float linearAccX,float linearAccY,float linearAccZ,float Latitude, float Longitude, float Altitude)
    {
        if (gyrosX<1000) {
            values[0] = gyrosX;
        }
        if (gyrosY<1000) {
            values[1] = gyrosY;
        }
        if (gyrosZ<1000) {
            values[2] = gyrosZ;
        }
        if (linearAccX<1000) {
            values[3] = linearAccX;
        }
        if (linearAccY<1000) {
            values[4] = linearAccY;
        }
        if (linearAccZ<1000) {
            values[5] = linearAccZ;
        }
        if (Latitude<1000) {
            values[6] = Latitude;
        }
        if (Longitude<1000) {
            values[7] = Longitude;
        }
        if (Altitude<1000) {
            values[7] = Altitude;
        }

        Log.d(TAG," "+values[0]+" "+values[1]+" "+values[2]+" "+values[3]+" "+values[4]+" "+values[5]+" "+values[6]+" "+values[7]+" "+values[8]);
        //try {
            klassifiziereCarreraBahn(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7],values[8]);
        /*}
        catch(InterruptedException e){
            System.out.print("interrupted");
    }*/
    }



    public void positionierungMitLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(getApplicationContext());
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                     List<Address> adressdaten = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Address address = adressdaten.get(0);
                    String adresszeile = address.getAddressLine(0);
                    tvLocationManager.setText("Latitude: " + location.getLatitude() +
                            "\nLongitude: " + location.getLongitude() /* +
                            "\nAltitude: " + location.getAltitude() +
                            "\nSpeed: " + location.getSpeed() +
                            "\nAdresse: " + adresszeile*/);
                    collectSensorValues(10000,10000,10000,10000,10000,10000,(float)location.getLatitude(),(float)location.getLongitude(),(float)location.getAltitude());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1L, 1f, locationListener);
    }

    private void positionierungMitLocationProvider() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());

    }

    private void onLocationChanged(Location location) {
        try {
            List<Address> adressdaten = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = adressdaten.get(0);
            String adresszeile = address.getAddressLine(0);
            /*tvLocProvClient.setText("Latitude: " + location.getLatitude() +
                    "\nLongitude: " + location.getLongitude() +
                    "\nAltitude: " + location.getAltitude() +
                    "\nSpeed: " + location.getSpeed() +
                    "\nAdresse: " + adresszeile);*/


            if (switchLoc.isChecked()) {


                try {
                    speichern(location);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void speichern(Location aktuellePosition) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();


        try {
            jsonObject = new JSONObject();
            jsonObject.put("data", aktuellePosition.getLatitude());
            jsonObject.put("time", aktuellePosition.getTime());
            jsonObject.put("vid", 14);
            jsonObject.put("sid", sConn.SessionID);
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("data", aktuellePosition.getLongitude());
            jsonObject.put("time", aktuellePosition.getTime());
            jsonObject.put("vid", 15);
            jsonObject.put("sid", sConn.SessionID);
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("data", aktuellePosition.getAltitude());
            jsonObject.put("time", aktuellePosition.getTime());
            jsonObject.put("vid", 21);
            jsonObject.put("sid", sConn.SessionID);
            jsonArray.put(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        new Connection().execute(jsonArray.toString());

    }




        public void gyroscope (SensorEvent sensorEvent){



            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            if(!logging.isChecked())
                return;

            if(switchGyr.isChecked()) {
                //Log.d(TAG,"GYROSCOPE_onSensorChanged: X: "+ sensorEvent.values[0] + "Y: " + sensorEvent.values[1] + "Z: "+ + sensorEvent.values[2]);
                collectSensorValues(sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2],10000,10000,10000,10000,10000,10000);
                //tvCarrera.setText(klassifiziereCarreraBahn(sensorEvent.values[2]));
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[0]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 14);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[1]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 15);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[2]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 21);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                new Connection().execute(jsonArray.toString());
            }

        }
    public void linearaccelerometer (SensorEvent sensorEvent){

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        if(!logging.isChecked())
            return;




        if(switchAccLin.isChecked()) {
           // Log.d(TAG,"LinearAccelerometer_onSensorChanged: X: "+ sensorEvent.values[0]+ "Y: " + sensorEvent.values[1] + "Z: "+ + sensorEvent.values[2]);
            //tvCarrera.setText(klassifiziereCarreraBahn(sensorEvent.values[0]));
            collectSensorValues(10000,10000,10000,sensorEvent.values[0],sensorEvent.values[1],sensorEvent.values[2],10000,10000,10000);
            try {
                jsonObject = new JSONObject();
                jsonObject.put("data", sensorEvent.values[0]);
                jsonObject.put("time", sensorEvent.timestamp);
                jsonObject.put("vid", 14);
                jsonObject.put("sid", sConn.SessionID);
                jsonArray.put(jsonObject);

                jsonObject = new JSONObject();
                jsonObject.put("data", sensorEvent.values[1]);
                jsonObject.put("time", sensorEvent.timestamp);
                jsonObject.put("vid", 15);
                jsonObject.put("sid", sConn.SessionID);
                jsonArray.put(jsonObject);

                jsonObject = new JSONObject();
                jsonObject.put("data", sensorEvent.values[2]);
                jsonObject.put("time", sensorEvent.timestamp);
                jsonObject.put("vid", 21);
                jsonObject.put("sid", sConn.SessionID);
                jsonArray.put(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }


            new Connection().execute(jsonArray.toString());
        }
    }

        private void accelerometer (SensorEvent sensorEvent){

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            if(!logging.isChecked())
                return;




            if(switchAcc.isChecked()) {
                Log.d(TAG,"Accelerometer_onSensorChanged: X: "+ sensorEvent.values[0] + "Y: " + sensorEvent.values[1] + "Z: "+ + sensorEvent.values[2]);
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[0]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 14);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[1]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 15);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                    jsonObject = new JSONObject();
                    jsonObject.put("data", sensorEvent.values[2]);
                    jsonObject.put("time", sensorEvent.timestamp);
                    jsonObject.put("vid", 21);
                    jsonObject.put("sid", sConn.SessionID);
                    jsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                new Connection().execute(jsonArray.toString());
            }
        }


    private boolean alleBerechtigungenErteilt(int[] erteilteBerechtigungen) {
        for (int erteilteBerechtigung : erteilteBerechtigungen) {
            if (erteilteBerechtigung == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    public void klassifiziereCarreraBahn(float gyrosX,float gyrosY, float gyrosZ, float linearAccX,float linearAccY,float linearAccZ, double Latitude, double Longitude, double Altitude)/*throws InterruptedException*/{
        String url;

        if (51.446<Longitude& Longitude<51.448& 7.270<Latitude&Latitude<7.274 ) { //Location HS Bochum
            if (Math.abs(linearAccZ) < 0.3) {
                if (gyrosZ < -1.00) {
                    tvCarrera.setText("right");
                    url = "https://www.google.de/?hl=de";
                    ausfuehren(url);
                    //sleep(5000);

                    return;


                } else if (gyrosZ > 1.00) {
                    tvCarrera.setText("left");
                    url = "https://www.hochschule-bochum.de";
                    ausfuehren(url);
                    //sleep(5000);
                    return;

                }

            } else {
                tvCarrera.setText("no Action");
                return;

            }
            if (linearAccZ < -7.00 & Math.abs(linearAccY) < 1.5 & Math.abs(linearAccX) < 1.5) {
                tvCarrera.setText("UP");
                url = "https://moodle.de/";
                ausfuehren(url);
                //sleep(5000);
                return;
            }
        }
        //if (51.49<Longitude& Longitude<51.491& 7.3<Latitude&Latitude<7.31 ) {  //Location Bochum Werne
            if (Math.abs(linearAccZ) < 0.8) {             //without eventuell
                if (gyrosZ < -1.00) {
                    tvCarrera.setText("right");
                    url = "https://www.google.de/?hl=de";
                    ausfuehren(url);
                    //sleep(5000);

                    return;


                } else if (gyrosZ > 1.00) {
                    tvCarrera.setText("left");
                    url = "https://www.hochschule-bochum.de";
                    ausfuehren(url);
                    //sleep(5000);
                    return;

                }
            }
            else if (gyrosX>4.00){
                    tvCarrera.setText("up");
                    url = "https://www.vfl-bochum.de/startseite/";
                    ausfuehren(url);
                    //sleep(5000);
                return;
            }
                else {
                tvCarrera.setText("no Action");
                return;

            }
            if (linearAccZ < -7.00 & Math.abs(linearAccY) < 1.5 & Math.abs(linearAccX) < 1.5) {
                tvCarrera.setText("UP");
                url = "https://moodle.de/";
                ausfuehren(url);
                //sleep(5000);
                return;
            }
        //}
    }
    String lasturl;
    public void ausfuehren(String url){
        if (activateAPP.isChecked()) {
            if(url.equals(lasturl)) {
                return;
            }
            else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                startActivity(intent);
                lasturl = url;
            }
        }

    }

    public static void sleep(long millis){
        sleep(5000);
    }

}



