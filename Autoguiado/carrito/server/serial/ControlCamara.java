/**
 * Paquete que contiene las clases que permiten el control de los dispositivos
 * conectados a los puertos COM
 */
package carrito.server.serial;

import carrito.configura.Constantes;

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
    private int altura;
    private int[] angulo;

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

    /**
     * Obtiene el �ngulo en altura de las c�maras
     * @return Devuelve el �ngulo en altura de las c�maras
     */
    public int getAltura() {
        return altura;
    }

    /**
     * Obtiene el �ngulo lateral para la c�mara i
     * @return Devuelve el �ngulo lateral para la c�mara i
     */
    public int[] getAngulo() {
        return angulo;
    }

    /**
     * Obtiene el objeto de comunicaci�n con el puerto
     * @return Devuelve el objeto de comunicaci�n con el puerto
     */
    public CamaraConnection getPuerto() {
        return puerto;
    }

}
