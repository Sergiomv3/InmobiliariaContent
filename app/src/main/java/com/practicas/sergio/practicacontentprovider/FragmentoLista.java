package com.practicas.sergio.practicacontentprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.practicas.sergio.practicacontentprovider.basededatos.Contrato;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentoLista extends Fragment {

    private Adaptador ad;
    private ListView lvLista;
    private Cursor cursor;
    private final int ACTIVIDAD_CAMARA = 0;
    private final int ACTIVIDAD_NUEVO = 2;
    private final int ACTIVIDAD_EDITAR = 1;
    private AlertDialog alert;
    private EscuchadorLista listenerLista;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bundle b;
            Inmueble inm;
            ContentValues valores;
            Uri uri = Contrato.TablaInmueble.CONTENT_URI;
            switch (requestCode) {
                case ACTIVIDAD_EDITAR:
                    b = data.getExtras();
                    inm = (Inmueble) b.getSerializable(getString(R.string.inmueble));
                    valores = conseguirValores(inm);
                    String where = Contrato.TablaInmueble._ID + " = ?";
                    String [] args = new String[]{inm.getId()+""};
                    getActivity().getContentResolver().update(uri, valores, where, args);
                    break;
                case ACTIVIDAD_NUEVO:
                    b = data.getExtras();
                    inm = (Inmueble) b.getSerializable(getString(R.string.inmueble));
                    valores = conseguirValores(inm);
                    getActivity().getContentResolver().insert(uri, valores);
                    break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) { // CUANDO CREAMOS LA ACTIVIDAD
        super.onActivityCreated(savedInstanceState);
        lvLista.setAdapter(ad);
        lvLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
                if (listenerLista != null) {
                    Inmueble inmueble = conseguirInmueble(pos);
                    listenerLista.onSelectedInmueble(inmueble);
                }
            }
        });
        registerForContextMenu(lvLista);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int posicion = info.position;
        cursor.moveToPosition(posicion);
        int idInmueble = cursor.getInt(6);
        if (id == R.id.contextual_borrar) {
            return borrarInmueble(idInmueble);
        } else if (id == R.id.contextual_editar) {
            return editarInmueble(posicion);
        } else if (id == R.id.contextual_foto) {
            nuevaFoto(idInmueble);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragmento_lista, container, false);
        lvLista = (ListView) v.findViewById(R.id.lvLista);
        cargarCursor();
        ad = new Adaptador(this.getActivity(), R.layout.detalle_lista, cursor);
        return v;
    }

    /*Método que crea el menú contextual*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextual, menu);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (alert != null) {
            alert.dismiss();
        }
    }

    private void borrarFotos(int id) {
        if (id != -1) {
            File[] array = getActivity().getExternalFilesDir(id + "/").listFiles();
            if (array != null && array.length > 0) {
                for (File a : array) {
                    a.delete();
                }
            }
            File directorio = getActivity().getExternalFilesDir(id + "/");
            directorio.delete();
        }
    }

    private boolean borrarInmueble(final int index) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.borrar_inmueble));
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View vista = inflater.inflate(R.layout.dialogo_borrar, null);
        alert.setView(vista);
        final String nombre = cursor.getString(1) + " (" + cursor.getString(3) + " " + cursor.getString(4) + ")";
        TextView texto = (TextView)vista.findViewById(R.id.tvBorrar);
        texto.setText(getString(R.string.seguro) + " " + nombre + "?");
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                borrarFotos(index);
                Uri uri = Contrato.TablaInmueble.CONTENT_URI;
                String where = Contrato.TablaInmueble._ID + " = ?";
                String[] args = new String[]{index+""};
                getActivity().getContentResolver().delete(uri,where, args);
                tostada(getString(R.string.elemento_borrado) + " " + nombre);
            }
        });
        alert.setNegativeButton(android.R.string.no, null);
        this.alert = alert.create();
        this.alert.show();
        return true;
    }

    private void cargarCursor(){
        Uri uri = Contrato.TablaInmueble.CONTENT_URI;
        String[] projection = new String[]{Contrato.TablaInmueble.LOCALIDAD,
                Contrato.TablaInmueble.TIPO,
                Contrato.TablaInmueble.PRECIO,
                Contrato.TablaInmueble.CALLE,
                Contrato.TablaInmueble.NUMERO,
                Contrato.TablaInmueble.SUBIDO,
                Contrato.TablaInmueble._ID};
        String sortOrder = Contrato.TablaInmueble.LOCALIDAD + ", " +
                Contrato.TablaInmueble.TIPO + ", " +
                Contrato.TablaInmueble.CALLE + ", " +
                Contrato.TablaInmueble.NUMERO;
        cursor = this.getActivity().getContentResolver().query(uri, projection, null, null, sortOrder);
    }

    private Inmueble conseguirInmueble(int posicion){
        cursor.moveToPosition(posicion);

        String localidad = cursor.getString(0);
        String tipo = cursor.getString(1);
        int precio = cursor.getInt(2);
        String calle = cursor.getString(3);
        String numero = cursor.getString(4);
        int subido = cursor.getInt(5);
        int id = cursor.getInt(6);

        return new Inmueble(id, calle, numero, localidad, tipo, precio, subido);
    }

    private ContentValues conseguirValores(Inmueble inm){
        ContentValues valores = new ContentValues();

        String localidad = inm.getLocalidad();
        String tipo = inm.getTipo();
        int precio = inm.getPrecio();
        String calle = inm.getCalle();
        String numero = inm.getNumero();
        //int subido = inm.getSubido();

        valores.put(Contrato.TablaInmueble.LOCALIDAD, localidad);
        valores.put(Contrato.TablaInmueble.TIPO, tipo);
        valores.put(Contrato.TablaInmueble.PRECIO, precio);
        valores.put(Contrato.TablaInmueble.CALLE, calle);
        valores.put(Contrato.TablaInmueble.NUMERO, numero);
        //valores.put(Contrato.TablaInmueble.SUBIDO, subido);

        return valores;
    }

    public boolean editarInmueble(int posicion) {
        Intent i = new Intent(getActivity(), Editar.class);
        Bundle b = new Bundle();
        if(posicion > -1) {
            b.putSerializable(getString(R.string.inmueble), conseguirInmueble(posicion));
            i.putExtras(b);
            startActivityForResult(i, ACTIVIDAD_EDITAR);
        }else {
            startActivityForResult(i, ACTIVIDAD_NUEVO);
        }
        return true;
    }

    public void nuevaFoto(int id){
        Intent tomaFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (tomaFoto.resolveActivity(getActivity().getPackageManager()) != null) {
            File fichero = new File(getActivity().getExternalFilesDir(id + "/"), nombreFoto(id) + getString(R.string.extension));
            if (fichero != null) {
                tomaFoto.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fichero));
                startActivityForResult(tomaFoto, ACTIVIDAD_CAMARA);
            }
        }
    }

    private String nombreFoto(int id){
        SimpleDateFormat formatoFecha = new SimpleDateFormat(getString(R.string.formato_fecha));
        String fecha = formatoFecha.format(new Date());
        return getString(R.string.nombre_foto)+ id +"_" + fecha;
    }

    public void typeList(int type) {
        lvLista.clearChoices();
        lvLista.setChoiceMode(type);
    }

    private void tostada(String s) {
        Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
    }

    public interface EscuchadorLista {
        public void onSelectedInmueble(Inmueble i);
    }

    public void setEscuchadorLista(EscuchadorLista escuchador) {
        this.listenerLista = escuchador;
    }
}
