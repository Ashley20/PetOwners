package com.owners.pet.petowners.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.owners.pet.petowners.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FetchAddressByNameIntentService extends IntentService {
    public static final String TAG = FetchAddressByNameIntentService.class.getSimpleName();
    protected ResultReceiver mReceiver;

    public FetchAddressByNameIntentService() {
        super("FetchAddressByNameIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        String  placeName = intent.getStringExtra(Constants.PLACE_NAME_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(placeName, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid place name argument
            errorMessage = getString(R.string.invalid_place_name_used);
            Log.e(TAG, errorMessage + ". " +
                    "Place name = " + placeName,
                  illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found_with_the_place_name);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT,3.5, 4.2, "", errorMessage);
        } else {
            Address address = addresses.get(0);
            Double latitude = address.getLatitude();
            Double longitude = address.getLongitude();
            String locality = address.getLocality();


            Log.i(TAG, getString(R.string.address_found));
            Log.i(TAG, latitude + " " + longitude + " " + locality);
            deliverResultToReceiver(Constants.SUCCESS_RESULT, latitude, longitude, locality, errorMessage);
        }



    }

    private void deliverResultToReceiver(int resultCode, Double latitude, Double longitude, String placeName, String errorMessage) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LOCATION_PLACE_NAME, placeName);
        bundle.putDouble(Constants.LOCATION_LATITUDE, latitude);
        bundle.putDouble(Constants.LOCATION_LONGITUDE, longitude);
        bundle.putString(Constants.ERROR_MESSAGE, errorMessage);
        bundle.putString(Constants.ACTION, Constants.ACTION_FETCH_ADDRESS_FROM_NAME);
        mReceiver.send(resultCode, bundle);
    }
}
