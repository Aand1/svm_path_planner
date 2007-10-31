/**
 * Paquete que contiene las clases correspondientes a la interfaz del cliente
 */
package carrito.cliente.interfaz;

import java.awt.*;

import javax.swing.*;

import carrito.media.*;

/**
 * Clase que crea un di�logo que contiene un panel encargado de visualizar el contenido multimedia
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Visor extends JDialog {
    /** Panel que visualiza el contenido multimedia */
    private MediaCanvas panel = null;
    /** Objeto multimedia */
    private Media media = null;
    /** Identificador de la instancia de la reproducci�n en VLC */
    private int id = -1;

    /**
     * Constructor que crea la interfaz del di�logo
     * @param icono Frame padre del cual se va a heredar el icono
     * @param media Objeto multimedia com�n a toda la aplicaci�n
     * @param id Identificador de la instancia VLC
     * @param posX Posici�n horizontal del cuadro de di�logo dentro de la pantalla
     * @param posY Posici�n vertical del cuadro de di�logo dentro de la pantalla
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     */
    public Visor(JFrame icono, Media media, int id, int posX, int posY, int width, int height) {
        super(icono);
        this.media = media;
        this.id = id;
        panel = new MediaCanvas(id);
        panel.paint(null, 0);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.getContentPane().setLayout(new FlowLayout());
        panel.setSize(new Dimension(width, height));
        getContentPane().add(panel);
        setLocation(posX, posY);
        pack();
        super.setVisible(true);
        setVisible(true);
        media.play(id);
    }

    /**
     * Sobrecarga del m�todo paint que permite actualizar el tama�o del panel
     * cuando se modifica el tama�o de la ventana
     * @param g Graphics
     */
    public void paint(Graphics g) {
        super.paint(g);
        panel.setBounds(0,0,this.getWidth() - 8, this.getHeight() - 27);
    }

    /**
     * Inicia la reproducci�n
     * @return Devuelve false si hubo alg�n problema al iniciar la reproducci�n
     */
    public boolean play() {
        if (! isVisible())
            setVisible(true);
        return media.play(id);
    }

    /**
     * Pausa la reproducci�n
     * @return Devuelve false si hubo alg�n problema al pausar la reproducci�n
     */
    public boolean pausa() {
        return media.pausa(id);
    }
    /**
     * Detiene la reproducci�n y oculta la ventana
     * @return Devuelve false si hubo alg�n problema al detener la reproducci�n
     */
    public boolean stop() {
        setVisible(false);
        return media.stop(id);
    }
}
