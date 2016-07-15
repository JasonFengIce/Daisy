#include <jni.h>
#include "mac_address.h"

JNIEXPORT jstring JNICALL
Java_cn_ismartv_activator_IsmartvActivator_getMacAddress(JNIEnv *env, jobject instance) {

    char *mac_address = get_mac_address();

    return (*env)->NewStringUTF(env, mac_address);
}