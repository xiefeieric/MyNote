package uk.co.feixie.mynote.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.co.feixie.mynote.activity.AddNoteActivity;
import uk.co.feixie.mynote.utils.Constants;

public class FetchAddressIntentService extends IntentService {

    protected ResultReceiver mReceiver;


    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";
            Log.e("tag", errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid longitude and latitude";
            Log.e("tag", errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found";
                Log.e("tag", errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
//            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                addressFragments.add(address.getAddressLine(i));
//            }
            String city = address.getAddressLine(address.getMaxAddressLineIndex() - 1);
            String postalCode = address.getPostalCode();
            String newCity = city;
            if (!TextUtils.isEmpty(postalCode)){
                newCity = city.replace(postalCode, "");
            }

//            System.out.println("address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT, newCity
//                    TextUtils.join(System.getProperty("line.separator"),
//                            addressFragments)
            );
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

}
