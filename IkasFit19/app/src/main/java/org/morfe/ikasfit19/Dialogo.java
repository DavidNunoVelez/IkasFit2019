package org.morfe.ikasfit19;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class Dialogo extends DialogFragment {

    private MainActivity principal;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setMessage("¿Quieres guardar los pasos?")
                .setTitle("Confirmacion")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()  {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmacion Aceptada.");

                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmacion Cancelada.");

                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    public MainActivity getPrincipal() {
        return principal;
    }

    public void setPrincipal(MainActivity principal) {
        this.principal = principal;
    }
}
