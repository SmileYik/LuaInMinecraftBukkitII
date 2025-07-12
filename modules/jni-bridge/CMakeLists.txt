cmake_minimum_required(VERSION 3.15)
project(jni-bridge VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(ENABLE_DEBUG)
    message(STATUS "Enable Debug")
    add_compile_definitions(ENABLE_DEBUG)
endif()

if(NOT LuaInMinecraftBukkitDir)
    set(LuaInMinecraftBukkitDir "${LuaInMinecraftBukkitDir}/../../")
endif()
message(STATUS "LuaInMinecraftBukkit Project Directory: ${LuaInMinecraftBukkitDir}")

find_package(JNI REQUIRED)
if (JNI_FOUND)
    message(STATUS "JNI Found:")
    message(STATUS "  JAVA_INCLUDE_PATH: ${JAVA_INCLUDE_PATH}")
    message(STATUS "  JAVA_INCLUDE_PATH_2: ${JAVA_INCLUDE_PATH2}")
else()
    message(FATAL_ERROR "Not found JNI, make sure you already installed JDK and configured PATH or JAVA_HOME environment variables.")
endif()

find_package(Java REQUIRED)
if (Java_FOUND)
    message(STATUS "Java Found:")
    message(STATUS "  Java_JAVAC_EXECUTABLE: ${Java_JAVAC_EXECUTABLE}")
else()
message(FATAL_ERROR "Not found Java, make sure you already installed JDK and configured PATH or JAVA_HOME environment variables.")
endif()

set(PROJECT_SOURCES
    src/jni_bridge_inner.cpp
    src/jni_bridge.cpp
    src/bridge.cpp
)
set(PROJECT_INCLUDE_DIRECTORIES
    src
    build/headers
    ${JAVA_INCLUDE_PATH}
    ${JAVA_INCLUDE_PATH2}
)

add_library(jni-bridge SHARED ${PROJECT_SOURCES})
target_include_directories(jni-bridge PUBLIC ${PROJECT_INCLUDE_DIRECTORIES})
target_compile_definitions(jni-bridge PRIVATE JNI_BRIDGE_EXPORTS)
add_custom_target(generateHeader
    WORKING_DIRECTORY "${LuaInMinecraftBukkitDir}/src/main/java"
    COMMAND ${Java_JAVAC_EXECUTABLE} -cp "${LuaInMinecraftBukkitDir}/src/main/java" -d "${PROJECT_SOURCE_DIR}/build/classes" -h "${PROJECT_SOURCE_DIR}/build/headers" "org/eu/smileyik/luaInMinecraftBukkitII/jniBridge/JNIBridge.java"
    COMMAND cmake -E copy_if_different "${PROJECT_SOURCE_DIR}/build/headers/org_eu_smileyik_luaInMinecraftBukkitII_jniBridge_JNIBridge.h" "${PROJECT_SOURCE_DIR}/build/headers/jni_bridge_inner.h"
)
add_dependencies(jni-bridge generateHeader)

set(PLUGIN_SOURCES
    src/plugin/ffi_init.cpp
    src/plugin/plugin.cpp
)

set(PLUGIN_INCLUDE_DIRECTORIES
    src
    src/plugin
    ${JAVA_INCLUDE_PATH}
    ${JAVA_INCLUDE_PATH2}
)

add_library(ffi-plugin SHARED ${PLUGIN_SOURCES})
target_include_directories(ffi-plugin PRIVATE ${PLUGIN_INCLUDE_DIRECTORIES})
target_link_libraries(ffi-plugin PRIVATE jni-bridge)