package com.example.EasyClassic;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Toaster;

@SuppressLint("StaticFieldLeak")

public class MainActivity extends AppCompatActivity {

    ImageView locked; //closed lock image
    ImageView unlocked; //open lock image
    int lockStatus; //variable to store lock status after received from the Particle Cloud

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the title and top bar in the app
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        locked = findViewById(R.id.locked);
        unlocked = findViewById(R.id.unlocked);
        Button driveBtn = findViewById(R.id.drive_button);
        Button setupBtn = findViewById(R.id.setup_button);

        //Intent to open DriveActivity on click of the button
        driveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driveActivityIntent = new Intent(MainActivity.this, DriveActivity.class);
                startActivity(driveActivityIntent);
            }
        });

        //Intent to open SetupActivity on click of the button
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setupActivityIntent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(setupActivityIntent);
            }
        });
        ParticleCloudSDK.init(this);

        //Log into Particle cloud
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    // Log in to Particle Cloud using username and password
                    ParticleCloudSDK.getCloud().logIn("varga.marton94@gmail.com", "particlepass");
                    return "Logged in!";
                } catch (ParticleCloudException e) {
                    return "Error logging in!";
                }
            }

            protected void onPostExecute(String msg) {
                Toaster.s(MainActivity.this, "Logged in");
            }
        }.execute();

        //Timer to read lock state periodically and make sure lock status stays in sync with the micro controller (autolock)
        Timer timer = new Timer();
        // Set the schedule function
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //Read Particle Cloud variable
                readVariable();
                }
            },
                1000, 1000);// 1000 Millisecond  = 1 second

        //Open lock on click of the lock image
        locked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Call lock function
                callLockFunction();

                // Read Particle variable
                readVariable();

                // Update lock status
                setLockStatus();
            }
        });
        unlocked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Call lock function
                callLockFunction();

                // Read Particle variable
                readVariable();

                // Update lock status
                setLockStatus();
            }
        });
    }

    // Read Particle variable
    public void readVariable() {
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {

                try {
                    // Get device instance
                    ParticleDevice Dashboard = ParticleCloudSDK.getCloud().getDevice("e00fce68412e03c04189238c");
                    // Read particle variable and assign it to lockStatus int
                    lockStatus = Dashboard.getIntVariable("lockstate");
                    //Toaster.s(MainActivity.this, "lockStatus " + lockStatus);
                    return "stuff";
                } catch (ParticleCloudException e) {
                    Toaster.s(MainActivity.this, "Error reading variable");
                    return "Error reading Variable!";
                } catch (ParticleDevice.VariableDoesNotExistException e) {
                    e.printStackTrace();
                    Toaster.s(MainActivity.this, "Variable doesn't exist!");
                    return "Variable doesn't exist!";
                } catch (IOException e) {
                    e.printStackTrace();
                    Toaster.s(MainActivity.this, "IOException");
                    return "IOException!";
                }
            }

            protected void onPostExecute(String msg) {
                setLockStatus();
            }
        }.execute();
    }

    //Call lock function in the Particle Cloud
    public void callLockFunction(){
        new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    // Get device instance
                    ParticleDevice Dashboard = ParticleCloudSDK.getCloud().getDevice("e00fce68412e03c04189238c");
                    //Call function named "carlock" and supply "1234" as and argument
                    Dashboard.callFunction("carlock", Collections.singletonList("1234"));
                    return "Lockstatus status change requested";
                } catch (ParticleCloudException e) {
                    Toaster.s(MainActivity.this, "ParticleCloudException");
                    return "Error calling'carlock'";
                } catch (IOException e) {
                    e.printStackTrace();
                    Toaster.s(MainActivity.this, "IOException");
                    return "IOException";
                } catch (ParticleDevice.FunctionDoesNotExistException e) {
                    e.printStackTrace();
                    Toaster.s(MainActivity.this, "Function doesn't exist");
                    return "Function doesn't exist";

                }
            }
            protected void onPostExecute(String msg) {
                //do nothing here
            }
        }.execute();
    }

    // Update lock status
    public void setLockStatus () {
        // Hide/unhide lock images
        if (lockStatus == 0) {
            locked.setVisibility(View.VISIBLE);
            unlocked.setVisibility(View.GONE);
        } else {
            locked.setVisibility(View.GONE);
            unlocked.setVisibility(View.VISIBLE);
        }
    }

}