#include <jni.h>
#include "kr_tibyte_ndktest2_MainActivity.h"

extern "C" {
JNIEXPORT jstring JNICALL Java_kr_tibyte_ndktest2_MainActivity_getNativeText(JNIEnv *env, jobject obj)
{
    return env->NewStringUTF("Native text");
}
}