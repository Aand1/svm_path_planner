/**
 * Paquete que contiene las clases correspondientes a la interfaz de la aplicaci�n
 * servidor
 */
package carrito.server.interfaz;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import carrito.configura.*;
import carrito.media.*;

/**
 * Clase que crea un di�logo que contiene un panel encargado de visualizar el contenido multimedia
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Visor extends JDialog implements WindowListener {
    /** Panel que visualiza el contenido multimedia */
    private MediaCanvas panel = null;

    /**
     * Constructor que crea la interfaz del di�logo
     * @param icono Frame padre del cual se va a heredar el icono
     * @param media Objeto multimedia com�n a toda la aplicaci�n
     * @param id Identificador de la instancia VLC
     * @param vsc VideoSvrConf
     */
    public Visor(JFrame icono, Media media, int id, VideoSvrConf vsc) {
        super(icono);
        setTitle("C�mara " + (vsc.getVideoDisp() + 1));
        panel = new MediaCanvas(id);
        panel.paint(null, 0);
        this.getContentPane().setLayout(new FlowLayout());
        panel.setSize(new Dimension(vsc.getWidth(), vsc.getHeight()));
        add(panel);
        pack();
        super.setVisible(true);
        setVisible(true);
        media.play(id);
        addWindowListener(this);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Sobrecarga del m�todo paint que permite actualizar el tama�o del panel
     * cuando se modifica el tama�o de la ventana
     * @param g Graphics
     */
    public void paint(Graphics g) {
        super.paint(g);
        panel.setBounds(0, 0, this.getWidth() - 8, this.getHeight() - 27);
    }

    /**
     * Evento <i>windowActivated</i>
     * @param e WindowEvent
     */
    public void windowActivated(WindowEvent e) {}

    /**
     * Evento <i>windowClosed</i>
     * @param e WindowEvent
     */
    public void windowClosed(WindowEvent e) {}

    /**
     * Evento <i>windowClosing</i>. Pide confirmaci�n al usuario de que desea
     * cerrar la ventana, ya que una vez cerrada no ser� posible abrirla de nuevo
     * @param e WindowEvent
     */
    public void windowClosing(WindowEvent e) {
        if (JOptionPane.showConfirmDialog(this,
                "�Est� seguro que desea cerrar la ventana?\rNo podr� volver " +
                                          "a abrirla a menos que reinicie la aplicaci�n",
                                          "�Desea cerrar la ventana?",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) ==
            JOptionPane.YES_OPTION) {
            setVisible(false);
            dispose();
        }
    }

    /**
     * Evento <i>windowDeactivated</i>
     * @param e WindowEvent
     */
    public void windowDeactivated(WindowEvent e) {}

    /**
     * Evento <i>windowDeiconified</i>
     * @param e WindowEvent
     */
    public void windowDeiconified(WindowEvent e) {}

    /**
     * Evento <i>windowIconified</i>
     * @param e WindowEvent
     */
    public void windowIconified(WindowEvent e) {}

    /**
     * Evento <i>windowOpened</i>
     * @param e WindowEvent
     */
    public void windowOpened(WindowEvent e) {}
}
