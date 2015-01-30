package com.practicas.sergio.practicacontentprovider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class FragmentoInmueble extends Fragment {

    private int foto;
    private ArrayList<File> fotos;
    private int idInmu;
    private ImageView ivImage;
    private Button btnSiguiente, btnAtras;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmento_inmueble, container, false);
    }

    private void cargarFotos(){
        File[] array;
        try {
            array = getActivity().getExternalFilesDir(idInmu + "/").listFiles();
        } catch (NullPointerException e){
            array = null;
        }
        fotos = new ArrayList<>();
        if(array != null && array.length >0){
            for(File a : array){
                if(a.getPath().contains(getString(R.string.nombre_foto)+ idInmu +"_")){
                    fotos.add(a);
                }
            }
        }
    }

    public void fotosInmueble(int id){
        inicio();
        idInmu = id;
        if(idInmu !=-1) {
            cargarFotos();
        }else{
            fotos = null;
        }
        foto = 0;
        mostrarFoto();
    }

    private void inicio(){
        ivImage = (ImageView)getView().findViewById(R.id.ivImagen);
        btnSiguiente = (Button)getView().findViewById(R.id.botonSiguiente);
        btnAtras = (Button)getView().findViewById(R.id.botonAnterior);

        View.OnClickListener escuchaFotoSiguiente = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idInmu !=-1) {
                    if (foto == fotos.size() - 1) {
                        foto = 0;
                    } else {
                        foto++;
                    }
                    mostrarFoto();
                }
            }
        };
        ivImage.setOnClickListener(escuchaFotoSiguiente);
        btnSiguiente.setOnClickListener(escuchaFotoSiguiente);
        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInmu != -1) {
                    if (foto == 0) {
                        foto = fotos.size() - 1;
                    } else {
                        foto--;
                    }
                    mostrarFoto();
                }
            }
        });
    }

    public void mostrarFoto(){
        if(idInmu !=-1) {
            if (fotos != null && fotos.size() > 0 && fotos.get(foto).exists()) {
                Bitmap d = new BitmapDrawable(getResources(), fotos.get(foto).getAbsolutePath()).getBitmap();
                int nh = (int) (d.getHeight() * (512.0 / d.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
                ivImage.setImageBitmap(scaled);
            } else {
                ivImage.setImageResource(R.drawable.sinfoto);
            }
        }else{
            ivImage.setImageBitmap(null);
        }
    }
}
