package com.gitz.jeff.andrew.uberclone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import dmax.dialog.SpotsDialog;

public class driverRegister extends AppCompatActivity {

    EditText userNames;      //User Name
    EditText phoneNumber;   //User Phone Number
    EditText vehicleRegistration;  //User Email Address
    EditText passWord1;     //User Password
    EditText passWord2;     //Password Confirmation
    Button loginButton;
    Button registerButton;
    ArrayList<String> userCredentials = new ArrayList<>(); //ArrayList to Hold User Data
    TinyDB saveUserPhoneNumber;  //Save User Phone Number within the App
    TinyDB getRegistration;
    TinyDB saveRegistrationComplete;
    int registrationStatus = 1; //Registration Done Successfully
    private final int displayTime = 3500;  //Alert Dialog Display Time



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        saveUserPhoneNumber = new TinyDB(getBaseContext());
        saveRegistrationComplete = new TinyDB(getBaseContext());
        getRegistration = new TinyDB(getBaseContext());
        registerButton = (Button)findViewById(R.id.register);

        loginButton = (Button)findViewById(R.id.login);

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                handleRegistrationProcess();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        });
    }


    public void handleRegistrationProcess()
    {
       // boolean registration = getRegistration.getBoolean("registrationStatus");
        //if(registration == true)
       // {
            userNames = (EditText) findViewById(R.id.userName);
            phoneNumber = (EditText) findViewById(R.id.phoneNumber);
            vehicleRegistration = (EditText) findViewById(R.id.vehicleRegistration);
            passWord1 = (EditText) findViewById(R.id.passWord1);
            passWord2 = (EditText) findViewById(R.id.passWord2);

            String pass1 = passWord1.getText().toString();    //First Password
            String pass2 = passWord2.getText().toString();     //Second Password/Password confirmation

            /*Perform Error Handling*/
            if (pass1.matches("") || pass2.matches(""))        //If No Input Entered
            {
                displayToast(getBaseContext(), "Error, you've not Entered an Input.");
            }
            else                                              //Acceptable Input
            {
                if (pass1.equals(pass2))
                {
                    String userType = "Driver";                                           //Customer
                    String userName = userNames.getText().toString().trim();                   //User's Name
                    String userPhone = phoneNumber.getText().toString().trim();              //User Mobile Number
                    String userPassword = pass1;                                              //Std name for user password
                    String registrationVehicle = vehicleRegistration.getText().toString().trim();              //User Email Address

                    saveUserPhoneNumber.putString("userPhoneNumber", userPhone);            //Save User Phone Number in Shared Prefs

                    ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected())
                    {
                        //final AlertDialog alertDialog = new SpotsDialog(driverRegister.this, R.style.customDriverRegister);  //Show a Dialog Box for 4 seconds
                        final AlertDialog alertDialog = new SpotsDialog(driverRegister.this);  //Show a Dialog Box for 4 seconds
                        alertDialog.show();

                        //IF Connected to Network either via Mobile Data or Wifi
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                        {
                            sendUserData.sendDriverRegistrationCredentials(getBaseContext(), userPhone, userName, userPassword, userType, registrationVehicle);     //Send Bloody Data
                        }

                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                saveRegistrationComplete.putInt("registrationStatus", registrationStatus);  //Save Integer that Registration Done Successfully
                                displayToast(getBaseContext(), "Registration Successful");
                                Intent intent = new Intent(getBaseContext(), Login.class);  //Open Login Activity upon Successfully Registration
                                startActivity(intent);

                                userNames.setText("");   //Clear all Edit Text Boxes
                                phoneNumber.setText("");
                                vehicleRegistration.setText("");
                                passWord1.setText("");
                                passWord2.setText("");
                                alertDialog.dismiss(); //Dismiss it after 4 seconds
                            }
                        }, displayTime);

                    }

                    else                               //If not connected to network
                    {
                        displayToast(getBaseContext(), "Turn ON Mobile Data");
                    }


                }
                else
                {
                    displayToast(getBaseContext(), "Password Mismatch");
                    passWord1.setText("");
                    passWord2.setText("");
                }
            }
       // }

        //else
       //{
          //  displayToast(getBaseContext(), "Error, User Already Exists");

          //  userNames.setText("");
          //  phoneNumber.setText("");
           // vehicleRegistration.setText("");
           // passWord1.setText("");
       // }
    }


    public void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }

}
