package com.mycompany.biblioteca;

/**
 * Datos de conexión en UN solo lugar.
 * Si tu MySQL tiene contraseña, escríbela en CLAVE (con XAMPP suele estar vacía).
 */
public class Config {
    public static final String HOST = "localhost";
    public static final String USUARIO = "root";
    public static final String CLAVE = "";
    public static final String BD = "biblioteca";

    public static accesobd nuevaConexion() throws Exception {
        accesobd bd = new accesobd(HOST, USUARIO, CLAVE, BD);
        bd.conectaBD();
        return bd;
    }
}
