package com.practicas.sergio.practicacontentprovider;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Adaptador extends CursorAdapter {

    private int recurso;
    private static LayoutInflater i;
    private Context context;
    private  ViewHolder vh;
    public Adaptador(Context co, int recurso, Cursor cu) {
        super(co, cu, true);
        this.recurso = recurso;
        this.context = co;
        this.i = (LayoutInflater)co.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = i.inflate(recurso, parent, false);
        ViewHolder vh = new ViewHolder();
        vh.tvLocalidad = (TextView)view.findViewById(R.id.tvLocalidad);
        vh.tvPrecio = (TextView)view.findViewById(R.id.tvPrecio);
        vh.tvDireccion = (TextView)view.findViewById(R.id.tvDireccion);
        vh.ivTipo = (ImageView)view.findViewById(R.id.ivFoto);
        view.setTag(vh);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        vh = (ViewHolder) view.getTag();
        String localidad = cursor.getString(0);
        String tipo = cursor.getString(1);
        int precio = cursor.getInt(2);
        String calle = cursor.getString(3);
        String numero = cursor.getString(4);
        int subido = cursor.getInt(5);
        Log.e("SUBIDO",String.valueOf(subido));
        vh.tvLocalidad.setText(tipo+" en "+localidad);
        vh.tvPrecio.setText(precio+"");
        vh.tvDireccion.setText(calle + ", Nº" + numero);


       tipoImagen(tipo);

    }

    private void tipoImagen(String tipo){ // ESTO NOS APORTA UNA IMAGEN EN LA LISTA EN FUNCION DE TIPO

        if(tipo.equalsIgnoreCase(context.getString(R.string.local_tipo))){
            vh.ivTipo.setImageResource(R.drawable.local);
        }else if(tipo.equalsIgnoreCase(context.getString(R.string.adosada_tipo))){
            vh.ivTipo.setImageResource(R.drawable.adosada);
        }else if(tipo.equalsIgnoreCase(context.getString(R.string.chalet_tipo))){
            vh.ivTipo.setImageResource(R.drawable.chalet);
        }else if(tipo.equalsIgnoreCase(context.getString(R.string.parcela_tipo))){
            vh.ivTipo.setImageResource(R.drawable.parcela);
        }else if(tipo.equalsIgnoreCase(context.getString(R.string.cortijo_tipo))){
            vh.ivTipo.setImageResource(R.drawable.cortijo);
        }else if(tipo.equalsIgnoreCase(context.getString(R.string.piso_tipo))){
            vh.ivTipo.setImageResource(R.drawable.piso);
        }else {
            vh.ivTipo.setImageResource(R.drawable.otro);
        }

    }

    public static class ViewHolder{
        public TextView tvLocalidad, tvPrecio, tvDireccion;
        public ImageView ivTipo;
    }
}
