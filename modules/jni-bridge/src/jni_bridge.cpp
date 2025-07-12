#ifndef __JNI_BRIDGE_H
#define __JNI_BRIDGE_H

#include <jni.h>
#include <stdint.h>
#include <string>
#include <vector>
#include <list>
#include <any>
#include "jni_bridge.h"
#include "jni_bridge_data.h"
#include "bridge.h"
#include "debug.h"

// set jni version to 1.8
#ifndef TARGET_JNI_VERSION
#define TARGET_JNI_VERSION JNI_VERSION_1_8
#endif

#define TO_JAVA_ARRAY(ENV, SIZE, OBJECT_TYPE, CODE) \
    jclass objectClass = OBJECT_TYPE; \
    if (!objectClass) { \
        checkJNIException(ENV); \
        return nullptr; \
    } \
    jobjectArray array = ENV->NewObjectArray(SIZE, objectClass, nullptr); \
    CODE \
    ENV->DeleteLocalRef(objectClass); \
    return array;

#define TO_JAVA_ARRAY_CUSTOM(ENV, SIZE, TYPE, NEW_INSTANCE_METHOD, CODE) \
    TYPE array = ENV->NEW_INSTANCE_METHOD(SIZE); \
    CODE \
    return array;

#define JAVA_FOREACH_LIST_AND_CLEAN(ENV, LIST, SIZE, ACCESS_ELEMENT, EXTRA_CODE) \
    JAVA_FOREACH_LIST_AND_CLEAN_CUSTOM(ENV, jobject, LIST, SIZE, SetObjectArrayElement, ACCESS_ELEMENT, EXTRA_CODE)

#define JAVA_FOREACH_LIST_AND_CLEAN_CUSTOM(ENV, TYPE, LIST, SIZE, SET_METHOD, ACCESS_ELEMENT, EXTRA_CODE) \
    size_t idx = 0; \
    TYPE obj = nullptr; \
    while (!LIST.empty()) { \
        obj = ACCESS_ELEMENT; \
        EXTRA_CODE; \
        ENV->SET_METHOD(array, idx++, obj); \
        LIST.pop_front(); \
    }

#define JAVA_FOREACH_VECTOR_AND_CLEAN(ENV, VEC, SIZE, ACCESS_ELEMENT, EXTRA_CODE) \
    JAVA_FOREACH_VECTOR_AND_CLEAN_CUSTOM(ENV, jobject, VEC, SIZE, SetObjectArrayElement, ACCESS_ELEMENT, EXTRA_CODE)

#define JAVA_FOREACH_VECTOR_AND_CLEAN_CUSTOM(ENV, TYPE, VEC, SIZE, SET_METHOD, ACCESS_ELEMENT, EXTRA_CODE) \
    TYPE obj = nullptr; \
    for (size_t i = 0; i < size; i += 1) { \
        obj = ACCESS_ELEMENT; \
        EXTRA_CODE; \
        env->SET_METHOD(array, i, obj); \
    } \
    VEC.clear()

#ifdef _WIN32
    #define LIST_TO_ARRAY(TYPE, SIZE, LIST) \
        std::vector<TYPE> typeVec; \
        typeVec.resize(SIZE); \
        while (!LIST.empty()) { \
            typeVec.push_back(LIST.front()); \
            LIST.pop_front(); \
        } \
        TYPE* typeArr = typeVec.data();

    #define VECTOR_TO_ARRAY(TYPE, SIZE, VEC) \
        std::vector<TYPE> typeVec; \
        typeVec.resize(SIZE); \
        for (size_t i = 0; i < SIZE; ++i) { \
            typeVec.push_back(VEC[i]); \
        } \
        VEC.clear(); \
        TYPE* typeArr = typeVec.data();
