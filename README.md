<p align="center">
    <img src="docs/logo.png" alt="logo"/>
</p>

<p align="center">
    <h1 align="center">LuaInMinecraftBukkit II</h1>
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

## 简介

LuaInMinecraftBukkit II 是一个 Minecraft Bukkit 服务端插件, 目的是实现使用 Lua 脚本与Bukkit服务器交互.

Lua 是一个小巧的脚本语言, 拥有非常简单的语法, 还有着较为不错的运行速度. 
试想一下, 用一个轻巧的, 不需要编译就可以运行的脚本编写Bukkit插件将多么美好. 
如果要修改一个地方, 那就修改, 然后重新加载脚本就好了, 简直就像梦一样.

## 与上一代的区别

相比于上一代, 本代更注重于Lua原生虚拟机. 同样的, 本代也基于[luajava](https://github.com/jasonsantos/luajava)项目.
不过与上一代相比, 本代使用[克隆后的luajava](https://github.com/SmileYik/luajava)仓库. 与原仓库相比, 克隆后的luajava仓库基本上重写了反射部分功能, 能够进一步简化Lua调用Java方法之间的过程, 并且在C语言端提供了非常友好的异常提示.

## 能做什么?

基本上就支持两大功能:

+ 注册指令: 注册你想要的任何指令, 并且自动生成帮助信息和指令层级关系
+ 监听事件: 监听任何你想要的Bukkit事件, 即使这个时间是其他插件的自定义事件

不过依托于Java的反射机制和动态代理机制, 目前可以实现在lua脚本中继承Java接口,
调用Java类型中的任何公开方法, 公开属性. 也就是说本插件可以动态的加载脚本, 
享受Java的子集功能. 当然, 反射也不是万能的, 还是会出现很多Lua端无法处理的情况, 
此时就需要使用Java为Lua架桥了. 不过在开发过程中我会尽量简化Lua与Java中的交互流程.

除开上述所说内容, 与第一代相同, 还能够加载C/C++所编写的动态链接库. 当然这都是
Lua 语言本身就支持的功能.

