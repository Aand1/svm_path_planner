/**
 * Paquete que contiene todas las clases relacionadas con el apartado multimedia de la aplicaci�n
 */
package carrito.media;

import carrito.configura.*;

/**
 * Clase que extiende la clase Media para adaptar la creaci�n de instancias para
 * el cliente de la aplicaci�n
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class ClienteMedia extends Media {
    /**
     * Constructor de la clase. �nicamente recibe la ubicaci�n de la librer�a
     * VLC y se la pasa ala clase padre.
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public ClienteMedia(String dllpath) {
        super(dllpath);
    }

    /**
     * A�ade una instancia para un cliente a partir de un objeto del tipo VideoCltConfig
     * @param vcc Objeto que contiene toda la descripci�n del cliente
     * @return Devuelve el identificador de la nueva instancia creada
     * @throws Lanza una excepci�n si alguno de los datos del cliente son inadecuados
     */
    public int addCliente(VideoCltConfig vcc) throws Exception {
        // Desglosa el objeto VideoCltConfig
        String ip = vcc.getIp();
        int port = vcc.getPort();
        int caching = vcc.getCaching();
        String codec = vcc.getCodec();
        int bitrate = vcc.getBitrate();
        float scale = vcc.getScale();
        String mux = vcc.getMux();
        String file = vcc.getFile();
        // Llama a la funci�n propia addCliente con el objeto ya desglosado
        return addCliente(ip, port, caching, codec, bitrate, scale, mux, file, true);
    }

    /**
     * Genera una cadena correspondiente a un comando VLC a partir de los par�metros
     * recibidos. A partir de esta cadena, crea una nueva instancia VLC
     * @param ip IP del servidor
     * @param port Puerto del servidor
     * @param caching Tama�o del buffer de recepci�n
     * @param codec C�dec del video recibido
     * @param bitrate Bitrate del video
     * @param scale Escala del video
     * @param mux Multiplexado
     * @param file Fichero en el que se va a guardar el video recibido
     * @param visible Indica si se va a mostrar el v�deo recibido
     * @return Identificador de la instancia creada
     * @throws Lanza una excepci�n si alguno de los datos es incorrecto
     */
    public int addCliente(String ip, int port, int caching, String codec,
                          int bitrate,
                          float scale, String mux, String file, boolean visible) throws
            Exception {
        String comando = "";
        if ((ip == null) || (port <= 1024)) {
            throw new Exception(
                    "IP o puerto de emisi�n inv�lidos. No se pudo continuar");
        }
        comando += "http://" + ip + ":" + port + " ";
        if (caching > Constantes.NULLINT) {
            comando += ":http-caching=" + caching + " ";
        }

        comando += ":sout=#";
        if (file != null) {
            if (codec != null) {
                int vb = 1024;
                float size = 1.0f;
                if (bitrate > Constantes.NULLINT) {
                    vb = bitrate;
                }
                if ((scale != Constantes.NULLFLOAT) && (scale >= 0.0f)) {
                    size = scale;
                }
                comando += "transcode{vcodec=" + codec + ",vb=" + vb +
                        ",scale=" + size + "}:";
            }
        }
        comando += "duplicate{";
        if (visible) {
            comando += "dst=display";
        }
        if (file != null) {
            if (mux == null) {
                throw new Exception(
                        "No se ha especificado formato de multiplexado. No se pudo continuar");
            }
            if (visible) {
                comando += ",";
            }
            comando += "dst=std{access=file,mux=" + mux + ",dst=\"" + file +
                    "\"}";
        }
        comando += "}";

        return addInstancia(comando);
    }

}
