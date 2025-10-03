> 最后更新于2025年06月29日 | [历史记录](https://github.com/SmileYik/LuaInMinecraftBukkitII/commits/gh-page/docs/zh/QuickStart.md)

> 此页面内容对应于 LuaInMinecraftBukkit II 插件的最新版本, 历史文档可以插件此页面的历史记录

在这个文档中, 将教您快速使用本插件制作一个游戏中的lua解释器,
使得游戏中的玩家能够执行指令后, 进入lua解释器控制台, 
通过聊天框执行lua脚本.

## 初识Lua

如果您已经对lua的语法有一定的了解, 您可以跳过本段内容.
本段内容较为粗略, 若想详细了解, 请寻找更加详细的教程.

您可以在互联网上搜索在线运行lua的网站, 或者自己打开一个lua解释器, 或者直接运行本插件内实现的lua解释器.

### 注释

在lua中, 注释以 "--" 开头, 例如以下代码, 仅会执行`print`

```lua
-- 我是注释
print("Hello")
```

### 数据类型

lua 中数据类型包含以下几种:

+ 空值(NULL): nil
+ 数字: 1, 1.5, -1, ...
+ 布尔: true, false
+ 字符串: "Hello", 'world'
+ 表: {}, {1, 2, 3}, {a = 1, b = 2}
+ 方法(闭包): function() end

#### 表-table

table类型可以使用 `.` 运算符访问里面的成员或元素, 
也可以使用`[]`运算符访问里面的成员或元素.

表可以分为两种, 一种为数组风格的表, 这类表以整数为键值,
键值总为1开头, 并且从头到尾都是连续的. 另一种为键值对表,
类似于Java/C++中的Map, Python 中的字典. 
不过无论是那种表, 表里面的元素可以是不一致的, 也可以是任意的.

对于数组风格的表可以使用`[idx]`访问其中的元素, idx 为整数, 
也就是它的键值, 并且总为 **1** 开始!

```lua
-- 字符串数组风格table, 
strArray = {"a", "b", "c"}
-- 数字版本
numArray = {1.5, 2.0, -1}
-- 这个也是数组风格, 存了好几种不同的类型
objArray = {true, 1.5, "aaa", {"123"}, function (msg) print(msg); end}

print(strArray[1]) -- 打印 a
print(numArray[2]) -- 打印 2.0
objArray[5]("hello") -- 打印 hello
```

对于非数组风格的表, 既可以使用`[]`访问其中的元素, 也可以使用`.`访问其中的元素

```lua
func = function (msg) 
    print(msg)
end

t1 = {
    a = 1,
    b = true,
    c = func
}
print(t1["a"]) -- 打印1
t1["a"] = 100  -- 为"a"重新赋值
print(t1.a)    -- 打印 100
t1.c("hello")  -- 打印 hello

t1.func = "123" -- 添加键值对 "func" 并赋值为 "123", 注意, 是字符串"func"
t1[func] = "456" -- 加入一个新键值对, 键为 func 函数, 值为 "456"
for key, value in pairs(t1) do
    print(key, value)
end
```

### 变量

lua中分为全局变量与局部变量, 可以进行如下声明或定义:

```lua
-- 全局变量
num1 = 1
num2 = -1.5
table1 = {1, 2, 3}
-- 局部变量
local func = function() print("Hello") end
local bool1 = true
```

全局变量, 顾名思义, 该变量在整个脚本范围内都可以使用,
如下例子:

```lua
function func()
    a = 1    -- 声明并定义全局变量 a
    print(a) -- 输出 a 的值, 这里是 1
end
print(a) -- 结果为 nil, 因为变量 a 还没有声明或还没有定义
func()   -- 执行 func 函数, 打印输出 1
print(a) -- func 函数中声明并定义了变量 a = 1, 所以输出 1
```

类似的, 局部变量就代表局部使用的变量, 也就是变量在一定范围内才可以使用.
如下例子:

```lua
function func()
    local a = 1 -- 声明并定义局部变量 a
    print(a)    -- 输出 a 的值, 这里是 1
end             -- 方法执行完成后, 局部变量 a 被清除
print(a) -- 结果为 nil, 因为变量 a 还没有声明或还没有定义
func()   -- 执行 func 函数, 打印输出 1
print(a) -- 执行结果为 nil
```

### 运算

与其他语言类似的运算规则, 以下为示例:

#### 算术运算

```lua
a = 1
b = 2.5
c = a + b       --  3.5
d = a - b       -- -1.5
e = (a + b) * b --  8.75
f = a + b * b   --  7.25

str1 = "Hello"
str2 = 'World'
-- 字符串拼接不能使用 + 号, 得使用 ..
str3 = str1 .. ', ' .. str2  -- Hello, World
```

#### 逻辑运算

这里介绍一下 lua 里怎么进行与或非运算.

```lua
a = true
b = false
c = a == b  -- a 是否等于 b, 结果: false
d = a or b  -- a,b 中有一个 true就是true, 结果: true
e = a and b -- a,b 中必须都为true则为true, 结果: false
f = a ~= b  -- a 是否不等于 b, 结果: true
g = not a   -- 取反, true变为false, 反之亦然, 结果: false

a = 1
h = a > 1       -- a是否大于1: false
i = a >= 1      -- a是否大于等于1: true
j = a < 10      -- a是否小于10: true 
k = a <= 0.1    -- a是否小于等于0.1: false
l = a == 1      -- a 是否等于1: true
m = a ~= 1      -- a 是否不等于1: false
```

### 流程控制

#### 分支判断

如果 xxx 为真则 xxx; 否则 xxx.
也就是 if 语句, 在lua中if语句形似:

```lua
-- if 
if condition then
    -- do something...
end

-- if-else
if condition then
    -- true
else 
    -- false
end

-- if-else-if
if condition1 then
    -- do something ...
elseif condition2 then
    -- do something ...
else 
    -- do something
end
```

例如用if-else判断一个数是百位数还是十位数还是一位数.

```lua
num = 96
if num < 1000 and num >= 100 then
    print("百位数")
elseif num >= 10 and num < 100 then
    print("十位数")
elseif num >= 0 and num < 10 then
    print("个位数")
else
    print("千位数及以上")
end
```

#### 循环

与其他语言类似, lua中也含有while循环与for循环, 也拥有break语句打破循环, 
但是, lua中没有continue语句跳过本次循环.

##### while循环

while循环格式如下

```lua
while condition 
do
    -- do something.
end

-- 把1加到10
a = 1
sum = 0
while a <= 10 do 
    sum = sum + a
    a = a + 1
end
print(sum)
```

##### for循环

for 循环有两种, 一种是数值for循环.

```lua
-- from, to, step都是整数
-- from是起始值, to是终止值, step是步长, 步长可以不填, 默认为1
for var=from, to, step do
    -- do something
end

-- 打印 1, 2, ..., 10(包括)
for a = 1, 10 do
    print(a)
end

-- 打印 10, 9, ..., 1(包括)
for a = 10, 1, -1 do
    print(a)
end
```

另外一种是迭代器.

```lua
-- key 是键, value是值, table是表
for key, value in pairs(table) do
    -- do something
end

-- 遍历数组风格的表
t1 = {1, 2, 3}
for i, v in pairs(t1) do
    print(i, v)
end

-- 遍历表
t2 = {a = 1, b = 2}
for k, v in pairs(t2) do
    print(k, v)
end
```

## 环境准备

现在该准备环境了, 首先需要安装好插件, 直接将插件放入
plugins 文件夹就可以了, 之后什么麻烦的动态链接库什么的东西,
插件会帮你搞定!

运行完一次服务器后, 会在 plugins 目录下新建一个名字为
LuaInMinecraftBukkitII 的目录, 里面有一个配置文件,
名字为 config.json, 还有其他目录, 具体样子长这样. 

```
❯ tree .
.
├── config.json   插件配置文件
├── debug.log     debug日志
├── luaLibrary    存放lua依赖库的文件夹
├── luaState      lua 环境文件夹
├── natives       native 动态链接库文件夹
└── scripts       lua 脚本文件夹
```

插件默认的配置文件中为你准备了一个名为`default`的 lua 环境, 
并且在初始化环境时, 自动加载`luaState`目录中的`test.lua`
文件, 我们刚好可以用`test.lua`文件进行练手! 

当然了, 你也可以自己设定, 以下是config.json的样例.

```json
{
  // 项目资源地址
  "projectUrl": "https://raw.githubusercontent.com/SmileYik/LuaInMinecraftBukkitII/refs/heads/gh-page",
  // 使用的lua版本
  "luaVersion": "luajit",
  // 总是检查依赖库Hash是否正确
  "alwaysCheckHashes": false,
  // debug 标志
  "debug": true,
  // lua 环境
  "luaState": {
    // lua 环境 id
    "default": {
      // 该环境运行在哪个目录下, "/" 代表插件目录下的luaState目录.
      "rootDir": "/",
      // 是否忽略访问限制, 忽略访问限制时可以强制访问java中的私有方法.
      "ignoreAccessLimit": false,
      // 初始化脚本列表, 下面填脚本名, 并且脚本文件得在rootDir中寻找的到.
      "initialization": [
        // 加载的文件
        {
          // 文件名, 这里默认是 test.lua
          "file": "test.lua",
          // 是否自动重载, 目前无用
          "autoReload": false,
          // 依赖的Bukkit插件
          "depends": []
        }
      ]
    }
  }
}
```

假设你要加一个要初始化环境时自动执行的lua脚本, 可以在
`initialization` 下添加要加的文件即可, 就本项目而言,
加一个 `chat-console.lua` 好了. 在 `rootDir` 
保持默认情况下, 在 `plugins/LuaInMiencraftBukkitII/luaState/chat-console.lua`
中编辑 lua 脚本就可以了!

```json
      "initialization": [
        // 加载的文件
        {
          // 文件名, 这里默认是 test.lua
          "file": "test.lua",
          // 是否自动重载, 目前无用
          "autoReload": false,
          // 依赖的Bukkit插件
          "depends": []
        },
        {
          // 文件名, 实际文件路径为 rootDir + "chat-console.lua"
          "file": "chat-console.lua",
          // 是否自动重载, 目前无用
          "autoReload": false,
          // 依赖的Bukkit插件
          "depends": []
        }
      ]
```

## Java与Lua

虽然本插件的目的是让Lua脚本与Bukkit服务器进行交互, 
其实这本质上也是让Lua脚本与Java进行交互.
而这一部分就没办法使用在线的 lua 解释器运行验证了,
只能运行服务器了!

### 热身

在以上环境准备中, 我们加入了一个 `chat-console.lua` 文件, 
我们打开这个文件, 并进行一个简单的编辑: 

```lua
-- /plugins/LuaInMiencraftBukkitII/luaState/chat-console.lua
luaBukkit.log:info("Hello, Lua!")
```

编辑完成后, 保存文件, 并且在控制台输入指令: `lua reloadEnv default`,
在重载lua环境完成后, 你就能看见控制台中输出了 `Hello, Lua!` 字样:

```
[19:32:58 INFO]: [LuaInMinecraftBukkitII] Hello, Lua!
[19:32:58 INFO]: Reloaded Lua Environment
```

热身完了, 得来一点真东西了.

### Java变量类型与Lua变量类型

回顾一下, Java中拥有若干基础类型: `boolean`, `byte`, `short`,
`int`, `long`, `float`, `double`, `char`; 除了基础类型之外,
还有字符串`String`, 基于 `Object` 的自定义类型; 除此之外,
还有基础类型数组和自定义类型数组.而Lua中包含布尔类型, 数字类型, 
字符串类型, 表, 方法 还有一个用户数据类型(实际上理解为一个C指针, 这里略过).

在 Lua 与 Java 进行交互时, 数据类型转换大多时候都是无感的, 

|Lua类型|Java类型|
|:-:|:-:|
| 数字 | `byte`, `short`, `int`, `long`, `float`, `double`, `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`|
| 布尔类型 | `boolean`, `Boolean` |
| 字符串 | `String` |
| 用户数据(C指针) | 其他自定义类型 |
| 一维数组风格表 | `LuaArray` |
| 表 | `LuaTable` |
| 函数 | `ILuaCallable`, `LuaFunction` |

#### 数组之间的转换

Java 中的所有数组到 Lua 中, 都会被转换成数组风格的表.
而 Lua 中数组风格的表会尝试转换成 Java 中的一维数组.

例如 Lua 中的 `{1, 2, 3, 4}` 传入 Java 中, 会尝试转换成 `int[]`;
`{"abc", "def"}` 会尝试转换成 `String[]`; 而 {`{1, 2, 3}, {4, 5, 6}`}
会尝试转换成 `Object[]` 

### 调用Java方法

在热身过程中我们调用了 `luaBukkit.log` 中的 `info` 方法, 
`luaBukkit.log` 是一个 Java 中的 `java.util.logging.Logger` 
类型的实例, `info` 方法接收一个字符串, 所以才会有热身中
`luaBukkit.log:info("Hello, Lua!")` 这样的使用方法.
而 `:` 运算符几乎是调用 Java 方法必备运算符, 使用 `.` 则会调用失败!

`luaBukkit.server` 是 Java 中 Bukkit 的 `CraftServer` 类型实例,
可以尝试一下使用 `luaBukkit.log:info(luaBukkit.server)`, 重载 Lua
环境后发现报错了, 报错内容为:

```
[19:36:37 WARN]: [LuaInMinecraftBukkitII] Failed to eval lua file 'chat-console.lua', because: [C Side Exception] Invalid method call. No such method.
	at [LuaVM] [0] [C] method: info (=[C]:-1)
	at [LuaVM] [1] [main] : (unknown name) (@plugins/LuaInMinecraftBukkitII/luaState/chat-console.lua:2)
```

上面报错提示我们 `luaBukkit.log` 没有 `info` 方法, 这是因为 `info`
只接受字符串类型作为参数, 传入 `luaBukkit.server`(`CraftServer` 类型实例)
它无法处理, 所以报出这个错误. 所以当报出类似错误时, 可以仔细看看自己的参数有没有传错!

### 获取Java实例属性

Java 中拥有静态属性和普通属性, 但是因为一些权限问题, lua环境默认关闭非公开属性获取, 可以在配置文件中自行打开.

获取Java实例属性与获取Lua表中字段一样, 使用`.`运算符进行获取. 例如

```lua
luaBukkit.log:info(luaBukkit.server.BROADCAST_CHANNEL_USERS)
```

实际上以上语句是打印`Server.BROADCAST_CHANNEL_USERS`常量值, 结果为
`bukkit.broadcast.user`.

## 编写基于聊天框的Lua终端

终于步入正题了, 现在我们来写一个基于聊天框的Lua终端吧!
在环境准备中我们创建了一个 `/plugins/LuaInMiencraftBukkitII/luaState/chat-console.lua` 文件, 打开这个文件开始编写把.

### 编写指令

编写两个指令, 用于控制某个玩家是否进入了终端模式. 这两个指令分别为:

+ `/luaconsole open`: 为执行指令的玩家打开终端.
+ `/luaconsole close`: 为执行指令的玩家关闭终端.

```lua
-- /plugins/LuaInMiencraftBukkitII/luaState/chat-console.lua
-- 进入终端模式的玩家
local players = {}

-- 定义指令
local commands = {
    {
        -- 指令名称
        command = "open",
        -- 指令描述
        description = "进入Lua终端",
        -- 指令参数, 因为没有参数, 所以我们放空.
        args = {},
        permission = "LuaConsole.Admin",
        -- 我们希望只有玩家才能运行. 控制台哪里来的聊天框!
        needPlayer = true,
        -- 当指令触发时执行的方法
        handler = function (sender, args)
            -- 既然是玩家才能运行, sender 一定是 Player 类型了
            -- 直接将玩家名标记为 true, 代表进入了终端模式
            players[sender:getName()] = true
            sender:sendMessage("---------- 进入 Lua 终端 ----------")
        end
    },
    {
        command = "close",
        description = "退出Lua终端",
        args = {},
        permission = "LuaConsole.Admin",
        needPlayer = true,
        handler = function (sender, args)
            -- 删除玩家名
            players[sender:getName()] = nil
            sender:sendMessage("---------- 退出 Lua 终端 ----------")
        end
    }
}

local topCommandClass = luaBukkit.env:commandClassBuilder()  -- 获取命令类型构造器
    :commands(commands)   -- 将我们要注册的指令信息传入进去
    :build("luaconsole")  -- 设定我们的指令名称为 luaconsole

-- 将指令注册为 'luaconsole', 需要与上面构建的指令类型名称一致.
local result = luaBukkit.env:registerCommand("luaconsole", { topCommandClass })
if result:isError() then
    luaBukkit.log:info("注册指令失败!")
else
    luaBukkit.log:info("注册指令成功!")
end
```

此时使用`/lua reloadEnv default`指令重新加载`chat-console.lua`脚本,
不出意外你应该就能看见 `注册指令成功!` 的字样. 之后在控制台中输入指令就能得到提示了.

```
[21:07:19 INFO]: [LuaInMinecraftBukkitII] 注册指令成功!
luaconsole help
[21:07:28 INFO]: 
------------HELP-----------------
luaconsole open - 进入Lua终端
luaconsole close - 退出Lua终端
---------------------------------
luaconsole open
[21:09:39 INFO]: 只能玩家执行这个指令!
```

**如果是玩家执行指令, 记得给玩家权限哦!**

### 注册监听器

有了指令可不行, 得监听玩家们在聊天框发送的消息, 有没有这个方法呢?
确实有, Bukkit 提供了一个玩家聊天事件, 我们得监听这个事件!

这个事件叫 [`AsyncPlayerChatEvent`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/player/AsyncPlayerChatEvent.html), 在Java中, 它的全类名为 `org.bukkit.event.player.AsyncPlayerChatEvent`. 现在我们来监听这个事件把!

可以使用以下方法注册事件:

``` lua
-- ... 注册指令部分

-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- 方法逻辑
end

-- 获取监听器构造器
-- 把自己的监听器命名成 "MyPlayerChatListener"
luaBukkit.env:listenerBuilder()
    -- 订阅一个事件
    :subscribe({
        -- 订阅 AsyncPlayerChatEvent 事件
        event = "org.bukkit.event.player.AsyncPlayerChatEvent",
        -- 当事件发生时, 交给 onPlayerChat 处理.
        handler = onPlayerChat
    })
    :build()
    :register("MyPlayerChatListener")
```

好了, 现在可以完善我们自己的事件处理逻辑了, 我们需要过滤玩家, 将
不再名单内的玩家过滤掉:

```lua
-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- 从事件中获取触发事件的玩家
    local player = event:getPlayer()
    -- 判断玩家是否在名单中, 如果没在名单中则直接返回
    if not players[player:getName()] then
        return
    end
end
```

好了, 下面要开始获取玩家发送的信息并解析成lua了!
解析文本值并当成lua运行需要使用到 `loadstring` 函数(Lua5.4之前),
或者 `load` 函数(Lua 5.4). 为了方便, 我们可以统一重命名为 `loadstring`

```lua
-- 这里是为了适配 Lua5.4 的改变,
-- 这里这样做是照顾到使用不同 Lua 版本的用户.
-- 在Lua5.4之前, loadstring 是执行lua脚本
-- 在Lua5.4后更名为 load
if loadstring == nil then
    loadstring = load
end

-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- .....
end
```

之后继续完善我们的 `onPlayerChat` 方法! 既然发送的消息都解析成
lua 代码了, 那么就不应该再发出消息了, 所以要取消事件, 然后再执行
lua 代码.

```lua
-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- 从事件中获取触发事件的玩家
    local player = event:getPlayer()
    -- 判断玩家是否在名单中, 如果没在名单中则直接返回
    if not players[player:getName()] then
        return
    end

    -- 设置事件取消
    event:setCancelled(true)
    -- 获取玩家发送的信息, 并单独发给玩家
    local msg = event:getMessage()
    player:sendMessage(msg)
    -- 加载并执行 lua 脚本
    -- 返回执行成功或失败和失败原因
    local result, error = pcall(loadstring(msg))
    -- 如果没有执行成功则向玩家输出执行失败.
    if not result then
        player:sendMessage("执行失败: " .. error)
    end
end
```

最后注册事件这一部分的完整代码是这个样子啦:

```lua
-- ... 之前的代码

-- 这里是为了适配 Lua5.4 的改变,
-- 这里这样做是照顾到使用不同 Lua 版本的用户.
-- 在Lua5.4之前, loadstring 是执行lua脚本
-- 在Lua5.4后更名为 load
if loadstring == nil then
    loadstring = load
end

-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- 从事件中获取触发事件的玩家
    local player = event:getPlayer()
    -- 判断玩家是否在名单中, 如果没在名单中则直接返回
    if not players[player:getName()] then
        return
    end

    -- 设置事件取消
    event:setCancelled(true)
    -- 获取玩家发送的信息, 并单独发给玩家
    local msg = event:getMessage()
    player:sendMessage(msg)
    -- 加载并执行 lua 脚本
    -- 返回执行成功或失败和失败原因
    local result, error = pcall(loadstring(msg))
    -- 如果没有执行成功则向玩家输出执行失败.
    if not result then
        player:sendMessage("执行失败: " .. error)
    end
end

-- 获取监听器构造器
-- 把自己的监听器命名成 "MyPlayerChatListener"
luaBukkit.env:listenerBuilder()
    -- 订阅一个事件
    :subscribe({
        -- 订阅 AsyncPlayerChatEvent 事件
        event = "org.bukkit.event.player.AsyncPlayerChatEvent",
        -- 当事件发生时, 交给 onPlayerChat 处理.
        handler = onPlayerChat
    })
    :build()
    :register("MyPlayerChatListener")
```

现在重载lua环境后, 进入游戏, 输入`/luaconsole open`进入终端模式,
然后发送`a = 1`以及`print(a)`后, 发现什么反应都没有, 聊天框和控制台里都没有输出a的值. 这是怎么回事呢? 这是因为 `print` 方法实际上是在 C 语言端执行的, 
需要另外执行 `io.stdout:flush()` 才会将输出的值刷出缓冲区.
这样未免太麻烦了, 所以, 我们重写`print`方法, 让它把值都在聊天框中显示好了.

为了让`print`方法对局部变量`player`也有可见性, 我们可以在`onPlayerChat`
内重写`print`方法.
```lua
    -- 获取玩家发送的信息, 并单独发给玩家
    local msg = event:getMessage()
    player:sendMessage(msg)

    -- 重写 print 方法, 在这个函数体内重写,
    -- 也能见到 player 变量.
    print = function (msg)
        -- userdata 一般为 java 对象, 可以直接与空字符串连接成为字符串.
        -- 其他类型得先转换成字符串
        if type(msg) ~= "userdata" then
            msg = tostring(msg)
        end
        player:sendMessage(msg .. "")
    end

    -- 加载并执行 lua 脚本
    -- 返回执行成功或失败和失败原因
    local result, error = pcall(loadstring(msg))
```

### 最终代码

终于写完了, 最终的代码是这个样子的!

```lua
-- /plugins/LuaInMiencraftBukkitII/luaState/chat-console.lua
-- 进入终端模式的玩家
local players = {}

-- 定义指令
local commands = {
    {
        -- 指令名称
        command = "open",
        -- 指令描述
        description = "进入Lua终端",
        -- 指令参数, 因为没有参数, 所以我们放空.
        args = {},
        permission = "LuaConsole.Admin",
        -- 我们希望只有玩家才能运行. 控制台哪里来的聊天框!
        needPlayer = true,
        -- 当指令触发时执行的方法
        handler = function (sender, args)
            -- 既然是玩家才能运行, sender 一定是 Player 类型了
            -- 直接将玩家名标记为 true, 代表进入了终端模式
            players[sender:getName()] = true
            sender:sendMessage("---------- 进入 Lua 终端 ----------")
        end
    },
    {
        command = "close",
        description = "退出Lua终端",
        args = {},
        permission = "LuaConsole.Admin",
        needPlayer = true,
        handler = function (sender, args)
            -- 删除玩家名
            players[sender:getName()] = nil
            sender:sendMessage("---------- 退出 Lua 终端 ----------")
        end
    }
}

local topCommandClass = luaBukkit.env:commandClassBuilder()  -- 获取命令类型构造器
    :commands(commands)   -- 将我们要注册的指令信息传入进去
    :build("luaconsole")  -- 设定我们的指令名称为 luaconsole

-- 将指令注册为 'luaconsole', 需要与上面构建的指令类型名称一致.
local result = luaBukkit.env:registerCommand("luaconsole", { topCommandClass })
if result:isError() then
    luaBukkit.log:info("注册指令失败!")
else
    luaBukkit.log:info("注册指令成功!")
end

-- 这里是为了适配 Lua5.4 的改变,
-- 这里这样做是照顾到使用不同 Lua 版本的用户.
-- 在Lua5.4之前, loadstring 是执行lua脚本
-- 在Lua5.4后更名为 load
if loadstring == nil then
    loadstring = load
end

-- onPlayerChat 是一个函数类型变量,
-- 这个函数接受一个参数, 用于接收事件实例.
local onPlayerChat = function (event)
    -- 从事件中获取触发事件的玩家
    local player = event:getPlayer()
    -- 判断玩家是否在名单中, 如果没在名单中则直接返回
    if not players[player:getName()] then
        return
    end

    -- 设置事件取消
    event:setCancelled(true)
    -- 获取玩家发送的信息, 并单独发给玩家
    local msg = event:getMessage()
    player:sendMessage(msg)

    -- 重写 print 方法, 在这个函数体内重写,
    -- 也能见到 player 变量.
    print = function (msg)
        -- userdata 一般为 java 对象, 可以直接与空字符串连接成为字符串.
        -- 其他类型得先转换成字符串
        if type(msg) ~= "userdata" then
            msg = tostring(msg)
        end
        player:sendMessage(msg .. "")
    end

    -- 加载并执行 lua 脚本
    -- 返回执行成功或失败和失败原因
    local result, error = pcall(loadstring(msg))
    -- 如果没有执行成功则向玩家输出执行失败.
    if not result then
        player:sendMessage("执行失败: " .. error)
    end
end

-- 获取监听器构造器
-- 把自己的监听器命名成 "MyPlayerChatListener"
luaBukkit.env:listenerBuilder()
    -- 订阅一个事件
    :subscribe({
        -- 订阅 AsyncPlayerChatEvent 事件
        event = "org.bukkit.event.player.AsyncPlayerChatEvent",
        -- 当事件发生时, 交给 onPlayerChat 处理.
        handler = onPlayerChat
    })
    :build()
    :register("MyPlayerChatListener")
```

然后重新加载lua环境, 进入服务器后, 可以直接运行lua终端啦.

![激动人心的结果](./images/QuickStart.1.png)