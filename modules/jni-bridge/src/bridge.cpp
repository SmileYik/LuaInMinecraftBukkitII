#include "bridge.h"
#include "jni.h"
#include "jni_bridge.h"

#define NULL_JOBJECT_PROXY JObjectProxy(nullptr, JObjectProxyType::Method, "");

JObject::JObject(jobject obj) : instance(obj) {

}

JObjectProxy JObject::get(const std::string& name) {
    if (0 == name.find_first_of("_m_")) {
        return JObjectProxy(instance, JObjectProxyType::Method, name.substr(3));
    }

    JNIEnv* env = getCurrentJNIEnv();
    if (env == nullptr) {
        return NULL_JOBJECT_PROXY
    }

    if (javaHasField(env, instance, name)) {
        return JObjectProxy(instance, JObjectProxyType::Field, name);
    }
    if (javaHasMethod(env, instance, name)) {
        return JObjectProxy(instance, JObjectProxyType::Method, name);
    }
    return NULL_JOBJECT_PROXY;
}

JObjectProxy JObject::f(const std::string& field) {
    JNIEnv* env = getCurrentJNIEnv();
    if (env && javaHasField(env, instance, field)) {
        return JObjectProxy(instance, JObjectProxyType::Field, field);
    }
    return NULL_JOBJECT_PROXY;
}

JObjectProxy JObject::m(const std::string& method) {
    JNIEnv* env = getCurrentJNIEnv();
    if (env && javaHasMethod(env, instance, method)) {
        return JObjectProxy(instance, JObjectProxyType::Method, method);
    }
    return NULL_JOBJECT_PROXY;
}

std::string JObject::toString() {
    if (isNull()) return "null";
    JNIEnv* env = getCurrentJNIEnv();
    std::string result = toString(env);
    destroyJNIEnv();
    return nullptr == env ? "" : result;
}

std::string JObject::toString(JNIEnv* env) {
    if (isNull()) return "null";
    return javaObject2String(env, instance);
}

bool JObject::isNull() {
    return instance == nullptr;
}

std::string JObject::getClassName() {
    if (isNull()) return "null";
    JNIEnv* env = getCurrentJNIEnv();
    if (env == nullptr) return "null";
    jclass objClazz = env->GetObjectClass(instance);
    if (!objClazz) {
        checkJNIException(env);
        return "null";
    }
    jclass clazz = env->GetObjectClass(objClazz);
    if (!clazz) {
        checkJNIException(env);
        env->DeleteLocalRef(objClazz);
        return "null";
    }
    jmethodID getNameMethod = env->GetMethodID(clazz, "getName", "()" JAVA_OBJECT_SINGLE(JAVA_STRING_CLASS));
    jstring name = (jstring) env->CallObjectMethod(objClazz, getNameMethod);
    const char* cname = env->GetStringUTFChars(name, nullptr);
    std::string ret(cname);
    
    env->ReleaseStringUTFChars(name, cname);
    env->DeleteLocalRef(clazz);
    env->DeleteLocalRef(objClazz);
    return ret;
}

std::string JObject::operator+(std::string str) {
    return toString() + str;
}

std::string JObject::operator+(JObject obj) {
    JNIEnv* env = getCurrentJNIEnv();
    if (env == nullptr) return "";
    std::string ret = toString(env) + obj.toString(env);
    destroyJNIEnv();
    return ret;
}

bool JObject::operator==(std::nullptr_t null) {
    return isNull();
}

bool JObject::operator!=(std::nullptr_t null) {
    return !isNull();
}

JObjectProxy::JObjectProxy(jobject obj, 
                            JObjectProxyType pxyType, 
                            const std::string& calledName) : 
                            instance(obj),
                            proxyType(pxyType),
                            name(calledName) {
    
}

JObject JObjectProxy::call(std::list<std::any>& params) const {
    return javaCallMethod(nullptr, instance, name, params);
}