#else
    #define LIST_TO_ARRAY(TYPE, SIZE, LIST) \
        TYPE typeArr[SIZE]; \
        size_t i = 0; \
        while (!LIST.empty()) { \
            typeArr[i++] = LIST.front(); \
            LIST.pop_front(); \
        }

    #define VECTOR_TO_ARRAY(TYPE, SIZE, VEC) \
        TYPE typeArr[SIZE]; \
        for (size_t i = 0; i < SIZE; ++i) { \
            typeArr[i] = VEC[i]; \
        } \
        VEC.clear();
#endif

// --- JNIEnv 操作 ---

static JavaVM* javaVM = nullptr;

void initJNIEnv(JNIEnv* env) {
    if (JNI_OK != env->GetJavaVM(&javaVM)) {
        javaVM = nullptr;
    }
}

JNIEnv* getCurrentJNIEnv() {
    if (nullptr == javaVM) {
        return nullptr;
    }
    JNIEnv* env;
    if (JNI_OK == javaVM->GetEnv((void**) &env, TARGET_JNI_VERSION)) {
        return env;
    } else if (JNI_OK == javaVM->AttachCurrentThread((void**) &env, nullptr)) {
        return env;
    }
    return nullptr;
}

void destroyJNIEnv() {
    if (nullptr != javaVM) {
        javaVM->DetachCurrentThread();
    }
}

void checkJNIException(JNIEnv* env) {
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear(); 
    }
}

// --- 整数类型转换 ---

