#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <opencv2/objdetect/objdetect.hpp>
#include <vector>

using namespace cv;

extern "C" {

int processGray(Mat img_input, Mat &img_result)
{
    cvtColor(img_input, img_result, CV_RGBA2GRAY);
    return(0);
}

int processBin(Mat img_input, Mat &img_result)
{
    Mat temp;
    cvtColor(img_input, temp, CV_RGBA2GRAY);

    //참고 : 이미지 밝기는 0부터 255까지로, 0은 검은색, 255는 흰색.

    //params
    //1st : 원본 이미지 (그레이스케일화된 이미지만 가능)
    //2nd : 연산 결과 이미지
    //3rd : threshold값 (0~255)
    //4th : 이미지 밝기 max값
    //5th : threshold 타입

    //기본 예제
    threshold(temp, img_result, 127, 255, THRESH_BINARY);

    //낮은 threshold값 : 흰 영역 많아짐
    //threshold(temp, img_result, 64, 255, THRESH_BINARY);

    //높은 threshold값 : 검은 영역 많아짐
    //threshold(temp, img_result, 192, 255, THRESH_BINARY);

    //이미지 밝기 max값 변경 (흰색이었던 영역이 회색으로 됨)
    //threshold(temp, img_result, 127, 64, THRESH_BINARY);

    //threshold 타입 변경 : 밝기가 96 이상인 영역에 대해서만 이진화 수행
    //threshold(temp, img_result, 96, 255, THRESH_TRUNC);

    //threshold 타입 변경 : 영역 반전
    //threshold(temp, img_result, 127, 255, THRESH_BINARY_INV);

    return(0);
}


int detect(Mat& img, Mat& gray, std::vector<Rect>& found)
{
    HOGDescriptor hog;
    hog.setSVMDetector(HOGDescriptor::getDefaultPeopleDetector());

    found.clear();

    cvtColor(img, gray, CV_RGBA2GRAY);
    //HoG알고리즘 적용 전 그레이스케일화 해야함

    IplImage temp = gray;
    IplImage *grayImg = &temp;
    CvSize size = cvGetSize(grayImg);


    //이미지 depth : 몇 bit 이미지인지를 결정 (https://en.wikipedia.org/wiki/Color_depth)
    //이미지 channel : 이미지 채널, OpenCV는 4개 채널까지 지원 (Red, Green, Blue, Alpha(투명도))
    IplImage *smallImg2 = cvCreateImage(cvSize(size.width/2, size.height/2), grayImg->depth, grayImg->nChannels);
    IplImage *smallImg4 = cvCreateImage(cvSize(size.width/4, size.height/4), grayImg->depth, grayImg->nChannels);
    cvPyrDown(grayImg, smallImg2);
    cvPyrDown(smallImg2, smallImg4);
    //이미지피라미드를 이용하여 이미지를 다운스케일림 함.(고해상도의 큰 이미지에서 사람을 찾으려면 오래걸리므로.
    //cyPyrDown() 한번 할 때마다 이미지 2배 축소.
    //2번 시행함으로써 4배 축소함
    //(http://darkpgmr.tistory.com/137)

    Mat small = cvarrToMat(smallImg4);

    hog.detectMultiScale(small, found, 0, Size(8, 8), Size(0, 0), 1, 1);
    //params 참고 : (http://www.pyimagesearch.com/2015/11/16/hog-detectmultiscale-parameters-explained/)
    //1st : 대상 이미지
    //2nd : 찾은 영역들 저장할 Rect 벡터
    //3rd : 윈도우 stride, 몇칸씩 건너뛰면서 스캔할것인지 값. (위 링크 보면 gif이미지로 설명있음)
    //4th : 찾은 (사람인식) 사각형 영역의 padding
    //5th : 이미지 축소여부
    //6th : 위 사이트 설명해놓은사람도 뭔지 모르겠다고 함 =_=

    //메모리해제
    cvReleaseImage(&smallImg2);
    cvReleaseImage(&smallImg4);

    return 0;
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

    int conv = detect(img_input, img_result, found);
    int ret = (jint) conv;


    //detect()함수로 찾은 사람의 영역들 (found (Rect 벡터)) 위에 사각형을 그리는 작업.
    for (int i = 0; i < (int)found.size(); i++) {
        Rect r = found[i];
        r.x += cvRound(r.width * 0.1);
        r.y += cvRound(r.height * 0.1);
        r.width = cvRound(r.width * 0.8);
        r.height = cvRound(r.height * 0.8);
        //이미지피라미드 4배 다운 시켰기 때문에 *4를 해야 원본위치에 사각형이 보임
        r.x *= 4;
        r.y *= 4;
        r.width *= 4;
        r.height *= 4;
        rectangle(img_result, r.tl(), r.br(), Scalar(0, 255, 0), 3);
        //tl : top left
        //br : bottom right
        //Scalar : 색상
        //3 : 선굵기
    }

    //기준시각 - 현재시각 구함
    //본 ConvertNativeLib 함수는 Java에서 onCameraFrame()함수가 실행될때마다 실행되는데,
    //기본적으로 일정시간간격으로 onCameraFrame()함수가 반복호출되고,
    //본 함수 내의 코드를 실행하는데 걸리는 시간에 따라 FPS가 결정됨.
    double interval = ((static_cast<double>(cv::getTickCount())-startTime)/cv::getTickFrequency());
    timeSum += interval;
    ++frames;
    if(timeSum>1) {
        //1초당 몇 프레임이 지나갔는지 계산함 (fps : frame per second)
        sprintf(fpsStr, "%.2lf FPS", (double)frames/timeSum);
        timeSum = 0;
        frames = 0;
    }
    //fpsPos : fps표시할 위치
    //fontFace : 폰트 종류
    //fontscale : 폰트크기
    //Scalar : 색상
    // 2 : 글자굵기
    putText(img_result, fpsStr, fpsPos, fontFace, fontScale, Scalar(240, 60, 180), 2);
    startTime = static_cast<double>(cv::getTickCount());


    return ret;
}


}
