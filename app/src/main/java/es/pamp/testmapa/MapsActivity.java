package es.pamp.testmapa;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {


    private Marker mInicio;
    private CameraUpdate campUp1;
    private LatLng posicionInicio;
    private GoogleMap mMap;

    //permisos
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    //geofence
    private static final String TAG = "MainActivity";
    private Button startLocationMonitoring; //TODO borrar botones
    private Button startGeofenceMonitoring;
    private Button stopGeofenceMonitoring;
    private GoogleApiClient googleApiClient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //geofence
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1234);
        }
        //Setup the Api client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.w(TAG, "connected to GoogleApi client");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.w(TAG, "suspended to GoogleApi client");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.w(TAG, "failed to GoogleApi client");

                    }
                })
                .build();
        googleApiClient.connect();
        //geofence
        //startLocationMonitoring = (Button) findViewById(R.id.startLocMonitoring);
        //startGeofenceMonitoring = (Button) findViewById(R.id.startGeoFMon);
        //stopGeofenceMonitoring = (Button) findViewById(R.id.stopGeofenMon);

        /*
        startLocationMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationMonitoring();
            }
        });
        startGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGeofencMonitroing();
            }
        });
        stopGeofenceMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGeofenceMonitoring();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1234);
        }
        */

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //posicionInicio = new LatLng(40.4095457,-3.6914855);//retiro
        //posicionInicio = new LatLng(40.2942539,-3.7456561);//getafe -1
        posicionInicio = new LatLng(40.29425,-3.74565);//getafe -1
        mInicio = mMap.addMarker(new MarkerOptions()
                .position(posicionInicio)
                .title("Inicio")
                .snippet("texto snippet")
        );
        mInicio.setTag(0);
        campUp1 = CameraUpdateFactory.newLatLngZoom(posicionInicio, (float)17);
        mMap.moveCamera(campUp1);

        mMap.setOnMyLocationButtonClickListener(this);

        enableMyLocation();

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);


            startGeofencMonitroing();
            startLocationMonitoring();
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "has pulsado en el botón mi localización", Toast.LENGTH_SHORT).show();
        //TODO poner acción al clicar en el botón mi posición si fuese necesario.

        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }



    //Geofence


    @Override
    public void onStart() {
        if(googleApiClient !=null) {
            googleApiClient.connect();
        }
        Log.w(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop() {

        if(googleApiClient !=null) {
            googleApiClient.disconnect();
        }
        Log.w(TAG, "onStop");
        super.onStop();
    }

    private void startLocationMonitoring() {
        Log.w(TAG, "Starting Location Monitoring");
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(2000)
                    .setFastestInterval(1000)
                    .setSmallestDisplacement(0)
                    .setMaxWaitTime(2000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Do Nothing
            } else {

                Log.w(TAG, "Starting request updates");
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                        locationRequest, new com.google.android.gms.location.LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.w(TAG, "Location update" + location.getLatitude() + "," + location.getLongitude());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }

    }

    //Añade puntos
    private void startGeofencMonitroing() {
        Log.w(TAG, "Starting Geofence Monitoring");
        try {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId("GEO-1")
                    .setCircularRegion(40.2929914,-3.7468589, 50) //latitud longitud y metros // posicion getafe

                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT) //TODO ¿Dejar solo Enter?
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build();

            //Pintar un circulo en la misma posicion
            CircleOptions circleOptions = new CircleOptions()
                    .center( new LatLng(40.2975554,-3.7473926) )
                    .radius(50)
                    .fillColor(0x40ff0000)
                    //.strokeColor(Color.TRANSPARENT)
                    .strokeColor(Color.RED)
                    .strokeWidth(2);
            mMap.addCircle(circleOptions);


            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (!googleApiClient.isConnected()) {
                Log.w(TAG, "Google API Not Connected");
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // do nothing
                } else {
                    LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent)
                            .setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if (status.isSuccess()) {
                                        Log.w(TAG, "Successfully Added Geofence");
                                    } else {
                                        Log.w(TAG, "Error added geofence");
                                    }
                                }
                            });
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }

    }
//Para quitar puntos
    private void stopGeofenceMonitoring() {
        Log.w(TAG, "Stopping Geofence Monitoring");
        ArrayList<String> geofenceIds = new ArrayList<>();
        geofenceIds.add("GEO-1");
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
