#include <jni.h>

#ifndef __FFI_TYPE_H
#define __FFI_TYPE_H

typedef struct LuaBukkit LuaBukkit;
typedef struct _LuaBukkit _LuaBukkit;

#ifdef __cplusplus
extern "C" {
#endif

// luaBukkit table.
struct LuaBukkit {
    void* env;
    void* helper;
    void* io;
    void* bukkit;
    void* plugin;
    void* server;
    void* log;
    void* out;
};

struct _LuaBukkit {
    jobject* env;
    jobject* helper;
    jobject* io;
    jobject* bukkit;
    jobject* plugin;
    jobject* server;
    jobject* log;
    jobject* out;
};

#ifdef __cplusplus
}
#endif

#endif // __FFI_TYPE_H