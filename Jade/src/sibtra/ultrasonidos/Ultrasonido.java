
package sibtra.ultrasonidos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * 
 * @author evelio
 *
 */
public class Ultrasonido {

	double x;
	double y;
	double xRel;
	double yRel;

	double orientacion;
	double arco;
	double radio;

	public Vector datosCarro = new Vector();
	int identificador;


	public Ultrasonido() { }

	public Ultrasonido(double x, double y, double orientacion, double arco) {
		this.x = x;
		this.y = y;
		this.orientacion = orientacion;
		this.arco = arco;
	}

	public Ultrasonido(Posicion position, double orientacion, double arco) {
		x = position.x;
		y= position.y;
		this.orientacion = orientacion;
		this.arco = arco;
	}

	public Posicion dimePosicion() {
		Posicion posicion = new Posicion(x,y);
		return posicion;
	}

	public void fijaRadio(double radio) {
		this.radio = radio;
		grabarDato();
	}

	public void fijaPosicionRel(Posicion posicionRel) {
		xRel = posicionRel.x;
		yRel = posicionRel.y;
	}

	public void grabarDato() {
		datosCarro.addElement(String.valueOf(radio));
	}

	public void volcarDatos(String cadena, int indice) {
		try {
			PrintWriter fichero = new PrintWriter(
					new BufferedWriter ( new FileWriter("ultra" + indice + ".m")));
			fichero.println("u" + indice + "=["   );
			for(int i=0; i<(datosCarro.size());i++) {
				fichero.println((String)(datosCarro.elementAt(i)));
			}
			fichero.println("];");

			fichero.close();
		} catch (Exception e) {System.out.println(e);}

	}

	public void volcarDatos() {
		try {
			PrintWriter fichero = new PrintWriter(
					new BufferedWriter ( new FileWriter("ultra" + identificador + ".m")));
			fichero.println("u" + identificador + "=["   );
			for(int i=0; i<(datosCarro.size());i++) {
				fichero.println((String)(datosCarro.elementAt(i)));
			}
			fichero.println("];");

			fichero.close();
		} catch (Exception e) {System.out.println(e);}

	}

	public void fijaIdentificador(int identificador) {
		this.identificador = identificador;
	}

	public int dimeId () {
		return identificador;
	}

}
