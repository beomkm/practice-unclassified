#include <jni.h>
#include <stack>
#include <string>
#include "kr_tibyte_ndktest3_MainActivity.h"

using namespace std;

extern "C" {

JNIEXPORT jstring JNICALL Java_kr_tibyte_ndktest3_MainActivity_getNativeText(JNIEnv *env, jobject obj)
{
    
    stack<int> st;
    string str = "abcd";
    return env->NewStringUTF(str.c_str());

}

}