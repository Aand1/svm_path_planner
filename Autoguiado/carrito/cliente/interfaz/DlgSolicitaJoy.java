/**
 * Paquete que contiene las clases correspondientes a la interfaz del cliente
 */
package carrito.cliente.interfaz;

import java.awt.*;
import java.awt.event.*;
import java.rmi.*;

import javax.swing.*;

import carrito.cliente.*;
import carrito.configura.*;
import carrito.server.*;

/**
 * Clase que se encarga de efectuar la solicitud del control del veh�culo y que
 * muestra un di�logo indicando el progreso de dicha solicitud.
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class DlgSolicitaJoy extends JDialog implements Runnable, MouseListener {
    // Variables de la interfaz
    private JLabel lblMensaje = new JLabel("<html><center>Esperando a que el Joystick quede disponible</center></html>");
    private JButton btnCancelar = new JButton("Cancelar solicitud");
    private JButton btnLibera = new JButton("Liberar Joystick");
    private JProgressBar pb = new JProgressBar(0, 100);
    private JPanel principal = new JPanel();

    /** Variable que indica si se est� solicitando el Joystick o no */
    private static boolean solicitud = false;

    /** Variable que indica si se tiene o no el Joystick (Si se tiene, es un valor
     * distinto a "Constantes.NULLINT" concedido por el servidor)
     */
    int tieneJoy = Constantes.NULLINT;

    /** Hilo de ejecuci�n */
    private Thread hilo = null;

    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto RMI */
    private InterfazRMI rmi = null;
    /** Di�logo de control de las c�maras */
    private DlgCamaras camaras = null;

    /** Objeto Listener del Joystick */
    private JoyListener joy = null;

    /**
     * Constructor de la clase. Crea la interfaz y comienza la ejecuci�n de un
     * hilo que va a lanzar solicitudes de control al servidor de forma peri�dica
     * @param cte Objeto que hace de interfaz entre todas las variables
     * comunes a la aplicaci�n
     * @param rmi Objeto RMI
     * @param camaras Di�logo de control de las c�maras
     */
    public DlgSolicitaJoy(Constantes cte, InterfazRMI rmi, DlgCamaras camaras) {
        this.cte = cte;
        this.rmi = rmi;
        this.camaras = camaras;

        // Crea la interfaz
        setTitle("Solicitando Joystick...");
        principal.setLayout(new FlowLayout());
        setBounds(Toolkit.getDefaultToolkit().getScreenSize().width - 350, 300, 300, 100);
        setResizable(false);

        pb.setPreferredSize(new Dimension(280, 10));

        getContentPane().add(principal);
        principal.add(lblMensaje);
        principal.add(pb);
        principal.add(btnCancelar);

        btnCancelar.addMouseListener(this);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Comienza la ejecuci�n del Thread
        hilo = new Thread(this);
        hilo.start();

        setVisible(true);
    }

    /**
     * Hilo de ejecuci�n. Mientras no se tenga el control, hace solicitudes
     * sucesivas al servidor cada 10 milisegundos para ver si este est�
     * disponible. Luego cambia la interfaz indicando que se concedi� el control
     * y permitiendo liberar el control en cualquier momento.
     */
    public void run() {
        int value = 0;
        int inc = 1;
        solicitud = true;
        // Hace solicitudes sucesivas al servidor
        while(tieneJoy == Constantes.NULLINT) {
            // Esta condici�n se da en caso de que el usuario decida que ya no quiere el control del veh�culo.
            if (! solicitud)
                return;
            value += inc;
            if ((value > 99) || value < 1)
                inc *= -1;
            pb.setValue(value);
            try {
                if ((tieneJoy = rmi.getJoystick()) != Constantes.NULLINT)
                    break;
                if (tieneJoy == Constantes.DESACTIVADA) {
                    Constantes.mensaje("El control del veh�culo est� actualmente inactivo");
                    return;
                }
            } catch(RemoteException re) {}
            try { Thread.sleep(10); } catch(Exception e){}
        }
        // Se le concedi� el control, por lo cual se crea un listener para el Joystick
        joy = new JoyListener(cte, rmi, tieneJoy, this);
        lblMensaje.setText("Pulse para liberar el Joystick");
        principal.remove(pb);
        principal.remove(btnCancelar);
        principal.add(btnLibera);
        principal.revalidate();
        principal.repaint();
        btnLibera.addMouseListener(this);
        camaras.setControla(true);
        this.setTitle("Joystick concedido");
    }

    /**
     * Evento <i>mouseClicked</i>. Comprueba qu� bot�n se puls� y realiza la
     * acci�n correspondiente.
     * <p>Si se puls� el bot�n <i>Liberar Joystick</i>, indica al di�logo de
     * control de las c�maras que ya no muestre los controles para las c�maras,
     * indica al servidor que se liber� el Joystick y detiene el Listener del
     * Joystick</p>
     * <p>Si se puls� el bot�n <i>Cancelar</i>, se pone la variable
     * <i>solicitud</i> a false</p>
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Se puls� el bot�n "Liberar Joystick"
            if (e.getSource() == btnLibera) {
                try {
                    camaras.setControla(false);
                    rmi.freeJoystick(tieneJoy);
                    joy.stop();
                } catch (RemoteException re) {}
                setVisible(false);
            }
            // Se puls� el bot�n "Cancelar"
            if (e.getSource() == btnCancelar) {
                solicitud = false;
                try {
                    camaras.setControla(false);
                    rmi.freeJoystick(tieneJoy);
                } catch (RemoteException re) {}
                setVisible(false);
            }
        }
    }

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
     * Evento <i>mouseReleased</i>
     * @param e MouseEvent
     */
    public void mouseReleased(MouseEvent e) {}

}
