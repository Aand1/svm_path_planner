/**
 * Paquete que contiene todas las clases correspondientes a la aplicaci�n servidor
 */
package carrito.server;

import java.rmi.*;

import javax.swing.*;

import carrito.configura.*;
import carrito.media.*;
import carrito.server.interfaz.*;
import carrito.server.interfaz.opciones.*;
import carrito.server.serial.*;

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
    /** Objeto de control de las c�maras */
    private ControlCamara camaras = null;
    /** Objeto de control del zoom */
    private ControlZoom zoom = null;

    /**
     * Constructor del servidor. Crea el objeto que contendr� todas las variables
     * comunes a la aplicaci�n. Posteriormente crea la ventana de configuraci�n de
     * dichas variables, a partir de las cuales iniciar� la comunicaci�n con los puertos
     * COM, la creaci�n de los servidores multimedia y el servidor RMI
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public Servidor(String dllpath) {
        cte = new Constantes();
        DlgConfiguraSvr dcs = new DlgConfiguraSvr(cte);
        dcs.setVisible(true);
        while (dcs.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }

        DlgConfiguraOpciones dco = new DlgConfiguraOpciones(cte);
        dco.setVisible(true);
        while (! dco.isInicializado()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }

        initSerial();
        initMedia(dllpath);
        initRMI();
    }
    /**
     * Inicializa los objetos encargados de la comunicaci�n por los puertos COM
     */
    private void initSerial() {
        control = new ControlCarro(cte);
        //camaras = new ControlCamara(cte);
        //zoom = new ControlZoom(cte);
    }

    /**
     * Inicializa el servidor RMI ubicando un objeto bajo el nombre
     * <i>ServidorCarrito</i>
     */
    private void initRMI() {
        // Crea el objeto RMI
        try {
            ServidorRMI rmi = new ServidorRMI(cte, control, camaras, zoom);
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
    private void initMedia(String dllpath) {
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
        //media.addInstancia("C:\\Documents&nbsp;and&nbsp;Settings\\neztol\\Escritorio\\Viaje&nbsp;La&nbsp;Graciosa&nbsp;'06\\Graciosa.wmv " +
        //                   ":sout=#transcode{vcodec=DIV3,vb=1024,scale=1}:duplicate{dst=std{access=http,mux=ts,dst=192.168.1.37:1234}}");

        // Reproduce todos los streams
        media.playAll();
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
        Servidor servidor = new Servidor(args[0]);
    }
}