jobject toJavaByte(JNIEnv* env, int8_t value) {
    DEBUG << "[jni bridge] to java byte\n";
    jclass byteClass = env->FindClass("java/lang/Byte");
    if (!byteClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(byteClass, "<init>", "(B)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(byteClass);
        return nullptr;
    }

    jobject byteObj = env->NewObject(byteClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(byteClass);
    return byteObj;
}

jobject toJavaShort(JNIEnv* env, int16_t value) {
    DEBUG << "[jni bridge] to java short\n";
    jclass shortClass = env->FindClass("java/lang/Short");
    if (!shortClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(shortClass, "<init>", "(S)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(shortClass);
        return nullptr;
    }

    jobject shortObj = env->NewObject(shortClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(shortClass);
    return shortObj;
}

jobject toJavaInteger(JNIEnv* env, int32_t value) {
    DEBUG << "[jni bridge] to java int\n";
    jclass integerClass = env->FindClass("java/lang/Integer");
    if (!integerClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(integerClass, "<init>", "(I)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(integerClass);
        return nullptr;
    }

    jobject integerObj = env->NewObject(integerClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(integerClass);
    return integerObj;
}

jobject toJavaLong(JNIEnv* env, int64_t value) {
    DEBUG << "[jni bridge] to java long\n";
    jclass longClass = env->FindClass("java/lang/Long");
    if (!longClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(longClass, "<init>", "(J)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(longClass);
        return nullptr;
    }

    jobject longObj = env->NewObject(longClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(longClass);
    return longObj;
}

// --- 浮点类型转换 ---

jobject toJavaFloat(JNIEnv* env, float value) {
    DEBUG << "[jni bridge] to java float\n";
    jclass floatClass = env->FindClass("java/lang/Float");
    if (!floatClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(floatClass, "<init>", "(F)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(floatClass);
        return nullptr;
    }

    jobject floatObj = env->NewObject(floatClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(floatClass);
    return floatObj;
}

jobject toJavaDouble(JNIEnv* env, double value) {
    DEBUG << "[jni bridge] to java double\n";
    jclass doubleClass = env->FindClass("java/lang/Double");
    if (!doubleClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(doubleClass, "<init>", "(D)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(doubleClass);
        return nullptr;
    }

    jobject doubleObj = env->NewObject(doubleClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(doubleClass);
    return doubleObj;
}

// --- 布尔和字符类型转换 ---

jobject toJavaBoolean(JNIEnv* env, bool value) {
    DEBUG << "[jni bridge] to java boolean\n";
    jclass booleanClass = env->FindClass("java/lang/Boolean");
    if (!booleanClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(booleanClass, "<init>", "(Z)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(booleanClass);
        return nullptr;
    }

    jobject booleanObj = env->NewObject(booleanClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(booleanClass);
    return booleanObj;
}

jobject toJavaCharacter(JNIEnv* env, char16_t value) {
    DEBUG << "[jni bridge] to java char\n";
    jclass characterClass = env->FindClass("java/lang/Character");
    if (!characterClass) {
        checkJNIException(env);
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(characterClass, "<init>", "(C)V");
    if (!constructor) {
        checkJNIException(env);
        env->DeleteLocalRef(characterClass);
        return nullptr;
    }

    jobject characterObj = env->NewObject(characterClass, constructor, value);
    checkJNIException(env);
    env->DeleteLocalRef(characterClass);
    return characterObj;
}

jstring toJavaString(JNIEnv* env, const char* str) {
    DEBUG << "[jni bridge] to java string\n";
    jstring javaString = env->NewStringUTF(str);
    checkJNIException(env);
    return javaString;
}

std::string fromJavaString(JNIEnv* env, jstring jstr) {
    if (!jstr) {
        return nullptr;
    }

    const char* utf_chars = env->GetStringUTFChars(jstr, nullptr);
    if (!utf_chars) {
        checkJNIException(env);
        return "";
    }
    std::string ret(utf_chars);

    env->ReleaseStringUTFChars(jstr, utf_chars);
    checkJNIException(env);

    return ret;
}

// --- 数组类型转换 ---

jobject wrapperObjectListToJavaArray(JNIEnv* env, std::list<JObject> objs) {
    DEBUG << "[jni bridge] to java object array\n";
    size_t size = objs.size();
    jclass clazz = nullptr;
    for (auto begin = objs.begin(),
                end = objs.end(); begin != end; begin++) {
        if (begin->instance) {
            clazz = env->GetObjectClass(begin->instance);
            break;
        }
    }
    if (nullptr == clazz) {
        clazz = env->FindClass("java/lang/Object");
    }
    TO_JAVA_ARRAY(env, size, clazz, {
        JAVA_FOREACH_LIST_AND_CLEAN(env, objs, size, objs.front().instance, );
    });
}

jobject wrapperObjectVectorToJavaArray(JNIEnv* env, std::vector<JObject> objs) {
    DEBUG << "[jni bridge] to java object array.\n";
    size_t size = objs.size();
    jclass clazz = nullptr;
    for (auto begin = objs.begin(),
                end = objs.end(); begin != end; begin++) {
        if (begin->instance) {
            clazz = env->GetObjectClass(begin->instance);
            break;
        }
    }
    if (nullptr == clazz) {
        clazz = env->FindClass("java/lang/Object");
    }
    TO_JAVA_ARRAY(env, size, clazz, {
        JAVA_FOREACH_VECTOR_AND_CLEAN(env, objs, size, objs[i].instance, );
    });
}

jobject jobjectListToJavaArray(JNIEnv* env, std::list<jobject> objs) {
    DEBUG << "[jni bridge] to java object array\n";
    size_t size = objs.size();
    jclass clazz = nullptr;
    for (auto begin = objs.begin(),
                end = objs.end(); begin != end; begin++) {
        if (*begin) {
            clazz = env->GetObjectClass(*begin);
            break;
        }
    }
    if (nullptr == clazz) {
        clazz = env->FindClass("java/lang/Object");
    }
    TO_JAVA_ARRAY(env, size, clazz, {
        JAVA_FOREACH_LIST_AND_CLEAN(env, objs, size, objs.front(), );
    });
}

jobject jobjectVectorToJavaArray(JNIEnv* env, std::vector<jobject> objs) {
    DEBUG << "[jni bridge] to java object array\n";
    size_t size = objs.size();
    jclass clazz = nullptr;
    for (auto begin = objs.begin(),
                end = objs.end(); begin != end; begin++) {
        if (*begin) {
            clazz = env->GetObjectClass(*begin);
            break;
        }
    }
    if (nullptr == clazz) {
        clazz = env->FindClass("java/lang/Object");
    }
    TO_JAVA_ARRAY(env, size, clazz, {
        JAVA_FOREACH_VECTOR_AND_CLEAN(env, objs, size, objs[i], );
    });
}

jobject stringListToJavaArray(JNIEnv* env, std::list<std::string> objs, std::list<jobject>& localParamRefs) {
    DEBUG << "[jni bridge] to java string array\n";
    size_t size = objs.size();
    TO_JAVA_ARRAY(env, size, env->FindClass("java/lang/String"), {
        JAVA_FOREACH_LIST_AND_CLEAN(env, objs, size, 
            toJavaString(env, objs.front().c_str()), 
            localParamRefs.push_back(obj);
        );
    });
}

jobject stringVectorToJavaArray(JNIEnv* env, std::vector<std::string> objs, std::list<jobject>& localParamRefs) {
    DEBUG << "[jni bridge] to java string array\n";
    size_t size = objs.size();
    TO_JAVA_ARRAY(env, size, env->FindClass("java/lang/String"), {
        JAVA_FOREACH_VECTOR_AND_CLEAN(env, objs, size, 
            toJavaString(env, objs[i].c_str()), 
            localParamRefs.push_back(obj);
        );
    });
}

jobject cstringListToJavaArray(JNIEnv* env, std::list<const char *> objs, std::list<jobject>& localParamRefs) {
    DEBUG << "[jni bridge] to java string array\n";
    size_t size = objs.size();
    TO_JAVA_ARRAY(env, size, env->FindClass("java/lang/String"), {
        JAVA_FOREACH_LIST_AND_CLEAN(env, objs, size, 
            toJavaString(env, objs.front()), 
            localParamRefs.push_back(obj);
        );
    });
}

jobject cstringVectorToJavaArray(JNIEnv* env, std::vector<const char *> objs, std::list<jobject>& localParamRefs) {
    DEBUG << "[jni bridge] to java string array\n";
    size_t size = objs.size();
    TO_JAVA_ARRAY(env, size, env->FindClass("java/lang/String"), {
        JAVA_FOREACH_VECTOR_AND_CLEAN(env, objs, size, 
            toJavaString(env, objs[i]), 
            localParamRefs.push_back(obj);
        );
    });
}

jobject int8ListToJavaArray(JNIEnv* env, std::list<int8_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jbyteArray, NewByteArray, {
        LIST_TO_ARRAY(int8_t, size, objs);
        env->SetByteArrayRegion(array, 0, size, typeArr);
    });
}

jobject int8VectorToJavaArray(JNIEnv* env, std::vector<int8_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jbyteArray, NewByteArray, {
        env->SetByteArrayRegion(array, 0, size, objs.data());
    });
}

jobject boolListToJavaArray(JNIEnv* env, std::list<bool> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jbooleanArray, NewBooleanArray, {
        LIST_TO_ARRAY(jboolean, size, objs);
        env->SetBooleanArrayRegion(array, 0, size, typeArr);
    });
}

jobject boolVectorToJavaArray(JNIEnv* env, std::vector<bool> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jbooleanArray, NewBooleanArray, {
        VECTOR_TO_ARRAY(jboolean, size, objs)
        env->SetBooleanArrayRegion(array, 0, size, typeArr);
    });
}

jobject charListToJavaArray(JNIEnv* env, std::list<char16_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jcharArray, NewCharArray, {
        LIST_TO_ARRAY(jchar, size, objs);
        env->SetCharArrayRegion(array, 0, size, typeArr);
    });
}

jobject charVectorToJavaArray(JNIEnv* env, std::vector<char16_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jcharArray, NewCharArray, {
        VECTOR_TO_ARRAY(jchar, size, objs);
        env->SetCharArrayRegion(array, 0, size, typeArr);
    });
}

jobject int16ListToJavaArray(JNIEnv* env, std::list<int16_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jshortArray, NewShortArray, {
        LIST_TO_ARRAY(int16_t, size, objs);
        env->SetShortArrayRegion(array, 0, size, typeArr);
    });
}

jobject int16VectorToJavaArray(JNIEnv* env, std::vector<int16_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jshortArray, NewShortArray, {
        env->SetShortArrayRegion(array, 0, size, objs.data());
    });
}

jobject int32ListToJavaArray(JNIEnv* env, std::list<int32_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jintArray, NewIntArray, {
        LIST_TO_ARRAY(jint, size, objs);
        env->SetIntArrayRegion(array, 0, size, typeArr);
    });
}

jobject int32VectorToJavaArray(JNIEnv* env, std::vector<int32_t> objs) {

#ifdef _WIN32
    #define SET_INT_ARRAY_REGION \
        VECTOR_TO_ARRAY(jint, size, objs);\
        env->SetIntArrayRegion(array, 0, size, typeArr);
#else
    #define SET_INT_ARRAY_REGION env->SetIntArrayRegion(array, 0, size, objs.data())
#endif

    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jintArray, NewIntArray, {
        SET_INT_ARRAY_REGION;
    });
}

jobject int64ListToJavaArray(JNIEnv* env, std::list<int64_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jlongArray, NewLongArray, {
        LIST_TO_ARRAY(int64_t, size, objs);
        env->SetLongArrayRegion(array, 0, size, typeArr);
    });
}

jobject int64VectorToJavaArray(JNIEnv* env, std::vector<int64_t> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jlongArray, NewLongArray, {
        env->SetLongArrayRegion(array, 0, size, objs.data());
    });
}

jobject floatListToJavaArray(JNIEnv* env, std::list<float> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jfloatArray, NewFloatArray, {
        LIST_TO_ARRAY(float, size, objs);
        env->SetFloatArrayRegion(array, 0, size, typeArr);
    });
}

jobject floatVectorToJavaArray(JNIEnv* env, std::vector<float> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jfloatArray, NewFloatArray, {
        env->SetFloatArrayRegion(array, 0, size, objs.data());
    });
}

jobject doubleListToJavaArray(JNIEnv* env, std::list<double> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jdoubleArray, NewDoubleArray, {
        LIST_TO_ARRAY(double, size, objs);
        env->SetDoubleArrayRegion(array, 0, size, typeArr);
    });
}

