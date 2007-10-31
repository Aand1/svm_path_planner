#pragma once
#include "bufPolig.h"
#include "Hormigas.h"

class tieneObstaculos
{
private:
	int bSize, params;		// Par�metros del AdaptiveThreshold
	int puntos;				// N� de puntos del pol�gono inicial
	bufPolig buf;				// Buffers para calcular el pol�gono
	int bufSize;			// Tama�o de los buffer
	int orAnd;				// Operacion or/and
	tPoligono puntos1;		// Pol�gonos anteriores
	int umbral;						// Valor del umbral no adaptativo
		
	Hormigas * hormigas;	// Clase contenedora del algoritmo de las hormigas

	void getContornos(IplImage * img, IplImage * mascara);
	void creaMascaraDesdePoligono(IplImage * img, IplImage * mascara);
	void anadeObjetos(IplImage * imagen, IplImage * mascara);
public:
	tieneObstaculos();
	tieneObstaculos(int bSize, int params, int puntos, int bufSize);	
	IplImage * getMask(IplImage * imagen);

	// Funciones de actualizaci�n haciendo uso de los trackBar
	int updateBSize(int val);
	int updateParams(int val);
	int updateBuffer(int val);
	int updatePuntos(int val);
	int updateOrAnd(int val);
	int updateUmbral(int val);
public:
	~tieneObstaculos();
};
