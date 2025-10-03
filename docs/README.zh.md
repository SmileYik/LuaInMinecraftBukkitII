<p align="center">
    <img src="logo.png" alt="logo"/>
</p>

<p align="center">
    <h1 align="center">LuaInMinecraftBukkit II</h1>
</p>

<p align="center">
    <img src="https://img.shields.io/github/last-commit/SmileYik/LuaInMinecraftBukkitII?style=flat-square" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/check-runs/SmileYik/LuaInMinecraftBukkitII/master?style=flat-square" alt="GitHub branch check runs"/>
    <img src="https://img.shields.io/github/actions/workflow/status/SmileYik/LuaInMinecraftBukkitII/gradle.yml?style=flat-square" alt="GitHub Actions Workflow Status"/>
    <img src="https://img.shields.io/github/license/SmileYik/LuaInMinecraftBukkitII?style=flat-square" alt="GitHub License"/>
    <a href="https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/gh-page/docs/zh/QuickStart.md">
        <img src="https://img.shields.io/badge/DOCS-QuickStart-blue?style=flat-square" alt="Docs-QuickStart"/>
    </a>
</p>

[luajava-jasonsantos]: https://github.com/jasonsantos/luajava
[luajava-smileyik]: https://github.com/SmileYik/luajava
[lua-pool-example]: https://github.com/SmileYik/LuaInMinecraftBukkitII/blob/gh-page/docs/zh/GlobalVariable.md#pooledcallable---transform-a-lua-closure-into-a-closure-that-can-be-run-in-the-lua-pool
[jni-bridge]: ../modules/jni-bridge

## 简介

LuaInMinecraftBukkit II 是一个 Minecraft Bukkit 服务端插件, 目的是实现使用 Lua 脚本与 Bukkit 服务器交互.

Lua 是一个小巧的脚本语言, 拥有非常简单的语法, 还有着较为不错的运行速度. 
试想一下, 用一个轻巧的, 不需要编译就可以运行的脚本编写Bukkit插件将多么美好. 
如果要修改一个地方, 那就修改, 然后重新加载脚本就好了, 简直就像梦一样.

## 与上一代的区别

相比于上一代, 本代更注重于Lua原生虚拟机. 同样的, 本代也基于[luajava][luajava-jasonsantos]项目.
不过与上一代相比, 本代使用[克隆后的luajava][luajava-smileyik]仓库. 与原仓库相比, 克隆后的luajava仓库基本上重写了反射部分功能, 能够进一步简化Lua调用Java方法之间的过程, 并且在C语言端提供了非常友好的异常提示.

## 能做什么?

本插件主要有三大功能:

+ 注册指令: 注册你想要的任何指令, 并且自动生成帮助信息和指令层级关系
+ 监听事件: 监听任何你想要的Bukkit事件, 即使这个事件是其他插件的自定义事件
+ Lua池化: 将 Lua 池化后, 允许提交 Lua 闭包至其他状态机上运行, 以获取真正的多线程并行运行能力: [示例][lua-pool-example]

其他功能有:

+ 快速反射: 现在, 为 Lua 的反射组件接入了以 MethodHandle 为基础的快速反射, 相比于标准反射能快上不少.
+ 自动重载: 插件能够监控到 Lua 脚本文件的更改, 并自动重新加载脚本文件.

不过依托于Java的反射机制和动态代理机制, 目前可以实现在lua脚本中继承Java接口,
调用Java类型中的任何公开方法, 公开属性. 也就是说本插件可以动态的加载脚本, 
享受Java的子集功能. 当然, 反射也不是万能的, 还是会出现很多Lua端无法处理的情况, 
此时就需要使用Java为Lua架桥了. 不过在开发过程中我会尽量简化Lua与Java中的交互流程.

除开上述所说内容, 与第一代相同, 还能够加载C/C++所编写的动态链接库. 当然这都是
Lua 语言本身就支持的功能.

## Lua 的池化

依托于 [luajava][luajava-smileyik] 的不懈努力, 现在可以在多个线程上执行不同的 Lua 代码.

