/**
 * Paquete que contiene todas las clases relacionadas con el apartado multimedia de la aplicaci�n
 */
package carrito.media;

import carrito.configura.*;

/**
 * Clase que extiende la clase Media para adaptar la creaci�n de instancias para
 * el servidor de la aplicaci�n
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class ServerMedia extends Media {
    /**
     * Constructor de la clase. �nicamente recibe la ubicaci�n de la librer�a
     * VLC y se la pasa ala clase padre.
     * @param dllpath Ubicaci�n de la librer�a VLC
     */
    public ServerMedia(String dllpath) {
        super(dllpath);
    }

    /**
     * A�ade una instancia para un servidor a partir de un objeto del tipo VideoSvrConf
     * @param vsc Objeto que contiene toda la descripci�n del servidor
     * @return Devuelve el identificador de la nueva instancia creada
     * @throws Lanza una excepci�n si alguno de los datos del servidor son inadecuados
     */
    public int addServidor(VideoSvrConf vsc)  throws Exception {
        // Desglosa el objeto VideoSvrConf
        int videoDisp = vsc.getVideoDisp();
        int width= vsc.getWidth();
        int height = vsc.getHeight();
        int caching = vsc.getCaching();
        float fps = vsc.getFps();
        String codec = vsc.getCodec();
        int bitrate = vsc.getBitrate();
        float scale = vsc.getScale();
        boolean display = vsc.isDisplay();
        String mux = vsc.getMux();
        String file = vsc.getFile();
        String ip = vsc.getIp();
        int port = vsc.getPort();
        // Llama a la funci�n propia addServidor con el objeto ya desglosado
        return addServidor(videoDisp, width, height, caching, null, fps, codec,
                    bitrate, scale, display, mux, file, ip, port);
    }

    /**
     * Genera una cadena correspondiente a un comando VLC a partir de los par�metros
     * recibidos. A partir de esta cadena, crea una nueva instancia VLC
     * @param videoDisp N�mero de dispositivo
     * @param width Ancho
     * @param height Alto
     * @param caching Tama�o de buffer de captura
     * @param croma Croma
     * @param fps FPS
     * @param codec C�dec
     * @param bitrate Bitrate
     * @param scale Escala
     * @param display Indica si se va a mostrar o no el v�deo recibido
     * @param mux Multiplexado
     * @param file Fichero donde se va a guardar el v�deo capturado
     * @param ip IP del servidor
     * @param port Puerto del servidor
     * @return Identificador de la instancia creada
     * @throws Lanza una excepci�n si alguno de los datos es incorrecto
     */
    public int addServidor(int videoDisp, int width, int height, int caching,
                           String croma, float fps,
                           String codec, int bitrate, float scale,
                           boolean display, String mux,
                           String file, String ip, int port) throws Exception {
        String comando = "";
        if (videoDisp < Constantes.NULLINT) {
            throw new Exception(
                    "No ha indicado un dispositivo. No se puede continuar");
        }

        comando += "dshow:// :dshow-vdev=" + getDispositivo(videoDisp) + ":" +
                videoDisp + " ";

        if ((width > Constantes.NULLINT + 1) && (height > Constantes.NULLINT + 1)) {
            comando += ":dshow-adev=none :dshow-size=" + width + "x" +
                    height + " ";
        }

        if (caching > Constantes.NULLINT) {
            comando += ":dshow-caching=" + caching + " ";
        }

        if (croma != null) {
            comando += ":dshow-chroma=\"" + croma + "\" ";
        }

        if ((fps != Constantes.NULLFLOAT) && (fps >= 0.0f)) {
            comando += ":dshow-fps=" + fps + " ";
        }

        comando += ":sout=#";

        if (codec != null) {
            int vb = 1024;
            float size = 1.0f;
            if (bitrate > Constantes.NULLINT) {
                vb = bitrate;
            }
            if ((scale != Constantes.NULLFLOAT) && (scale >= 0.0f)) {
                size = scale;
            }
            comando += "transcode{vcodec=" + codec + ",vb=" + vb + ",scale=" +
                    size + "}:";
        }

        comando += "duplicate{";

        if (display) {
            comando += "dst=display,";
        }

        if (mux == null) {
            throw new Exception(
                    "No se ha especificado formato de multiplexado. No se pudo continuar");
        }

        if (file != null) {
            comando += "dst=std{access=file,mux=" + mux + ",dst=\"" + file +
                    "\"},";
        }

        if ((ip == null) || (port <= 1024)) {
            throw new Exception(
                    "IP o puerto de emisi�n inv�lidos. No se pudo continuar");
        }

        comando += "dst=std{access=http,mux=" + mux + ",dst=" + ip + ":" + port +
                "}} ";

        return addInstancia(comando);

    }

}
