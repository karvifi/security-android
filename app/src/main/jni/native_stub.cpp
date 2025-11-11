#include <jni.h>
#include <string>

extern "C" JNIEXPORT jint JNICALL
Java_com_fortress_hypervisor_core_HypervisorRustScanner_scanSystem(JNIEnv* env, jobject /* this */, jstring paths) {
    // Stub: return 0 threats. Real implementation in Rust library.
    return 0;
}
