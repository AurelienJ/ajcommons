/*
 * PrivilegedRuntime.c
 *
 *  Created on: 7 août 2008
 *      Author: aurelien
 */

#include "PrivilegedRuntime.h"

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <Security/Authorization.h>

#define SUCCESS			errAuthorizationSuccess
#define BAD_AUTHREF	errAuthorizationInvalidRef

#define NO_ENV				kAuthorizationEmptyEnvironment
#define NO_OPTIONS		kAuthorizationFlagDefaults

/**
 * Class:     org_ajdeveloppement_macosx_PrivilegedRuntime
 * Method:    makeSession
 * Signature: ()J
 *
 * @Author: Aurélien Jeoffray
 */
JNIEXPORT jlong JNICALL Java_org_ajdeveloppement_macosx_PrivilegedRuntime_makeSession
  (JNIEnv * env, jclass process)
{
	AuthorizationRef authRef;
	OSStatus result;

	// Create AuthRef with no Rights, no env, default flags.
	result = AuthorizationCreate( NULL, NO_ENV, NO_OPTIONS, &authRef );

	// On failure, force the contents of sessionRef to NULL.
	if ( result != SUCCESS ) {
		authRef = NULL;

		jclass IOException = (*env)->FindClass(env, "java/io/IOException");
	    (*env)->ThrowNew(env, IOException,"Enable to create Auth session");

	}

	return ( (jlong)authRef );
}

/**
 * Class:     org_ajdeveloppement_macosx_PrivilegedRuntime
 * Method:    killSession
 * Signature: (JI)I
 *
 * @Author: Aurélien Jeoffray
 */
JNIEXPORT jint JNICALL Java_org_ajdeveloppement_macosx_PrivilegedRuntime_killSession
  (JNIEnv * env, jclass process, jlong session, jint options)
{
	OSStatus result;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	result = AuthorizationFree( (AuthorizationRef) session, options );

	return ( result );
}
