<p align="center">
    <img src="docs/logo.png" alt="logo"/>
</p>

<p align="center">
    <h1 align="center">LuaInMinecraftBukkit II</h1>
    <p align="center">
        English | <a href="./docs/README.zh.md">中文</a>
    </p>
</p>

<p align="center">
    <img src="https://img.shields.io/github/last-commit/SmileYik/LuaInMinecraftBukkitII?style=flat-square" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/check-runs/SmileYik/LuaInMinecraftBukkitII/master?style=flat-square" alt="GitHub branch check runs"/>
    <img src="https://img.shields.io/github/actions/workflow/status/SmileYik/LuaInMinecraftBukkitII/gradle.yml?style=flat-square" alt="GitHub Actions Workflow Status"/>
    <img src="https://img.shields.io/github/license/SmileYik/LuaInMinecraftBukkitII?style=flat-square" alt="GitHub License"/>
    <a href="https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/gh-page/docs/QuickStart.md">
        <img src="https://img.shields.io/badge/DOCS-QuickStart-blue?style=flat-square" alt="Docs-QuickStart"/>
    </a>
</p>

[luajava-jasonsantos]: https://github.com/jasonsantos/luajava
[luajava-smileyik]: https://github.com/SmileYik/luajava
[lua-pool-example]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/gh-page/docs/en/GlobalVariable.md#pooledcallable---transform-a-lua-closure-into-a-closure-that-can-be-run-in-the-lua-pool

## Introduction

LuaInMinecraftBukkit II is a Minecraft Bukkit server plugin that aims to use Lua scripts to interact with Bukkit servers.

Lua is a small scripting language with very simple syntax and decent execution speed. Just imagine how wonderful it would be to write a Bukkit plugin with a lightweight script that doesn't need to be compiled to run. If you need to modify something, you can just change it and reload the script—it's like a dream.

## Differences from the Previous Generation

Compared to the previous generation, this version focuses more on the native Lua virtual machine. Similarly, this version is also based on the [luajava][luajava-jasonsantos] project. However, this version uses a [cloned luajava][luajava-smileyik] repository. Compared to the original repository, the cloned luajava repository has fundamentally rewritten parts of the reflection functionality, further simplifying the process of Lua calling Java methods, and provides very friendly exception messages on the C language side.

## What Can It Do?

It basically supports three major functions:

+ **Command Registration**: Register any command you want, and it will automatically generate help information and command hierarchy.
+ **Event Listening**: Listen for any Bukkit event you want, even if it's a custom event from another plugin.
+ **Lua Pooling**: By pooling Lua, you can submit Lua closures to run on other state machines to achieve true multi-threaded parallel execution: [Example][lua-pool-example].

However, relying on Java's reflection and dynamic proxy mechanisms, it is currently possible to inherit Java interfaces and call any public method or public attribute of a Java type in a Lua script. This means this plugin can dynamically load scripts and enjoy a subset of Java's functionality. Of course, reflection isn't a silver bullet, and there will still be many situations that cannot be handled on the Lua side. In such cases, Java needs to bridge the gap for Lua. But in the development process, I will try to simplify the interaction between Lua and Java.

In addition to what has been mentioned, similar to the first generation, it can also load dynamic link libraries written in C/C++. Of course, these are features that the Lua language itself supports.

## JNIBridge

Thanks to the support of cffi, JNIBridge was born\! Now it's possible to interact with dynamic link libraries. JNIBridge is still being improved, but currently supports:

+ [x] Java method invocation
+ [x] Basic type conversion

Features planned for the next stage include:

- [ ] Field access and assignment
- [ ] Array access and assignment

The image below shows a simple player join event, but the event handling logic is managed on the Cpp side.

![ffi-plugin](./docs/ffi-plugin.png)
