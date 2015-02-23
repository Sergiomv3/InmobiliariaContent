package com.practicas.sergio.practicacontentprovider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

public class Editar extends Activity {

    private ArrayList<String> tipo;
    private EditText etLocalidad, etNumero, etCalle, etPrecio;
    private Spinner spTipo;

    private boolean editar;
    private int id;
    private int subido;

    @Override
    public void onBackPressed() {
        cancelar(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar);
        rellenaSpinnerTipo();
        initComponents();
        cargarDatosIniciales();
    }

    public void cancelar(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void aceptar(View view){
        Inmueble inmueble = obtenerDatos();
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(getString(R.string.inmueble), inmueble);
        i.putExtras(b);
        setResult(RESULT_OK, i);
        finish();
    }

    private void cargarDatosIniciales(){
        Inmueble inmueble = null;
        if(getIntent().getExtras() != null)
            inmueble = (Inmueble)getIntent().getExtras().getSerializable(getString(R.string.inmueble));
        if(inmueble!=null){
            editar = true;
            etCalle.setText(inmueble.getCalle());
            etPrecio.setText(inmueble.getPrecio()+"");
            etLocalidad.setText(inmueble.getLocalidad());
            etNumero.setText(inmueble.getNumero());
            spTipo.setSelection(tipo.indexOf(inmueble.getTipo()));
            id = inmueble.getId();
            subido = inmueble.getSubido();
        }else{
            editar = false;
            id = 0;
            subido = 0;
        }
    }

    private void initComponents(){
        etCalle = (EditText)findViewById(R.id.etCalle);
        etPrecio = (EditText)findViewById(R.id.etPrecio);
        etLocalidad = (EditText)findViewById(R.id.etLocalidad);
        etNumero = (EditText)findViewById(R.id.etNumero);
        spTipo = (Spinner)findViewById(R.id.spTipo);

        spTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipo));
    }

    private void rellenaSpinnerTipo(){
        tipo = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.valoresTipo)));
    }
    private Inmueble obtenerDatos(){
        String calle = etCalle.getText().toString();
        String tipo = spTipo.getSelectedItem().toString();
        String num = etNumero.getText().toString();
        String localidad = etLocalidad.getText().toString();
        int precio;
        try {
            precio = Integer.parseInt(etPrecio.getText().toString());
        }catch (NumberFormatException e){
            precio = 0;
        }
        return new Inmueble(id, calle, num, localidad, tipo, precio, subido);

    }
}
