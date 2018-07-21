package com.owners.pet.petowners;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.owners.pet.petowners.adapters.CustomInfoViewAdapter;
import com.owners.pet.petowners.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MapSearchFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    public static final String TAG = MapSearchFragment.class.getSimpleName();

    public static final int LOCATION_REQUEST = 1;
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
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private FirebaseUser currentUser;
    private ArrayList<Marker> orangeMarkers = new ArrayList<>();
    private ArrayList<Marker> cyanMarkers = new ArrayList<>();
    private ArrayList<Marker> greenMarkers = new ArrayList<>();

    public MapSearchFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_map, container, false);

        ButterKnife.bind(this, rootView);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
            }
        });

        return rootView;
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
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.setOnInfoWindowClickListener(this);

        getLocationPermission();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String place) {
                Geocoder geocoder = new Geocoder(getContext());
                try {
                    List<Address> likelyAddressesList = geocoder.getFromLocationName(place, 1);
                    if (likelyAddressesList != null) {
                        Address address = likelyAddressesList.get(0);
                        Toast.makeText(getContext(), address.getLocality(), Toast.LENGTH_SHORT).show();
                        goToLocation(address.getLatitude(), address.getLongitude(), 15);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

    }

    private void updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
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

        db.collection(getString(R.string.COLLECTION_USERS))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<User> userList = queryDocumentSnapshots.toObjects(User.class);
                        for (User user : userList) {
                            if (user != null && user.getLatitude() != null) {
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
                });
    }


    private void getDeviceLocation() {
        updateLocationUI();
        if (mLocationPermissionGranted) {
            try {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mLastKnownLocation = location;
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), 15));
                                    // Save the last known location into firestore database
                                    saveLocationIntoDb(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                } else {
                                    Log.d(TAG, "Current location is null. Using defaults.");
                                   /* mGoogleMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(new LatLng(-33.8523341, 151.2106085), 15)); */
                                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                                    placeMarkers(mGoogleMap);
                                }
                            }
                        });

            } catch (SecurityException e) {
            }
        }
    }

    /**
     * Funtion which saves the user's lastknownlocation in firestore database
     *
     * @param latitude
     * @param longitude
     */
    private void saveLocationIntoDb(double latitude, double longitude) {
        if (currentUser != null) {
            DocumentReference user = db.collection(getString(R.string.COLLECTION_USERS)).document(currentUser.getUid());
            user.update(getString(R.string.LATITUDE_KEY), latitude);
            user.update(getString(R.string.LONGTITUDE_KEY), longitude).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
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


    public void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                } else {
                    mLocationPermissionGranted = false;
                    updateLocationUI();
                }
            }
        }

    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        HashMap<String, String> tag = (HashMap<String, String>) marker.getTag();
        // The uid of the user profile which wants to be displayed
        if(tag != null){
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
        if (checkBoxOrange.isChecked()) {
            for (Marker orangeMarker : orangeMarkers) {
                orangeMarker.setVisible(true);
            }

        } else {
            for (Marker orangeMarker : orangeMarkers) {
                orangeMarker.setVisible(false);
            }
        }
    }

    @OnClick(R.id.state_1_cb)
    public void updateCyanMarkers() {
        if (checkBoxCyan.isChecked()) {
            for (Marker cyanMarker : cyanMarkers) {
                cyanMarker.setVisible(true);
            }

        } else {
            for (Marker cyanMarker : cyanMarkers) {
                cyanMarker.setVisible(false);
            }
        }
    }

    @OnClick(R.id.state_2_cb)
    public void updateGreenMarkers() {
        if (checkBoxGreen.isChecked()) {
            for (Marker greenMarker : greenMarkers) {
                greenMarker.setVisible(true);
            }

        } else {
            for (Marker greenMarker : greenMarkers) {
                greenMarker.setVisible(false);
            }
        }
    }


}

