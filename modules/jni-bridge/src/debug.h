#ifndef __DEBUG_H
#define __DEBUG_H



#ifdef ENABLE_DEBUG
    #include <iostream>
    #define DEBUG std::cout
#else
    #include <any>
    struct DISABLE_DEBUG {
        DISABLE_DEBUG operator<<(std::any something) {
            return *this;
        }
    };
    #define DEBUG DISABLE_DEBUG()
#endif



#endif // __DEBUG_H