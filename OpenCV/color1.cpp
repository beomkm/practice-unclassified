#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <opencv2/objdetect/objdetect.hpp>
#include <vector>

using namespace cv;

int row = 720;
int col = 960;

extern "C" {

int process(Mat &img_input, Mat &img_result)
{
    Mat temp = img_input.clone();

    uint8_t *inData = (uint8_t*)img_input.data;
    uint8_t *resData = (uint8_t*)img_result.data;

    int cr = 255;
    int cg = 0;
    int cb = 0;

    for (int i = 0; i < row; i++) {
        for (int j = 0; j < col; j++) {
            int r = inData[(i*col + j)*4 + 0];
            int g = inData[(i*col + j)*4 + 1];
            int b = inData[(i*col + j)*4 + 2];

            if((cr-r)*(cr-r) + (cg-g)*(cg-g) + (cb-b)*(cb-b) < 9000) {
                inData[(i*col + j)*4 + 0] = 255;
                inData[(i*col + j)*4 + 1] = 255;
                inData[(i*col + j)*4 + 2] = 255;
            }
        }
    }

    return(0);
}

std::vector<Rect> found;
double startTime = 0;
double timeSum = 0;
int frames = 0;
char fpsStr[16];
cv::Point fpsPos(10, 40);
int fontFace = 3;
double fontScale = 1.5;

JNIEXPORT jint JNICALL
Java_kr_ac_koreatech_hilab_newndk_MainActivity_convertNativeLib(JNIEnv*, jobject, jlong addrInput, jlong addrResult) {

    Mat &img_input = *(Mat *) addrInput;
    Mat &img_result = *(Mat *) addrResult;

    int conv = process(img_input, img_result);
    int ret = (jint) conv;



    double interval = ((static_cast<double>(cv::getTickCount())-startTime)/cv::getTickFrequency());
    timeSum += interval;
    ++frames;
    if(timeSum>1) {
        sprintf(fpsStr, "%.2lf FPS", (double)frames/timeSum);
        timeSum = 0;
        frames = 0;
    }
    putText(img_result, fpsStr, fpsPos, fontFace, fontScale, Scalar(240, 60, 180), 2);
    startTime = static_cast<double>(cv::getTickCount());


    return ret;
}


}
