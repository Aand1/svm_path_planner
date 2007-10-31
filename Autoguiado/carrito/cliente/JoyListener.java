/**
 * Paquete que contiene todas las clases que son espec�ficas del cliente
 */
package carrito.cliente;

import java.io.*;
import java.rmi.*;

import carrito.cliente.interfaz.*;
import carrito.configura.*;
import carrito.server.*;
import com.centralnexus.input.*;

/**
 * Clase que escucha el estado del Joystick y env�a la informaci�n al servidor
 * para controlar as� el veh�culo
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class JoyListener implements JoystickListener, Runnable {
    /** Objeto que monitoriza el Joystick */
    private Joystick joy = null;

    // Variables que indican la "zona muerta"
    private float deadD = Constantes.NULLFLOAT, deadI = Constantes.NULLFLOAT;
    private float deadArr = Constantes.NULLFLOAT, deadAb = Constantes.NULLFLOAT;

    /** Posici�n actual del Joystick */
    private static float posX = Constantes.NULLFLOAT, posY = Constantes.NULLFLOAT;

    /** Indica si se est� retrocediendo */
    private boolean retroceso = false;

    /** Valor de la aceleraci�n resultante de transportar los datos le�dos
     * al rango manejado por el veh�culo
     */
    private float aceleracion;
    /** Valor del frenado resultante de transportar los datos le�dos
     * al rango manejado por el veh�culo
     */
    private float frenado;

    /** Valor del giro resultante de transportar los datos le�dos
     * al rango manejado por el veh�culo
     */
    private float giro;

    /** Hilo de ejecuci�n */
    private Thread hilo = null;

    /** Objeto que hace de interfaz entre todas las variables comunes a la aplicaci�n */
    private Constantes cte = null;
    /** Objeto RMI */
    private InterfazRMI rmi = null;

    /** Flag que permite conocer cu�ndo se ha de detener el hilo de ejecuci�n
     * (El usuario se desconecta o es desconectado por el servidor)
     */
    private boolean detener = false;

    /** Identificador devuelto por el servidor al conceder el control al cliente */
    private int id = Constantes.NULLINT;

    /**  Di�logo de solicitud del Joystick */
    private DlgSolicitaJoy dsj = null;

    /**
     * Constructor de la clase. Comprueba el n�mero de dispositivos conectados y
     * obtiene el primero que est� activo. Adem�s, inicializa las variables que
     * indican la <i>zona muerta</i> y arranca el hilo de ejecuci�n
     * @param cte Constantes
     * @param rmi InterfazRMI
     * @param id int
     * @param dsj DlgSolicitaJoy
     */
    public JoyListener(Constantes cte, InterfazRMI rmi, int id, DlgSolicitaJoy dsj) {
        this.cte = cte;
        this.rmi = rmi;
        this.id = id;
        this.dsj = dsj;

        // Obtiene el n�mero de dispositivos
        int numDevices = Joystick.getNumDevices();

        if (numDevices > 0) {
            try {
                // Recorre todos los que est�n conectados y se queda con el primero de ellos
                for (int i = 0; i < numDevices; i++) {
                    if (Joystick.isPluggedIn(i)) {
                        joy = Joystick.createInstance(i);
                        joy.addJoystickListener(this);
                        break;
                    }
                }

                // Inicializa las variables l�mite de la "zona muerta"
                deadD = cte.getXNone() + cte.getXDif();
                deadI = cte.getXNone() - cte.getXDif();

                deadArr = cte.getYNone() - cte.getYDif();
                deadAb = cte.getYNone() + cte.getYDif();

            } catch(IOException ioe) {
                System.out.println("Error: " + ioe.getMessage());
            }
        }

        // Inicia el hilo de ejecuci�n
        hilo = new Thread(this);
        hilo.start();
    }

    /**
     * Hilo de ejecuci�n. Mientras no se desconecte, comprueba el estado del Joystick
     * y lo env�a al servidor cada 50 milisegundos
     */
    public void run() {
        detener = false;
        while(true) {
            if (detener)
                break;
            // Comprueba el estado del joystick
            pollJoy();
            try {
                Thread.sleep(50);
            } catch(Exception e) {}
        }
    }

    /**
     * Comprueba el estado del Joystick y lo env�a al servidor. Adem�s, toma los
     * valores obtenidos y los transforma al rango en el cual trabaja el veh�culo
     */
    private void pollJoy() {
        // Pone las variables a "cero"
        aceleracion = 0;
        frenado = 0;
        giro = 32767;
        // Comprueba si se gir� hacia la derecha y transforma el rango
        if (posX > deadD) {
            if (posX > cte.getMaxDerecha()) {
                cte.setMaxDerecha(posX);
            }
            giro = (int)(Constantes.CARRO_DIST * (posX - deadD) / (cte.getMaxDerecha() - deadD)) + Constantes.CARRO_CENTRO;
            // Comprueba si se gir� hacia la izquierda y transforma el rango
        } else if (posX < deadI) {
            if (posX < cte.getMaxIzquierda()) {
                cte.setMaxIzquierda(posX);
            }
            giro = (int)((-Constantes.CARRO_DIST * (deadI - posX)) / (deadI - cte.getMaxIzquierda())) + Constantes.CARRO_CENTRO;
        }
        // Comprueba si se fren� y transforma el rango
        if (posY > deadAb) {
            if (posY > cte.getMaxAbajo()) {
                cte.setMaxAbajo(posY);
            }
            frenado = 255 - (254 * (posY - deadAb) / (cte.getMaxAbajo() - deadAb));
            if (frenado > 240)
                frenado = 0;
            // Comprueba si se aceler� y transforma el rango
        } else if (posY < deadArr) {
            if (posY < cte.getMaxArriba()) {
                cte.setMaxArriba(posY);
            }
            aceleracion = (255 * (posY - deadArr) / (cte.getMaxArriba() - deadArr));
            // Si se puls� el bot�n de retroceso, la aceleraci�n ser� negativa
            if (retroceso)
                aceleracion *= -1;
        }

        try {
            // Se env�an los resultados al servidor, indicando de paso al servidor que el cliente a�n
            // est� activo. Si el resultado es false, el servidor nos habr� rechazado por haber tardado
            // demasiado en responder.
            if (! rmi.avanzaCarro(id, aceleracion, frenado, giro)) {
                Constantes.mensaje("El servidor le ha desconectado como due�o del joystick. Int�ntelo de nuevo");
                this.stop();
            }
        } catch(RemoteException re) {
            System.out.println("Error al enviar datos al coche: " + re.getMessage());
        }
    }

    /**
     * Evento <i>joystickAxisChanged</i>. Actualiza los valores de las variables
     * <i>posX</i> y <i>posY</i>
     * @param j Joystick
     */
    public void  joystickAxisChanged(Joystick j) {
       // Si se trata de otro Joystick distinto al nuestro, directamente lo descartamos
       if (j != joy) return;
       posX = j.getX();
       posY = j.getY();
   }

   /**
    * Evento <i>joystickAxisChanged</i>. Comprueba si se puls� el bot�n de retroceso
    * @param j Joystick
    */
   public void joystickButtonChanged(Joystick j) {
       // Si se trata de otro Joystick distinto al nuestro, directamente lo descartamos
       if (j != joy) return;
       if (j.getButtons() == j.BUTTON2) {
           retroceso = true;
       } else {
           retroceso = false;
       }
   }

   /**
    * Detiene la lectura del Joystick y el env�o de datos al servidor. Adem�s, se
    * oculta el di�logo de solicitud del Joystick
    */
   public void stop() {
       detener = true;
       dsj.setVisible(false);
   }

}
