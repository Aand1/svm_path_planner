/* 
 * File:   CRealMatches.h
 * Author: neztol
 *
 * Created on 22 de febrero de 2010, 16:19
 */

#ifndef _CREALMATCHES_H
#define	_CREALMATCHES_H

#include "ViewMorphing.h"
#include "ImageRegistration.h"
#include "ACO/CAntColony.h"
#include <sqlite3.h>
#include <map>

#define MIN_NFEAT 8
#define MIN_DIST 8
#define MIN_DIST_SQR MIN_DIST * MIN_DIST

#define SIZE1 cvSize(800, 600)
#define SIZE2 cvSize(640, 480)
#define SIZE3 cvSize(320, 240)
#define SIZE4 cvSize(160, 120)
#define SIZE5 cvSize(315, 240)
#define SIZE6 cvSize(536, 356)

typedef struct {
    CvPoint2D32f p1;
    CvPoint2D32f p2;
} t_Pair;

typedef struct {
    CvPoint3D32f p1;
    CvPoint3D32f p2;
} t_Pair3D;

typedef struct {
    CvPoint2D32f a, b, c, d;
} t_PiecewiseTransform;

typedef struct {
    int index;
    double dist;
    CvPoint2D32f p;
} t_DistDesc;

class CRealMatches {
public:
    CRealMatches(bool usePrevious = false, CvSize sizeIn = SIZE5);
    CRealMatches(const CRealMatches& orig);
    void mainTest(IplImage * img1, IplImage * img2);
    virtual ~CRealMatches();

    void startTest(string path, string filename, string testName);
    void startTest2();
    void startTest3();
    void startTest4();
    void startTest5();
    void startTest6();
    void startTest7();
    void startTestRoadDetection();
    void onMouse1(int event, int x, int y, int flags, void * param);
    void onMouse2(int event, int x, int y, int flags, void * param);
private:    
    void getPoints(IplImage * img, vector<CvPoint2D32f> &points);
    void getOflow(IplImage * img1, IplImage * img2, vector<CvPoint2D32f> points, vector<t_Pair> &pairs);
    void cleanRANSAC(int method, vector<t_Pair> &pairs);
    void fusePairs(vector<t_Pair> pairs1, vector<t_Pair> pairs2, bool crossed);
    void paint(char * img1Name = "Img1", char * img2Name = "Img2", char * plinearName = "PLinear", char * diffName = "Resta");
    void testSurf(IplImage * img1, IplImage * img2);
    void testFast(IplImage * img, vector<CvPoint2D32f> &points);
    void remap(CImageRegistration ir);
    void setMaskFromPoints(IplImage * &mask, int index);
    void pieceWiseLinear();
    void cleanByTriangles();
    void mainTest();
    void findOFlowPairs(const IplImage * img1, const IplImage * img2, const CvPoint2D32f * origPoints, int nOrigFeat, CvPoint2D32f * &points1, CvPoint2D32f * &points2, int &numberOfFeatures);
    void oFlow(vector <CvPoint2D32f> &points1, vector <t_Pair> &pairs, IplImage * &img1, IplImage * &img2);
    void removeOutliers(CvMat **points1, CvMat **points2, CvMat *status);
    void getTriangle(CvSubdiv2D * subdiv, CvPoint2D32f point, CvPoint2D32f * &tri);
    void updatePrevious();
    void cleanPairsByDistance(vector<t_Pair> input, vector<t_Pair> &pairs);

    void wm(vector<t_Pair> pairs, IplImage * img1, IplImage * img2);
    void calculateCoefs(vector<t_Pair> pairs, double * &coefs1, double * &coefs2, map<int, vector<t_DistDesc> > distances);
    void calculateCoefsWM(vector<t_Pair> pairs, double * &coefs1, double * &coefs2, map<int, vector<t_DistDesc> > distances, int nMax);
    void calculateCoefsMQ(vector<t_Pair> pairs, double * &coefs1, double * &coefs2);
    void getValueByCoefs(CvPoint2D32f p, double &u, double &v, vector<t_Pair> pairs, map<int, vector<t_DistDesc> > distances, vector <double *> polynomialsX, vector <double *> polynomialsY);
    void getValueByCoefsWM(CvPoint2D32f p, double &u, double &v, vector<t_Pair> pairs, map<int, vector<t_DistDesc> > distances, double * coefs1, double * coefs2, int nMax);
    void getValueByCoefsMQ(CvPoint2D32f p, double &u, double &v, vector<t_Pair> pairs, double * coefs1, double * coefs2);
    void getDistances(vector<t_Pair> pairs, map<int, vector<t_DistDesc> > &distances, int nMax);
    void getDistancesVector(int index, vector<t_Pair> pairs, vector<t_DistDesc> &points, int nMax);
    void getDistancesVector(CvPoint2D32f p, vector<t_Pair> pairs, vector<t_DistDesc> &points, int nMax);
    void getPolynomials(vector<t_Pair> pairs, vector <double *> &polynomialsX, vector <double *> &polynomialsY, map<int, vector<t_DistDesc> > distances, double * coefs1, double * coefs2);
    double * getPolynoms(CvPoint3D32f o, CvPoint3D32f p1, CvPoint3D32f p2, double coef);

    void drawDelaunay(char * name1, char * name2, CvSubdiv2D * subdiv, IplImage * img, CvSize size, CvPoint2D32f currPoint);
    void drawTriangles(char * name1, char * name2, bool originPoints);

    void calcPCA(IplImage * img1, IplImage * img2, IplImage * mask);

    void changeDetection(IplImage * oldImg, IplImage * newImg);    
    void obstacleDetectionChauvenet(IplImage * pcaResult, IplImage * maskIn);
    void obstacleDetectionQuartile(IplImage * pcaResult, IplImage * maskIn);
    double normal(double x, double mean, double sdv);
    void detectObstacles(IplImage * mask);

    void checkCoveredArea(IplImage * imgA, IplImage * imgB, int &coveredArea);

    void test3D();
    void test3D_2();
    void calibrateCameras();
    CvMat * M1;
    CvMat * M2;
    CvMat * D1;
    CvMat * D2;

    CvPoint2D32f currentPoint1;
    int currentIndex1;
    vector <CvPoint2D32f> points1;
    vector <CvPoint2D32f> points2;
    vector <t_Pair> pairs;
    CvSize size;    

    IplImage * img1;
    IplImage * img2;
    IplImage * mask2;
    IplImage * mask1;
    IplImage * pointsMask;
    IplImage * plinear;

    IplImage * roadMask;

    IplImage * img1Prev;
    IplImage * img2Prev;
    vector <t_Pair> pairsPrev;
    vector <t_Pair> pairs1;
    vector <t_Pair> pairs2;
    vector <t_Pair> tmpPairs;

    bool usePrevious;

    IplImage * lastObst;
};

#endif	/* _CREALMATCHES_H */

