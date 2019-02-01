package org.morfe.ikasfit19;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.google.android.gms.fit.samples.stepcounter.R;

public class Dialogo extends DialogFragment {

    private MainActivity mainActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setMessage("Sigue así y anda todo lo que puedas que es muy sano.")
                .setTitle("¡Pasos guardados!")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmacion Aceptada.");
                        MainActivity.guardarPasos=true;
                        dialog.cancel();
                    }
                })
                .setView(R.layout.activity_dialogo);
        return builder.create();
    }
    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