在本插件中, 每个 Lua 环境配置都拥有一个**主要的 Lua 虚拟机**. 

在没有池化 Lua 虚拟机时, **主Lua虚拟机**正在运行的同时, 
另外一个线程B需要获取**主Lua虚拟机**的运行权(如 Bukkit 事件触发时, 或 Bukkit 的异步任务)去运行
Lua 的代码(如 Lua 的闭包函数), 此时线程B需要**阻塞等待主Lua虚拟机**, 当**主Lua虚拟机**当前任务完成后, 
线程B才能获取到**主Lua虚拟机**的运行权去运行 Lua 代码; 并在等待运行完 Lua 代码后, 才能释放**主Lua虚拟机**的运行权.
假设线程B执行的代码耗时非常长, 则会导致 **主Lua虚拟机** 一直被线程B所占用, 阻塞其他 Lua 代码的执行, 直到线程B释放运行权.

而在拥有 Lua 池后, 相同的情况下, 线程B在获取到 **主Lua虚拟机** 运行权后, 
会将即将执行的Lua代码及所有相关的数据全部转移到 **其他Lua虚拟机** 中, 
转移完成后会立即释放 **主Lua虚拟机** 的运行权, 而去获取 **其他Lua虚拟机** 的运行权去运行刚刚转移的代码. 
此时线程B就能做到在获取到 **主Lua虚拟机** 运行权的极短时间内释放运行权, 
而不必等待 Lua 代码运行完毕, 以保证 **主Lua虚拟机** 能够快速响应其他调用.

这里举一个更加具体的例子, 如下代码会调用两次Bukkit的异步任务, 并且要执行的异步任务都是无限循环.

```lua
local import = require "import"
local Thread = import "java.lang.Thread"

for i = 1, 2 do
    luaBukkit.helper:asyncCall(luaBukkit.env:pooledCallable(
        function ()
            while true do
                luaBukkit.log:info(Thread:currentThread():getName() .. " async call " .. i .. "!")
                Thread:sleep(1000)
            end
        end
    ))
end
```

当启用 Lua 池, 并且容量大小为 2 时, 上述代码输出以下内容, 可以看到有多个线程的输出, 并且能够响应 `stop` 指令关闭服务器:

```
[13:14:41 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 3 - LuaInMinecraftBukkitII async call 1!
[13:14:41 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 1 - LuaInMinecraftBukkitII async call 2!
[13:14:42 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 1 - LuaInMinecraftBukkitII async call 2!
[13:14:42 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 3 - LuaInMinecraftBukkitII async call 1!
```

禁用 Lua 池功能后, 上述代码输出以下内容, 可以看到仅有一个线程的输出, 
并且虽然能够响应 `stop` 指令, 但是无法关闭服务器, 因为尝试释放 Lua 虚拟机资源时无法从死循环情况下获取到运行权:

```
[13:17:49 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
[13:17:52 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
stop
[13:17:53 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
[13:17:53 INFO]: Stopping the server
[13:17:53 INFO]: Stopping server
[13:17:53 INFO]: [LuaInMinecraftBukkitII] Disabling LuaInMinecraftBukkitII vmaster-59748d7+luajava-master-a3ddaf6+java-21
[13:17:53 INFO]: [LuaInMinecraftBukkitII] [LuaEnv default] Shutdown auto-reload.
[13:17:54 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
[13:17:55 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
[13:17:56 INFO]: [LuaInMinecraftBukkitII] Craft Scheduler Thread - 2 - LuaInMinecraftBukkitII async call 2!
```

## JNIBridge

多亏了 cffi 的加持, [JNIBridge][jni-bridge] 为此而生! 现在能够与动态链接库进行交互了.
目前 JNIBridge 还在完善中, 现支持功能有:

+ [x] Java 方法调用
+ [x] 基础类型转换

下一阶段计划支持的功能有:

- [ ] 字段访问与赋值
- [ ] 数组访问与赋值

下图是一个简单的玩家加入服务器事件, 不过事件处理逻辑由 Cpp 端处理.

![ffi-plugin](./ffi-plugin.png)

