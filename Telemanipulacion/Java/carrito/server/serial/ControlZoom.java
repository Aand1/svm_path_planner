/**
 * Paquete que contiene las clases que permiten el control de los dispositivos
 * conectados a los puertos COM
 */
package carrito.server.serial;

import carrito.configura.*;

/**
 * Clase que permite controlar el zoom de las c�maras a trav�s de un puerto COM
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class ControlZoom {
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Lista de objetos ZoomConnection que permiten interactuar con las c�maras a trav�s
     * del puerto COM
     */
    private ZoomConnection puerto[];

    /**
     * Constructor. Abre la conexi�n con el puerto COM para cada una da las c�maras
     * @param cte Constantes
     */
    public ControlZoom(Constantes cte) {
        this.cte = cte;
        puerto = new ZoomConnection[cte.getNumEmisores()];
        for (int i = 0; i < puerto.length; i++) {
            puerto[i] = new ZoomConnection(cte.getEmisor(i).getSerial());
        }
    }

    /**
     * Establece el zoom de la c�mara indicada
     * @param zoom Indica el valor del zoom
     * @param id Identificador de la c�mara que se va a modificar
     */
    public void setZoom(int zoom, int id) {
        puerto[id].setZoom(zoom);
    }
}
