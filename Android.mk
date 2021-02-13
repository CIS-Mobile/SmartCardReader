ifeq ($(BUILD_AS_GUARD_PHONE), true)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(call all-java-files-under, app/src)

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := GuardMode
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PRODUCT_MODULE := true

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.appcompat_appcompat \
    androidx-constraintlayout_constraintlayout

LOCAL_STATIC_JAVA_LIBRARIES := \
    libsecurespaces \
    libsscm \
    gson

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/app/src/main/res

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml

LOCAL_USE_AAPT2 := true

include $(BUILD_PACKAGE)

endif
