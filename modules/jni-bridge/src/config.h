#ifndef __CONFIG_H
#define __CONFIG_H

#ifdef _WIN32
    #ifdef JNI_BRIDGE_EXPORTS
        #define JNI_BRIDGE_API __declspec(dllexport)
    #else
        #define JNI_BRIDGE_API __declspec(dllimport)
    #endif
#else
    #define JNI_BRIDGE_API
#endif

// target jni version
#define TARGET_JNI_VERSION JNI_VERSION_1_8

#endif // __CONFIG_H