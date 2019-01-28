package org.morfe.ikasfit19.Clases;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import org.morfe.ikasfit19.R;
import org.morfe.ikasfit19.Ventanas.Principal;


public class Dialogo extends DialogFragment {

    private Principal principal;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());
            builder.setMessage("¿Quieres guardar los pasos?")
                    .setTitle("Confirmacion")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener()  {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("Dialogos", "Confirmacion Aceptada.");
                            Principal.guardarPasos=true;
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("Dialogos", "Confirmacion Cancelada.");
                            Principal.guardarPasos=false;
                            dialog.cancel();
                        }
                    });

            return builder.create();
        }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
