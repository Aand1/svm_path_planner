/**
 * Paquete que contiene las clases que permiten el control de los dispositivos
 * conectados a los puertos COM
 */
package carrito.server.serial;

import carrito.configura.*;

/**
 * Clase que lleva el control del motor de las c�maras
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class ControlCamara {
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto CamaraConnection que permite interactuar con el puerto COM */
    private CamaraConnection puerto;

    /**
     * Constructor. Abre la conexi�n con el puerto COM
     * @param cte Constantes
     */
    public ControlCamara(Constantes cte) {
        this.cte = cte;
        puerto = new CamaraConnection(cte.getCOMCamara());
    }

    /**
     * Establece el �ngulo lateral de una c�mara indicada
     * @param camara Identificador de la c�mara
     * @param angulo �ngulo indicado
     */
    public void setAngulo(int camara, int angulo) {
        puerto.setAngulo(camara, angulo);
        System.out.println("puerto.setAngulo(" + camara + ", " + angulo + ")");
    }

    /**
     * Establece el �ngulo vertical de una c�mara indicada
     * @param angulo �ngulo indicado
     */
    public void setAltura(int angulo) {
        puerto.setAltura(angulo);
        System.out.println("puerto.setAltura(" + angulo + ")");
    }

}
