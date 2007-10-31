/**
 * Paquete que contiene todas las clases descriptoras de las propiedades que
 * afectan a la aplicaci�n
 */
package carrito.configura;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * Clase que hace de filtro gen�rico para la selecci�n de los ficheros con el
 * JFileChooser
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Filtro extends FileFilter {
    /** Array que contiene todas las posibles extensiones */
    private String filtros[] = null;
    /** String que describe dichas extensiones */
    private String descripcion = null;

    /**
     * Constructor que inicializa las propiedades de la clase
     * @param filtros Array que contiene todas las posibles extensiones
     * @param descripcion String que describe dichas extensiones
     */
    public Filtro(String[] filtros, String descripcion) {
        this.filtros = filtros;
        this.descripcion = descripcion;
    }

    /**
     * M�todo que decide si un fichero pasa o no por el filtro, bas�ndonos en la
     * extensi�n
     * @param file Fichero a comprobar
     * @return Devuelve true si es aceptado
     */
    public boolean accept(File file) {
        String filename = file.getName();
        for (int i = 0; i < filtros.length; i++) {
            if (filename.endsWith(filtros[i]))
                return true;
        }
        return false;
    }

    // Obtiene la descripci�n del filtro
    public String getDescription() {
        return descripcion;
    }
}
