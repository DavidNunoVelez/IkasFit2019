package org.morfe.ikasfit19;

import android.app.Activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore baseDatos = FirebaseFirestore.getInstance();
    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private FirebaseAuth mAuth;
    private Button botonRanking;
    private Button botonMostrar;
    private TextView textoMostrar;
    private TextView textoRanking;
    private TextView textoMostrarTotal;
    public static boolean guardarPasos = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        mAuth = FirebaseAuth.getInstance();

        botonMostrar = (Button) findViewById(R.id.botonGuardarPasos);
        botonRanking = (Button) findViewById(R.id.botonRanking);
        textoMostrar = (TextView) findViewById(R.id.textoMostrar);
        textoRanking = (TextView) findViewById(R.id.textoRanking);
        textoMostrarTotal = (TextView) findViewById(R.id.textoMostrarTotal);


        //Google Fit API builder
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
        //Google Fit API builder

        //Firestore anónimo inicio
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Al iniciar sesión correctamente, actualiza la interfaz de usuario con la información del usuario registrado
                            Log.d(TAG, "signInAnonymously:success");
                            //Va a base de datos y busca todos los ID de los docu,entos documentos
                            baseDatos.collection("usuarios")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                //Si en la base de datos existe el  ID que se está conectando, pongo bandera "exixte".
                                                boolean existe = false;
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String id = document.getId();
                                                    if (id.equalsIgnoreCase(mAuth.getUid())) {
                                                        existe = true;
                                                    } else if (existe) {
                                                        existe = true;
                                                    }
                                                }
                                                //Si no existe en la base de datos ese usuario lo creamos con la fecha de hoy y los pasos a 0
                                                if (!existe) {
                                                    Date hoyLocal = Calendar.getInstance().getTime();
                                                    Map<String, Object> user = new HashMap<>();
                                                    user.put("fecha", hoyLocal);
                                                    user.put("pasosTotales", 0);
                                                    user.put("pasosParcial", 0);
                                                    user.put("id", mAuth.getUid());
                                                    // Añade un documento a la colección con la ID generada
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
                                                //Busca ese usuario en la base de datos exista o no, y muestra sus pasos al iniciar la APP
                                                DocumentReference docRef = baseDatos.collection("usuarios").document(mAuth.getUid());
                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            Usuario usua = document.toObject(Usuario.class);
                                                           readDataSinGuardar();
                                                            textoMostrarTotal.setText(String.valueOf(usua.getPasosTotales()));
                                                        } else {
                                                            Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });

                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            // Si la señal falla, mandamos mensaje al usuario
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //Firestore anónimo fin
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }
    public void subscribe() {
        //Para crear una suscripción, se llama a la API de grabación.
        // Tan pronto como la suscripción esté activa, los datos de aptitud física comenzarán a registrarse.
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

    //Este método recopila los datos de los pasos en la variable long "total" para mostrarlo en la UI
    private void readDataSinGuardar() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                int total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + total);
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
    //Este método recopila los datos de los pasos en la variable long "pasosGenerados" para mostrarlo en la UI
    //También llama a guardar los datos en base de datos.
    private void readDataGuardando() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                final int pasosGenerados =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps: " + pasosGenerados);
                                //Va a la base de datos y busca ese usuario para sacar sus datos antes de guardar
                                DocumentReference docRef = baseDatos.collection("usuarios").document(mAuth.getUid());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            Usuario usua = document.toObject(Usuario.class);
                                          long parcialAmacen=usua.getPasosParcial();
                                          long totalAmacen=usua.getPasosTotales();
                                          long pasosTotalesAAlmacenar=0;
                                          long pasosParcialesAAlmacenar=0;
                                            //Cada dia el parcial de pasos se reinicia a cero,
                                            // este código evita sumas incorrectas tanto del total de pasos como del parcial.
                                          if(pasosGenerados<parcialAmacen){
                                              pasosParcialesAAlmacenar=pasosGenerados;
                                              pasosTotalesAAlmacenar=totalAmacen+pasosGenerados;
                                          }else{
                                              pasosParcialesAAlmacenar=pasosGenerados;
                                              pasosTotalesAAlmacenar=totalAmacen+(pasosGenerados-parcialAmacen);
                                          }
                                          //Llamamos a guardar los pasos parciales y totales. Actualizamos la UI
                                            guardaPasos(pasosParcialesAAlmacenar,pasosTotalesAAlmacenar);
                                            textoMostrar.setText(String.valueOf(pasosGenerados));
                                            textoMostrarTotal.setText(String.valueOf(pasosTotalesAAlmacenar));
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
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
    //Método que guarda los pasos en Base de datos
    public void guardaPasos( long parcialPasos,long totalPasos) {
        Date hoy2 = Calendar.getInstance().getTime();
        Map<String, Object> user = new HashMap<>();
        user.put("fecha", hoy2);
        user.put("pasosTotales", totalPasos);
        user.put("pasosParcial", parcialPasos);
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
            Dialogo dialogo = new Dialogo();
            dialogo.setMainActivity(this);
            dialogo.show(getSupportFragmentManager(), "Aviso");
            readDataGuardando();
            return true;
        }else{
            //TODO:Lista ranking


            baseDatos.collection("usuarios").orderBy("pasosTotales", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int numeroTotalusuarios = 0;
                            if (task.isSuccessful()) {
                                numeroTotalusuarios = task.getResult().size();
                                int reg=0;
                                List<Usuario> usuarios = new ArrayList<>();
                                String[] clasificaciones= new String[numeroTotalusuarios];
                                //Recorre todos los documentos de la firestore y los convierte en Usuarios, que se guardan en una List
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Usuario usuario = document.toObject(Usuario.class);
                                    usuarios.add(usuario);
                                    clasificaciones[reg]=usuario.getId()+"                                          Pasos de Hoy: "+usuario.getPasosParcial()+"   Pasos Totales: "+usuario.getPasosTotales();
                                            reg++;
                                }
                                Intent clasificacion = new Intent(getApplicationContext(), ListaActivity.class);
                                ListaActivity.setResultados(clasificaciones);
                                startActivity(clasificacion);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }


                    });


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        procesarBoton(v.getId());
    }

    private void procesarBoton(int o) {
        switch (o) {
            case R.id.botonGuardarPasos:
                readDataSinGuardar();
                break;
            case R.id.botonRanking:
                crearRanking();
                break;
        }
    }
    //Método que muestra la posición del usuario conectado en el ranking de la base de datos, según sus "pasosTotales"
    public void crearRanking() {
        //Consulto documentos ordenados por pasos en orden descendente, para el que más tenga sea el primero.
        baseDatos.collection("usuarios").orderBy("pasosTotales", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int numeroTotalusuarios = 0;
                        if (task.isSuccessful()) {
                            numeroTotalusuarios = task.getResult().size();
                            List<Usuario> usuarios = new ArrayList<>();
                            //Recorre todos los documentos de la firestore y los convierte en Usuarios, que se guardan en una List
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Usuario usuario = document.toObject(Usuario.class);
                                usuarios.add(usuario);
                                //Recorro esa List y si el usuario que está conectado coincide con uno de esa lista, mustro su posición y el total de usuarios
                                int posicion = 0;
                                for (Usuario usu : usuarios) {
                                    posicion++;
                                    if (usu.getId().equalsIgnoreCase(mAuth.getUid())) {
                                        textoRanking.setText(String.valueOf(posicion) + " / " + String.valueOf(numeroTotalusuarios));
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }


                });
    }


}
