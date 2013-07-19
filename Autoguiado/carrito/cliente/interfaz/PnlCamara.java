/**
 * Paquete que contiene las clases correspondientes a la interfaz del cliente
 */
package carrito.cliente.interfaz;

import java.awt.*;
import java.awt.event.*;
import java.rmi.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import carrito.cliente.interfaz.opciones.*;
import carrito.configura.*;
import carrito.media.*;
import carrito.server.*;

/**
 * Clase que permite el control de la c�mara que est� asociado a la misma y muestra
 * una interfaz para hacerlo
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class PnlCamara extends JPanel implements MouseListener, ChangeListener, Runnable {
    // Variables de la interfaz
    private TitledBorder lblBorde = new TitledBorder("");
    private JButton btnPlay = new JButton(new ImageIcon("pause.png"));
    private JButton btnStop = new JButton(new ImageIcon("stop.png"));
    private JButton btnRec = new JButton(new ImageIcon("record.png"));
    private JLabel lblZoom = new JLabel("Zoom: ");
    private JSpinner spnZoom = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private JLabel lblAngulo = new JLabel("Giro: ");
    private JSpinner spnAngulo = new JSpinner(new SpinnerNumberModel(0, -90, 90, 0.5f));

    /** Identificador de la instancia en VLC encargada de grabar el v�deo recibido */
    private int id_rec = Constantes.NULLINT;
    /** Objeto multimedia */
    private ClienteMedia media = null;
    /** Indica si se est� reproduciendo el v�deo actualmente */
    private boolean playing = false;
    /** Objeto RMI */
    private InterfazRMI rmi = null;
    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Identificador de la instancia VLC encargada de la reproducci�n de v�deo */
    private int id = -1;
    /** N�mero de c�mara actual */
    private int cam = -1;
    /** Hilo de ejecuci�n necesario para la grabaci�n */
    private Thread hilo = null;
    /** Di�logo que visualiza el v�deo */
    private Visor visor = null;
    /** Indica si se ha de parar la grabaci�n */
    private static boolean parar = false;

    /**
     * Contructor que crea la interfaz del panel, a�ade los listeners y crea un
     * visor para el stream de v�deo correspondiente al panel
     * @param ancho Indica el ancho m�ximo que ha de tener el panel para ser
     * insertado en el di�logo
     * @param cam N�mero de c�mara
     * @param cte Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n
     * @param media Objeto multimedia
     * @param rmi Objeto RMI
     */
    public PnlCamara(int ancho, int cam, Constantes cte, ClienteMedia media, InterfazRMI rmi) {
        this.media = media;
        this.cam = cam;
        this.cte = cte;
        this.id = cte.getIdVideo(cam);
        this.rmi = rmi;

        this.setLayout(new GridBagLayout());
        this.setPreferredSize(new Dimension(ancho - 10, 100));
        lblBorde.setTitle("C�mara " + (cam + 1));

        this.setBorder(lblBorde);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(2, 4, 2, 4);

        add(btnPlay,c);
        add(btnStop,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(btnRec, c);
        c.gridwidth = GridBagConstraints.RELATIVE;
        add(lblAngulo,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(spnAngulo,c);
        c.gridwidth = GridBagConstraints.RELATIVE;
        add(lblZoom,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(spnZoom,c);

        btnPlay.addMouseListener(this);
        btnStop.addMouseListener(this);
        btnRec.addMouseListener(this);
        spnAngulo.addChangeListener(this);
        spnZoom.addChangeListener(this);
        try {
            // Se crea un frame invisible que har� de owner. Esto �nicamente se hace para
            // poder modificar el icono del di�logo, el cual hereda de su owner
            JFrame icono = new JFrame();
            icono.setVisible(false);
            icono.setIconImage(new ImageIcon("cars.jpg").getImage());
            // Crea el visor
            visor = new Visor(icono, media, id, cte.getReceptor(cam).getPosicion().width, cte.getReceptor(cam).getPosicion().height, rmi.getSize(cam).width, rmi.getSize(cam).height);
            visor.setTitle("C�mara " + (cam + 1));
        } catch (RemoteException re) {
            System.err.println ("Error al crear el visor: " + re.getMessage());
        }
    }

    /**
     * Establece si el control del veh�culo ha sido concedido o no, mostrando u
     * ocultando de esta forma los spinners correspondientes
     * @param activo Indica si se activas o se desactivan los controles
     */
    public void setControla(boolean activo) {
        lblAngulo.setVisible(activo);
        spnAngulo.setVisible(activo);
        lblZoom.setVisible(activo);
        spnZoom.setVisible(activo);
    }

    /**
     * Establece el valor de la instancia que graba, la cual ha sido inicializada
     * en el di�logo de configuraci�n de la grabaci�n, y lanza el hilo que va a
     * modificar el estado del bot�n de grabaci�n.
     * @param id_rec Identificador de la instancia en VLC
     */
    public void setMedia(int id_rec) {
        if (this.id_rec != Constantes.NULLINT)
            return;
        this.id_rec = id_rec;
        media.play(id_rec);
        btnRec.setIcon(new ImageIcon("recording.png"));
        hilo = new Thread(this);
        hilo.start();
    }

    /**
     * Propaga la llamada al m�todo <i>toFront()</i> del Visor asociado con el panel
     */
    public void toFront() {
        visor.toFront();
    }

    /**
     * Hilo de ejecuci�n. Alterna el color del bot�n entre rojo y azul para indicar
     * que se est� grabando. Cuando se activa la variable <i>parar</i>, se detiene
     * la grabaci�n
     */
    public void run() {
        boolean indicador = false;
        while(true) {
            if (parar)
                break;
            if (indicador) {
                btnRec.setIcon(new ImageIcon("record.png"));
            } else {
                btnRec.setIcon(new ImageIcon("recording.png"));
            }
            indicador = !indicador;
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
        btnRec.setIcon(new ImageIcon("record.png"));
        parar = false;
    }

    /**
     * Evento <i>mouseClicked</i>. Comprueba la fuente del evento y realiza la
     * acci�n asociada
     * <p>Si se puls� el bot�n play, distingue si se est� reproduciendo o no,
     * pausando o reanudando la reproducci�n seg�n se requiera</p>
     * <p>Si se puls� el bot�n stop, se detiene la reproducci�n y se elimina el visor</p>
     * <p>Si se puls� el bot�n rec, se llama al di�logo de configuraci�n de la grabaci�n para
     * comenzar a grabar o detiene la grabaci�n si ya se encuentra grabando</p>
     * @param e MouseEvent
     */
    public void mouseClicked(MouseEvent e) {
        // Si no es el bot�n izquierdo, retorna
        if (e.getButton() != MouseEvent.BUTTON1)
            return;
        // Si se puls� el bot�n Play
        if (e.getSource() == btnPlay) {
            // Si ya se est� reprociendo, lo que se quiere es pausar
            if (playing) {
                playing = false;
                if (visor.pausa()) {
                    // Se cambia el icono
                    btnPlay.setIcon(new ImageIcon("play.png"));
                } else {
                    Constantes.mensaje("Error al pausar la c�mara " + id);
                    visor.stop();
                }
                // Si no, lo que se quiere es reanudar la reproducci�n
            } else {
                playing = true;
                if (visor.play()) {
                    // Se cambia el icono
                    btnPlay.setIcon(new ImageIcon("pause.png"));
                } else {
                    Constantes.mensaje("Error al reproducir la c�mara " + id);
                    visor.stop();
                }
            }
            // Si se pulsa el bot�n stop, se detiene la reproducci�n y se elimina el visor
        } else if (e.getSource() == btnStop) {
            if (playing) {
                playing = false;
                btnPlay.setIcon(new ImageIcon("play.png"));
                if (! visor.stop()) {
                    Constantes.mensaje("Error al detener la c�mara " + id);
                }
            }
            // Si se pulsa el bot�n de grabaci�n
        } else if (e.getSource() == btnRec) {
            // Si no se est� grabando, se abre la ventana de configuraci�n, que se encarga
            // de iniciar la reproducci�n
            if (id_rec == Constantes.NULLINT) {
                VideoCltConfig vcc = cte.getReceptor(cam);
                new DlgConfiguraRec(vcc.getIp(), vcc.getPort(), vcc.getCaching(), media, this).setVisible(true);
                // Si no, se detiene la grabaci�n y se detiene el hilo que modifica el icono cada segundo
            } else {
                media.stop(id_rec);
                parar = true;
                id_rec = Constantes.NULLINT;
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

    /**
     * Comprueba si se ha modificado alguno de los spinners
     * @param e ChangeEvent
     */
    public void stateChanged(ChangeEvent e) {
        // Si se modifica el �ngulo, se transforma en un valor entre 0 y 255 y se env�a al objeto RMI
        if (e.getSource() == spnAngulo) {
            float ang = ((Double)spnAngulo.getValue()).floatValue();
            ang = 255 * (ang + 90) / 180;
            try {
                rmi.setAnguloCamaras((cam + 1), (int) ang);
            } catch(RemoteException re) {
                Constantes.mensaje("No se pudo mover la c�mara ", re);
            }
        }
        // Si se modifica el zoom, se transforma en un valor entre 0 y 1070 y se env�a al objeto RMI
        if (e.getSource() == spnZoom) {
            int zoom = 1070 * ((Integer)spnZoom.getValue()).intValue() / 100;
            try {
                rmi.setZoom(zoom, cam);
            } catch(RemoteException re) {
                Constantes.mensaje("No se pudo cambiar el zoom ", re);
            } catch (Exception ex) {}

        }
    }
}

