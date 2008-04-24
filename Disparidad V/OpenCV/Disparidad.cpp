#include <cv.h>
#include <highgui.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include "..\..\CapturaImagen\CapturaImagen\CapturaVLC.h"

#define MAXD 70				// Disparidad m�xima
#define MIND 15				// M�nimo valor de disparidad a tener en cuenta para detectar obst�culos

typedef struct {			// Tipo de datos para indicar los par�metros de ajuste de los diferentes algoritmos
	int filtro,
		sobel,
		umbral,
		porcentaje,
		umbralObstaculos;
} parameter;


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void printImage(IplImage *image){
	int pixelSize;				// Tipo de dato del pixel en bytes
	char *data;
	double dato;
	int signo;


	pixelSize = (image->depth & 0x0000FFFF) / 8;
	signo = (image->depth & 0xF0000000);
	
	for (int i=image->height - 1; i >= 0; i--){
		printf("Fila: %d\n", i);
		for (int j=0; j < image->width; j++){
			data = (image->imageData + (i * image->widthStep + j * pixelSize));
			switch (image->depth) {
				case IPL_DEPTH_8U:{
						printf ("%hhu ", (unsigned char)*data);
						break;
				}
				case IPL_DEPTH_8S:{
						printf ("%hhd ", *((char*)data));
						break;
				}
				case IPL_DEPTH_16U:{
						printf ("%hu ", (unsigned short int)*data);
						break;
				}
				case IPL_DEPTH_16S:{
					printf ("%hd ", *((short int*)data));	
					break;
				}
				case IPL_DEPTH_32F:{
					printf ("%f ", *((float *)data));
					break;
				}
				case IPL_DEPTH_64F:{
					printf ("%f ", *((double *)data));
					break;
				}
			}
		}
		printf("\n\n");
	}
//	printf ("Signo: %d  pixelSize: %d\n", signo, pixelSize);
	getchar();
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N: Adapta el brillo y contraste de la imagen2 a los valores de la imagen1. Primero calcula ambas 
				magnitudes para las dos im�genes y calcula la diferencia entre ellas. Esta diferencia se usa para 
				modificar el brillo y contraste de imagen2.
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void iguala1D(IplImage * img1, IplImage * img2) {
	uchar lut[256];
	CvMat* lut_mat = cvCreateMatHeader(1, 256, CV_8UC1);
    cvSetData(lut_mat, lut, 0);

	CvScalar media1, desv1;
	CvScalar media2, desv2;

	// Calcular brillo y contraste de ambas im�genes
	cvAvgSdv(img1, &media1, &desv1);
	cvAvgSdv(img2, &media2, &desv2);

	double brightness = ((media1.val[0] - media2.val[0]) * 100 / 128);
	double contrast = ((desv1.val[0] - desv2.val[0]) * 100 / 128);

    /*
     * The algorithm is by Werner D. Streidt
     * (http://visca.com/ffactory/archives/5-99/msg00021.html)
     */
    if( contrast > 0 ) {
        double delta = 127.*contrast/100;
        double a = 255./(255. - delta*2);
        double b = a*(brightness - delta);
        for(int i = 0; i < 256; i++ )
        {
            int v = cvRound(a*i + b);
            if( v < 0 )
                v = 0;
            if( v > 255 )
                v = 255;
            lut[i] = (uchar)v;
        }
    } else {
        double delta = -128.*contrast/100;
        double a = (256.-delta*2)/255.;
        double b = a*brightness + delta;
        for(int i = 0; i < 256; i++ )
        {
            int v = cvRound(a*i + b);
            if( v < 0 )
                v = 0;
            if( v > 255 )
                v = 255;
            lut[i] = (uchar)v;
        }
    }

    cvLUT(img2, img2, lut_mat);
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void preprocesado (IplImage *left, IplImage *right, int filterSize){

	iguala1D(right, left);						// Igualar brillo y contraste de ambas im�genes

	cvSmooth(left, left, CV_BLUR, filterSize, filterSize);			// Filtrar para eliminar bordes superfluos
	cvSmooth(right, right, CV_BLUR, filterSize, filterSize);

	// Cuantizar la imagen descartando los bits menos significativos
	//cvAndS(left, cvScalar(224), left);
	//cvAndS(right, cvScalar(224), right);

	//cvDilate(left, left, NULL, 1);
	//cvDilate(right, right, NULL, 1);


	//cvNamedWindow("Preprocesado Izquierda", CV_WINDOW_AUTOSIZE);
	//cvShowImage("Preprocesado Izquierda", left);
	//cvNamedWindow("Preprocesado Derecha", CV_WINDOW_AUTOSIZE);
	//cvShowImage("Preprocesado Derecha", right);
	
	
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N: Ternariza una imagen realizando previamente un filtrado para eliminar el n�mero de bordes.
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void ternarizar (IplImage *left, IplImage *right, int filterSize, int sobelSize, int th){
	IplImage *mask,				// M�scara
			 *temp,				// Imagen ternarizada temporal
			 *auxSobel,
			 *auxThreshold,
			 *aux;				
	IplImage *b;
	CvScalar mean;
	double threshold;

	cvSmooth(left, left, CV_BLUR, filterSize, filterSize);			// Filtrar para eliminar bordes superfluos
	cvSmooth(right, right, CV_BLUR, filterSize, filterSize);

	mask = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	temp = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	aux = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	auxSobel = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	auxThreshold = cvCreateImage(cvGetSize(left), IPL_DEPTH_32F, 1);
	b = cvCreateImage(cvGetSize(right), IPL_DEPTH_32F, 1);

	/* Ternarizaci�n de imagen izquierda*/
	cvNamedWindow("Preprocesado Izquierda", CV_WINDOW_AUTOSIZE);
			
	cvSobel(left, auxSobel, 1, 0, sobelSize);			// Filtrado de Sobel de bordes verticales
	
	cvSetZero(b);										// C�lculo automatizado del umbral
	cvConvertScale(auxSobel, auxThreshold, 1, 0);		// Pasar a punto flotante
	cvSquareAcc(auxThreshold, b);						// Elevar al cuadrado
	mean = cvAvg(b);									// Hallar la media
	threshold = sqrt(4 * (double)mean.val[0]);
	
	cvSet (temp, cvScalar(127));						// Inicializar imagen ternarizada

	cvSetZero (aux);
	cvCmpS(auxSobel,  - (threshold / th), mask, CV_CMP_LT);			// Construir m�scara para valores por debajo del umbral
	cvCopy(aux, temp, mask);							// Aplicar m�scara

	cvSet (aux, cvScalar(255));
	cvCmpS(auxSobel,  threshold / th, mask, CV_CMP_GT);			// Construir m�scara para valores por encima del umbral
	cvCopy(aux, temp, mask);							// Aplicar m�scara

	cvCopy(temp, left);
//cvShowImage("Preprocesado Izquierda", left);

	/* Ternarizaci�n de imagen derecha*/
	cvSobel(right, auxSobel, 1, 0, 3);						// Filtrado de Sobel de bordes verticales
	
	cvSetZero(b);											// C�lculo automatizado del umbral
	cvConvertScale(auxSobel, auxThreshold, 1, 0);			// Pasar a punto flotante
	cvSquareAcc(auxThreshold, b);
	mean = cvAvg(b);
	threshold = sqrt(4 * (double)mean.val[0]);

	cvSet (temp, cvScalar(128));									// Inicializar imagen ternarizada

	cvSetZero (aux);
	cvCmpS(auxSobel, - (threshold / th), mask, CV_CMP_LT);			// Construir la m�scara para valores por debajo del umbral
	cvCopy(aux, temp, mask);										// Aplicar m�scara

	cvSet (aux, cvScalar(255));
	cvCmpS(auxSobel,  + (threshold / th), mask, CV_CMP_GT);			// Construir m�scara para valores por encima del umbral
	cvCopy(aux, temp, mask);										// Aplicar m�scara

	cvCopy(temp, right);

	cvReleaseImage (&auxSobel);								// Liberar memoria
	cvReleaseImage (&auxThreshold);
	cvReleaseImage (&b);
	cvReleaseImage (&mask);
	cvReleaseImage (&aux);
	cvReleaseImage (&temp);
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void correlacion (IplImage *left, IplImage *right, int d, IplImage *mapa){
	int i;
	IplImage *corr,			// Auxiliar para la correlaci�n
			 *auxU,			// Auxiliar sin signo
			 *auxS, 		// Auxiliar con signo
			 *auxL,			// Imagen izquierda con signo
 			 *auxR,			// Imagen derecha con signo
			 *min,
			 *mask;			
	CvMat *kernel;			// Kernel de convoluci�n

	corr = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	auxU = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	auxS = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	min = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	mask = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);

	auxL = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	auxR = cvCreateImage(cvGetSize(left), IPL_DEPTH_16S, 1);
	auxL->origin = 1;
	auxR->origin = 1;

	cvScale(left, auxL, 1, -127);
	cvScale(right, auxR, 1, -127);

	kernel = cvCreateMat(1, 9, CV_8UC1);						// Inicializar el kernel de convoluci�n
	cvSet(kernel, cvScalar(1));
	
	for (i=d-1; i > 0 ; i--){
		cvResetImageROI(auxL);									
		cvAbs(auxL, auxS);							// La parte no solapada se deja con el original en valor absoluto

		cvSetImageROI(auxL,cvRect(i, 0, auxL->width - i, auxL->height));
		cvSetImageROI(auxR,cvRect(0, 0, (auxR->width - i), auxR->height));
	
		cvSetImageROI(auxS,cvRect(i, 0, auxS->width - i, auxS->height));

		cvAbsDiff(auxL, auxR, auxS);						// Diferencia en valor absoluto entre im�genes

		cvResetImageROI(auxS);		

		cvFilter2D(auxS, corr, kernel, cvPoint(-1, -1));	// Convoluci�n (en los bordes rellena para cubrir el kernel)

/*Construir mapa de disparidad */	
		if (i != d-1) {										
			cvMin(corr, min, min);							// Actualizar la "imagen" de m�nimos
			cvCmp(corr, min, mask, CV_CMP_EQ);				// Buscar los pixeles de la capa actual que represntan minimos
			cvSet (auxU, cvScalar(i));						// Construir imagen 
			cvCopy(auxU, mapa, mask);						// Poner al valor de la "capa" los pixeles con valor minimo 	
		} else {
			cvCopy(corr, min);								// Inicializar la imagen de m�nimos
			cvSet (mapa, cvScalar(0));						// Inicializar el mapa de disparidad
		}
	}


	cvResetImageROI(left);
	cvResetImageROI(right);

	cvReleaseImage (&corr);									// Liberar memoria
	cvReleaseImage (&auxR);											
	cvReleaseImage (&auxL);											
	cvReleaseImage (&auxS);											
	cvReleaseImage (&auxU);
	cvReleaseImage (&mask);
	cvReleaseImage (&min);

	cvReleaseMat (&kernel);
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void crearImagen (IplImage *mapa, IplImage *imagen){
	int	i, j, count;
	IplImage *mask;

	mask = cvCreateImage(cvGetSize(mapa), 8, 1);

	for (i = 0; i < mapa->height; i ++) {
		for (j = 0; j < MAXD; j++){
			cvSetImageROI(mapa, cvRect(0, i, mapa->width, 1));			// Recorrer fila a fila de la imagen
			cvSetImageROI(mask, cvRect(0, i, mapa->width, 1));
			cvCmpS(mapa, j, mask, CV_CMP_EQ);							// Ver cuantos pixeles tienen el valor de disparidad actual
			count = cvCountNonZero(mask);								// Acumularlos
			cvSet2D(imagen, i, j, cvScalar(count));						// Rellenar con la cuenta el pixel de la imagen de disparidad
		}
	}
	cvResetImageROI(mapa);	
	
	cvReleaseImage (&mask);						// Liberar memoria
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void crearImagenH (IplImage *mapa, IplImage *imagen){
	int	i, j, count;
	IplImage *mask;

	mask = cvCreateImage(cvGetSize(mapa), 8, 1);

	for (i = 0; i < mapa->width; i ++) {
		for (j = 0; j < MAXD; j++){
			cvSetImageROI(mapa, cvRect(i, 0, 1, mapa->height));			// Recorrer columna a columna de la imagen
			cvSetImageROI(mask, cvRect(i, 0, 1, mapa->height));
			cvCmpS(mapa, j, mask, CV_CMP_EQ);							// Ver cuantos pixeles tienen el valor de disparidad actual
			count = cvCountNonZero(mask);								// Acumularlos
			cvSet2D(imagen, j, i, cvScalar(count));						// Rellenar con la cuenta el pixel de la imagen de disparidad
		}
	}
	cvResetImageROI(mapa);	
	
	cvReleaseImage (&mask);						// Liberar memoria
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void lineas(IplImage *src){         
	IplImage* color_dst = cvCreateImage( cvGetSize(src), 8, 3 );
    CvMemStorage* storage = cvCreateMemStorage(0);
    CvSeq* lines = 0;
    int i;

    cvCvtColor( src, color_dst, CV_GRAY2BGR );
#if 0
        lines = cvHoughLines2( src, storage, CV_HOUGH_STANDARD, 1, CV_PI/180, 50, 0, 0 );

        for( i = 0; i < lines->total; i++ )
        {
            float* line = (float*)cvGetSeqElem(lines,i);
            float rho = line[0];
            float theta = line[1];
            CvPoint pt1, pt2;
            double a = cos(theta), b = sin(theta);
            if( fabs(a) < 0.001 )
            {
                pt1.x = pt2.x = cvRound(rho);
                pt1.y = 0;
                pt2.y = color_dst->height;
            }
            else if( fabs(b) < 0.001 )
            {
                pt1.y = pt2.y = cvRound(rho);
                pt1.x = 0;
                pt2.x = color_dst->width;
            }
            else
            {
                pt1.x = 0;
                pt1.y = cvRound(rho/b);
                pt2.x = cvRound(rho/a);
                pt2.y = 0;
            }
            cvLine( color_dst, pt1, pt2, CV_RGB(255,0,0), 3, 8 );
        }
#else
        lines = cvHoughLines2( src, storage, CV_HOUGH_PROBABILISTIC, 2, 10*CV_PI/180, 40, 30, 20 );
        
		// Habr�a que ir construyendo una estructura en la que devolver s�lo aquellas l�neas que resulten interesantes 
		// (aprovechar para separarlas en verticales, horizontales y oblicuas)
		for( i = 0; i < lines->total; i++ ) {
            CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
            if (line[0].y <= line[1].y)						// L�neas de pendiente positiva
				cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 3, 8 );
			else if (abs(line[0].x - line[1].x) < 5)		// L�neas semi-verticales
				cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 3, 8 );
			else if (abs(line[0].y - line[1].y) < 5)		// L�neas semi-horizontales
				cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 3, 8 );
        }
#endif
        cvShowImage( "Hough", color_dst );

	cvReleaseImage (&color_dst);		
	cvReleaseMemStorage(&storage);

}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
static int cmp_vert( const void* _a, const void* _b, void* userdata ) {
    CvPoint* a = (CvPoint*)_a;
    CvPoint* b = (CvPoint*)_b;
 
    int x_diff = a[1].x - b[1].x;
    return x_diff;
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void lineasV(IplImage *src, CvSeq *vertical, CvSeq *diagonal){         
	IplImage* color_dst = cvCreateImage( cvGetSize(src), 8, 3 );
    CvMemStorage* storage = cvCreateMemStorage(0);
    CvSeq* lines = 0;
	CvPoint *line;
    int i,
		nLines;

    cvCvtColor( src, color_dst, CV_GRAY2BGR );
	line = (CvPoint*) malloc (sizeof (CvPoint *));
	lines = cvHoughLines2( src, storage, CV_HOUGH_PROBABILISTIC, 2, 10*CV_PI/180, 40, 30, 20 );
        	
	nLines = lines->total;
	for (i = 0; i < nLines; i++) {
		cvSeqPopFront(lines, line);						// Sacar el primer elemento de la lista
        		
		if (line[0].x == line[1].x){					// L�neas verticales
			cvSeqPush(vertical, line);	
			cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 1, 8 );
		}else if (abs((line[0].y - line[1].y) / (line[0].x - line[1].x)) > 5){				// L�neas semi-verticales		
			cvSeqPush(vertical, line);			
			cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 1, 8 );
			//printf ("Semivertical (%d, %d)(%d, %d) m=%f\n", line[1].x, line[1].y, line[0].x, line[0].y, (float) abs((line[0].y - line[1].y) / (line[0].x - line[1].x)));
		} else if (line[0].y <= line[1].y){					// L�neas de pendiente negativa
			cvSeqPush(diagonal, line);			
		}
	}

	cvSeqSort(vertical, cmp_vert, 0);					// Ordenar las l�neas verticales
	cvShowImage ("Imagen disparidad", color_dst);
	
	cvReleaseImage (&color_dst);		
	cvReleaseMemStorage(&storage);

}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
static int cmp_hor( const void* _a, const void* _b, void* userdata ) {
    CvPoint* a = (CvPoint*)_a;
    CvPoint* b = (CvPoint*)_b;
 
    int y_diff = a[1].y - b[1].y;
    return y_diff;
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void lineasH(IplImage *src, CvSeq *horizontal, CvSeq *pendpos, CvSeq *pendneg){         
	IplImage* color_dst = cvCreateImage( cvGetSize(src), 8, 3 );
    CvMemStorage* storage = cvCreateMemStorage(0);
    CvSeq* lines = 0;
	CvPoint *line;
    int i,
		nLines;

    cvCvtColor( src, color_dst, CV_GRAY2BGR );
	line = (CvPoint*) malloc (sizeof (CvPoint *));
	lines = cvHoughLines2( src, storage, CV_HOUGH_PROBABILISTIC, 2, 10*CV_PI/180, 40, 30, 20 );
        	
	nLines = lines->total;
	for (i = 0; i < nLines; i++) {
		cvSeqPopFront(lines, line);						// Sacar el primer elemento de la lista
        		
		if (line[0].y == line[1].y){					// L�neas horizontale
			cvSeqPush(horizontal, line);			
			cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 1, 8 );
		}else if (abs((line[0].y - line[1].y) / (line[0].x - line[1].x)) < 3){				// L�neas semi-horizontales		
			cvSeqPush(horizontal, line);			
			cvLine( color_dst, line[0], line[1], CV_RGB(255,0,0), 1, 8 );
			//printf ("Semihorizontal (%d, %d)(%d, %d) m=%f\n", line[1].x, line[1].y, line[0].x, line[0].y, (float) abs((line[0].y - line[1].y) / (line[0].x - line[1].x)));
		} else if (line[0].y < line[1].y){					// L�neas de pendiente negativa
			cvSeqPush(pendpos, line);			
		} else {					// L�neas de pendiente positiva
			cvSeqPush(pendneg, line);			
		}
	}

	cvSeqSort(horizontal, cmp_hor, 0);					// Ordenar las l�neas verticales
	cvShowImage ("Imagen disparidad H", color_dst);

	cvReleaseImage (&color_dst);		
	cvReleaseMemStorage(&storage);

}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void obstaculos (IplImage *img, int th, int factor){
	int i,
		nMax,
		counts[MAXD],
		maximos[MAXD],
		greatest;
	IplImage *salida;
		
	salida = cvCreateImage(cvGetSize(img), 8, 1);
	salida->origin = 1;

	cvThreshold(img, img, th, 255, CV_THRESH_BINARY);			// Umbralizar
	
	cvSetZero(salida);
	
	greatest = 0;
	for (i = 0, nMax = 0; i < MAXD; i++){
		cvSetImageROI(img, cvRect(i, 0, 1, img->height));					// Seleccionar columna
		counts[i] = cvCountNonZero(img);									// Contar puntos
		
		if ((i > MIND) & (i < MAXD - 1)){									// S�lo buscar m�ximos entre MIND y MAXD -> Descartar objetos lejanos
			if ((counts[i]>= counts[i-1]) & (counts[i] > counts[i + 1]) & (i - 1 != maximos[nMax - 1])){
				maximos[nMax] = i;
				nMax++;
				if (counts[i] > greatest)
					greatest = counts[i];
			}
		}
		
		cvCircle(salida, cvPoint(i, counts[i]), 1, cvScalar(255));
		if (i > 0)
			cvLine(salida, cvPoint(i, counts[i-1]), cvPoint(i, counts[i]), cvScalar(255));
	}
	
	printf ("greatest = %d\n", greatest);
	for (i = 0; i < nMax; i++) {
		if (counts[maximos[i]] > greatest * factor / 100)	
			printf ("Posible obstaculo a %f m (disparidad %d)\n", (float)(0.545*425/maximos[i]), maximos[i]);
	}
	
	cvShowImage("Obstaculos", salida);

	cvResetImageROI(img);
	cvReleaseImage(&salida);
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void checkFilter (int id){
	int aux;

	aux = cvGetTrackbarPos("Filtro", "Controles");
	
	if (aux%2 == 0){
		cvSetTrackbarPos("Filtro", "Controles", aux + 1);
	}
}


/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void checkSobel (int id){
	int aux;

	aux = cvGetTrackbarPos("Sobel", "Controles");
	
	if (aux%2 == 0){
		cvSetTrackbarPos("Sobel", "Controles", aux + 1);
	}
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE: rotate
	   FUNCI�N:
	PAR�METROS: IplImage *src --> Imagen a rotar
				IplImage *dst --> Imagen de destino (debe tener las medidas apropiadas)
				double degree --> �ngulo a rotar
	  DEVUELVE: void
-----------------------------------------------------------------------------------------------------------------*/
void rotate(const IplImage *src, IplImage *dst, double degree){
	double angle = degree * CV_PI / 180.;			// angle in radian
	double a = sin(angle), b = cos(angle);			// sine and cosine of angle
	int w_src = src->width,							// dimensions of src, dst and actual needed size
		h_src = src->height;
	int w_dst = dst->width, 
		h_dst = dst->height;
	int w_rot = 0, 
		h_rot = 0;									// actual needed size
	double scale_w = 0.,							// scale factor for rotation
		scale_h = 0., 
		scale = 0.;	
	double map[6];									// map matrix for WarpAffine, stored in array
	CvMat map_matrix = cvMat(2, 3, CV_64FC1, map);
	CvPoint2D32f pt = cvPoint2D32f(w_src / 2, h_src / 2);// Rotation center needed for cv2DRotationMatrix
  
	// Make w_rot and h_rot according to phase
	w_rot = (int)(h_src * fabs(a) + w_src * fabs(b));
	h_rot = (int)(w_src * fabs(a) + h_src * fabs(b));
	scale_w = (double)w_dst / (double)w_rot;
	scale_h = (double)h_dst / (double)h_rot;
	scale = MIN(scale_w, scale_h);
	cv2DRotationMatrix(pt, -degree, scale, &map_matrix);
	
	// Adjust rotation center to dst's center
	map[2] += (w_dst - w_src) / 2;
	map[5] += (h_dst - h_src) / 2;
	cvWarpAffine( src, dst, &map_matrix, CV_INTER_LINEAR | CV_WARP_FILL_OUTLIERS, cvScalarAll(0));
}

/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE: disparity
	   FUNCI�N:
	PAR�METROS: IplImage *left    -> Imagen izquierda. Se asume una imagen RGB de 3 planos.
				IplImage *right   -> Imagen derecha. Se asume una imagen RGB de 3 planos.
				parameter adjusts -> Ajustes para los algoritmos que componen la disparidad.
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
void disparity (IplImage *left, IplImage* right, parameter adjusts){
	IplImage *izquierda,		// Imagen izquierda en escala de grises
			 *derecha,			// Imagen derecha en escala de grises
			 *mapaDisparidad,	// Mapa de disparidad
			 *imagenDisparidadH,
			 *imagenDisparidad;

	CvSeq *vertical, 
		  *diagonal,
		  *horizontal,
		  *pendpos,
		  *pendneg; 

	CvMemStorage *storageV,
				 *storageD,
				 *storageH,
				 *storageMP,
				 *storageMN;
				 
	
	mapaDisparidad = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	imagenDisparidad = cvCreateImage(cvSize(MAXD,cvGetSize(left).height), IPL_DEPTH_8U, 1);

imagenDisparidadH = cvCreateImage(cvSize(cvGetSize(left).width, MAXD), IPL_DEPTH_8U, 1);

	izquierda = cvCreateImage(cvGetSize(left), IPL_DEPTH_8U, 1);
	derecha = cvCreateImage(cvGetSize(right), IPL_DEPTH_8U, 1);

	mapaDisparidad->origin = 1;
	imagenDisparidad->origin = 1;
	
	cvCvtColor(left, izquierda, CV_RGB2GRAY);					// Pasar a escala de grises
	izquierda->origin = 1;

	cvCvtColor(right, derecha, CV_RGB2GRAY);					// Pasar a escala de grises
	derecha->origin = 1;

cvShowImage ("Izquierda", izquierda);	
cvShowImage ("Derecha", derecha);	


clock_t start = clock();
//	ternarizar (izquierda, derecha, adjusts.filtro, adjusts.sobel, adjusts.umbral);

preprocesado (izquierda, derecha, adjusts.filtro);
	correlacion (izquierda, derecha, MAXD, mapaDisparidad);
	crearImagen (mapaDisparidad, imagenDisparidad);

crearImagenH(mapaDisparidad, imagenDisparidadH);
cvThreshold(imagenDisparidadH, imagenDisparidadH, 10, 255, CV_THRESH_BINARY);			// Umbralizar

	obstaculos(imagenDisparidad, adjusts.umbralObstaculos, adjusts.porcentaje * 10);
clock_t stop = clock();
	
	storageV = cvCreateMemStorage(0);
	storageD= cvCreateMemStorage(0);
	vertical = cvCreateSeq( CV_32SC4, sizeof(CvSeq), 2 * sizeof(CvPoint), storageV);
	diagonal = cvCreateSeq( CV_32SC4, sizeof(CvSeq), 2 * sizeof(CvPoint), storageD);

	lineasV(imagenDisparidad, vertical, diagonal);

	storageH = cvCreateMemStorage(0);
	storageMP= cvCreateMemStorage(0);
	storageMN = cvCreateMemStorage(0);
	horizontal = cvCreateSeq( CV_32SC4, sizeof(CvSeq), 2 * sizeof(CvPoint), storageV);
	pendpos = cvCreateSeq( CV_32SC4, sizeof(CvSeq), 2 * sizeof(CvPoint), storageD);
	pendneg = cvCreateSeq( CV_32SC4, sizeof(CvSeq), 2 * sizeof(CvPoint), storageV);

	lineasH(imagenDisparidadH, horizontal, pendpos, pendneg);


IplImage* color_dst = cvCreateImage( cvGetSize(izquierda), 8, 3 );
//color_dst->origin = 1;
cvCvtColor( izquierda, color_dst, CV_GRAY2BGR );
CvPoint *obj;
int j;
CvPoint *hor;
CvPoint *ver;
for (int i= 0; i < horizontal->total; i ++){
	hor = (CvPoint *) cvGetSeqElem(horizontal, i);
	j = 0;
	ver = (CvPoint *) cvGetSeqElem(vertical, j);
	while (/*(ver[0].x < hor[0].y) &&*/ (j < vertical -> total)){
		j++;
		ver = (CvPoint *) cvGetSeqElem(vertical, j);
	
	if ((abs(ver[0].x - hor[0].y) < 2) || (abs(ver[0].x - hor[1].y) < 2) ||
		(abs(ver[1].x - hor[0].y) < 2) || (abs(ver[1].x - hor[1].y) < 2)){   // Coincidencia
		cvRectangle(color_dst, cvPoint(min(hor[0].x, hor[1].x), min(ver[0].y, ver[1].y)), cvPoint(max(hor[0].x, hor[1].x), max(ver[0].y, ver[1].y)), CV_RGB(255,0,0));
		//cvLine( color_dst, ver[0], ver[1], CV_RGB(255,255,0), 3, 8 );
		//cvLine( color_dst, hor[0], hor[1], CV_RGB(255,255,0), 3, 8 );
		//printf ("box (%d, %d) (%d, %d)\n", min(hor[0].x, hor[1].x),min(ver[0].y, ver[1].y) , max(hor[0].x, hor[1].x), max(ver[0].y, ver[1].y));
		//cvRectangle(color_dst, cvPoint(min(hor[0].x, hor[1].x), min(ver[0].y, ver[1].y)), cvPoint(max(ver[0].y, ver[1].y), max(hor[0].x, hor[1].x)), CV_RGB(255,255,0));
		//cvRect(ver[0].x, hor[0].y, abs(hor[0].x-hor[1].x), abs(ver[0].y-hor[1].y));
	cvShowImage( "Hough", color_dst );
	//cvWaitKey(0);
	} 
	}
}


cvShowImage( "Hough", color_dst );


	printf("%.10lf\n", (double)(stop - start)/CLOCKS_PER_SEC);

		cvShowImage ("Mapa disparidad", mapaDisparidad);	
		//cvShowImage ("Imagen disparidad", imagenDisparidad);
		//cvShowImage ("Imagen disparidad H", imagenDisparidadH);
	

	cvReleaseImage(&mapaDisparidad);
	cvReleaseImage(&imagenDisparidad);
	cvReleaseImage(&izquierda);
	cvReleaseImage(&derecha);

	cvWaitKey(1);

}



/*-----------------------------------------------------------------------------------------------------------------
		NOMBRE:
	   FUNCI�N:
	PAR�METROS:
	  DEVUELVE:
-----------------------------------------------------------------------------------------------------------------*/
int main (int argc, char* argv[]){
	IplImage *tempImage,		// Temporal para la conversi�n RGB a escala de grises
			 *izquierda,		// Imagen izquierda
			 *derecha;			// Imagen derecha

	LPWSTR *lista;
	int totalDisp = 0;
	CCapturaVLC captura;
	parameter ajustes;

	lista = captura.listaDispositivos(&totalDisp);
	
	printf("TotalDisp = %d\n", totalDisp);

	for (int i = 0; i < totalDisp; i++) {
		printf("%d: %S\n", i + 1, lista[i]);
	}

	izquierda = cvCreateImage(cvSize(320,240), IPL_DEPTH_8U, 3);
	derecha = cvCreateImage(cvSize(320,240), IPL_DEPTH_8U, 3);

	cvNamedWindow("Izquierda", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Derecha", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Obstaculos", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Hough", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Mapa disparidad", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Imagen disparidad", CV_WINDOW_AUTOSIZE);
	cvNamedWindow("Imagen disparidad H", CV_WINDOW_AUTOSIZE);

	/* Crear la ventana de controles */
	cvNamedWindow("Controles", CV_WINDOW_AUTOSIZE);
	ajustes.filtro = 3;
	ajustes.sobel = 3;
	ajustes.umbral = 4;
	ajustes.umbralObstaculos = 10;
	ajustes.porcentaje = 5;
	cvCreateTrackbar ("Filtro", "Controles", &ajustes.filtro, 21, checkFilter);
	cvCreateTrackbar ("Sobel", "Controles", &ajustes.sobel, 7, checkSobel);
	cvCreateTrackbar ("Umbral Ter", "Controles", &ajustes.umbral, 10, NULL);
	cvCreateTrackbar ("Umbral Obs", "Controles", &ajustes.umbralObstaculos, 15, NULL);
	cvCreateTrackbar ("Porcentaje", "Controles", &ajustes.porcentaje, 10, NULL);

	while(1) {
		
		//izquierda = cvLoadImage("clio_izquierda.bmp");
		izquierda = cvLoadImage("izquierda8.jpg");
		izquierda->origin = 1;
	
		//derecha = cvLoadImage("clio_derecha.bmp");
		derecha = cvLoadImage("derecha8.jpg");
		derecha->origin = 1;
	
		disparity (izquierda, derecha, ajustes);

		cvWaitKey(1);
	}
	return (0);
}
