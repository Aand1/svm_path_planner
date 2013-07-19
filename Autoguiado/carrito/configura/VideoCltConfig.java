/**
 * Paquete que contiene todas las clases descriptoras de las propiedades que
 * afectan a la aplicaci�n
 */
package carrito.configura;

import java.awt.*;
import java.io.*;

/**
 * Clase descriptora de un receptor de v�deo en la aplicaci�n cliente
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class VideoCltConfig implements Serializable {
    /** Direcci�n IP del servidor de v�deo */
    private String ip = null;
    /** Puerto del servidor de v�deo */
    private int port = Constantes.NULLINT;
    /** Valor de la cach� de v�deo del cliente */
    private int caching = Constantes.NULLINT;
    /** Codec empleado para transformar el v�deo */
    private String codec = null;
    /** Bitrate empleado para transformar el v�deo */
    private int bitrate = Constantes.NULLINT;
    /** Escala empleada para transformar el v�deo */
    private float scale = Constantes.NULLFLOAT;
    /** Multiplexado empleado para transformar el v�deo */
    private String mux = null;
    /** Fichero destino de la grabaci�n */
    private String file = null;
    /** Posici�n inicial del visor de v�deo */
    private Dimension posicion = null;

    /**
     * Constructor que inicializa las variables de la clase
     * @param ip Direcci�n IP del servidor de v�deo
     * @param port Puerto del servidor de v�deo
     * @param posicion Posici�n inicial del visor de v�deo
     * @param caching Valor de la cach� de v�deo del cliente
     * @param codec Codec empleado para transformar el v�deo
     * @param bitrate Bitrate empleado para transformar el v�deo
     * @param scale Escala empleada para transformar el v�deo
     * @param mux Multiplexado empleado para transformar el v�deo
     * @param file Fichero destino de la grabaci�n
     */
    public VideoCltConfig(String ip, int port, Dimension posicion, int caching,
                          String codec,
                          int bitrate, float scale, String mux, String file) {
        this.ip = ip;
        this.port = port;
        this.posicion = posicion;
        this.caching = caching;
        this.codec = codec;
        this.bitrate = bitrate;
        this.scale = scale;
        this.mux = mux;
        this.file = file;
    }

    /**
     * Obtiene el valor de Bitrate
     * @return Bitrate empleado para transformar el v�deo
     */
    public int getBitrate() {
        return bitrate;
    }

    /**
     * Obtiene el valor de la cach� de v�deo del cliente
     * @return Devuelve el valor de la cach� de v�deo del cliente
     */
    public int getCaching() {
        return caching;
    }

    /**
     * Obtiene el codec empleado para transformar el v�deo
     * @return Codec empleado para transformar el v�deo
     */
    public String getCodec() {
        return codec;
    }

    /**
     * Obtiene el fichero destino de la grabaci�n
     * @return Fichero destino de la grabaci�n
     */
    public String getFile() {
        return file;
    }

    /**
     * Obtiene la IP del servidor de v�deo
     * @return IP del servidor de v�deo
     */
    public String getIp() {
        return ip;
    }

    /**
     * Obtiene el multiplexado empleado para transformar el v�deo
     * @return Multiplexado empleado para transformar el v�deo
     */
    public String getMux() {
        return mux;
    }

    /**
     * Obtiene el puerto del servidor de v�deo
     * @return Puerto del servidor de v�deo
     */
    public int getPort() {
        return port;
    }

    /**
     * Obtiene la escala empleada para transformar el v�deo
     * @return Escala empleada para transformar el v�deo
     */
    public float getScale() {
        return scale;
    }

    /**
     * Obtiene la posici�n inicial del visor de v�deo
     * @return Posici�n inicial del visor de v�deo
     */
    public Dimension getPosicion() {
        return posicion;
    }

    /**
     * Establece el bitrate
     * @param bitrate Nuevo bitrate
     */
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    /**
     * Establece el valor de la cach� del cliente de v�deo
     * @param caching Nuevo valor de cach�
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
     * Establece el fichero destino de la grabaci�n
     * @param file Fichero destino
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Establece la IP del servidor
     * @param ip Nueva IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Establece el valor de multiplexado
     * @param mux Nuevo multiplexado
     */
    public void setMux(String mux) {
        this.mux = mux;
    }

    /**
     * Establece el puerto del servidor
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
     * Establece la posici�n del visor de v�deo
     * @param posicion Nueva posici�n
     */
    public void setPosicion(Dimension posicion) {
        this.posicion = posicion;
    }

}
