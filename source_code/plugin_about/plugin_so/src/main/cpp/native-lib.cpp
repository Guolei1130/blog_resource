#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring
JNICALL
Java_com_guolei_so_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
  std::string hello = "Hello from plugin so";
  return env->NewStringUTF(hello.c_str());
}
