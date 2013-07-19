/**
 * Paquete que contiene las clases correspondientes a la interfaz del cliente
 */
package carrito.cliente.interfaz;

import java.awt.event.*;

import javax.swing.*;

import carrito.cliente.interfaz.opciones.*;
import carrito.configura.*;
import carrito.server.*;
import java.rmi.RemoteException;

/**
 * Clase que construye el men� de la aplicaci�n.
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Menu extends JMenuBar implements MouseListener {
    // Men�s principales
    private JMenu mnuVentanas = new JMenu("Ventanas");
    private JMenu mnuOpciones = new JMenu("Opciones");

    // Submen�s del men� ventanas
    private JCheckBoxMenuItem mnuVentanasCamaras = new JCheckBoxMenuItem("Ver control de las c�maras", null, true);
    private JMenuItem mnuVentanasJoy = new JMenuItem("Solicitar Joystick");

    // Submen�s del men� de opciones
    private JMenuItem mnuOpcionesJoy = new JMenuItem("Calibrar joystick");
    private JMenuItem mnuOpcionesOtros = new JMenuItem("Otras opciones...");

    // Di�logos de la aplicaci�n
    private DlgCamaras camaras = null;
    private DlgSolicitaJoy dsj = null;
    private frmCliente form = null;

    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto RMI */
    private InterfazRMI rmi = null;

    /** Variable para controlar que al activar la aplicaci�n no se produzca un ciclo
     * al traer el resto de ventanas al frente
     */
    private boolean activo = false;

    /**
     * Constructor de la clase Menu. Crea el men� principal de la aplicaci�n
     * @param cte Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n
     * @param form Ventana principal de la aplicaci�n
     * @param rmi Objeto RMI
     * @param camaras Di�logo de control de las c�maras
     */
    public Menu(Constantes cte, frmCliente form, InterfazRMI rmi, DlgCamaras camaras) {
        this.cte = cte;
        this.form = form;
        this.camaras = camaras;
        this.rmi = rmi;

        this.add(mnuVentanas);
        this.add(mnuOpciones);

        mnuVentanas.add(mnuVentanasCamaras);
        try {
            if (rmi.isControlActivo())
                mnuVentanas.add(mnuVentanasJoy);
        } catch (RemoteException re) {}
        mnuOpciones.add(mnuOpcionesJoy);
        mnuOpciones.add(mnuOpcionesOtros);

        mnuVentanasJoy.addMouseListener(this);
        mnuVentanasCamaras.addMouseListener(this);
        mnuOpcionesJoy.addMouseListener(this);
        mnuOpcionesOtros.addMouseListener(this);
    }

    /**
     * M�todo que trae al frente todas las ventanas de la aplicaci�n
     */
    public void toFront() {
        // Si es la primera vez que se llama al m�todo, trae todas las ventanas al frente
        if (! activo) {
            camaras.toFront();
            if (dsj != null)
                dsj.toFront();
            activo = true;
            // Si no, cambia el valor de la variable activo para que no se produzcan ciclos
        } else {
            activo = false;
        }
    }

    /**
     * M�todo que indica si la ventana de control de las c�maras est� visible o no
     * @param valor boolean
     */
    public void setActivoCamaras(boolean valor) {
        mnuVentanasCamaras.setState(valor);
    }

    /**
     * Evento <i>mouseClicked</i>
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {}
    /**
     * Evento <i>mouseEntered</i>
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}
    /**
     * Evento <i>mouseExited</i>
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Evento <i>mousePressed</i>
     * @param e MouseEvent
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Evento <i>mouseReleased</i>. Comprueba cu�l de los men�s fue seleccionado
     * y realiza la acci�n correspondiente a cada uno
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {
        // Se puls� el bot�n izquierdo
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Si la fuente fue la solicitud del Joystick, crea una nueva ventana
            // de solicitud, siempre y cuando no exista ya una
            if (e.getSource() == mnuVentanasJoy) {
                if ((dsj == null) || (! dsj.isVisible())){
                    dsj = new DlgSolicitaJoy(cte, rmi, camaras);
                }
                // Si se seleccion� la ventana de control de las c�maras, esta
                // se muestra o se oculta seg�n proceda
            } else if (e.getSource() == mnuVentanasCamaras) {
                if (mnuVentanasCamaras.getState()) {
                    camaras.setVisible(true);;
                } else {
                    camaras.setVisible(false);;
                }
                // Si se selecciona el asistente de calibrado, se crea un nuevo
                // asistente
            } else if (e.getSource() == mnuOpcionesJoy) {
                DlgOptJoystick doj = new DlgOptJoystick(form, cte);
                doj.setVisible(true);
                // Si se selecciona la configuraci�n del cliente, se muestra la
                // ventana de configuraci�n
            } else if (e.getSource() == this.mnuOpcionesOtros) {
                DlgConfiguraClt dcc = new DlgConfiguraClt(form, cte);
                dcc.setVisible(true);
            }
        }
    }
}

