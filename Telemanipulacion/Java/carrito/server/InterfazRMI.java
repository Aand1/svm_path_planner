/**
 * Paquete que contiene todas las clases correspondientes a la aplicaci�n servidor
 */
package carrito.server;

import java.awt.*;
import java.rmi.*;

import carrito.configura.*;

/**
 * Interfaz que describe todos los m�todos que han de ser escritos para la creaci�n
 * del objeto RMI remoto
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public interface InterfazRMI extends Remote {
    /**
     * Devuelve una lista de objetos VideoCltConfig, los cuales describen un cliente
     * multimedia que va a corresponder a cada uno de los servidores multimedia en la
     * aplicaci�n servidor
     * @return Devuelve una lista de objetos VideoCltConfig
     * @throws RemoteException
     */
    public VideoCltConfig[] getVideos() throws RemoteException;

    /**
     * Obtiene el tama�o del v�deo transmitido desde el dispositivo indicado
     * @param cam N�mero de dispositivo al que se hace referencia
     * @return Devuelve el tama�o de v�deo solicitado
     * @throws RemoteException
     */
    public Dimension getSize(int cam) throws RemoteException;

    /**
     * Indica nuevos par�metros de avance al veh�culo, para llevar a cabo su control
     * @param own Identificador de due�o del control, para asegurarnos que nadie intercepta
     * el env�o de comandos
     * @param aceleracion Indica la aceleraci�n que se le va a mandar al veh�culo
     * @param frenado Indica la fuerza de frenado
     * @param giro Indica el �ngulo de giro del veh�culo
     * @return Devuelve <i>true</i> si todo ha ido bien. Si no, es que el usuario
     * ha perdido el control del veh�culo
     * @throws RemoteException
     */
    public boolean avanzaCarro(int own, float aceleracion, float frenado,
                               float giro) throws RemoteException;

    /**
     * Indica el �ngulo lateral de las c�maras
     * @param camara C�mara a la que se hace referencia
     * @param angulo �ngulo indicado
     * @throws RemoteException
     */
    public void setAnguloCamaras(int camara, int angulo) throws RemoteException;

    /**
     * Indica el �ngulo en altura de las c�maras
     * @param angulo �ngulo indicado
     * @throws RemoteException
     */
    public void setAlturaCamaras(int angulo) throws RemoteException;

    /**
     * Indica el zoom de una determinada c�mara
     * @param zoom Indica el nuevo zoom
     * @param id C�mara a la que se hace referencia
     * @throws RemoteException
     */
    public void setZoom(int zoom, int id) throws RemoteException;

    /**
     * Solicita el control del veh�culo
     * @return Devuelve -1 si se deniega el control, u otro n�mero en caso de que
     * este haya sido concedido, indicando el identificador de posesi�n del control
     * @throws RemoteException
     */
    public int getJoystick() throws RemoteException;

    /**
     * Libera el control del veh�culo
     * @param id Identificador de posesi�n del veh�culo
     * @throws RemoteException
     */
    public void freeJoystick(int id) throws RemoteException;
    
    public boolean frenoTotal(int id) throws RemoteException;
    public boolean desfrenoTotal(int id) throws RemoteException;
    public boolean resetAvance(int id, boolean retrocede) throws RemoteException;
    
    
}
