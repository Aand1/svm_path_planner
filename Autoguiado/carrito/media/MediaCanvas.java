/**
 * Paquete que contiene todas las clases relacionadas con el apartado multimedia de la aplicaci�n
 */
package carrito.media;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;
import java.awt.color.ColorSpace;

/**
 * Clase que permite dibujar en un panel el contenido de una instancia de v�deo
 *  multimedia para poderlo integrar en la interfaz
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class MediaCanvas extends Canvas {
    // La librer�a es cargada al crearse la clase
    static {
        System.loadLibrary("jawt");
        System.loadLibrary("javavlc");
    }
    /** Identificador de la instancia que ser� dibujada en el panel */
    private int id = -1;
    private int indice = 0;
    private Canvas canvas = null;
    private Image image = null;

    /**
     * M�todo nativo que dibuja en el objeto Graphics el contenido de la instancia
     * referenciada por id
     * @param g Objeto Graphics
     * @param id Identificador de la instancia
     */
    public native void paint(Graphics g, int id);

    /**
     * Constructor que obtiene la instancia que ser� dibujada en el panel
     * @param id Identificador de la instancia
     */
    public MediaCanvas(int id) {
        this.id = id;
    }

    /**
     * M�todo paint sobrescrito para que haga la llamada al m�todo nativo
     * @param g Objeto Graphics
     */
    public void paint(Graphics g) {
       paint(g, id);
   }

   /**
    * Cambia la instancia que ser� dibujada en el panel
    * @param id int
    */
   public void setId(int id) {
       this.id = id;
   }

   public Graphics leeImagen() {
     repaint();
     return getGraphics();
   }
}
