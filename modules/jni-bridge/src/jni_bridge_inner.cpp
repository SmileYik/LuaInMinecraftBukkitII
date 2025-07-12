#include <jni.h>
#include "jni_bridge_data.h"
#include "jni_bridge_inner.h"
/*
 * Class:     org_eu_smileyik_luaInMinecraftBukkitII_jniBridge_JNIBridge
 * Method:    initJNIBridge
 * Signature: ()V
 */
JNIEXPORT 
void JNICALL Java_org_eu_smileyik_luaInMinecraftBukkitII_jniBridge_JNIBridge_initBridge(JNIEnv* env, jclass obj) {
    initJNIEnv(env);
}