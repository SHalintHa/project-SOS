package com.shalintha.sos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String ANONYMOUS = "anonymous";
    GoogleApiClient gac;
    LocationRequest locationRequest;
    private static final int UPDATE_INTERVAL = 15 * 1000;
    private static final int FASTEST_UPDATE_INTERVAL = 2 * 1000;
    private static final int notification_request_code = 100;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location lastLocation,lastLocationGpsProvider;
    private Button mapButton;

    private static final String BACKGROUND_SERVICE_STATUS = "bgServiceStatus";
    SharedPreferences sharedpreferences;
    private String MyPREFERENCES="SOS_DATA";
    private boolean isServiceBackground;
    private ImageButton emergencyButton ;
    private ImageButton policeButton;
    private ImageButton hospitalButton;
    TextView tv;
    Button b;
    private String location_long,location_lat;
    private GoogleMap mMap;

    ArrayList<String> PhoneList = new ArrayList<String>();
    SmsManager smsManager = SmsManager.getDefault();
    String locType;
    ArrayList<String> PhoneType = new ArrayList<String>();
    private String mapJsonData="";
    private String jsonData="";



    final LatLng TutorialsPoint = new LatLng(21 , 57);
    //Marker TP = mMap.addMarker(new MarkerOptions()
            //.position(TutorialsPoint).title("TutorialsPoint"));

    //Firebase Variables
