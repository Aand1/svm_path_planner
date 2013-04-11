/*
    Copyright (c) 2013, NÃ©stor Morales HernÃ¡ndez <nestor@isaatc.ull.es>
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
        * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
        * Neither the name of the <organization> nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY NÃ©stor Morales HernÃ¡ndez <nestor@isaatc.ull.es> ''AS IS'' AND ANY
    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL NÃ©stor Morales HernÃ¡ndez <nestor@isaatc.ull.es> BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


#ifndef SVMPATHPLANNING_H
#define SVMPATHPLANNING_H

#include "svm.h"
// #include "nodexyzrgb.h"

#include <string.h>
#include <fstream>

#include <pcl/point_cloud.h>
#include <pcl/common/common.h>
#include <pcl/visualization/pcl_visualizer.h>
#include <pcl/visualization/cloud_viewer.h>

#include <opencv2/opencv.hpp>

#include <vector_types.h>

#include <lemon/list_graph.h>

#define NDIMS 2

using namespace std;

namespace svmpp {

extern "C"
void launchSVMPrediction(const svm_model * &model, 
                         const unsigned int & rows, const unsigned int & cols, 
                         unsigned char * &h_data);

extern "C"
void GPUPredictWrapper(int m, int n, int k, float kernelwidth, const float *Test, 
                       const float *Svs, float * alphas,float *prediction, float beta,
                       float isregression, float * elapsed);

typedef double2 CornerLimitsType;
typedef pcl::PointXYZ PointType;
typedef pcl::PointCloud<PointType> PointCloudType;
typedef pcl::PointXYZRGB PointTypeExt;
typedef pcl::PointCloud<PointTypeExt> PointCloudTypeExt;

typedef lemon::ListGraph Graph;
typedef lemon::ListGraph::Node Node;
typedef lemon::ListGraph::Edge Edge;
typedef lemon::ListGraph::EdgeMap<double> EdgeMap;
typedef lemon::ListGraph::NodeMap<PointType> NodeMap;

class SVMPathPlanning {
    
public:
    SVMPathPlanning();
    SVMPathPlanning ( const SVMPathPlanning& other );
    virtual ~SVMPathPlanning();
    
    void testSingleProblem();
    void obtainGraphFromMap(const PointCloudType::Ptr & inputCloud, const bool & visualize);
    
    bool findShortestPath(const PointType & start, const PointType & goal,
                          PointCloudType::Ptr rtObstacles, bool visualize);
    
private:
    void loadDataFromFile ( const std::string & fileName,
                            PointCloudType::Ptr & X,
                            PointCloudType::Ptr & Y );
    
    void addLineToPointCloud(const PointType& p1, const PointType& p2, 
                             const uint8_t & r, const uint8_t & g, const uint8_t  & b,
                             PointCloudTypeExt::Ptr &linesPointCloud, double zOffset);
    void getBorderFromPointClouds (PointCloudType::Ptr & X, PointCloudType::Ptr & Y,
                                   const CornerLimitsType & minCorner, const CornerLimitsType & maxCorner, 
                                   const CornerLimitsType & interval, const cv::Size & gridSize, 
                                   const uint32_t & label, PointCloudType::Ptr & pathNodes, vector<Node> & nodeList);
    void getContoursFromSVMPrediction(const svm_model * &model, const CornerLimitsType & interval,
                                      const CornerLimitsType & minCorner, const CornerLimitsType & maxCorner,
                                      const cv::Size & gridSize, const uint32_t & label,
                                      PointCloudType::Ptr & pathNodes, vector<Node> & nodeList);
    
    void clusterize(const PointCloudType::Ptr & pointCloud, vector< PointCloudType::Ptr > & classes,
                    CornerLimitsType & minCorner, CornerLimitsType & maxCorner);
    
    void generateRNG(const PointCloudType::Ptr & pathNodes, const vector<Node> & nodeList);
    
    bool getFootPrint(const PointType & position, const PointCloudType::Ptr & rtObstacles, PointCloudType::Ptr & footprint);
    
    void filterExistingObstacles(PointCloudType::Ptr & rtObstacles);
    
    void visualizeClasses(const vector< PointCloudType::Ptr > & classes, const PointCloudType::Ptr & pathNodes,
                          const PointCloudType::Ptr & rtObstacles, const PointCloudType::Ptr & path);
        
    bool isSegmentValid(const PointType & v, const PointType & w);
    double lineToPointDistanceSqr(const PointType & v, const PointType & w, const PointType & p);
  
    struct svm_parameter m_param;
    
    double m_minPointDistance;                  // Minimal distance between samples in downsampling
    cv::Size m_mapGridSize;
    cv::Size m_mapGridSizeRT;
    double m_minDistBetweenObstacles;          // Minimal distance between obstacles in clustering
    double m_distBetweenSamples;               // Maximal distance to be considered as an edge in the graph
    
    double m_carWidth;
    double m_minDistCarObstacle;               // Minimal distance between the car and an obstacle in path finding
    
    // TODO: Revisar que se reinicializan las estructuras al cambiar de mapa
    
    PointCloudType::Ptr m_originalMap;
    PointCloudType::Ptr m_pathNodes;
    
    vector< PointCloudType::Ptr > m_classes;
    
    Graph m_graph;
    boost::shared_ptr<EdgeMap> m_distMap;
    boost::shared_ptr<NodeMap> m_nodeMap;
    vector<Node> m_nodeList;
    
    PointCloudType::Ptr m_path;
    
    CornerLimitsType m_minCorner, m_maxCorner;
    
    bool m_mapGenerated;
};
    
}

#endif // SVMPATHPLANNING_H
