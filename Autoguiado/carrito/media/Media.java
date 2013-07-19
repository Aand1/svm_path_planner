/**
 * Paquete que contiene todas las clases relacionadas con el apartado multimedia de la aplicaci�n
 */
package carrito.media;

import java.util.*;
import java.awt.Canvas;

/**
 * Clase que automatiza todas las tareas multimedia mediante la creaci�n de una instancia
 * a partir de una serie de comandos VLC y el control posterior de dichas instancias. Muchas de
 * las funciones no se emplean en la aplicaci�n actual, pero se ha dejado preparado para su uso
 * en cualquier aplicaci�n multimedia, permitiendo as� usar todo el potencial de VLC en aplicaciones
 * Java.
 * @author N�stor Morales Hern�ndez
 * @version 1.0
 */
public class Media {

    // La librer�a es cargada al crearse la clase
    static {
        System.loadLibrary("javavlc");
    }

    private native int _endMC();
    private native int _getAncho(String nombre);
    private native int _getAlto(String nombre);
    private native int[] _getImagen(String nombre);
    public native void _saveImagenActual(String dispositivo, String nombre);
    public native void _loadImagen(String nombre);

    /**
     * M�todo nativo que carga la librer�a VLC (NOTA: En este caso nos referimos
     * a la librer�a usada por el programa VLC original que hemos usado para
     * crear la librer�a propia
     * @return Devuelve <i>true</i> si la librer�a se carg� sin problemas
     */
    private native boolean _cargaLibreria(String dllpath);

    /**
     * M�todo nativo que crea una instancia multimedia a partir de una serie de
     * par�metros incluidos en un array de Sring
     * @param cadena Array con los par�metros especificados
     * @return Devuelve el n�mero identificador de la instancia creada
     */
    private native int _creaInstancia(String cadena[]);

    /**
     * M�todo nativo que inicia la reproducci�n de una determinada instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * reproducir
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _play(int id);

    /**
     * M�todo nativo que pausa una determinada instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * pausar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _pausa(int id);

    /**
     * M�todo nativo que detiene una determinada instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * detener
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _stop(int id);

    /**
     * M�todo nativo que reproduce una instancia determinada a pantalla completa
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * reproducir
     * @return v
     */
    private native boolean _fullScreen(int id);

    /**
     * M�todo nativo que comprueba si se est� reproduciendo una instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * comprobar
     * @return Devuelve <i>true</i> si se est� reproduciendo
     */
    //private native boolean _isPlaying(int id);

    /**
     * M�todo nativo que obtiene la longitud del stream correspondiente a una determinada instancia
     * multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * comprobar
     * @return Devuelve la longitud del stream
     */
    private native int _getLength(int id);

    /**
     * M�todo nativo que vac�a el playlist de una determinada instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * vaciar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _clearPlaylist(int id);

    /**
     * M�todo nativo que obtiene el �ndice en la lista de reproducci�n actual para una determinada
     * instancia multimedia
     * @param id N�mero identificador de la instancia multimedia que se quiere
     * comprobar
     * @return �ndice actual en la lista de reproducci�n
     */
    private native int _getPlaylistIndex(int id);

    /**
     * M�todo nativo que reproduce el siguiente stream en una determinada lista de reproducci�n para
     * una determinada instancia
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _nextPlaylist(int id);

    /**
     * M�todo nativo que reproduce el stream anterior en una determinada lista de reproducci�n para
     * una determinada instancia
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _lastPlaylist(int id);

    /**
     * M�todo nativo que obtiene la longitud de la lista de reproducci�n para una instancia determinada
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Longitud de la lista de reproducci�n
     */
    private native int _getPlayListLength(int id);

    /**
     * M�todo nativo que obtiene la posici�n actual (entre 0 y 1) dentro del stream
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Posici�n actual
     */
    private native float _getPos(int id);

    /**
     * M�todo nativo que establece la posici�n (entre 0 y 1) dentro del stream
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @param pos Posici�n que se desea establecer
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setPos(int id, float pos);

    /**
     * M�todo nativo que acelera la velocidad de reproducci�n
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setFaster(int id);

    /**
     * M�todo nativo que ralentiza la velocidad de reproducci�n
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setSlower(int id);

    /**
     * M�todo nativo que obtiene el tiempo actual de reproducci�n en segundos
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Tiempo de reproducci�n actual desde el inicio de la reproducci�n (en segundos)
     */
    private native int _getTime(int id);

    /**
     * M�todo nativo que establece el tiempo actual de reproducci�n en segundos
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @param seconds Posici�n de tiempo en la que se desea pasar
     * @param relative Si est� a <i>true</i>, se empezar� a contar a partir de la posici�n actual
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setTime(int id, int seconds, boolean relative);

    /**
     * M�todo nativo que obtiene el volumen del sonido actual
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Valor del volumen actual
     */
    private native int _getVolume(int id);