jobject doubleVectorToJavaArray(JNIEnv* env, std::vector<double> objs) {
    size_t size = objs.size();
    TO_JAVA_ARRAY_CUSTOM(env, size, jdoubleArray, NewDoubleArray, {
        env->SetDoubleArrayRegion(array, 0, size, objs.data());
    });
}

// --- 实用方法 ---

std::string javaObject2String(JNIEnv* env, jobject obj) {
    jclass objectClass = env->FindClass("java/lang/Object");
    if (!objectClass) {
        checkJNIException(env);
        return "";
    }
    jmethodID methodId = env->GetMethodID(objectClass, "toString", "()Ljava/lang/String;");
    jstring ret = (jstring) env->CallObjectMethod(obj, methodId);
    std::string str = fromJavaString(env, ret);
    env->DeleteLocalRef(ret);
    env->DeleteLocalRef(objectClass);
    return str;
}

bool cppParams2JavaParams(
    JNIEnv* env, 
    std::list<std::any>& params, 
    const size_t paramCount, 
    const size_t base, 
    std::list<jobject>& localParamRefs, 
    jobjectArray& javaParams
) {
    jclass objectClass = env->FindClass("java/lang/Object");
    if (!objectClass) {
        checkJNIException(env);
        return false;
    }
    localParamRefs.push_back(objectClass);

    size_t idx = base;
    javaParams = env->NewObjectArray(paramCount - idx, objectClass, nullptr);
    localParamRefs.push_back(javaParams);
    while (idx > 0) {
        params.pop_front();
        idx -= 1;
    }
    while (!params.empty()) {
        std::any param = params.front();
        params.pop_front();
        jobject javaParam = nullptr;
        
        try {
            javaParam = std::any_cast<jobject>(param);
        } catch (const std::bad_any_cast& e) {
            if (param.has_value()) {
                printf("--- '%s' '%d'---\n", param.type().name(), 1);
                fflush(stdout);
                // jobject
                if (param.type() == typeid(JObject)) {
                    DEBUG << "[jni bridge] cast toJObject\n";
                    javaParam = std::any_cast<JObject>(param).instance;
                }
                // object array
                else if (param.type() == typeid(std::list<JObject>)) {
                    DEBUG << "[jni bridge] cast toJObject List\n";
                    javaParam = wrapperObjectListToJavaArray(env, std::any_cast<std::list<JObject>>(param));
                } else if (param.type() == typeid(std::vector<JObject>)) {
                    DEBUG << "[jni bridge] cast toJObject Vector\n";
                    javaParam = wrapperObjectVectorToJavaArray(env, std::any_cast<std::vector<JObject>>(param));
                } else if (param.type() == typeid(std::list<jobject>)) {
                    DEBUG << "[jni bridge] cast tojobject List\n";
                    javaParam = jobjectListToJavaArray(env, std::any_cast<std::list<jobject>>(param));
                } else if (param.type() == typeid(std::vector<jobject>)) {
                    DEBUG << "[jni bridge] cast tojobject Vector\n";
                    javaParam = jobjectVectorToJavaArray(env, std::any_cast<std::vector<jobject>>(param));
                } 
                // string arrays
                else if (param.type() == typeid(std::vector<std::string>)) {
                    DEBUG << "[jni bridge] cast tostd::string vector\n";
                    javaParam = stringVectorToJavaArray(env, std::any_cast<std::vector<std::string>>(param), localParamRefs);
                } else if (param.type() == typeid(std::list<std::string>)) {
                    DEBUG << "[jni bridge] cast tostd::string list\n";
                    javaParam = stringListToJavaArray(env, std::any_cast<std::list<std::string>>(param), localParamRefs);
                } else if (param.type() == typeid(std::vector<const char *>)) {
                    DEBUG << "[jni bridge] cast toconst char* vector\n";
                    javaParam = cstringVectorToJavaArray(env, std::any_cast<std::vector<const char *>>(param), localParamRefs);
                } else if (param.type() == typeid(std::list<const char *>)) {
                    DEBUG << "[jni bridge] cast toconst char* list\n";
                    javaParam = cstringListToJavaArray(env, std::any_cast<std::list<const char *>>(param), localParamRefs);
                }
                //  strings
                else if (param.type() == typeid(std::string)) {
                    DEBUG << "[jni bridge] cast tostd::string\n";
                    javaParam = toJavaString(env, std::any_cast<std::string>(param).c_str());
                } else if (param.type() == typeid(const char *)) {
                    DEBUG << "[jni bridge] cast toconst char*\n";
                    javaParam = toJavaString(env, std::any_cast<const char *>(param));
                }
                // other types
                else if (param.type() == typeid(int8_t)) {
                    javaParam = toJavaByte(env, std::any_cast<int8_t>(param));
                } else if (param.type() == typeid(int16_t)) {
                    javaParam = toJavaShort(env, std::any_cast<int16_t>(param));
                } else if (param.type() == typeid(int32_t)) {
                    javaParam = toJavaInteger(env, std::any_cast<int32_t>(param));
                } else if (param.type() == typeid(int64_t)) {
                    javaParam = toJavaLong(env, std::any_cast<int64_t>(param));
                } else if (param.type() == typeid(bool)) {
                    javaParam = toJavaBoolean(env, std::any_cast<bool>(param));
                } else if (param.type() == typeid(char16_t)) {
                    javaParam = toJavaCharacter(env, std::any_cast<char16_t>(param));
                } else if (param.type() == typeid(float)) {
                    javaParam = toJavaFloat(env, std::any_cast<float>(param));
                } else if (param.type() == typeid(double)) {
                    javaParam = toJavaDouble(env, std::any_cast<double>(param));
                }
                // other arrays
                else if (param.type() == typeid(std::list<int32_t>)) {
                    javaParam = int32ListToJavaArray(env, std::any_cast<std::list<int32_t>>(param));
                } else if (param.type() == typeid(std::vector<int32_t>)) {
                    javaParam = int32VectorToJavaArray(env, std::any_cast<std::vector<int32_t>>(param));
                } 
                else if (param.type() == typeid(std::list<int64_t>)) {
                    javaParam = int64ListToJavaArray(env, std::any_cast<std::list<int64_t>>(param));
                } else if (param.type() == typeid(std::vector<int64_t>)) {
                    javaParam = int64VectorToJavaArray(env, std::any_cast<std::vector<int64_t>>(param));
                } 
                else if (param.type() == typeid(std::list<bool>)) {
                    javaParam = boolListToJavaArray(env, std::any_cast<std::list<bool>>(param));
                } else if (param.type() == typeid(std::vector<bool>)) {
                    javaParam = boolVectorToJavaArray(env, std::any_cast<std::vector<bool>>(param));
                } 
                else if (param.type() == typeid(std::list<int8_t>)) {
                    javaParam = int8ListToJavaArray(env, std::any_cast<std::list<int8_t>>(param));
                } else if (param.type() == typeid(std::vector<int8_t>)) {
                    javaParam = int8VectorToJavaArray(env, std::any_cast<std::vector<int8_t>>(param));
                } 
                else if (param.type() == typeid(std::list<double>)) {
                    javaParam = doubleListToJavaArray(env, std::any_cast<std::list<double>>(param));
                } else if (param.type() == typeid(std::vector<double>)) {
                    javaParam = doubleVectorToJavaArray(env, std::any_cast<std::vector<double>>(param));
                } 
                else if (param.type() == typeid(std::list<float>)) {
                    javaParam = floatListToJavaArray(env, std::any_cast<std::list<float>>(param));
                } else if (param.type() == typeid(std::vector<float>)) {
                    javaParam = floatVectorToJavaArray(env, std::any_cast<std::vector<float>>(param));
                } 
                else if (param.type() == typeid(std::list<int16_t>)) {
                    javaParam = int16ListToJavaArray(env, std::any_cast<std::list<int16_t>>(param));
                } else if (param.type() == typeid(std::vector<int16_t>)) {
                    javaParam = int16VectorToJavaArray(env, std::any_cast<std::vector<int16_t>>(param));
                } 
                else if (param.type() == typeid(std::list<char16_t>)) {
                    javaParam = charListToJavaArray(env, std::any_cast<std::list<char16_t>>(param));
                } else if (param.type() == typeid(std::vector<char16_t>)) {
                    javaParam = charVectorToJavaArray(env, std::any_cast<std::vector<char16_t>>(param));
                }

                if (nullptr != javaParam) {
                    localParamRefs.push_back(javaParam);
                }
            }
        }
        env->SetObjectArrayElement(javaParams, idx, javaParam);
        checkJNIException(env); 
        idx += 1;
    }
    return true;
}

