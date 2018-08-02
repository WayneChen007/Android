
#include "fill_memory_service.h"
#include "native_fill.h"

JNIEXPORT void JNICALL
Java_com_chen_wayne_myfillmemory_FillMemoryService_fill(JNIEnv
                                                        *pEnv,
                                                        jobject obj, jint
                                                        size) {
    NativeFill *pNativeFill = NativeFill::getInstance();
    pNativeFill->fill((int) size);
}

JNIEXPORT jint
JNICALL Java_com_chen_wayne_myfillmemory_FillMemoryService_getFilledSize(JNIEnv *pEnv, jobject
obj) {
    NativeFill *pNativeFill = NativeFill::getInstance();
    return (jint) pNativeFill->
            getFilledSize();
}