    /**
     * M�todo nativo que establece el volumen del sonido actual
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @param volume Valor del volumen que se desea establecer
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setVolume(int id, int volume);

    /**
     * M�todo nativo que silencia el stream. Equivale a setVolume(0)
     * @param id N�mero identificador de la instancia multimedia sobre la que se quiere
     * actuar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _setMute(int id);

    /**
     * M�todo nativo que elimina una instancia de VLC
     * @param id N�mero identificador de la instancia multimedia que se desea eliminar
     * @return Devuelve <i>true</i> si no ha habido problemas
     */
    private native boolean _eliminaInstancia(int id);

    /**
     * M�todo nativo que libera la librer�a VLC (NOTA: En este caso nos referimos
     * a la librer�a usada por el programa VLC original que hemos usado para
     * crear la librer�a propia
     * @return Devuelve <i>true</i> si la librer�a se liber� sin problemas
     */
    private native boolean _liberaLibreria();

    /**
     * M�todo nativo que devuelve un array con todos los dispositivos conectados actualmente
     * @return Devuelve un array con el nombre de los todos los dispositivos
     * conectados actualmente de la forma: "<dispositivo>:<N�mero de dispositivo>"
     */
    private native static String[] _listaDispositivos();

    /** Lista de los dispositivos conectados actualmente */
    private String dispositivos[] = null;
    /** Vector de instancias VLC creadas (contiene los identificadores) */
    private Vector instancias = null;
    /** Path para acceder a la librer�a VLC */
    private String dllpath = "";

    /**
     * Constructor de la clase. Obtiene la ubicaci�n de la librer�a VLC y carga la
     * librer�a propia. Inicializa el vector de dispositivos conectados y cambia los
     * espacios por &nbsp;, para poder separar los comandos en cadenas separadas posteriormente.
     * Tambi�n crea el vector de instancias
     * @param dllpath Path de la librer�a VLC
     */
    public Media(String dllpath) {
        this.dllpath = dllpath;

        // Carga la librer�a JNI que hemos creado
        System.out.println("Libreria " + dllpath);
        if (!_cargaLibreria(dllpath)) {
            System.err.println("Error al cargar la libreria");
            System.exit( -1);
        }
        // Obtiene la lista de dispositivos conectados
        dispositivos = listaDispositivos();

        // Transforma los espacios en &nbsp;
        if (dispositivos != null) {
            for (int i = 0; i < dispositivos.length; i++) {
                dispositivos[i] = dispositivos[i].replaceAll(" ", "&nbsp;");
            }
        }
        // Crea un vector vac�o de instancias
        instancias = new Vector();
    }

    /**
     * Destructor de la clase. Elimina todas las instancias y libera la librer�a
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean Destruye() {
        boolean isOk = true;

        // Elimina todas las instancias
        for (int i = 0; i < instancias.size(); i++) {
            if (!_eliminaInstancia(((Integer) instancias.elementAt(i)).intValue())) {
                isOk = false;
            }
        }

        // Libera la librer�a VLC
        if (!_liberaLibreria()) {
            isOk = false;
        }

        this._endMC();

        return isOk;
    }

    /**
     * Hace una llamada al m�todo nativo _listaDispositivos() y obtiene la lista
     * de dispositivos conectados
     * @return Lista de dispositivos conectados
     */
    public static String[] listaDispositivos() {
        return _listaDispositivos();
    }

    /**
     * Utilidad para transformar una cadena en un array de Strings, que ser� pasada
     * como argumentos a la librer�a JNI
     * @param cadena Cadena a transformas
     * @return Array de String resultante
     */
    private String[] parser(String cadena) {
        String patrones[] = cadena.split(" ");
        String retorno[] = new String[patrones.length + 1];
        // El primer par�metro siempre ha de ser la ubicaci�n de la librer�a, si no
        // dar� error
        retorno[0] = dllpath;
        for (int i = 0; i < patrones.length; i++) {
            retorno[i + 1] = patrones[i].replaceAll("&nbsp;", " ");
        }
        return retorno;
    }

    /**
     * A�ade una instancia a partir de un comando
     * @param comando Comando indicado
     * @return Identificador de la instancia creada
     */
    public int addInstancia(String comando) {
      System.out.println(parser(comando)[2]);
        int id = -1;
        // Crea la instancia con el comando dividido en subcadenas
        id = _creaInstancia(parser(comando));

        // A�ade el identificador a la lista de instancias
        instancias.add(new Integer(id));

        // Devuelve el identificador de la instancia
        return id;
    }

    /**
     * A�ade una instancia a partir de un array de String
     * @param args Array de String con los distintos comandos
     * @return Devuelve el identificador de la instancia
     */
    public int addInstancia(String args[]) {
        String args2[] = new String[args.length + 1];
        // El primer par�metro siempre ha de ser la ubicaci�n de la librer�a, si no
        // dar� error
        args2[0] = dllpath;
        for (int i = 0; i < args.length; i++) {
            args2[i + 1] = args[i];
        }
        // Crea la instancia
        int id = _creaInstancia(args2);
        // A�ade el identificador a la lista de instancias
        instancias.add(new Integer(id));
        // Devuelve el identificador de la instancia
        return id;
    }

