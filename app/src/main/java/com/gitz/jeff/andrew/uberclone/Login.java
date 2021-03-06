package com.gitz.jeff.andrew.uberclone;

import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class Login extends AppCompatActivity
{

    EditText loginPhone;
    EditText loginPassword;

    Button loginButton;
    LinearLayout confirmationMsg;
    TinyDB loginStatus;  //Will save a boolean value representing registration status
    TinyDB savedUserPhoneNumber;
    TinyDB savedRegistrationStatus;
    TinyDB savedUserType;

    String userPhone;
    String userPassword;

    int userType = 0;    //Whether Use is a Customer or a Driver

    boolean loginState = false;
    boolean registrationComplete = false;

    AlertDialog alertDialog;

    private static Login inst;
    public static Login instance()
    {
        return inst;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        inst = this;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        alertDialog = new SpotsDialog(Login.this);  //Display Alert for 4 Seconds before going to next Activity
        loginButton = (Button)findViewById(R.id.login);
        savedUserType = new TinyDB(getBaseContext());
        loginStatus = new TinyDB(getBaseContext());
        savedUserPhoneNumber = new TinyDB(getBaseContext());
        savedRegistrationStatus = new TinyDB(getBaseContext());

        registrationComplete = savedRegistrationStatus.getBoolean("regStatus");
        confirmationMsg = (LinearLayout)findViewById(R.id.confirmation);
        confirmationMsg.setVisibility(View.INVISIBLE);


        //final AlertDialog alertDialog = new SpotsDialog(Login.this, R.style.customLogin);  //Display Alert for 4 Seconds before going to next Activity
        loginPhone = (EditText)findViewById(R.id.phoneNumber);
        loginPassword = (EditText)findViewById(R.id.loginPass);
        userType = savedUserType.getInt("usesType");


        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                registrationComplete = true; //Rem to Uncoment
                if(registrationComplete)   //If Registration successful
                {
                    handleLoginRequest();
                    showDialogAlertDuringLogin();
                }

                else
                {
                    confirmationMsg.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            confirmationMsg.setVisibility(View.INVISIBLE);
                        }
                    }, 3000);
                }
            }
        });
    }


    public void handleLoginRequest()
    {
        userPhone = loginPhone.getText().toString().trim();
        userPassword = loginPassword.getText().toString().trim();

        sendUserData.sendLoginRequest(getBaseContext(), userPhone, userPassword);  //Use Phone Number as ID and Password

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                alertDialog.dismiss(); //Dismiss just in case there was network error
                loginPhone.setText("");
                loginPassword.setText("");
            }
        }, 2500);
    }


    public void updateUIAfterSuccessLoginRequest()
    {
        loginState = true;
        loginStatus.putBoolean("loginStatus", loginState);

        savedUserPhoneNumber.putString("userPhoneNumber", userPhone);  //Save user Phone Number that will be used throughout

        if (userType == 1)             //If Customer in Use
        {
            Intent intent = new Intent(getBaseContext(), CustomerMapActivity.class);
            startActivity(intent);
            alertDialog.dismiss();     //Dismiss it after 4 seconds
        }
        else if (userType == 2)        //If Driver in Use
        {
            Intent intent = new Intent(getBaseContext(), DriverMapActivity.class);
            startActivity(intent);
            alertDialog.dismiss();    //Dismiss it after 4 seconds
        }

    }

    public void showDialogAlertDuringLogin()
    {
        alertDialog.show();
    }

    public void hideDialogAlertDuringLogin()
    {
        alertDialog.dismiss();    //Dismiss it after 4 seconds
    }

    public void clearEditTextBoxes()
    {
        loginPassword.setText("");
        loginPhone.setText("");
    }


    public void Support(View view)
    {
        Intent intent = new Intent(getBaseContext(), Help.class);
        startActivity(intent);
    }

    public void displayToast(Context myContext, String displayToastMessage)
    {
        Context context = myContext;
        Toast.makeText(myContext, displayToastMessage, Toast.LENGTH_LONG).show();
    }

}
