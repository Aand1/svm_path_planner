/**
 * Paquete que contiene todas las clases correspondientes a la aplicaci�n servidor
 */
package carrito.server;

import java.rmi.Naming;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import carrito.configura.Constantes;
import carrito.media.ServerMedia;
import carrito.server.interfaz.Visor;
import carrito.server.interfaz.opciones.DlgConfiguraSvr;
import carrito.server.serial.*;
import carrito.gps.CambioCoordenadas;

/**
 * Clase principal del Servidor. Inicializa todos los componentes del servidor
 * y queda a la espera de las peticiones del cliente
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Servidor {
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto multimedia */
    private ServerMedia media = null;
    /** Objeto de control del veh�culo */
    private ControlCarro control = null;
    /** Objeto de comunicaci�n del control del veh�culo */
    private SerialConnection controlConn = null;
    /** Objeto de control de las c�maras */
    private ControlCamara camaras = null;
    /** Objeto de comunicaci�n del control de las c�maras */
    private CamaraConnection camarasConn = null;
    /** Objeto de control del zoom */
    private ControlZoom zoom = null;
    /** Objeto de comunicaci�n del control del zoom */
    private ZoomConnection[] zoomConn = null;
    /** Servidor RMI */
    private ServidorRMI rmi = null;
    /** Indica si el arranque se ha hecho desde MATLAB */
    private boolean fromMatlab = false;

    CambioCoordenadas cc = null;


    /**
     * Constructor del servidor. Crea el objeto que contendr� todas las variables
     * comunes a la aplicaci�n. Posteriormente crea la ventana de configuraci�n de
     * dichas variables, a partir de las cuales iniciar� la comunicaci�n con los puertos
     * COM, la creaci�n de los servidores multimedia y el servidor RMI
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public Servidor(String dllpath, boolean fromMatlab) {
        this.fromMatlab = fromMatlab;
        cte = new Constantes();
        DlgConfiguraSvr dcs = new DlgConfiguraSvr(cte);
        dcs.setVisible(true);
        while (dcs.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }

        if (! fromMatlab) {
            initSerial();            
            //initMedia(dllpath);            
            initGPS();
            initRMI();
        }
    }
    
    /**
     * Inicializa los objetos encargados de la comunicaci�n por los puertos COM
     */
    public void initSerial() {
        control = new ControlCarro(cte);
        camaras = new ControlCamara(cte);
        zoom = new ControlZoom(cte);
        if (fromMatlab) {
            camarasConn = camaras.getPuerto();
            controlConn = control.getPuerto();
            zoomConn = zoom.getPuerto();
        }
    }

    /**
     * Inicializa el servidor RMI ubicando un objeto bajo el nombre
     * <i>ServidorCarrito</i>
     */
    public void initRMI() {
        // Crea el objeto RMI
        try {
            rmi = new ServidorRMI(cte, control, camaras, zoom, fromMatlab);
            Naming.bind("ServidorCarrito", rmi);
        } catch (Exception e) {
            System.out.println("Excepci�n: " + e.getMessage());
            System.out.println("Error al crear objeto RMI");
            System.exit(-1);
        }
        // Ubica el objeto RMI en el servidor
        System.out.println("Objeto RMI creado");
        try {
            String[] bindings = Naming.list( "" );
            System.out.println( "V�nculos disponibles:");
            for ( int i = 0; i < bindings.length; i++ )
                System.out.println( bindings[i] );
        } catch (Exception e) {
            System.out.println("Excepci�n: " + e.getMessage());
            System.out.println("Error al obtener v�nculos RMI disponibles");
            System.exit(-1);
        }
    }

    /**
     * Inicializa el servidor multimedia
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public void initMedia(String dllpath) {
        // Crea el objeto multimedia        
        media = new ServerMedia(dllpath);     
        // Recorre la lista de descriptores de los servidores y a�ade una
        // instancia multimedia por cada uno de ellos
        for (int i = 0; i < cte.getNumEmisores(); i++) {
            try {
                int id = media.addServidor(cte.getEmisor(i));
                // Si est� activada la opci�n de mostrar, crea un visor
                if (cte.getEmisor(i).isDisplay()) {
                        // Se crea un frame invisible que har� de owner. Esto �nicamente se hace para
                        // poder modificar el icono del di�logo, el cual hereda de su owner
                        JFrame icono = new JFrame();
                        icono.setVisible(false);
                        icono.setIconImage(new ImageIcon("cars.jpg").getImage());
                        new Visor(icono, media, id, cte.getEmisor(i));
                }
            } catch(Exception e) {
                System.err.println("Excepci�n: " + e.getMessage());
                System.err.println("No se pudo crear el emisor de video " + i);
            }
        }

        // Reproduce todos los streams
        media.playAll();
    }

    public void initGPS() {
      String puerto = cte.getCOMCarrito().substring(0, 3);
      int num = Integer.parseInt(cte.getCOMCarrito().substring(3)) + 1;
      puerto += Integer.toString(num);      
      cc = new CambioCoordenadas(puerto, "paramsInformatica.dat", control, true);
      cc.loadRuta("carroMay2.dat", false);
      cc.setAngulos(3);
      
      cc.setKConsigna(1);
      cc.setKAngulo(1);
      cc.setKAlfa(1.2);
      cc.setKDer(0.5);
      cc.setKIntegral(0);
      cc.setKVel(0.5);
      cc.setAcelera(false);
      
      cc.showCanvas();
      control.setCanvas(cc.getCanvas());
      cc.setIndependiente(true, 100);
    }

    /**
     * M�todo main del servidor. Arranca el servidor y obtiene el primer argumento,
     * que ha de indicar la ubicaci�n de la librer�a VLC. Si no existe este argumento, devuelve
     * un error indicando el uso adecuado de la aplicaci�n
     * @param args Argumentos
    */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Faltan argumentos:\n\tUso: $>java [opciones] carrito.server.Servidor <ubicaci�n de la librer�a VLC>");
            System.exit(1);
        }
        Servidor servidor = new Servidor(args[0], false);
    }

    /**
     * Obtiene el objeto de control de las c�maras
     * @return Devuelve el objeto de control de las c�maras
     */
    public ControlCamara getCamaras() {
        return camaras;
    }

    /**
     * Obtiene el objeto de comunicaci�n con el puerto serie de las c�maras
     * @return Devuelve el objeto de comunicaci�n con el puerto serie de las c�maras
     */
    public CamaraConnection getCamarasConn() {
        return camarasConn;
    }

    /**
     * Obtiene el objeto de control del veh�culo
     * @return Devuelve el objeto de control del veh�culo
     */
    public ControlCarro getControl() {
        return control;
    }

    /**
     * Obtiene el objeto de comunicaci�n con el puerto serie del veh�culo
     * @return Devuelve el objeto de comunicaci�n con el puerto serie del veh�culo
     */
    public SerialConnection getControlConn() {
        return controlConn;
    }

    /**
     * Obtiene el objeto que controla todas las variables de la aplicaci�n
     * @return Devuelve el objeto que controla todas las variables de la aplicaci�n
     */
    public Constantes getCte() {
        return cte;
    }

    /**
     * Obtiene el objeto que controla todas las tareas multimedia en el servidor
     * @return Devuelve el objeto que controla todas las tareas multimedia en el servidor
     */
    public ServerMedia getMedia() {
        return media;
    }

    /**
     * Obtiene el objeto RMI
     * @return Devuelve el objeto RMI
     */
    public ServidorRMI getRmi() {
        return rmi;
    }

    /**
     * Obtiene el objeto de control del zoom
     * @return Devuelve el objeto de control del zoom
     */
    public ControlZoom getZoom() {
        return zoom;
    }

    /**
     * Obtiene los objetos de comunicaci�n con el puerto serie del zoom
     * @return Devuelve los objeto de comunicaci�n con el puerto serie del zoom
     */
    public ZoomConnection[] getZoomConn() {
         return zoomConn;
     }
    
    public CambioCoordenadas getCc() {
        return cc;
    }
}
