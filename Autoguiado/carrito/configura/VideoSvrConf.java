/**
 * Paquete que contiene todas las clases descriptoras de las propiedades que
 * afectan a la aplicaci�n
 */
package carrito.configura;

import java.io.*;
import java.net.*;

/**
 * Clase descriptora de un emisor de v�deo en la aplicaci�n servidor
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class VideoSvrConf implements Serializable {
    /** Nombre del dispositivo de captura de v�deo */
    private String name = "";
    /** N�mero de dispositivo de captura de v�deo */
    private int videoDisp = Constantes.NULLINT;
    /** Ancho */
    private int width = 176;
    /** Alto */
    private int height = 144;
    /** Cach� de captura */
    private int caching = 300;
    /** FPS */
    private float fps = 25.0f;
    /** C�dec */
    private String codec = "DIV3";
    /** Bitrate */
    private int bitrate = 1024;
    /** Escala */
    private float scale = 1;
    /** Indica si se va a mostrar el v�deo en la m�quina servidor o no */
    private boolean display = false;
    /** Multiplexado */
    private String mux = "ts";
    /** Fichero destino en caso que se desee grabar lo que se est� transmitiendo */
    private String file = null;
    /** Direcci�n IP del servidor */
    private String ip = "127.0.0.1";
    /** Puerto del servidor */
    private int port = 1234;
    /** Puerto serie para modificar el Zoom de la c�mara */
    private String serial = "COM3";

    /**
     * Constructor del descriptor. Establece el nombre del dispositivo asociado
     * y el n�mero del dispositivo. Adem�s, establece una IP y un n�mero de puerto
     * por defecto
     * @param name Nombre del dispositivo de captura de v�deo asociado
     * @param videoDisp N�mero del dispositivo de captura de v�deo asociado
     */
    public VideoSvrConf(String name, int videoDisp) {
        this.name = name;
        this.videoDisp = videoDisp;
        this.port += videoDisp;
        try {
            this.ip = InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException uhe) {}
    }

    /**
     * Establece todos los par�metros del descriptor
     * @param width Ancho
     * @param height Alto
     * @param caching Valor de la cach� respecto al dispositivo de captura
     * @param fps Frames por segundo
     * @param codec C�dec
     * @param bitrate Bitrate
     * @param scale Escala
     * @param display Indica si se va a mostrar el v�deo capturado en la m�quina emisora
     * @param mux Multiplexado
     * @param file Fichero, en caso que se desee registrar lo que se est� grabando
     * @param ip Direcci�n IP del servidor
     * @param port Puerto del servidor
     * @param serial Puerto COM de control del Zoom
     */
    public void setValues(int width, int height, int caching, float fps,
                          String codec, int bitrate, float scale, boolean display, String mux,
                          String file, String ip, int port, String serial) {

        this.videoDisp = videoDisp;
        this.width = width;
        this.height = height;
        this.caching = caching;
        this.fps = fps;
        this.codec = codec;
        this.bitrate = bitrate;
        this.scale = scale;
        this.display = display;
        this.mux = mux;
        this.file = file;
        this.ip = ip;
        this.port = port;
        this.serial = serial;
    }

    /**
     * Obtiene el Bitrate
     * @return Bitrate
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Obtiene la cach� respecto al dispositivo de captura
     * @return Tama�o de la cach�
     */
    public int getCaching() {
        return caching;
    }

    /**
     * Obtiene el c�dec que se est� empleando para transformar el v�deo al formato
     * en el cual ser� enviado
     * @return C�dec usado
     */
    public String getCodec() {
        return codec;
    }

    /**
     * Indica si se va a mostrar el v�deo capturado en el servidor
     * @return true, en caso afirmativo
     */
    public boolean isDisplay() {
        return display;
    }

    /**
     * Obtiene el nombre del fichero en el que se va a grabar
     * @return Nombre del fichero
     */
    public String getFile() {
        return file;
    }

    /**
     * Obtiene el valor de FPS
     * @return Valor de FPS
     */
    public float getFps() {
        return fps;
    }

    /**
     * Obtiene la altura del v�deo
     * @return Altura del v�deo
     */
    public int getHeight() {
        return height;
    }

    /**
     * Obtiene la IP del servidor
     * @return IP del servidor
     */
    public String getIp() {
        return ip;
    }

    /**
     * Obtiene el valor de multiplexado
     * @return Multiplexado
     */
    public String getMux() {
        return mux;
    }

    /**
     * Obtiene el puerto del servidor
     * @return Puerto del servidor
     */
    public int getPort() {
        return port;
    }

    /**
     * Obtiene la escala del v�deo
     * @return Escala del v�deo
     */
    public float getScale() {
        return scale;
    }

    /**
     * Obtiene el n�mero de dispositivo en el sistema
     * @return int
     */
    public int getVideoDisp() {
        return videoDisp;
    }

    /**
     * Obtiene el ancho del v�deo
     * @return Ancho del v�deo
     */
    public int getWidth() {
        return width;
    }

    /**
     * Obtiene el nombre del dispositivo
     * @return Nombre del dispositivo
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene el puerto COM que controla el zoom de la c�mara
     * @return String
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Establece el Bitrate
     * @param bitrate Nuevo bitrate
     */
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    /**
     * Establece el tama�o de la cach� respecto al dispositivo de captura
     * @param caching Nuevo tama�o de la cach�
     */
    public void setCaching(int caching) {
        this.caching = caching;
    }

    /**
     * Establece el nuevo c�dec
     * @param codec Nuevo c�dec
     */
    public void setCodec(String codec) {
        this.codec = codec;
    }

    /**
     * Establece si el v�deo ser� mostrado en el servidor
     * @param display Indica si ser� mostrado o no
     */
    public void setDisplay(boolean display) {
        this.display = display;
    }

    /**
     * Establece el nombre del fichero en el que se grabar�
     * @param file Nuevo nombre de fichero
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Establece el valor de FPS
     * @param fps Nuevo valor de FPS
     */
    public void setFps(float fps) {
        this.fps = fps;
    }

    /**
     * Establece la altura del v�deo
     * @param height Nueva altura
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Establece la direcci�n IP del servidor
     * @param ip Nueva IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Establece el nuevo valor de multiplexado
     * @param mux Nuevo valor de multiplexado
     */
    public void setMux(String mux) {
        this.mux = mux;
    }

    /**
     * Establece el nuevo puerto del servidor
     * @param port Nuevo puerto
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Establece la escala del v�deo
     * @param scale Nueva escala
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Establece el n�mero del dispositivo
     * @param videoDisp Nuevo n�mero de dispositivo
     */
    public void setVideoDisp(int videoDisp) {
        this.videoDisp = videoDisp;
    }

    /**
     * Establece el ancho del v�deo
     * @param width Nuevo ancho
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Establece el nombre del dispositivo
     * @param name Nuevo nombre
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Establece el puerto COM de control del Zoom de las c�maras
     * @param serial Nuevo puerto COM
     */
    public void setSerial(String serial) {
        this.serial = serial;
    }

    /**
     * Devuelve el nombre del dispositivo
     * @return Nombre del dispositivo
     */
    public String toString() {
        return name;
    }
}
