package com.example.iju95.ambient_browser;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by iju95 on 2018-06-14.
 */

public class FetchAddressIntentService extends IntentService {
    private static final String TAG = "FetchAddressIS";
    private ResultReceiver mReceiver;
    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        double lat = intent.getExtras().getDouble("Latitude");
        double lng = intent.getExtras().getDouble("Longitude");
        try {
            addresses = geocoder.getFromLocation(lat, lng,1);
        } catch (IOException e) {
            errorMessage = "ioException !!";
            Log.e(TAG,errorMessage,e);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Latitude or Longtitue is invalid";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + "37.599951"+
                    ", Longitude = " + "126.864576", illegalArgumentException);
        }
        if(addresses == null || addresses.size() == 0){
            if(errorMessage.isEmpty()){
                errorMessage ="Address is not exist";
                Log.e(TAG,errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT,errorMessage);
        }else{
            Address address = addresses.get(0);
            Log.i(TAG,"addresses size : "+addresses.size());
            Log.i(TAG,"address getAddressLine.tostring : "+address.getAddressLine(0).toString());

            deliverResultToReceiver(Constants.SUCCESS_RESULT,address.getAddressLine(0).toString());
        }
    }
    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY,message);
        mReceiver.send(resultCode,bundle);
    }
}