//    static {
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//    }
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseuser;
    FirebaseDatabase database;
    DatabaseReference myRef,myUserRef;
    private String mUsername;
    private String mPhotoUrl;
    private ChildEventListener childEventListener;
    private String mEmail;
    private String mUid;

    //Notification
    NotificationManager nm;
    String CHANNEL_ID = "my_sos_channel";// The id of the channel.
    Notification n;
    Notification.Builder nb;

    private static final String TAG = "MainActivity";


    SmsManager smgr = SmsManager.getDefault();

    protected void sendSMS(String number){


        try {
            smgr.sendTextMessage(number,null,"SOS",null,null);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    protected void sendCall(String number){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        gac.connect();
        if (childEventListener != null) {
            myRef.addChildEventListener(childEventListener);
            Log.d(TAG, "onStart: ChildEventListener Attached");
        }

        //Optional parameters
        requestSmsPermission();


        mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                Intent myIntent = new Intent(MainActivity.this, MapActivity.class);
                myIntent.putExtra("long",location_long);
                myIntent.putExtra("lat",location_lat);
                startActivity(myIntent);

            }
        });

        emergencyButton =findViewById(R.id.imageButton);
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button


                sendSMS("+94754871491");
                executeEmergency();

            }
        });

        policeButton = findViewById(R.id.imageButton2);
        policeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS("+94754871491");
                sendCall("+94754871491");
                executeEmergency();
            }
        });

        hospitalButton = findViewById(R.id.imageButton3);
        hospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSMS("+94754871491");
                sendCall("+94754871491");
                executeEmergency();
            }
        });


        stopService(new Intent(this,MyService.class));
        super.onStart();
    }




    protected void executeEmergency(){
        Toast.makeText(getApplicationContext(),"Emergency Activated",Toast.LENGTH_LONG).show();

        SmsManager smgr = SmsManager.getDefault();
        //smgr.sendTextMessage("+94714076576",null,"sms message",null,null);
        mapApiCall(6.927079,	79.861244, "hospital");
        mapApiCall(6.927079,	79.861244, "police");

        System.out.println(PhoneList.toString());
        System.out.println("Done");
    }

    protected void mapApiCall(double  lng , double lat, final String type){
        // locType = type;
        final TextView textView = (TextView) findViewById(R.id.apiResponseText);
        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +    lng + "," + lat  +  "&radius=7500&type="+type+"&keyword=&key=AIzaSyBV1O_pdaq984Lqjf8DAe-fdpn8egV8Dtw";

        //https://maps.googleapis.com/maps/api/place/details/json?place_id=ChIJde33x7sPdkgR9bu4O8Rl0Gw&fields=name,rating,formatted_phone_number&key=AIzaSyBV1O_pdaq984Lqjf8DAe-fdpn8egV8Dtw
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //textView.setText("Response is: "+ response);
                        Log.d(TAG,response);
                        mapJsonData=response;
                        //System.out.println("MAP Json Data :" +mapJsonData);
                        // System.out.println("MAP Json Data  :" +mapJsonData);

                        try {
                            mapJsonParser(mapJsonData,type);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });


        RequestQueue queue = Volley.newRequestQueue(this);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

        //return mapJsonData;

    }
    protected void  mapJsonParser(String jsonString ,String type) throws JSONException {
        ArrayList<String> placeIdList = new ArrayList<String>();

        JSONObject reader = new JSONObject(jsonString);
        JSONArray results  = reader.getJSONArray("results");
        int jsonArrayLength = results.length();
        System.out.println("result :" +results);

        for (int i=0 ;i <jsonArrayLength ; i ++){

            JSONObject r1 = results.getJSONObject(i);
            String place_id = r1.getString("place_id");
            placeIdList.add(place_id);

            String place_data =placeApiCall(place_id,type);
            return;
            //System.out.println("Place Data :"+ place_data);

        }

        //return placeIdList;
        // JSONObject r1 = results.getJSONObject(1);
        //String id = r1.getString("place_id");

        //Log.d(TAG,id);
        // System.out.println(id);
        //Toast.makeText(getApplicationContext(),id,Toast.LENGTH_LONG).show();


    }
    protected String placeApiCall(String id, final String type){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/details/json?place_id="+id+"&fields=name,rating,formatted_phone_number&key=AIzaSyBV1O_pdaq984Lqjf8DAe-fdpn8egV8Dtw";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //textView.setText("Response is: "+ response);

                        Log.d(TAG,response);

                        jsonData=  (response);
                        try {
                            placeJsonParser(jsonData,type);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //System.out.println(jsonData);
                        /*try {
                            ArrayList<String> phoneList = placeJsonParser(jsonData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        */
                        //textView.setText("Response is: "+ mapJsonParser(response));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"That didn't work!");
            }
        });

        queue.add(stringRequest);
        return jsonData;


    }
    protected void  placeJsonParser(String jsonString,String type) throws JSONException {
        while (true){
            JSONObject reader = new JSONObject(jsonString);
            JSONObject results  = reader.getJSONObject("result");
            String phone = results.getString("formatted_phone_number");
            System.out.println(type +" Phone:" +phone);
            Toast.makeText(getApplicationContext(),type +" Phone:" +phone,Toast.LENGTH_LONG).show();
            PhoneList.add(phone);
            PhoneType.add(type);
            //sendSMS(phone);

            return;
            //return PhoneList;
        }


    }














    @Override
    protected void onStop() {
        gac.disconnect();
        if (childEventListener != null) {
            myRef.removeEventListener(childEventListener);
            Log.d(TAG, "onStop: ChildEventListener Removed");
        }

        if(isServiceBackground&&FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            startService(new Intent(this,MyService.class));
            Log.d(TAG, "onStop: starting service");
        }
        super.onStop();
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.textView);
        b=findViewById(R.id.button);

        nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence name = "SOS ALERT";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    name,
                    importance);
            nm.createNotificationChannel(channel);
        }


        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        isServiceBackground=sharedpreferences.getBoolean(BACKGROUND_SERVICE_STATUS,true);
        if(isServiceBackground)
            b.setText("Stop background Notification");
        else
            b.setText("Start background Notification");




        /* firebase initialization */
        mUsername = ANONYMOUS;
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseuser=mFirebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Locations");
        myUserRef=database.getReference("Users");
        myRef.keepSynced(true);

        if(mFirebaseuser==null)
        {
            startActivity(new Intent(this,LoginActivity.class));
            finish();
            return;
        }
        else {

            mEmail=mFirebaseuser.getEmail();
            if(!TextUtils.isEmpty(mFirebaseuser.getDisplayName()))
                mUsername=mFirebaseuser.getDisplayName();
            else {
                mUsername = mEmail.split("@")[0];
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(mUsername).build();

                mFirebaseuser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: name intialized as "+mUsername);
                                }
                            }
                        });



            }
            mUid=mFirebaseuser.getUid();
            if(mFirebaseuser.getPhotoUrl()!=null)
                mPhotoUrl=mFirebaseuser.getPhotoUrl().toString();
            Toast.makeText(this, "Welcome\n"+mUsername, Toast.LENGTH_SHORT).show();

        }

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

//                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
//                Toast.makeText(MainActivity.this, fld.getEmail()+" added", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());


                final FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                Log.d(TAG, "onChildChanged: Creating Notification");
                assert fld != null;
                myUserRef.child(fld.getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String sos_name=dataSnapshot.getValue(String.class);
                        nb= new Notification.Builder(MainActivity.this);
                        nb.setContentTitle("Emergency");
                        nb.setContentText("SOS broadcasted from "+sos_name);
                        nb.setSmallIcon(android.R.drawable.ic_dialog_alert);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            nb.setChannelId(CHANNEL_ID);
                        }
                        nb.setDefaults(Notification.DEFAULT_ALL);
                        Intent i =new Intent(MainActivity.this,MapsActivity.class);
                        Bundle b = new Bundle();
                        b.putDouble("lat", fld.getLatitude());
                        b.putDouble("long", fld.getLongitude());
                        b.putString("name", sos_name);
                        b.putString("time",fld.getSos_time());
                        i.putExtras(b);;
                        nb.setAutoCancel(false);
                        PendingIntent pi =PendingIntent.getActivity(MainActivity.this,notification_request_code,i,PendingIntent.FLAG_UPDATE_CURRENT);
                        nb.setContentIntent(pi);
                        n=nb.build();
                        nm.notify(notification_request_code,n);
                        Log.d(TAG, "onDataChange: NOTIFICATION CREATED");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });





