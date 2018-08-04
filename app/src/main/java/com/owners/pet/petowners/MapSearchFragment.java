package com.owners.pet.petowners;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.owners.pet.petowners.adapters.CustomInfoViewAdapter;
import com.owners.pet.petowners.models.User;
import com.owners.pet.petowners.services.Constants;
import com.owners.pet.petowners.services.FetchAddressByLatLngIntentService;
import com.owners.pet.petowners.services.FetchAddressByNameIntentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MapSearchFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {
    public static final String TAG = MapSearchFragment.class.getSimpleName();

    public static final int LOCATION_REQUEST = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATON_UPDATES_KEY";
    private long UPDATE_INTERVAL = 300000;  /* 5min */
    private long FASTEST_INTERVAL = 5000; /* 5 sec */

    @BindView(R.id.mapView)
    MapView mMapView;
    @BindView(R.id.search)
    SearchView searchView;
    @BindView(R.id.state_0_cb)
    CheckBox checkBoxOrange;
    @BindView(R.id.state_1_cb)
    CheckBox checkBoxCyan;
    @BindView(R.id.state_2_cb)
    CheckBox checkBoxGreen;

    private GoogleMap mGoogleMap;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private FirebaseUser currentUser;
    private ArrayList<Marker> orangeMarkers = new ArrayList<>();
    private ArrayList<Marker> cyanMarkers = new ArrayList<>();
    private ArrayList<Marker> greenMarkers = new ArrayList<>();
    public AddressResultReceiver mResultReceiver;
    LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;

    public MapSearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_map, container, false);

        ButterKnife.bind(this, rootView);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        mResultReceiver = new AddressResultReceiver(null);

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
            }
        });

        updateValuesFromBundle(savedInstanceState);


        mLocationCallback = new LocationCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    mGoogleMap.setMyLocationEnabled(false);
                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    return;
                }
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                for (Location location : locationResult.getLocations()) {
                    mLastKnownLocation = location;
                    startFetchAddressByLatLngIntentService();
                    // Update UI with location data
                    // ...
                    Log.d(TAG, (location.getProvider()));
                }
            }

            ;
        };

        return rootView;
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            mMapView.getMapAsync(this);

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            Log.d(TAG, "onResume : location updates started");
            startLocationUpdates();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause : location updates stopped");
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setOnInfoWindowClickListener(this);

        createLocationRequest();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String place) {
                Geocoder geocoder = new Geocoder(getContext());
                // Start the service
                startFetchAddressByNameService(place);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            askLocationPermission();
            return false;
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                mRequestingLocationUpdates = true;
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }


    /**
     * Places markers on the map where users are located based on their state.
     * If they are willing to adopt a pet then the marker color is orange
     * or if they want to post a pet for adoption then the marker color is cyan
     * otherwise the default marker color is green
     *
     * @param mGoogleMap
     */
    private void placeMarkers(final GoogleMap mGoogleMap) {
        mDatabase.child(getString(R.string.COLLECTION_USERS)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        User user = s.getValue(User.class);
                        if (user != null && user.getLatitude() != null) {

                            Log.d(TAG, "placeMarkers: " + user.getLatitude().toString());
                            LatLng latLng = new LatLng(user.getLatitude(), user.getLongtitude());
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .title(user.getName())
                                    .position(latLng);
                            HashMap<String, String> markerOptionsMap = new HashMap<>();
                            markerOptionsMap.put(getString(R.string.UID_KEY), user.getUid());

                            switch (user.getUserState()) {
                                case User.WANTS_TO_ADOPT:
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                    markerOptions.snippet(getString(R.string.USER_WANTS_TO_ADOPT_STATE));
                                    markerOptionsMap.put(getString(R.string.COLOR_KEY), "HUE_ORANGE");
                                    Marker markerOrange = mGoogleMap.addMarker(markerOptions);
                                    markerOrange.setTag(markerOptionsMap);
                                    orangeMarkers.add(markerOrange);
                                    break;
                                case User.WANTS_TO_POST_FOR_ADOPTION:
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                                    markerOptions.snippet(getString(R.string.USER_WANTS_TO_POST_FOR_ADOPTION_STATE));
                                    markerOptionsMap.put(getString(R.string.COLOR_KEY), "HUE_CYAN");
                                    Marker markerCyan = mGoogleMap.addMarker(markerOptions);
                                    markerCyan.setTag(markerOptionsMap);
                                    cyanMarkers.add(markerCyan);
                                    break;
                                case User.NONE:
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    markerOptions.snippet(getString(R.string.USER_DEFAULT_STATE));
                                    markerOptionsMap.put(getString(R.string.COLOR_KEY), "HUE_GREEN");
                                    Marker markerGreen = mGoogleMap.addMarker(markerOptions);
                                    markerGreen.setTag(markerOptionsMap);
                                    greenMarkers.add(markerGreen);
                                    break;
                            }

                            //Set Custom InfoWindow Adapter
                            CustomInfoViewAdapter adapter = new CustomInfoViewAdapter(getContext());
                            mGoogleMap.setInfoWindowAdapter(adapter);
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {

        if (checkPermissions()) {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            mLastKnownLocation = location;

                            if (mLastKnownLocation != null) {

                                mGoogleMap.setMyLocationEnabled(true);
                                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), 15));

                                if (!Geocoder.isPresent()) {
                                    Toast.makeText(getContext(),
                                            R.string.no_geocoder_available,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                startFetchAddressByLatLngIntentService();
                                Log.d(TAG, "Last known location is not null");
                            } else {
                                Log.d(TAG, "Last known location is null so start location updates");
                                // Last known location is null so we need to request location updates
                                startLocationUpdates();
                            }

                        }
                    });

            placeMarkers(mGoogleMap);
        }

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }


    /**
     * Function which stores the user's latitude,
     * longtitude, country and admin area information into
     * firebase realtime database.
     *
     * @param adminArea
     * @param countryName
     */
    private void saveLocationIntoDb(final String adminArea, final String countryName) {
        if (currentUser != null) {

            Map<String, Object> updates = new HashMap<>();

            updates.put(getString(R.string.ADMIN_AREA_KEY), adminArea);
            updates.put(getString(R.string.COUNTRY_KEY), countryName);
            updates.put(getString(R.string.LATITUDE_KEY), mLastKnownLocation.getLatitude());
            updates.put(getString(R.string.LONGTITUDE_KEY), mLastKnownLocation.getLongitude());

            mDatabase.child(getString(R.string.COLLECTION_USERS)).child(currentUser.getUid())
                    .updateChildren(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Location is saved into firestore");
                            placeMarkers(mGoogleMap);
                        }
                    });
        }
    }

    /**
     * Function which locates the map camera to the position it gets from its parameters
     *
     * @param latitude
     * @param longitude
     * @param zoom
     */
    private void goToLocation(double latitude, double longitude, float zoom) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    public void askLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation();

                } else {
                    mGoogleMap.setMyLocationEnabled(false);
                }
            }
        }

    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        HashMap<String, String> tag = (HashMap<String, String>) marker.getTag();
        // The uid of the user profile which wants to be displayed
        if (tag != null) {
            String uid = tag.get(getString(R.string.UID_KEY));
            if (uid != null) {
                if (currentUser.getUid().equals(uid)) {
                    Intent openCurrentUserProfileIntent = new Intent(getContext(), ProfileActivity.class);
                    startActivity(openCurrentUserProfileIntent);
                } else {
                    Intent openOtherUsersProfileIntent = new Intent(getContext(), OthersProfileActivity.class);
                    openOtherUsersProfileIntent.putExtra(getString(R.string.USER_PROFILE_UID), uid);
                    startActivity(openOtherUsersProfileIntent);
                }
            }
        }
    }

    @OnClick(R.id.state_0_cb)
    public void updateOrangeMarkers() {
        updateMarkerVisibility(orangeMarkers, checkBoxOrange.isChecked());
    }

    @OnClick(R.id.state_1_cb)
    public void updateCyanMarkers() {
        updateMarkerVisibility(cyanMarkers, checkBoxCyan.isChecked());
    }

    @OnClick(R.id.state_2_cb)
    public void updateGreenMarkers() {
        updateMarkerVisibility(greenMarkers, checkBoxGreen.isChecked());
    }


    /**
     * Function which updates the markers visibility based on
     * the visibility parameter it takes
     *
     * @param markerList
     * @param visibility
     */
    public void updateMarkerVisibility(ArrayList<Marker> markerList, Boolean visibility) {
        for (Marker marker : markerList) {
            marker.setVisible(visibility);
        }
    }

    protected void startFetchAddressByLatLngIntentService() {
        Intent intent = new Intent(getActivity(), FetchAddressByLatLngIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastKnownLocation);
        getActivity().startService(intent);
    }

    protected void startFetchAddressByNameService(String placeName) {
        Intent intent = new Intent(getActivity(), FetchAddressByNameIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.PLACE_NAME_DATA_EXTRA, placeName);
        getActivity().startService(intent);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                Log.d(TAG, "on activity result for the fragment");
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        mRequestingLocationUpdates = true;
                        getDeviceLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        mRequestingLocationUpdates = false;
                        mGoogleMap.setMyLocationEnabled(false);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        Toast.makeText(getContext(), "Location Service not Enabled", Toast.LENGTH_SHORT).show();
                        placeMarkers(mGoogleMap);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData != null) {
                String ACTION = resultData.getString(Constants.ACTION);
                if (ACTION != null) {
                    switch (ACTION) {
                        case Constants.ACTION_FETCH_ADDRESS_FROM_LOCATION:
                            if (resultCode == Constants.SUCCESS_RESULT) {
                                String adminArea = resultData.getString(Constants.LOCATION_ADMIN_AREA);
                                String countryName = resultData.getString(Constants.LOCATION_COUNTRY_NAME);

                                // Now we have the address store it into firestore database
                                saveLocationIntoDb(adminArea, countryName);
                            } else {
                                String errorMessage = resultData.getString(Constants.ERROR_MESSAGE);
                                Log.d(TAG, errorMessage);
                            }
                            break;

                        case Constants.ACTION_FETCH_ADDRESS_FROM_NAME:
                            if (resultCode == Constants.SUCCESS_RESULT) {
                                final String locality = resultData.getString(Constants.LOCATION_PLACE_NAME);
                                final Double latitude = resultData.getDouble(Constants.LOCATION_LATITUDE);
                                final Double longitude = resultData.getDouble(Constants.LOCATION_LONGITUDE);
                                Log.d(TAG, latitude + " " + longitude + " " + locality);

                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Now we have the locality show it to the user and go to that location
                                        Toast.makeText(getContext(), locality, Toast.LENGTH_SHORT).show();
                                        goToLocation(latitude, longitude, 15);
                                    }
                                });

                            } else {
                                final String errorMessage = resultData.getString(Constants.ERROR_MESSAGE);
                                Log.d(TAG, errorMessage);
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                    }
                }
            }


        }
    }


}

