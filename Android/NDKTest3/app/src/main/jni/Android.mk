LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_DEFAULT_CPP_EXTENSION := cpp
LOCAL_MODULE    := main
LOCAL_SRC_FILES := main.cpp
LOCAL_LDLIBS    := -llog -latomic
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include-all

include $(BUILD_STATIC_LIBRARY)