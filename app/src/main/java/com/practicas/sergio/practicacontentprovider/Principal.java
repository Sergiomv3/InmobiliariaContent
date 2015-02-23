package com.practicas.sergio.practicacontentprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.practicas.sergio.practicacontentprovider.basededatos.Contrato;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Principal extends Activity implements FragmentoLista.EscuchadorLista{

    private boolean Detallado;
    private AlertDialog alerta;
    private String url;
    private Cursor c;
    private final static String rutaControlador = "/AADP3/controladorandroid";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal);
        this.setTitle(getString(R.string.app_name) + leerSharedPreferences(getString(R.string.nuevo_usuario)));
        String ip = leerSharedPreferences(getString(R.string.servidor));
        url = "http://"+ip+ rutaControlador;

        int sw = this.getResources().getConfiguration().smallestScreenWidthDp;
        if(sw<600){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
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
                return cambiarValor(true);
            case R.id.menu_servidor:
                return cambiarValor(false);
            case R.id.menu_sincronizar:
                return subirDatos();
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
        if (alerta != null) {
            alerta.dismiss();
        }
    }

    private void guardarSharedPreferences(String valor, String pref) {
        SharedPreferences pc;
        SharedPreferences.Editor ed;
        pc = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        ed = pc.edit();
        if(pref.equals(getString(R.string.nuevo_usuario))) {
            ed.putString(getString(R.string.nuevo_usuario), valor);
        } else {
            ed.putString(getString(R.string.servidor), valor);
            url = "http://" + valor + rutaControlador;
        }
        ed.apply();
    }
    /*
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
        */

    private String leerSharedPreferences(String pref) {
        SharedPreferences pc;
        pc = getSharedPreferences(getString(R.string.preferencias), MODE_PRIVATE);
        if(pref.equals(getString(R.string.nuevo_usuario))) {
            return pc.getString(getString(R.string.nuevo_usuario), "");
        } else {
            return pc.getString(getString(R.string.servidor), "");
        }

    }
    private boolean subirDatos(){
        Uri uri = Contrato.TablaInmueble.CONTENT_URI;
        String[] p = new String[]{Contrato.TablaInmueble.LOCALIDAD,
                Contrato.TablaInmueble.TIPO,
                Contrato.TablaInmueble.PRECIO,
                Contrato.TablaInmueble.CALLE,
                Contrato.TablaInmueble.NUMERO,
                Contrato.TablaInmueble.SUBIDO,
                Contrato.TablaInmueble._ID};
        String seleccion = Contrato.TablaInmueble.SUBIDO + " = ?";
        String[] parametrosSeleccion = new String[]{"0"};
        c = this.getContentResolver().query(uri, p, null,
                null, null);
        Log.e("COUNT",String.valueOf(c.getCount()));
        if(c.getCount()>0) {

            int i = 0;
            c.moveToFirst();
            do {
                JSONObject objetoJSON = new JSONObject();
                try {
                    objetoJSON.put("localidad", c.getString(0) + "");
                    objetoJSON.put("tipo", c.getString(1));
                    objetoJSON.put("precio", c.getInt(2) + "");
                    objetoJSON.put("calle", c.getString(3));
                    objetoJSON.put("numero", c.getString(4));
                    objetoJSON.put("usuario", leerSharedPreferences(getString(R.string.nuevo_usuario)));
                    subirInmueble(objetoJSON, c.getString(6));
                    System.out.println(objetoJSON.toString());
                } catch (JSONException e) {
                }
                i++;
            } while (c.moveToNext());
        }else{
            // SI ESTAN TODOS SUBIDOS
            Toast.makeText(this, getString(R.string.subido), Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    private void subirInmueble(final JSONObject objetoJSON, final String idOriginal){
        Thread s = new Thread(){
            @Override
            public synchronized void run() {
                System.out.println(objetoJSON.toString()+"");
                String res = postData(url+"?action=inmueble", objetoJSON);
                long id = -1;
                if(res!=null) {
                    try {
                        JSONObject jsonObj = new JSONObject(res);
                        String ide = jsonObj.getString("idinmueble");
                        id = Long.parseLong(ide);
                    } catch (Exception e) {
                        id = -1;
                    }
                }else{
                    Principal.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(Principal.this, getString(R.string.errorServidor), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if(id > 0){
                    ContentValues valores;
                    Uri uri = Contrato.TablaInmueble.CONTENT_URI;
                    valores = consVal(objetoJSON);
                    if(valores!=null) {
                        String where = Contrato.TablaInmueble._ID + " = ?";
                        String[] args = new String[]{idOriginal};
                        getContentResolver().update(uri, valores, where, args);
                        subirFotos(idOriginal, id+"");
                    }else{

                    }
                }
            }
        };
        s.start();
    }
    private ContentValues consVal(JSONObject o){
        ContentValues valores = new ContentValues();

        try {
            valores.put(Contrato.TablaInmueble.LOCALIDAD, o.getString("localidad"));
            valores.put(Contrato.TablaInmueble.TIPO, o.getString("tipo"));
            valores.put(Contrato.TablaInmueble.PRECIO, o.getString("precio"));
            valores.put(Contrato.TablaInmueble.CALLE, o.getString("calle"));
            valores.put(Contrato.TablaInmueble.NUMERO, o.getString("numero"));
            valores.put(Contrato.TablaInmueble.SUBIDO, 1);
        } catch (JSONException e) {
            valores = null;
        }
        return valores;
    }

    private void subirFotos(final String idOriginal, final String id){
        File[] array;
        try {
            array = getExternalFilesDir(idOriginal + "/").listFiles();
        } catch (NullPointerException e){
            array = null;
        }
        final ArrayList<String> fotos = new ArrayList<>();
        if(array != null && array.length >0){
            for(File a : array){
                if(a.getPath().contains(getString(R.string.nombre_foto)+idOriginal+"_")){
                    fotos.add(a.getPath());
                }
            }
        }
        for (int i = 0; i < fotos.size(); i++) {
            final int finalI = i;
            Thread t = new Thread(){
                @Override
                public synchronized void run() {
                    String res =postFile(url+"?action=fichero", "archivo", fotos.get(finalI), id);
                    if(res == null)
                      Log.e("LOG","error archivo");
                }
            };
            t.start();
        }
    }

    private boolean cambiarValor(final boolean usuario){
        String valor;
        if(usuario){
            valor = leerSharedPreferences(getString(R.string.nuevo_usuario));
        } else{
            valor = leerSharedPreferences(getString(R.string.servidor));
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if(usuario) {
            alert.setTitle(getString(R.string.intro_usuario));
        }else{
            alert.setTitle(getString(R.string.intro_server));
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        final View vista = inflater.inflate(R.layout.dialogo_usuario, null);
        alert.setView(vista);

        final EditText texto = (EditText)vista.findViewById(R.id.etUsuario);
        texto.setText(valor);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                if(!texto.getText().toString().equals("")){
                    if(usuario) {
                        guardarSharedPreferences(texto.getText().toString(), getString(R.string.nuevo_usuario));
                        Principal.this.setTitle(getString(R.string.app_name) + texto.getText().toString());
                    } else{
                        guardarSharedPreferences(texto.getText().toString(), getString(R.string.servidor));
                    }
                }
            }
        });
        alerta = alert.create();
        alerta.show();
        return true;
    }

    private String postData(String url, JSONObject obj) {
        HttpParams parametros = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(parametros, 10000);
        HttpConnectionParams.setSoTimeout(parametros, 10000);
        HttpClient httpclient = new DefaultHttpClient(parametros);
        String json = obj.toString();

        try {
            HttpPost httppost = new HttpPost(url.toString());
            httppost.setHeader("Content-type", "application/json");

            StringEntity se = new StringEntity(json);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setEntity(se);

            HttpResponse response = httpclient.execute(httppost);
            String temp = EntityUtils.toString(response.getEntity());
            return temp;
        } catch (ClientProtocolException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String postFile(String urlPeticion, String nombreParametro, String nombreArchivo, String idInmueble) {
        String result="";
        try {
            URL url = new URL(urlPeticion);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setDoOutput(true);
            conexion.setRequestMethod("POST");
            FileBody fileBody = new FileBody(new File(nombreArchivo));
            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            multipartEntity.addPart(nombreParametro, fileBody);
            multipartEntity.addPart("idinmueble", new StringBody(idInmueble));
            multipartEntity.addPart("nombre", new StringBody(nombreArchivo));
            conexion.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue());
            OutputStream out = conexion.getOutputStream();
            try {
                multipartEntity.writeTo(out);
            } finally {
                out.close();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                result+=decodedString+"\n";
            }
            in.close();
        } catch (MalformedURLException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        return result;
    }
}
