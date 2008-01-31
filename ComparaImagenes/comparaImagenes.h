#pragma once

#include "Hormigas.h"

#define HORIZONTE 40
#define ORIGEN 0

class comparaImagenes
{
private:
	// Array de esquinas
	CvPoint2D32f * esquinas1;
	CvPoint2D32f * esquinas2;
	
	// Tama�o de los arrays anteriores
	int cuenta;

	// Criterio para hacer el flujo optico
	CvTermCriteria criterio;

	// Imagen en perspectiva
	IplImage * persp;

	// M�scara de la perspectiva
	IplImage * perspMask;

	// Im�genes piramidales
	IplImage * prev_pyramid;
	IplImage * pyramid;

	// Matriz de transformaci�n
	CvMat * matrix;
	CvMat * matrixAnt;

	// Imagen en blanco y negro	
	IplImage * gris1 , * gris2;
	// Valores de eigen
	IplImage * eigen;
	// Imagen temporal para el flujo �ptico
	IplImage * temp;

	// Objeto encargado de aplicar el algoritmo ACO
	Hormigas * hormigas;

	// Estructura que representa la m�scara de la carretera
	tMascara pFuga;

	// M�scara de la carretera
	IplImage * mascaraCarretera;

	int ficheros;

private:
	void init(CvSize size);
	void cleanOpticalFlow(bool * estado, float * error);
	void opticalFlow(IplImage * img1, IplImage * img2, IplImage * mask);	
	void calculaCoeficientes();
	void aplicaPerspectiva(IplImage * img1, IplImage * img2);
	void iguala3D(IplImage * img1, IplImage * img2);
	void iguala1D(IplImage * img1, IplImage * img2);
	void aplicaBrilloContraste(IplImage * img1, IplImage * img2, double brightness, double contrast);
	void apaisaImagen(IplImage * img1, IplImage * img2);
	void preProcesado(IplImage * imagen);
	void restaImagenes(IplImage * img, IplImage * resta);
	void filtraImagen(IplImage * resta, IplImage * mask, IplImage * img2);
	void getCarretera(IplImage * img1);
	void liberaMem();

public:
	comparaImagenes();
	~comparaImagenes();	
	void compara(IplImage * img1, IplImage * img2, IplImage * mask, IplImage * resta);
};
