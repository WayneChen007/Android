#ifndef MYFILLMEMORY_NATIVE_FILL_H
#define MYFILLMEMORY_NATIVE_FILL_H
class NativeFill {
private:
    char **mppBuff;
    int mBuffSize;
    static NativeFill *mpInstance;
public:
    NativeFill();
    void fill(int size);
    int getFilledSize();
    static NativeFill *getInstance();
};
#endif //MYFILLMEMORY_NATIVE_FILL_H
