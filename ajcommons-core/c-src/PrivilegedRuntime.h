/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_ajdeveloppement_macosx_PrivilegedRuntime */

#ifndef _Included_org_ajdeveloppement_macosx_PrivilegedRuntime
#define _Included_org_ajdeveloppement_macosx_PrivilegedRuntime
#ifdef __cplusplus
extern "C" {
#endif
#undef org_ajdeveloppement_macosx_PrivilegedRuntime_DEFAULT
#define org_ajdeveloppement_macosx_PrivilegedRuntime_DEFAULT 0L
#undef org_ajdeveloppement_macosx_PrivilegedRuntime_DESTROY
#define org_ajdeveloppement_macosx_PrivilegedRuntime_DESTROY 8L
/*
 * Class:     org_ajdeveloppement_macosx_PrivilegedRuntime
 * Method:    makeSession
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_ajdeveloppement_macosx_PrivilegedRuntime_makeSession
  (JNIEnv *, jclass);

/*
 * Class:     org_ajdeveloppement_macosx_PrivilegedRuntime
 * Method:    killSession
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_ajdeveloppement_macosx_PrivilegedRuntime_killSession
  (JNIEnv *, jclass, jlong, jint);

#ifdef __cplusplus
}
#endif
#endif