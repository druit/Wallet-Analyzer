package Services;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
import java.util.concurrent.atomic.AtomicReference;

public class GPSTracker extends Service implements LocationListener{

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 2;
    // GPS status
    private Location location;
    private Location lastLocation;
    private Context context;

    private int notifyOnceFlag = 1;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            AtomicReference<LocationManager> atomicReference;
            atomicReference = new AtomicReference<>((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));

            LocationManager locationManager = atomicReference.get();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lastLocation = location;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getLatitude() throws NullPointerException {
        return location.getLatitude();
    }

    public double getLongitude() throws NullPointerException {
        return location.getLongitude();
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        float[] results = new float[1];
        if (lastLocation != null)
            Location.distanceBetween(latLng.latitude, latLng.longitude, lastLocation.getLatitude(), lastLocation.getLongitude(), results);
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            Toast.makeText(context, "Error NETWROK PROVIDER", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
            getLocation();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}