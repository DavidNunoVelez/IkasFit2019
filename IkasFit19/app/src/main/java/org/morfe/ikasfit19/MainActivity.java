/*
 * Copyright (C) 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.morfe.ikasfit19;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fit.samples.common.logger.Log;
import com.google.android.gms.fit.samples.stepcounter.R;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * This sample demonstrates combining the Recording API and History API of the Google Fit platform
 * to record steps, and display the daily current step count. It also demonstrates how to
 * authenticate a user with Google Play Services.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore baseDatos = FirebaseFirestore.getInstance();
    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private FirebaseAuth mAuth;
    private Button botonRanking;
    private Button botonMostrar;
    private TextView textoMostrar;
    private TextView textoRanking;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_principal);
    mAuth = FirebaseAuth.getInstance();

    botonMostrar = (Button) findViewById(R.id.botonGuardarPasos);
      botonRanking = (Button) findViewById(R.id.botonRanking);
      textoMostrar = (TextView) findViewById(R.id.textoMostrar);
      textoRanking = (TextView) findViewById(R.id.textoRanking);


    FitnessOptions fitnessOptions =
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build();
    if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
      GoogleSignIn.requestPermissions(
          this,
          REQUEST_OAUTH_REQUEST_CODE,
          GoogleSignIn.getLastSignedInAccount(this),
          fitnessOptions);
    } else {
      subscribe();
    }

    //Firestore anónimo
    mAuth.signInAnonymously()
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // Sign in success, update UI with the signed-in user's information
                  Log.d(TAG, "signInAnonymously:success");
                  String hoy = Calendar.getInstance().getTime().toString();
                  Map<String, Object> user = new HashMap<>();
                  user.put("fecha", hoy);
                  user.put("pasosTotales", 0);
                  user.put("id", mAuth.getUid());
                  // Add a new document with a generated ID
                  baseDatos.collection("usuarios").document(mAuth.getUid())
                          .set(user)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              android.util.Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                          })
                          .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              android.util.Log.w(TAG, "Error writing document", e);
                            }
                          });

                } else {
                  // If sign in fails, display a message to the user.
                  Log.w(TAG, "signInAnonymously:failure", task.getException());
                  Toast.makeText(MainActivity.this, "Authentication failed.",
                          Toast.LENGTH_SHORT).show();

                }

                // ...
              }
            });
    //Firestore anónimo
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
        subscribe();
      }
    }
  }

  /** Records step data by requesting a subscription to background step data. */
  public void subscribe() {
    // To create a subscription, invoke the Recording API. As soon as the subscription is
    // active, fitness data will start recording.
    Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
        .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addOnCompleteListener(
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                  Log.i(TAG, "Successfully subscribed!");
                } else {
                  Log.w(TAG, "There was a problem subscribing.", task.getException());
                }
              }
            });
  }

  /**
   * Reads the current daily step total, computed from midnight of the current day on the device's
   * current timezone.
   */
  private void readData() {
    Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
        .addOnSuccessListener(
            new OnSuccessListener<DataSet>() {
              @Override
              public void onSuccess(DataSet dataSet) {
                long total =
                    dataSet.isEmpty()
                        ? 0
                        : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                Log.i(TAG, "Total steps: " + total);
                guardaPasos(total);
                textoMostrar.setText(String.valueOf(total));
              }
            })
        .addOnFailureListener(
            new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "There was a problem getting the step count.", e);
              }
            });

  }
  public void guardaPasos(long totalPasos){
      String hoy = Calendar.getInstance().getTime().toString();
      Map<String, Object> user = new HashMap<>();
      user.put("fecha", hoy);
      user.put("pasosTotales", totalPasos);
      user.put("id", mAuth.getUid());
      // Add a new document with a generated ID
      baseDatos.collection("usuarios").document(mAuth.getUid())
              .set(user)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {
                      android.util.Log.d(TAG, "DocumentSnapshot successfully written!");
                  }
              })
              .addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      android.util.Log.w(TAG, "Error writing document", e);
                  }
              });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the main; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_read_data) {
      readData();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

    @Override
    public void onClick(View v) {
        procesarBoton(v.getId());
    }

    private void procesarBoton(int o) {

        switch (o){
            case R.id.botonGuardarPasos:

                readData();

                break;
            case R.id.botonRanking:

                break;
        }
    }
}
