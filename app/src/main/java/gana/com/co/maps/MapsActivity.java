package gana.com.co.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public static final String TAG_CLASS = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(checkPlayServices()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(result)){
                googleAPI.getErrorDialog(this, result, 9000).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       createMarkers();
        changeStateControls();

    }

    private void changeStateControls() {
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
    }

    private void createMarkers() {
        // Add a marker in Sabaneta-Antioquia and move the camera
        LatLng myHome = new LatLng(6.1508333333333,  -75.615);
        mMap.addMarker(new MarkerOptions().position(myHome).title("Marcador En Mi Casita").
                icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_on_black_24dp)));


        LatLng myOffice = new LatLng(6.249120, -75.569551);
        mMap.addMarker(new MarkerOptions().position(myOffice).title("Marcador En La Oficina"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myOffice));
        //el zoom va desde 1 a 21, donde 1 es lo mas lejano y 21 lo mas cerca
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myHome,14));

        //Trazar la ruta entre dos puntos, esto traza una linea recta
        Polyline line = mMap.addPolyline(new PolylineOptions().add(myHome,myOffice).width(4).color(Color.BLUE));

        //Calcular la ruta
        calculateRoute(myOffice,myHome);



    }

    RoutingListener rountingListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
            Log.e(TAG_CLASS, e.getMessage());
        }

        @Override
        public void onRoutingStart() {
            Log.i(TAG_CLASS,"Iniciando Ruta");
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {

            ArrayList polyLines = new ArrayList<>();

            for(int i = 0; i < routes.size() ; i++){

                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                polylineOptions.width(10);
                polylineOptions.addAll(routes.get(i).getPoints());

                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyLines.add(polyline);

                int distance = routes.get(i).getDistanceValue();
                int duration = routes.get(i).getDurationValue();

                Toast.makeText(MapsActivity.this, "distance "+distance + " - Tiempo: "+ duration, Toast.LENGTH_LONG).show();

            }

        }

        @Override
        public void onRoutingCancelled() {
            Log.w(TAG_CLASS,"Ruta Cancelada");
        }
    };

    private void calculateRoute(LatLng myOffice, LatLng myHome) {
        ArrayList<LatLng> points =new ArrayList<>();
        points.add(myOffice);
        points.add(myHome);


        ;
        Routing routing = new Routing.Builder().travelMode(AbstractRouting.TravelMode.DRIVING).waypoints(points).key(getString(R.string.google_maps_key))
                .optimize(false)
                .withListener(rountingListener).build();
        routing.execute();

    }

    //Permite la transformaciòn de un vector a bitmap
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context,vectorId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicHeight(),vectorDrawable.getIntrinsicWidth());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
