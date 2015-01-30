package com.practicas.sergio.practicacontentprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Principal extends Activity implements FragmentoLista.EscuchadorLista{

    private boolean Detallado;
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);
        this.setTitle(getString(R.string.app_name) + leerSharedPreferences());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_nuevo:
                FragmentoLista f = (FragmentoLista)getFragmentManager().findFragmentById(R.id.fragmentoLista);
                return f.editarInmueble(-1);
            case R.id.menu_usuario:
                return cambiarUser();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FragmentoLista fragmentoLista =(FragmentoLista)getFragmentManager().findFragmentById(R.id.fragmentoLista);
        fragmentoLista.setEscuchadorLista(this);

        FragmentoInmueble f2 = (FragmentoInmueble)getFragmentManager().findFragmentById(R.id.fragmentoInmueble);
        Detallado = (f2 != null && f2.isInLayout());
        if(Detallado) {
            fragmentoLista.typeList(1);
        }else{
            fragmentoLista.typeList(0);
        }
    }

    @Override
    public void onSelectedInmueble(Inmueble i) {
        if(Detallado) {
            if(i!=null) {
                ((FragmentoInmueble) getFragmentManager()
                        .findFragmentById(R.id.fragmentoInmueble)).fotosInmueble(i.getId());
            }else {
                ((FragmentoInmueble)getFragmentManager()
                        .findFragmentById(R.id.fragmentoInmueble)).fotosInmueble(-1);
            }
        }
        else {
            Intent intent = new Intent(this, DatosInmuebles.class);
            intent.putExtra(getString(R.string.identificador), i.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (alert != null) {
            alert.dismiss();
        }
    }

    private void guardarSharedPreferences(String valor) {
        SharedPreferences pc;
        SharedPreferences.Editor ed;
        pc = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        ed = pc.edit();
        ed.putString(getString(R.string.nuevo_usuario), valor);
        ed.apply();
    }

    private boolean cambiarUser(){
        String usuarioActual = leerSharedPreferences();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.intro_usuario));
        LayoutInflater inflater = LayoutInflater.from(this);
        final View vista = inflater.inflate(R.layout.dialogo_usuario, null);
        alert.setView(vista);

        final EditText texto = (EditText)vista.findViewById(R.id.etUsuario);
        texto.setText(usuarioActual);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                if(!texto.getText().toString().equals("")){
                    guardarSharedPreferences(texto.getText().toString());
                    Principal.this.setTitle(getString(R.string.app_name) + texto.getText().toString());
                }
            }
        });
        this.alert = alert.create();
        this.alert.show();
        return true;
    }

    private String leerSharedPreferences() {
        SharedPreferences pc;
        pc = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        return pc.getString(getString(R.string.nuevo_usuario), "");
    }
}
