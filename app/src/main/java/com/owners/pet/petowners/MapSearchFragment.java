package com.owners.pet.petowners;


import android.Manifest;
import android.app.Activity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.owners.pet.petowners.models.User;

import java.io.IOException;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;


public class MapSearchFragment extends Fragment implements OnMapReadyCallback {
    public static final int LOCATION_REQUEST = 1;
    @BindView(R.id.mapView)
    MapView mMapView;
    @BindView(R.id.search)
    SearchView searchView;
    private GoogleMap mGoogleMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;

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

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

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
     * If they are willing to adopt a pet then the marker color is green
     * or if they want to post a pet for adoption then the marker color is blue
     * otherwise the default marker color is gray
     *
     * @param mGoogleMap
     */
    private void placeMarkers(final GoogleMap mGoogleMap) {
        getDeviceLocation();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    db.collection(getString(R.string.COLLECTION_USERS)).document(currentUser.getUid())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot doc = task.getResult();
                                        if (doc.exists()) {
                                            User user = doc.toObject(User.class);
                                            if (user != null) {
                                                mGoogleMap.addMarker(new MarkerOptions()
                                                        .title(user.getName())
                                                        .position(new LatLng(0, 0)));
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
        });

    }

    private void getDeviceLocation() {
        if (mLocationPermissionGranted) {
            try {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mLastKnownLocation = location;
                                    Toast.makeText(getContext(), "lat: " + mLastKnownLocation.getLatitude() + " long: " + mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), 15));
                                } else {
                                    Log.d("HATA", "Current location is null. Using defaults.");
                                    mGoogleMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(new LatLng(-33.8523341, 151.2106085), 15));
                                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                                }
                            }
                        });

            } catch (SecurityException e) {
            }
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
        } else {
            mLocationPermissionGranted = false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
}

