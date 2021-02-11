LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := SmartCardReader
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PRODUCT_MODULE := true

LOCAL_SDK_VERSION := 28

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.appcompat_appcompat \
    androidx-constraintlayout_constraintlayout

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res

LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml

LOCAL_USE_AAPT2 := true

include $(BUILD_PACKAGE)
