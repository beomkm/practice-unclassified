
#include "kr_tibyte_ndktest_MainActivity.h"
#include <string>
#include <sstream>

using namespace std;

JNIEXPORT jstring JNICALL Java_kr_tibyte_ndktest_MainActivity_getStringFromNative(JNIEnv *env, jobject obj) {

    stringstream ss;
    for(int i=1; i<=9; i++) {
        for (int j=1; j<=9; j++) {
            int n = i*j;
            ss << " ";
            if(n < 10) ss << "0";
            ss << n;
        }
        ss << endl;
    }

    string str = ss.str();
    return env->NewStringUTF(str.c_str());
}