// --- 反射核心 ---

bool javaHasMethod(JNIEnv* env, jobject instance, std::string methodName) {
    jclass nativeBridgeClass = env->FindClass("org/eu/smileyik/luaInMinecraftBukkitII/reflect/ReflectUtil");
    if (!nativeBridgeClass) {
        checkJNIException(env);
        return false;
    }
    jmethodID javaCallMethodID = env->GetStaticMethodID(
        nativeBridgeClass, "hasMethod", "(Ljava/lang/Object;Ljava/lang/String;)Z");
    if (!javaCallMethodID) {
        checkJNIException(env);
        env->DeleteLocalRef(nativeBridgeClass);
        return false;
    }

    jboolean result = env->CallStaticBooleanMethod(nativeBridgeClass, javaCallMethodID,
                                                instance, toJavaString(env, methodName.c_str()));
    env->DeleteLocalRef(nativeBridgeClass);                                
    return result;
}

bool javaHasField(JNIEnv* env, jobject instance, std::string fieldName) {
    jclass nativeBridgeClass = env->FindClass("org/eu/smileyik/luaInMinecraftBukkitII/reflect/ReflectUtil");
    if (!nativeBridgeClass) {
        checkJNIException(env);
        return false;
    }
    jmethodID javaCallMethodID = env->GetStaticMethodID(
        nativeBridgeClass, "hasField", "(Ljava/lang/Object;Ljava/lang/String;)Z");
    if (!javaCallMethodID) {
        checkJNIException(env);
        env->DeleteLocalRef(nativeBridgeClass);
        return false;
    }

    jboolean result = env->CallStaticBooleanMethod(nativeBridgeClass, javaCallMethodID,
                                                instance, toJavaString(env, fieldName.c_str()));
    env->DeleteLocalRef(nativeBridgeClass);
    return result;
}

