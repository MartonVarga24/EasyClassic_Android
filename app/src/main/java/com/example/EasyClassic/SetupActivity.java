package com.example.EasyClassic;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Collections;

import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Toaster;

public class SetupActivity extends AppCompatActivity {

    private EditText passTxt; //variable to contained the entered password

    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the title and top bar in the app
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_setup);

        Button enterBtn = findViewById(R.id.enter_button);
        passTxt = findViewById(R.id.editTextTextPassword);

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password = passTxt.getText().toString();
                // Publish event into Particle Cloud containing the engine start code
                new AsyncTask<Void, Void, String>() {
                    protected String doInBackground(Void... params) {
                        try {
                            // Publish event
                            ParticleCloudSDK.getCloud().publishEvent(
                                    // the event name
                                    "engine_start_code",
                                    // the event payload data
                                    password, ParticleEventVisibility.PUBLIC,

                                    //After this time, the event is considered stale/outdated
                                    60
                            );
                            return "Engine Start Code entered";
                        }
                        catch(ParticleCloudException e) {
                            Toaster.s(SetupActivity.this, "Error submitting code");
                            return "Error submitting code";
                        }
                    }

                    protected void onPostExecute(String msg) {
                        // Do nothing
                    }
                }.execute();
            }
        });
    }
}