    /**
     * Reproduce todas las instancias creadas a la vez
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean playAll() {
        boolean retorno = true;

        // Recorre todas las instancias e inicia su reproducci�n
        for (int i = 0; i < instancias.size(); i++) {
            if (!play(((Integer) instancias.elementAt(i)).intValue())) {
                retorno = false;
            }
        }
        return retorno;
    }

    /**
     * Detiene todas las instancias creadas a la vez
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean stopAll() {
        boolean retorno = true;

        for (int i = 0; i < instancias.size(); i++) {
            if (!stop(((Integer) instancias.elementAt(i)).intValue())) {
                retorno = false;
            }
        }
        return retorno;
    }

    /**
     * Pausa todas las instancias creadas a la vez
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean pauseAll() {
        boolean retorno = true;

        for (int i = 0; i < instancias.size(); i++) {
            if (!stop(((Integer) instancias.elementAt(i)).intValue())) {
                retorno = false;
            }
        }
        return retorno;
    }

    /**
     * Inicia la reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean play(int id) {
        return _play(id);
    }

    /**
     * Detiene la reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean stop(int id) {
        return _stop(id);
    }

    /**
     * Hace que la reproducci�n se realice a pantalla completa
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean fullScreen(int id) {
        return _fullScreen(id);
    }
    /**
     * Pausa la reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean pausa(int id) {
        return _pausa(id);
    }

    /**
     * Comprueba si se est� reproduciendo
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si se est� reproduciendo actualmente
     */
    /*public boolean isPlaying(int id) {
        return _isPlaying(id);
    }*/
    /**
     * Obtiene el tama�o del stream
     * @param id N�mero de instancia
     * @return Devuelve el tama�o del stream
     */
    public int getLength(int id) {
        return _getLength(id);
    }

    /**
     * Vac�a la lista de reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean clearPlaylist(int id) {
        return _clearPlaylist(id);
    }

    /**
     * Obtiene el �ndice actual dentro de la lista de reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve el �ndice actual dentro de la lista de reproducci�n
     */
    public int getPlaylistIndex(int id) {
        return _getPlaylistIndex(id);
    }

    /**
     * Cambia al siguiente stream dentro de la lista de reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean nextPlaylist(int id) {
        return _nextPlaylist(id);
    }

    /**
     * Cambia al stream anterior dentro de la lista de reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean lastPlaylist(int id) {
        return _lastPlaylist(id);
    }

    /**
     * Obtiene el tama�o de la lista de reproducci�n
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public int getPlaylistLength(int id) {
        return _getPlayListLength(id);
    }

    /**
     * Obtiene la posici�n dentro del stream
     * @param id N�mero de instancia
     * @return Un n�mero entre 0.0f y 1.0f indicando la posici�n proporcional dentro del stream
     */
    public float getPos(int id) {
        return _getPos(id);
    }

    /**
     * Fija una nueva posici�n entre 0.0f y 1.0f dentro del stream actual
     * @param id N�mero de instancia
     * @param pos Nueva posici�n
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setPos(int id, float pos) {
        return _setPos(id, pos);
    }

    /**
     * Hace que la reproducci�n se acelere
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setFaster(int id) {
        return _setFaster(id);
    }

    /**
     * Hace que la reproducci�n se ralentice
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setSlower(int id) {
        return _setSlower(id);
    }

    /**
     * Obtiene la posici�n absoluta dentro del stream (en segundos)
     * @param id N�mero de instancia
     * @return Devuelve la posici�n absoluta dentro del stream (en segundos)
     */
    public int getTime(int id) {
        return _getTime(id);
    }

    /**
     * Fija la posici�n absoluta dentro del stream (en segundos)
     * @param id N�mero de instancia
     * @param seconds Nueva posici�n
     * @param relative Indica si la nueva posici�n se va a fijar a partir de la actual
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setTime(int id, int seconds, boolean relative) {
        return _setTime(id, seconds, relative);
    }

    /**
     * Obtiene el volumen actual del audio
     * @param id N�mero de instancia
     * @return Devuelve el volumen actual del audio
     */
    public int getVolume(int id) {
        return _getVolume(id);
    }

    /**
     * Establece un nuevo volumen de sonido
     * @param id N�mero de instancia
     * @param volume Nuevo volumen
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setVolume(int id, int volume) {
        return _setVolume(id, volume);
    }

    /**
     * Silencia el sonido
     * @param id N�mero de instancia
     * @return Devuelve <i>true</i> si todo ha ido bien
     */
    public boolean setMute(int id) {
        return _setMute(id);
    }

    /**
     * Getter de la propiedad <i>dispositivos</i>
     * @param i N�mero de dispositivo que se quiere conocer
     * @return Valor del dispositivo <i>i</i>
     */
    public String getDispositivo(int i) {
        return dispositivos[i];
    }

    public int getAncho(String dispositivo) {
      return _getAncho(dispositivo);
    }

    public int getAlto(String dispositivo) {
      return _getAlto(dispositivo);
    }

    public int[] getImagen(String dispositivo) {
      return _getImagen(dispositivo);
    }

    public void saveImagenActual(String dispositivo, String nombre) {
      _saveImagenActual(dispositivo, nombre);
    }

    public void loadImagen(String nombre) {
      _loadImagen(nombre);
    }
  }
