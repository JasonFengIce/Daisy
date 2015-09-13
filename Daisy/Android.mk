LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := 

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := accessProxy android-support androidannotations ormlite-android ormlite-core spring-android-core spring-android-rest-template

LOCAL_PACKAGE_NAME := Daisy

#LOCAL_SDK_VERSION := current

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := accessProxy:libs/accessProxy.jar android-support:libs/gson-2.2.2.jar androidannotations:libs/androidannotations-2.6-api.jar ormlite-android:libs/ormlite-android-4.41.jar ormlite-core:libs/ormlite-core-4.41.jar spring-android-core:libs/spring-android-core-1.0.0.jar spring-android-rest-template:libs/spring-android-rest-template-1.0.0.jar
include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
