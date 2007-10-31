/**
 * Paquete que contiene todas las clases correspondientes a la aplicaci�n servidor
 */
package carrito.server;

import java.awt.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

import carrito.configura.*;
import carrito.server.serial.*;

/**
 * Objeto que va a recibir las instrucciones remotas desde el cliente y va a
 * traducir estas llamadas a cada uno de los objetos encargados de cada tarea
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class ServidorRMI extends UnicastRemoteObject implements InterfazRMI, Runnable {
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto de control del veh�culo */
    private ControlCarro control = null;
    /** Objeto de control del motor de las c�maras */
    private ControlCamara camara = null;
    /** Objeto que controla el zoom de las c�maras */
    private ControlZoom CZoom = null;
    /** Objeto que permite la creaci�n de n�meros aleatorios */
    private Random rnd = null;
    /** Identificador del due�o del control del veh�culo */
    private int joyOwner = Constantes.NULLINT;
    /** Indica si el due�o del control del veh�culo est� activo */
    private boolean activo = false;
    /** Indica si el veh�culo est� funcionando */
    private boolean funcionando = false;
    /** Hilo de ejecuci�n para comprobar si el cliente que tiene el control se desconecta */
    private Thread hilo = null;

    /**
     * Constructor. Inicializa las variables y crea el hilo de ejecuci�n.
     * @param cte Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n
     * @param control Objeto de control del veh�culo
     * @param camara Objeto de control del motor de las c�maras
     * @param CZoom Objeto que controla el zoom de las c�maras
     * @throws RemoteException
     */
    public ServidorRMI(Constantes cte, ControlCarro control, ControlCamara camara, ControlZoom CZoom) throws RemoteException {
        this.cte = cte;
        this.control = control;
        this.camara = camara;
        this.CZoom = CZoom;
        this.rnd = new Random(System.currentTimeMillis());

        hilo = new Thread(this);
        hilo.start();
    }

    /**
     * Comprueba si el cliente se desconect�, y en caso afirmativo, frena el
     * veh�culo
     */
    private synchronized void inactivos() {
        if ((! activo) && (funcionando)) {
            control.frenoEmergencia();
            joyOwner = Constantes.NULLINT;
            funcionando = false;
        }
        // Pone activo a false, lo cual significa que el cliente tiene 1 segundo
        // para volverlo a poner a true, para indicar que no se ha desconectado
        activo = false;
    }

    /**
     * Hilo de ejecuci�n que hace sucesivas llamadas a la funci�n <i>inactivos</i>
     */
    public void run() {
        while(true) {
            inactivos();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {}
        }
    }

    /**
     * Devuelve una lista de objetos VideoCltConfig, los cuales describen un cliente
     * multimedia que va a corresponder a cada uno de los servidores multimedia en la
     * aplicaci�n servidor
     * @return Devuelve una lista de objetos VideoCltConfig
     * @throws RemoteException
     */
    public VideoCltConfig[] getVideos() throws RemoteException {
        VideoCltConfig[] retorno = new VideoCltConfig[cte.getNumEmisores()];
        for (int i = 0; i < retorno.length; i++) {
            retorno[i] = new VideoCltConfig("", 1, null, 10, null, Constantes.NULLINT,
                                            Constantes.NULLFLOAT, null, null);
            retorno[i].setIp(cte.getEmisor(i).getIp());
            retorno[i].setPort(cte.getEmisor(i).getPort());
        }
        return retorno;
    }

    /**
         * Obtiene el tama�o del v�deo transmitido desde el dispositivo indicado
         * @param cam N�mero de dispositivo al que se hace referencia
         * @return Devuelve el tama�o de v�deo solicitado
         * @throws RemoteException
     */
    public Dimension getSize(int cam) {
        return new Dimension(cte.getEmisor(cam).getWidth(), cte.getEmisor(cam).getHeight());
    }

    /**
     * Comprueba que el cliente que mand� un comando de control del veh�culo est�
     * activo como due�o de dicho control. Adem�s, si <i>funcionando</i> est� a <i>false</i>,
     * lo cambia a <i>true</i> para indicar que ya est� andando
     * @param own Identificador del due�o del control del veh�culo
     * @return Devuelve si est� activo o no
     */
    private synchronized boolean isActivo(int own) {
        funcionando = true;
        if (own == joyOwner) {
            activo = true;
            return true;
        } else {
            return false;
        }
    }

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
    public boolean avanzaCarro(int own, float aceleracion, float frenado, float giro) throws RemoteException {
        if (! isActivo(own))
            return false;
        if (aceleracion != Constantes.NULLINT)
            control.setAvance(aceleracion, frenado);
        control.setGiro(giro);
        return true;
    }

    /**
     * Indica el �ngulo lateral de las c�maras
     * @param id C�mara a la que se hace referencia
     * @param angulo �ngulo indicado
     * @throws RemoteException
     */
    public void setAnguloCamaras(int id, int angulo) throws RemoteException {
        camara.setAngulo(id, angulo);
    }

    /**
     * Indica el �ngulo en altura de las c�maras
     * @param angulo �ngulo indicado
     * @throws RemoteException
     */
    public void setAlturaCamaras(int angulo) throws RemoteException {
        camara.setAltura(angulo);
    }

    /**
     * Indica el zoom de una determinada c�mara
     * @param zoom Indica el nuevo zoom
     * @param id C�mara a la que se hace referencia
     * @throws RemoteException
     */
    public void setZoom(int zoom, int id) throws RemoteException {
        CZoom.setZoom(zoom, id);
    }

    /**
     * Solicita el control del veh�culo
     * @return Devuelve -1 si se deniega el control, u otro n�mero en caso de que
     * este haya sido concedido, indicando el identificador de posesi�n del control
     * @throws RemoteException
     */
    public synchronized int getJoystick() throws RemoteException {
        // Si est� siendo usado, devuelve -1
        if (joyOwner != Constantes.NULLINT) {            
            return Constantes.NULLINT;
            // Si no, reinicia las variables de control y le da un nuevo identificador
            // al cliente
        } else {
            activo = true;
            funcionando = false;
            joyOwner = rnd.nextInt();
            control.reinit();
            return joyOwner;
        }
    }

    /**
     * Libera el control del veh�culo
     * @param id Identificador de posesi�n del veh�culo
     * @throws RemoteException
     */
    public synchronized void freeJoystick(int id) throws RemoteException {
        if (joyOwner != id)
            return;
        joyOwner = Constantes.NULLINT;
    }
    
    public boolean frenoTotal(int id) throws RemoteException {
        
        if (! isActivo(id))
            return false;
        
        control.frenadoTotal();
        return true;        
    }
    
    public boolean desfrenoTotal(int id) throws RemoteException {
        
        if (! isActivo(id))
            return false;

        control.desfrenadoTotal();
        return true;        
    }
    
    public boolean resetAvance(int id, boolean retrocede) throws RemoteException {
        if (! isActivo(id))
            return false;

        control.resetAvance(retrocede);
        return true;
    }
}
