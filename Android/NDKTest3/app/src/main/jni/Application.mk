NDK_TOOLCHAIN_VERSION := 4.9
APP_STL := gnustl_static
APP_MODULED := main
APP_ABI := armeabi-v7a
APP_CPPFLAGS += -std=c++11
LOCAL_C_INCLUDES += ${ANDROID_NDK}/sources/cxx-stl/gnu-libstdc++/4.9/include