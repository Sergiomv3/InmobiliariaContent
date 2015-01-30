package com.practicas.sergio.practicacontentprovider;

import android.app.Activity;
import android.os.Bundle;

public class DatosInmuebles extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datos_inmuebles);

        FragmentoInmueble fragmentoInmueble =(FragmentoInmueble)getFragmentManager().findFragmentById(R.id.fragmentoInmueble);

        fragmentoInmueble.fotosInmueble(getIntent().getIntExtra(getString(R.string.identificador), -1));
    }
}
