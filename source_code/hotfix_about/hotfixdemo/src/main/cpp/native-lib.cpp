#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring
JNICALL
Java_com_guolei_hotfixdemo_MainActivity_stringFromJNI(
    JNIEnv *env,
    jobject /* this */) {
  std::string hello = "Hello from so";
  return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_guolei_hotfixdemo_MainActivity_replace(JNIEnv *env,
                                                jobject instance,
                                                jlong src,
                                                jlong des,
                                                jint size) {

  memcpy((void*)src, (void*)des, (size_t) size);

}

