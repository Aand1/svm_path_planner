/**
 * 
 */
package sibtra.log;

import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

import sibtra.util.SalvaMATv4;

/**
 * Objeto para registrar datos durante la ejecución para despues poder salvarlos y analizarlos.
 * En esta clase sólo se salvan los instantes de tiempo, en sus descendiantes se salvarán, además,
 * otros tipos de datos sencillos.
 * Cada logger tendrá un nombre y un objeto al que está asociado.
 * No tiene constructor accesible sino que deben ser creados a través del {@link LoggerFactory}.
 * 
 * @author alberto
 */
public class Logger {
	
	/** Vector que contendrá los instantes de tiempo en que se almacenan los datos */
	Vector<Long> tiempos=null;
	
	/** Nombre de la variable guardada en este looger */
	String nombre;
	
	/** Objeto donde está la variable*/
	Object objeto;
	
	/** Estimación del numero de muestras por segundo que se generarán en esta variable.
	 * por defecto 20 */
	int muestrasSg=20;
	
	/** Para indicar si esta activado o no*/
	boolean activado=false;
	
	/** tiempo 0 de todos los loggers */
	long t0;

	/**
	 * Asignamos los campos 
	 * @param nombre nombre de la varialbe
	 * @param clase clase a la que pertenece la variable
	 * @param muestrasSg Estimación del numero de muestras por segundo
	 * @param t0 nuestro instante inicial
	 */
	Logger(Object objeto, String nombre,long t0, int muestrasSg) {
		this.objeto=objeto;
		this.nombre=nombre;
		this.muestrasSg=muestrasSg;
		this.t0=t0;
	}
	
	/**
	 * Asignamos los campos 
	 * @param nombre nombre de la varialbe
	 * @param clase clase a la que pertenece la variable
	 */
	Logger(Object objeto, String nombre,long t0) {
		this.objeto=objeto;
		this.nombre=nombre;
		this.t0=t0;
	}
	
	/**
	 * Activa el logger y crea nuevo vector de tiempos usando duracion estimada y {@link #muestrasSg}
	 * @param duracionSg duracion estimada del experimento en segundos
	 */
	void activa(int duracionSg) {
		//TODO para borrar vector ya existente ??
		/*
		if(tiempos!=null)
			tiempos.setSize(0);
			*/
		if(tiempos==null)
			tiempos=new Vector<Long>(duracionSg*muestrasSg+10);
		activado=true;
	}
	
	/** Añade el instante de tiempo acutal */
	public void add() {
		tiempos.add(System.currentTimeMillis()-t0);
	}
	
	/** Borra los datos almacenados */
	void clear() {
		tiempos=null;
	}
	
	/** @return el minimo valor en el vector de tiempos (el tiempo actual si no hay vector aún) */
	long tiempoMin() {
		if(tiempos!=null && tiempos.size()>=1)
			return tiempos.get(0);
		else
			return System.currentTimeMillis();
	}

	/**	Devuelve String en formato para fichero octave de texto	 */	
	public String toString() {
		if(tiempos==null) return null;
		String st="# name: "+nombre+"_t\n# type: matrix\n# rows: "+tiempos.size()+"\n# columns: 1\n";
		for(int i=0; i<tiempos.size(); i++) {
			st+=String.format((Locale)null,"%d\n", tiempos.get(i));
		}
		return st;
	}

	/** Vuelca vector de tiempos a fichero MATv4 */
	void vuelcaMATv4(SalvaMATv4 smv4) throws IOException {
		smv4.vectorLongs(tiempos, nombre+"_t");
	}
}
