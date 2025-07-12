#include <cstddef>
#include <jni.h>
#include <list>
#include <string>
#include <any>
#include "config.h"

#ifndef __BRIDGE_H
#define __BRIDGE_H

struct JObject;
struct JObjectProxy;

/**
 * @brief 代理方法类型.
 * 
 */
enum JObjectProxyType {
    Field,
    Method,
    Constructor
};

/**
 * @brief jobject 包装类
 * 
 */
class JNI_BRIDGE_API JObject {
public:
    const jobject instance;
public:
    JObject(jobject object);
public:
    JObjectProxy get(const std::string& name);
    JObjectProxy f(const std::string& field);
    JObjectProxy m(const std::string& method);

    std::string toString();
    std::string toString(JNIEnv* env);
    bool isNull();
    std::string getClassName();
public:
    std::string operator+(std::string str);
    std::string operator+(JObject obj);
    bool operator==(std::nullptr_t null);
    bool operator!=(std::nullptr_t null);
};

class JNI_BRIDGE_API JObjectProxy {
private:
    const jobject instance;
    const JObjectProxyType proxyType;
    std::string name;
public:
    JObjectProxy(jobject obj, JObjectProxyType pxyType, const std::string& calledName);

    template<typename... Args>
    JObject operator()(Args&&... args) const {
        if (instance == nullptr) {
            // TODO ERROR
            return JObject(nullptr);
        }
        if (proxyType == Method) {
            std::list<std::any> params;
            (params.push_back(std::forward<Args>(args)), ...);
            return JObject(call(params));
        }
        return JObject(nullptr);
    }
private:
    JObject call(std::list<std::any>& params) const;
};

#endif // __BRIDGE_H