//                Toast.makeText(MainActivity.this,fld.getEmail()+" changed", Toast.LENGTH_SHORT).show();
            }







            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                String locKey = dataSnapshot.getKey();


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                FirebaseLocationData fld = dataSnapshot.getValue(FirebaseLocationData.class);
                String locKey = dataSnapshot.getKey();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, ":onCancelled", databaseError.toException());
            }
        };
        if(childEventListener!=null)
            Log.d(TAG, "onCreate: childEventListenerCreated");



        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);



        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert service != null;
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        gac = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.firebase_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.profile:
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));

                return true;
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                mUsername=ANONYMOUS;
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                return  true;
            case R.id.view_location:
                startActivity(new Intent(MainActivity.this,LocationListActivity.class));
                return true;
            case R.id.info:
                startActivity(new Intent(MainActivity.this,AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void startSos(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
//        mFusedLocationProviderClient.getLastLocation()
//                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            lastLocation = task.getResult();
//
////                            txtLatitude.setText(String.valueOf(lastLocation.getLatitude()));
////                            txtLongitude.setText(String.valueOf(lastLocation.getLongitude()));
//
//                        } else {
//                            Log.w(TAG, "getLastLocation:exception", task.getException());
////                            showSnackbar(getString(R.string.no_location_detected));
//                        }
//                    }
//                });

        LocationManager lm= (LocationManager) getSystemService(LOCATION_SERVICE);
        lastLocationGpsProvider=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        //write data to firebase

        if(lastLocationGpsProvider!=null)
            updateUserLocationToFirebase(lastLocationGpsProvider);
        else
        {
            lastLocationGpsProvider=LocationServices.FusedLocationApi.getLastLocation(gac);
            if(lastLocationGpsProvider!=null)
                updateUserLocationToFirebase(lastLocationGpsProvider);
        }



//        Toast.makeText(this, "GPS location without google client\n"+lm.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString(), Toast.LENGTH_SHORT).show();



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            return;
        }
        else{
//            Toast.makeText(this, "Permission is given", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Permission is already given");
        }

        // TODO: use fusedlocaionproviderclient
        /* mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.

                        if (location != null) {
                            // Logic to handle location object
                            lastLocation=location;
                        }
                    }
                });
        */
        Location ll = LocationServices.FusedLocationApi.getLastLocation(gac);


        Log.d(TAG, "LastLocation from Deprecated: " + (ll == null ? "NO LastLocation" : ll.toString()));
//        tv.setText("LastLocation from Deprecated: " + (ll == null ? "NO LastLocation" : ll.toString()));
//        Log.d(TAG, "LastLocation: " + (ll == null ? "NO LastLocation" : lastLocation.toString()));
        assert ll != null;
//        updateUI(ll);

        LocationServices.FusedLocationApi.requestLocationUpdates(gac, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "onConnectionFailed: \n" + connectionResult.toString(),
                Toast.LENGTH_LONG).show();
        Log.d(TAG, connectionResult.toString());

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updateUI(location);
        }

    }
    @SuppressLint("SetTextI18n")
    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");
        location_lat = Double.toString(loc.getLatitude());
        location_long = Double.toString(loc.getLongitude());
        tv.setText(Double.toString(loc.getLatitude()) + '\n' + Double.toString(loc.getLongitude()) + '\n' + DateFormat.getTimeInstance().format(loc.getTime()));
    }
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }



    private void updateUserLocationToFirebase( Location location) {
        FirebaseLocationData fld= new FirebaseLocationData(mEmail,location.getLatitude() ,location.getLongitude(),DateFormat.getTimeInstance().format(location.getTime()),DateFormat.getTimeInstance().format(new Date()));
        fld.setUid(mUid);
        myUserRef.child(mUid).child("name").setValue(mUsername);
        myRef.child(mUid).setValue(fld);
    }

    private void requestSmsPermission() {
        String permission = Manifest.permission.READ_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"SMS permission granted", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(MainActivity.this,"SMS permission not granted", Toast.LENGTH_SHORT).show();
            }
        }

    }





    @SuppressLint("SetTextI18n")
    public void changeServiceState(View view) { //

        isServiceBackground = !isServiceBackground;
        SharedPreferences.Editor editor=sharedpreferences.edit();
        editor.putBoolean(BACKGROUND_SERVICE_STATUS,isServiceBackground);
        editor.apply();
//        tv.setText(String.valueOf(isServiceBackground));
        if(isServiceBackground)
        {
            b.setText("Stop background Notification");

            Toast.makeText(this, "Background Notification Started\nYou will get SOS notification even if app is closed", Toast.LENGTH_SHORT).show();

        }
        else
        {
            b.setText("Start background Notification");

            Toast.makeText(this, "Background Notification Stopped\nYou won't get notification if app is closed\nPlease Turn it back on", Toast.LENGTH_SHORT).show();
        }






    }
}
