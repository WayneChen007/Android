#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "native_fill.h"
#define SIZE_M (1024*1024)
NativeFill *NativeFill::mpInstance = NULL;

NativeFill::NativeFill() :
        mppBuff(NULL),
        mBuffSize(0) {
}

void NativeFill::fill(int size) {
    // free all
    if (mppBuff != NULL) {
        for (int i = 0; i < mBuffSize; i++) {
            free(mppBuff[i]);
        }
        free(mppBuff);
        mppBuff = NULL;
    }
    mBuffSize = 0;
    // new
    if (size > 0) {
        mppBuff = (char **) malloc(sizeof(char *) * size);
        for (int i = 0; i < size; i++) {
            mppBuff[i] = (char *) malloc(sizeof(char) * SIZE_M);
            memset(mppBuff[i], 0, SIZE_M);
        }
        mBuffSize = size;
    }
}

int NativeFill::getFilledSize() {
    return mBuffSize;
}

NativeFill *NativeFill::getInstance() {
    if (mpInstance == NULL) {
        mpInstance = new NativeFill();
    }
    return mpInstance;
}
