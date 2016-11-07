#include <jni.h>
#include <vector>
#include <string>
#include "kr_tibyte_ndktest3_MainActivity.h"

using namespace std;

extern "C" {

JNIEXPORT jstring JNICALL Java_kr_tibyte_ndktest3_MainActivity_getNativeText(JNIEnv *env, jobject obj)
{
    string str = "";
    vector<char> vec;
    vec.push_back('a');
    vec.push_back('b');
    vec.push_back('c');

    for(auto& x : vec) {
        x ^= 32;
        str += x;
    }

    return env->NewStringUTF(str.c_str());

}

}