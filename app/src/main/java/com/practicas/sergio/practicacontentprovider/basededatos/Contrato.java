package com.practicas.sergio.practicacontentprovider.basededatos;

import android.net.Uri;
import android.provider.BaseColumns;

public class Contrato {

    private Contrato () {
    }

    public static abstract class TablaInmueble implements BaseColumns {
        public static final String TABLA = "inmueble";
        public static final String NUMERO = "numero";
        public static final String CALLE = "calle";
        public static final String TIPO = "tipo";
        public static final String LOCALIDAD = "localidad";
        public static final String SUBIDO = "subido";
        public static final String PRECIO = "precio";
        public static final String CONTENT_TYPE_INMUEBLE =
                "vnd.android.cursor.item/vnd.inmueble";
        public static final String CONTENT_TYPE_INMUEBLES =
                "vnd.android.cursor.dir/vnd.inmuebles";
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                ProveedorInmueble.AUTORIDAD + "/" + TABLA);
    }
}