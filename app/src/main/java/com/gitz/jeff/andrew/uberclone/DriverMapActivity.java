package com.gitz.jeff.andrew.uberclone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static com.gitz.jeff.andrew.uberclone.R.id.map;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    TinyDB getSavedUserPhoneNumber; //Get user Driver User Phone Number to act as ID
    String driverUserId = null;
    public static float myZoomLevel = 14;
    LinearLayout customerInformation;
    ImageView customerProfileImage;   //Assigned Customer Profile Image
    TextView customerPickup, customerName, customerPhoneNumber;  //Assigned Customer Name and Phone Number
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
    private static final int MY_PERMISSIONS_REQUEST_ACCOUNTS = 1;
    final int LOCATION_REQUEST_CODE = 1;
    ImageView callCustomer;
    Marker pickUpMarker;
    public LatLng pickUpLocation;   //Will Hold Pick Up Location Co-ordinates
    Button driverMainButton;
    Button endOfSession;
    public boolean customerAssigned = true;   //Customer not assigned by default
    public boolean customerFound = true;
    TinyDB savedUserPhoneNumber;
    String userPhoneNumber;
    public boolean rideInSession = false;
    boolean locationDataCopied = false;
    Marker markerCustomerLocaton;  //Destination Marker
    Marker markerCurrentLocation; //My Current Locaton Marker


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        savedUserPhoneNumber = new TinyDB(getBaseContext());
        userPhoneNumber = savedUserPhoneNumber.getString("userPhoneNumber");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else
        {
            mapFragment.getMapAsync(this);
        }

        getSavedUserPhoneNumber = new TinyDB(getBaseContext());
        driverUserId = getSavedUserPhoneNumber.getString("userPhoneNumber");  //Get Saved Phone Number to act as User ID
        customerInformation = (LinearLayout) findViewById(R.id.customerInfo);
        customerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        customerName = (TextView) findViewById(R.id.customerName);
        customerPhoneNumber = (TextView) findViewById(R.id.customerPhoneNumber);
        customerPickup = (TextView) findViewById(R.id.customerPickup);
        callCustomer = (ImageView) findViewById(R.id.callCustomer);
        driverMainButton = (Button)findViewById(R.id.driverMainButton);
        endOfSession  = (Button)findViewById(R.id.endOfSession);
        endOfSession.setVisibility(View.INVISIBLE);
        customerInformation.setVisibility(View.GONE);


        driverMainButton.setOnClickListener(new View.OnClickListener()    //Main Driver Functionality Button
        {
            @Override
            public void onClick(View v)
            {
                String eventID = "driverAvailable";
                sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);

                driverMainButton.setText("Checking For Customers");
                driverMainButton.setBackgroundColor(Color.RED);
                driverMainButton.setTextColor(Color.WHITE);

                newCustomerAlert();

            }
        });



        callCustomer.setOnClickListener(new View.OnClickListener()    //Call Customer Listener
        {
            @Override
            public void onClick(View v) {
                String eventID = "driverCalledDriver";
                String customerNumber = "0735555255";
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customerNumber));

                if (ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {

                    return;
                }
                startActivity(callIntent);
                sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);

            }
        });
    }



    public void showAssignedCustomerDetails()
    {
        String pickup = "Upper Hill";
        String name = "Lisa Randall";  //Dummy Data
        String phoneNumber = "0728648142";  //Dummy Data

        // pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());   //Get Customer Pickup Location Co-ordinates
        // pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Me Up Here"));  //Add Marker, and Set Title of Marker

        customerPickup.setText(pickup);
        customerName.setText(name);
        customerPhoneNumber.setText(phoneNumber);
        customerInformation.setVisibility(View.VISIBLE);

    }


    public void  hideAssignedCustomerDetails()
    {
        customerInformation.setVisibility(View.GONE);
    }



    public void newCustomerAlert()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                String customerFoundAlertMsg = "Destination: " + "Upper Hill" + "\n"
                        + "Pickup Point: " + "Prestige Plaza" + "\n";
                       // + "Customer Name: " + "Lisa Randall" + "\n"
                       //+ "Customer Phone: " + "0728648142" + "\n";

                //Prompt user to Accept or Reject request
                driverMainButton.setVisibility(View.GONE); //Not Visible
                driverMainButton.setClickable(false);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(DriverMapActivity.this);
                dialog.setTitle("Customer Found");
                dialog.setMessage(customerFoundAlertMsg);
                dialog.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener()

                {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                    {
                        String eventID = "customerRideRequestAccepted";
                        sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);
                        showAssignedCustomerDetails();
                        rideInSession = true;


                        endOfSession.setVisibility(View.VISIBLE);
                        endOfSession.setBackgroundColor(Color.BLUE);
                        endOfSession.setTextColor(Color.WHITE);

                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                hideAssignedCustomerDetails();
                            }
                        }, 15000);
                    }

                });

                dialog.setNegativeButton("REJECT", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt)
                    {
                        String eventID = "customerRideRequestRejected";
                        sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);

                        driverMainButton.setBackgroundColor(Color.RED);
                        driverMainButton.setTextColor(Color.WHITE);
                        driverMainButton.setText("Available");
                        driverMainButton.setVisibility(View.VISIBLE); //Not Visible
                        driverMainButton.setClickable(true);
                        rideInSession = false;
                        //sendUserData.sendEventData(getBaseContext(),userPhoneNumber, customerRejected);   //Send Customer Rejected Event
                    }
                });

                dialog.show();

            }
        }, 10000);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)   //Notifies when map is ready for use
    {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()   //Update my Zoom Level to Manual Inputs
        {
            @Override
            public void onCameraMove() {
                myZoomLevel = mMap.getCameraPosition().zoom;
            }
        });
    }


    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location)  //Will be called every second
    {

        if(!locationDataCopied)
        {
            lastLocation = location;  //Copy the Data
            LatLng initLatLang = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(initLatLang).title("My Current Location"));  //Add Marker, and Set Title of Marker
            locationDataCopied = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(initLatLang));

        }

        else if(lastLocation != location)
        {
            if(rideInSession)  //Only recentre when Driver is in Motion
            {
                markerCurrentLocation.remove();
                lastLocation = location;
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Current Location"));  //Add Marker, and Set Title of Marker
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            }
        }

        //markerCurrentLocation = mMap.addMarker(new MarkerOptions().position(latLng).title("My Current Location"));  //Add Marker, and Set Title of Marker
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        mMap.animateCamera(CameraUpdateFactory.zoomTo(myZoomLevel), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish()
            {
                myZoomLevel = mMap.getCameraPosition().zoom;
            }

            @Override
            public void onCancel()
            {

            }
        });

        // Log.e("ZOOM_LEVEL",""+myZoomLevel);
        Double currentLatitudeAddress = location.getLatitude();     //Get current Latitude coordinates
        Double currentLongitudeAddress = location.getLongitude();   //Get current Longitude coordinates
    }


    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2500);    //Refresh rate
        locationRequest.setFastestInterval(2500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);  //Highest Accuracy, However drains a lot of battery power

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    public void endRideSession(View view)
    {
        if(rideInSession)
        {
           endOfRideConfirmationAlert();
        }

        else
        {
            endOfSession.setClickable(false);
        }
    }


    public void endOfRideConfirmationAlert()
    {
        //Pop up an Alert Dialog to confirm End of ride
        final String eventID = "endOfRideSession";

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("End Session?");
        dialog.setMessage("Confirm you want to end Session?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                sendUserData.sendEventData(getBaseContext(), userPhoneNumber, eventID,  null, null, null);
                endOfSession.setVisibility(View.GONE);
                customerInformation.setVisibility(View.GONE);
                driverMainButton.setVisibility(View.VISIBLE);
                driverMainButton.setClickable(true);
                driverMainButton.setText("Available");
                rideInSession = false;
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
               //Do Nothing
                rideInSession = true;
            }
        });

        dialog.show();

    }


    public void rideCancelled()
    {
        clearRouteFromMap();   //Clear Route From Map
    }


    public void drawRouteToMarker(LatLng pickUpPointCoordinates, LatLng destinationCoordinates)
    {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)  //Disable Alternative Routes for Now
                .waypoints(pickUpPointCoordinates, destinationCoordinates)     //end is pickUpLatLang, start will be last latlong coordininates
                .build();
        routing.execute();
    }

    public void clearRouteFromMap()
    {
        for(Polyline line: polylines)
        {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    protected void onStop()  //If Driver gets out of this activity, Notify Db, for Him to be removed as Hes is no longer Active
    {
        super.onStop();
        //sendUserData.sendUserID(getBaseContext(), "driverLoggedOut", driverUserId);     //Send Driver ID that End of Activity
    }

    @Override
    public void onRoutingFailure(RouteException e)
    {
        if(e != null)
        {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart()
    {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": Distance: "+ route.get(i).getDistanceValue()+": Duration- "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRoutingCancelled()
    {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settingsId:
                Intent intent1 = new Intent(getBaseContext(), settingsApp.class);
                startActivity(intent1);
                break;

            case R.id.logoutId:
                Intent intent4 = new Intent(getBaseContext(), driverRegister.class);
                startActivity(intent4);
                break;


            case R.id.helpId:
                Intent intent2 = new Intent(getBaseContext(), Help.class);
                startActivity(intent2);
                break;

            case R.id.aboutId:
                Intent intent3 = new Intent(getBaseContext(), About.class);
                startActivity(intent3);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}
