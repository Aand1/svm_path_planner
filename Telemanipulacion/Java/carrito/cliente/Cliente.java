/**
 * Paquete que contiene todas las clases que son espec�ficas del cliente
 */
package carrito.cliente;

import java.rmi.*;

import javax.swing.*;

import carrito.cliente.interfaz.*;
import carrito.cliente.interfaz.opciones.*;
import carrito.configura.*;
import carrito.media.*;
import carrito.server.*;

/**
 * Clase principal del cliente. Inicia todas las partes que componen la
 * aplicaci�n y establece la comunicaci�n con el servidor
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Cliente extends JFrame {
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto multimedia */
    private ClienteMedia media = null;
    /** Objeto RMI */
    private InterfazRMI rmi = null;
    /** Ventana principal de la aplicaci�n */
    private frmCliente form = null;
    /** Di�logo de control de las c�maras */
    private DlgCamaras camaras = null;

    /**
     * Constructor de la clase cliente. Inicializa el objeto Constantes, obtiene
     * la configuraci�n del fichero de configuraci�n, muestra la ventana de
     * configuraci�n, inicia las distintas partes de la aplicaci�n y muestra la
     * interfaz de usuario.
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public Cliente(String dllpath) {
        // Inicializa el objeto constantes
        cte = new Constantes();
        // Abre la configuraci�n desde un fichero
        cte.openCliente();

        // Establece el icono que van a heredar las ventanas asociadas
        setIconImage(new ImageIcon("cars.jpg").getImage());
        // Abre la ventana de configuraci�n
        DlgConfiguraClt dcc = new DlgConfiguraClt(this, cte);
        setResizable(false);
        dcc.setVisible(true);
        while (dcc.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }

        // Si es la primera vez que se abre la aplicaci�n o el volante no est�
        // calibrado, inicia el asistente de calibrado
        if (! cte.isCalibrado()) {
            DlgOptJoystick doj = new DlgOptJoystick(this, cte);
            doj.show();
        }

        // Inicia la comunicaci�n con el servidor RMI
        iniciaRMI();
        // Inicia el objeto multimedia y las instancias que van a leer el stream remoto
        iniciaMedia(dllpath);
        // Inicia la interfaz
        iniciaInterfaz();
    }

    /**
     * Inicia la interfaz de la aplicaci�n
     */
    private void iniciaInterfaz() {
        // Crea la ventana de control de las c�maras
        camaras = new DlgCamaras(cte, media, rmi);
        // Crea la ventana principal
        form = new frmCliente(cte, rmi, camaras);
        form.show();
    }

    /**
     * Crea el objeto multimedia, asi como una instancia para cada una de las
     * c�maras que est�n enviando video desde el servidor
     * @param dllpath Ubicaci�n de la librer�a VLC. Es necesario indicarlo, ya que ha
     * de ser el primer par�metro en la cadena que maneja esta librer�a para
     * crear las instancias multimedia
     */
    private void iniciaMedia(String dllpath) {
        // Crea el objeto multimedia
        media = new ClienteMedia(dllpath);
        // Crea los objetos descriptores de la recepci�n multimedia a partir de
        cte.setReceptores(rmi);
        // Crea una instancia para cada descriptor y almacena su identificador en un
        // array que va a ser incluido en el objeto constantes
        try {
            int idVideos[] = new int[cte.getNumReceptores()];
            for (int i = 0; i < cte.getNumReceptores(); i++) {
                idVideos[i] = media.addCliente(cte.getReceptor(i));
            }
            cte.setIdVideos(idVideos);
        } catch(Exception e) {
            Constantes.mensaje("No se pudo crear instancia de v�deo", e);
        }
    }

    /**
     * Inicia la comunicaci�n RMI con el objeto <i>ServidorCarrito</i>
     */
    private void iniciaRMI() {
        String url = "rmi://" + cte.getIp() + "/ServidorCarrito";
        try {
            rmi = (InterfazRMI) Naming.lookup(url);
        } catch (Exception e) {
            Constantes.mensaje("No se pudo obtener el objeto RMI. ", e);
        }
    }

    /**
     * M�todo main del cliente. Arranca el cliente y obtiene el primer argumento,
     * que ha de indicar la ubicaci�n de la librer�a VLC. Si no existe este argumento, devuelve
     * un error indicando el uso adecuado de la aplicaci�n
     * @param args Argumentos
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Faltan argumentos:\n\tUso: $>java [opciones] carrito.cliente.Cliente <ubicaci�n de la librer�a VLC>");
            System.exit(1);
        }
        Cliente cliente = new Cliente(args[0]);
    }
}
