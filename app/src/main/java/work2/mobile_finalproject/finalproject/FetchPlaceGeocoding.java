package work2.mobile_finalproject.finalproject;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchPlaceGeocoding extends IntentService  {
    final static String TAG = "FetchLatLng";
    private Geocoder geocoder;
    private ResultReceiver receiver;
    /**
     * @param name
     * @deprecated
     */
    public FetchPlaceGeocoding(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //        Geocoder 생성
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        if (intent == null) return;
        String location = intent.getStringExtra(Constants.ADDRESS_DATA_EXTRA);
        receiver = intent.getParcelableExtra(Constants.RECEIVER);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(location, 1); // 실 주소로 Geocoding
        } catch (IOException e) { // Catch network or other I/O problems.
            e.printStackTrace();
        } catch (IllegalArgumentException e) { // Catch invalid address values.
            e.printStackTrace();
        }

        if (addresses == null || addresses.size()  == 0) {
            Log.e(TAG, "not found");
            deliverResultToReceiver(Constants.FAILURE_RESULT, null);
        } else {
            Address addressList = addresses.get(0);
            ArrayList<LatLng> addressFragments = new ArrayList<LatLng>();
            for(int i = 0; i <= addressList.getMaxAddressLineIndex(); i++) {
                LatLng latLng = new LatLng(addressList.getLatitude(), addressList.getLongitude());
                addressFragments.add(latLng);
            }
            Log.i(TAG, "found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT, addressFragments);
        }
    }

    private void deliverResultToReceiver(int resultCode, ArrayList<LatLng> message) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
