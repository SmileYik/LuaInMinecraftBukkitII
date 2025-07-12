#include <jni.h>
#include "ffi_type.h"

#ifndef __FFI_INIT_H
#define __FFI_INIT_H

#ifdef _WIN32
    #define MY_API __declspec(dllexport)
#else
    #define MY_API
#endif

#ifdef __cplusplus
extern "C" {
#endif

MY_API
void onEnable(struct LuaBukkit luaBukkit);

MY_API
void onDisable();

#ifdef __cplusplus
}
#endif

_LuaBukkit* getLuaBukkit();

#endif // __FFI_INIT_H