jobject javaCallMethod(JNIEnv* env, jobject instance, std::string methodName, std::list<std::any>& params) {
    size_t paramCount = params.size();
    size_t idx = 0;

    if (paramCount > 0) {
        std::any first = params.front();
        if (first.type() == typeid(JNIEnv*)) {
            env = std::any_cast<JNIEnv*>(first);
            idx = 1;
        }
    }
    if (nullptr == env) {
        env = getCurrentJNIEnv();
    }
    if (nullptr == env) {
        return nullptr;
    }

    jclass nativeBridgeClass = env->FindClass("org/eu/smileyik/luaInMinecraftBukkitII/reflect/ReflectUtil");
    if (!nativeBridgeClass) {
        checkJNIException(env);
        return nullptr;
    }
    jmethodID javaCallMethodID = env->GetStaticMethodID(
        nativeBridgeClass, "callMethod", "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
    if (!javaCallMethodID) {
        checkJNIException(env);
        env->DeleteLocalRef(nativeBridgeClass);
        return nullptr;
    }
    jobjectArray javaParams;
    std::list<jobject> localParamRefs;
    localParamRefs.push_back(nativeBridgeClass);

    if (!cppParams2JavaParams(env, params, paramCount, idx, localParamRefs, javaParams)) {
        while (!localParamRefs.empty()) {
            env->DeleteLocalRef(localParamRefs.front());
            localParamRefs.pop_front();
        }
    }
    

    jobject result = env->CallStaticObjectMethod(nativeBridgeClass, javaCallMethodID,
                                                instance, toJavaString(env, methodName.c_str()), javaParams);
    checkJNIException(env);

    while (!localParamRefs.empty()) {
        env->DeleteLocalRef(localParamRefs.front());
        localParamRefs.pop_front();
    }
    destroyJNIEnv();
    return result;
}

JNI_BRIDGE_API
bool javaCallBooleanStaticMethod(
    JNIEnv* env, 
    const std::string& clazzName, 
    const std::string& methodName, 
    const std::string& sign, 
    ...
) {
    jclass clazz = env->FindClass(clazzName.c_str());
    if (!clazz) {
        checkJNIException(env);
        return false;
    }

    jmethodID methodId = env->GetMethodID(clazz, methodName.c_str(), sign.c_str());
    if (!methodId) {
        checkJNIException(env);
        env->DeleteLocalRef(clazz);
        return false;
    }
    va_list args;
    jboolean ret = env->CallStaticBooleanMethod(clazz, methodId, args);

    env->DeleteLocalRef(clazz);
    return ret;
}

#endif // __JNI_BRIDGE_H