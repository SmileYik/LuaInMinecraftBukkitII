-- Module Name: import
-- Author: SmileYik
-- Create Time: 2025-08-06

-- import java class
-- example:
--     local import = require "import"
--     import "java.lang.Math"
--     print(Math:random())
-- className: java class name
local function import(className)
    if className == nil then
        return nil
    end

    local varName = string.match(className, "([^%.]+)$")
    if varName == nil or varName == "" then
        varName = className
    end
    local result, clazz = pcall(luajava.bindClass, className)
    _G[varName] = clazz
    return clazz
end

return import