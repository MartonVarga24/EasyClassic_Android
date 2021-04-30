package com.example.EasyClassic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Toaster;

public class DriveActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    double frontProximity = 0.0;
    double rearProximity = 0.0;
    ImageView frontClose;
    ImageView frontMedium;
    ImageView frontFar;
    ImageView rearClose;
    ImageView rearMedium;
    ImageView rearFar;

    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the title and top bar in the app
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_drive);

        //Assignment of layout elements
        frontClose = findViewById(R.id.proximity_front_close);
        frontMedium = findViewById(R.id.proximity_front_medium);
        frontFar = findViewById(R.id.proximity_front_far);
        rearClose = findViewById(R.id.proximity_rear_close);
        rearMedium = findViewById(R.id.proximity_rear_medium);
        rearFar = findViewById(R.id.proximity_rear_far);

        //Initialise LocationManager that provides access to system location services
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Check if FINE and COARSE location services have been granted
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(DriveActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        //If permission has not been granted request it from the user
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        //Request location on regular intervals
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.onLocationChanged(null);

        //Initialise Particle Cloud SDK
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
                //Nothing here
            }
        }.execute();

        // Set scheduled function to read parking sensor data
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {

                                          ///  Async thread to read Particle variable for front proximity
                                          new AsyncTask<Void, Void, String>() {
                                              protected String doInBackground(Void... params) {

                                                  try {
                                                      // Get Particle device instance via device id
                                                      ParticleDevice Sensors = ParticleCloudSDK.getCloud().getDevice("e00fce6861913dea5045b51f");
                                                      // Read Particle variable and assign it to "frontProximity"
                                                      frontProximity = Sensors.getDoubleVariable("cmFront");
                                                      //Toaster.s(DriveActivity.this, "cmFront " + frontProximity);
                                                      return "stuff";
                                                  } catch (ParticleCloudException e) {
                                                      Toaster.s(DriveActivity.this, "Error reading variable");
                                                      return "Error reading Variable!";
                                                  } catch (ParticleDevice.VariableDoesNotExistException e) {
                                                      e.printStackTrace();
                                                      Toaster.s(DriveActivity.this, "Variable doesn't exist!");
                                                      return "Variable doesn't exist!";
                                                  } catch (IOException e) {
                                                      e.printStackTrace();
                                                      Toaster.s(DriveActivity.this, "IOException");
                                                      return "IOException!";
                                                  }
                                              }

                                              //If the assignment was successful, change the visibility of imageViews based on the front proximity value
                                              protected void onPostExecute(String msg) {
                                                  //Toaster.s(DriveActivity.this, String.valueOf(frontProximity));
                                                  if (frontProximity < 10 && frontProximity > 0) {
                                                      frontClose.setVisibility(View.VISIBLE);
                                                      frontMedium.setVisibility(View.VISIBLE);
                                                      frontFar.setVisibility(View.VISIBLE);

                                                  } else if (frontProximity < 20 && frontProximity > 10) {
                                                      frontClose.setVisibility(View.GONE);
                                                      frontMedium.setVisibility(View.VISIBLE);
                                                      frontFar.setVisibility(View.VISIBLE);
                                                  } else if (frontProximity < 30 && frontProximity > 20) {
                                                      frontClose.setVisibility(View.GONE);
                                                      frontMedium.setVisibility(View.GONE);
                                                      frontFar.setVisibility(View.VISIBLE);
                                                  } else {
                                                      frontClose.setVisibility(View.GONE);
                                                      frontMedium.setVisibility(View.GONE);
                                                      frontFar.setVisibility(View.GONE);
                                                  }


                                              }
                                          }.execute();

                                          ///  Async thread to read Particle variable for rear proximity
                                          new AsyncTask<Void, Void, String>() {
                                              protected String doInBackground(Void... params) {

                                                  try {
                                                      // Get Particle device instance via device id
                                                      ParticleDevice Sensors = ParticleCloudSDK.getCloud().getDevice("e00fce6861913dea5045b51f");
                                                      // Read Particle variable and assign it to "rearProximity"
                                                      rearProximity = Sensors.getDoubleVariable("cmRear");
                                                      //Toaster.s(DriveActivity.this, "cmRear" + rearProximity);
                                                      return "stuff";
                                                  } catch (ParticleCloudException e) {
                                                      Toaster.s(DriveActivity.this, "Error reading variable");
                                                      return "Error reading Variable!";
                                                  } catch (ParticleDevice.VariableDoesNotExistException e) {
                                                      e.printStackTrace();
                                                      Toaster.s(DriveActivity.this, "Variable doesn't exist!");
                                                      return "Variable doesn't exist!";
                                                  } catch (IOException e) {
                                                      e.printStackTrace();
                                                      Toaster.s(DriveActivity.this, "IOException");
                                                      return "IOException!";
                                                  }
                                              }

                                              //If the assignment was successful, change the visibility of imageViews based on the rear proximity value
                                              protected void onPostExecute(String msg) {
                                                  //Toaster.s(DriveActivity.this, String.valueOf(rearProximity));
                                                  if (rearProximity < 10 && rearProximity > 0) {
                                                      rearClose.setVisibility(View.VISIBLE);
                                                      rearMedium.setVisibility(View.VISIBLE);
                                                      rearFar.setVisibility(View.VISIBLE);
                                                  } else if (rearProximity < 20 && rearProximity > 10) {
                                                      rearClose.setVisibility(View.GONE);
                                                      rearMedium.setVisibility(View.VISIBLE);
                                                      rearFar.setVisibility(View.VISIBLE);
                                                  } else if (rearProximity < 30 && rearProximity > 20) {
                                                      rearClose.setVisibility(View.GONE);
                                                      rearMedium.setVisibility(View.GONE);
                                                      rearFar.setVisibility(View.VISIBLE);
                                                  } else {
                                                      rearClose.setVisibility(View.GONE);
                                                      rearMedium.setVisibility(View.GONE);
                                                      rearFar.setVisibility(View.GONE);
                                                  }


                                              }
                                          }.execute();
                                      }
                                  },
                0, 1000);// 1000 Millisecond  = 1 second


    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Method must be implemented, but no action required here
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Method must be implemented, but no action required here
    }

    //Speedometer code
    @Override
    public void onLocationChanged(@NonNull Location location) {

        //Assign speed provided by the API to our textView

        TextView currentSpeed = this.findViewById(R.id.speed_text);

        if (location == null) {
            currentSpeed.setText(" -,- mph");
        } else {
            float nCurrentSpeed = location.getSpeed();
            currentSpeed.setText(String.format("%.0f", nCurrentSpeed * 2.237) + " mph");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}

