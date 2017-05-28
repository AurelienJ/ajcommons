/*
** MacOSXAuthProcess.c -- JNI C code
**
*/

#include "MacOSXAuthProcess.h"

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <Security/Authorization.h>

/* # # # # # # # # # # # # # # # # # # # # # # # # # # # # */

#ifndef WIFEXITED
#define WIFEXITED(status) (((status)&0xFF) == 0)
#endif

#ifndef WEXITSTATUS
#define WEXITSTATUS(status) (((status)>>8)&0xFF)
#endif

#ifndef WIFSIGNALED
#define WIFSIGNALED(status) (((status)&0xFF) > 0 && ((status)&0xFF00) == 0)
#endif

#ifndef WTERMSIG
#define WTERMSIG(status) ((status)&0x7F)
#endif

#define SUCCESS		errAuthorizationSuccess
#define BAD_AUTHREF	errAuthorizationInvalidRef

#define NO_ENV		kAuthorizationEmptyEnvironment
#define NO_OPTIONS	kAuthorizationFlagDefaults

/**
 * Add the Standard err Pipe and Pid support to AuthorizationExecuteWithPrivileges()
 * method
 *
 * @Author: Miklós Fazekas
 */
static OSStatus AuthorizationExecuteWithPrivilegesStdErrAndPid (
   AuthorizationRef authorization,
   const char *pathToTool,
   AuthorizationFlags options,
   char * const *arguments,
   FILE **communicationsPipe,
   FILE **errPipe,
   pid_t* processid
)
{
    char stderrpath[] = "/tmp/AuthorizationExecuteWithPrivilegesStdErrXXXXXXX.err" ;
	const char* commandtemplate = "echo $$; \"$@\" 2>%s" ;
    if (communicationsPipe == errPipe) {
        commandtemplate = "echo $$; \"$@\" 2>1";
    } else if (errPipe == 0) {
        commandtemplate = "echo $$; \"$@\"";
    }
	char command[1024];
	char ** args;
	OSStatus result;
	int argcount = 0;
	int i;
	int stderrfd = 0;
	FILE* commPipe = 0;

	/* Create temporary file for stderr */

    if (errPipe) {
        stderrfd = mkstemps (stderrpath, strlen(".err"));

        /* create a pipe on that path */
        close(stderrfd); unlink(stderrpath);
        if (mkfifo(stderrpath,S_IRWXU | S_IRWXG) != 0) {
            fprintf(stderr,"Error mkfifo:%d\n",errno);
            return errAuthorizationInternal;
        }

        if (stderrfd < 0)
            return errAuthorizationInternal;
    }

	/* Create command to be executed */
	for (argcount = 0; arguments[argcount] != 0; ++argcount) {}
	args = (char**)malloc (sizeof(char*)*(argcount + 5));
	args[0] = "-c";
	snprintf (command, sizeof (command), commandtemplate, stderrpath);
	args[1] = command;
	args[2] = "";
	args[3] = (char*)pathToTool;
	for (i = 0; i < argcount; ++i) {
		args[i+4] = arguments[i];
	}
	args[argcount+4] = 0;



    /* for debugging: log the executed command */
	printf ("Exec:\n%s", "/bin/sh"); for (i = 0; args[i] != 0; ++i) { printf (" \"%s\"", args[i]); } printf ("\n");

	/* Execute command */
	result = AuthorizationExecuteWithPrivileges(
			authorization, "/bin/sh",  options, args, &commPipe );
	if (result != noErr) {
		unlink (stderrpath);
		return result;
	}

	/* Read the first line of stdout => it's the pid */
	{
		int stdoutfd = fileno (commPipe);
		char pidnum[1024];
		pid_t pid = 0;
		int i = 0;
		char ch = 0;
		while ((read(stdoutfd, &ch, sizeof(ch)) == 1) && (ch != '\n') && (i < sizeof(pidnum))) {
			pidnum[i++] = ch;
		}
		pidnum[i] = 0;
		if (ch != '\n') {
			// we shouldn't get there
			unlink (stderrpath);
			return errAuthorizationInternal;
		}
		sscanf(pidnum, "%d", &pid);
		if (processid) {
			*processid = pid;
		}
	}

	if (errPipe) {
        stderrfd = open(stderrpath, O_RDONLY, 0);
        *errPipe = fdopen(stderrfd, "r");
        /* Now it's safe to unlink the stderr file, as the opened handle will be still valid */
        unlink (stderrpath);
	} else {
		unlink(stderrpath);
	}
	if (communicationsPipe) {
		*communicationsPipe = commPipe;
	} else {
		fclose (commPipe);
	}

	return noErr;
}

/**
 * Free the NULL-terminated array
 * of nul-terminated UTF-8 C-strings,
 * stored in malloc'ed memory.
 *
 * @Author: Gregory Guerin
 */
static void freeArgs( char **args )
{
	char **scan;
	char * each;

	for ( scan = args;  true; )
	{
		each = *scan++;
		if ( each == NULL )
			break;
		free( each );
	}

	free( args );
}


/**
 * Translate String[] into NULL-terminated array
 * of nul-terminated UTF-8 C-strings,
 * stored in malloc'ed memory.
 * If any failures, free everything allocated so far and return NULL.
 *
 * @Author: Gregory Guerin
 */
static char ** getArgs( JNIEnv * env, jobjectArray jArgs )
{
	char **args;
//	char ** scan;
	char * each;
	jsize nargs, i, lenStr, lenUTF;
	jobject jArg;

	// Allocate a zeroed (NULL-filled) block to serve as args array.
	// It's one larger to hold the terminating NULL pointer.
	nargs = (*env)->GetArrayLength( env, jArgs );
	args = (char **) calloc( nargs + 1, sizeof( char * ) );
	if ( args == NULL )
		return ( NULL );

	for ( i = 0;  i < nargs;  ++i )
	{
		// The GetObjectArrayElement() must have a matching DeleteLocalRef(),
		// otherwise we'll consume too many local-refs in the loop.
		// On failure, we DO NOT have to clean up the local-ref,
		// since returning from the JNI function will accomplish that.
		jArg = (*env)->GetObjectArrayElement( env, jArgs, i );

		// I don't know if GetStringUTFLength() counts the terminating NUL-byte or not.
		// To be safe, allocate space for lenUTF+1 bytes.  If malloc() fails, abandon it all.
		lenUTF = (*env)->GetStringUTFLength( env, jArg );
		args[ i ] = each = (char *) malloc( lenUTF + 1 );
		if ( each == NULL )
		{
			freeArgs( args );
			return ( NULL );
		}

		// Copy entire String (i.e. lenStr Unicode chars) from jArg into buffer as UTF-8.
		lenStr = (*env)->GetStringLength( env, jArg );
		(*env)->GetStringUTFRegion( env, jArg, 0, lenStr, each );

		// The malloc()'ed memory was not cleared, so store a NUL byte after the UTF-8 bytes.
		// If the JVM put its own NUL at end, we'll just be storing a NUL after that one,
		// which is redundant but safe.
		each[ lenUTF ] = 0;

		// Release the local-ref of jArg, in preparation for getting another one.
		(*env)->DeleteLocalRef( env, jArg );
	}

	return ( args );
}


/**
 * Exec a command with root privilege
 *
 * Java signature: rootExec( int session, int options, String pathToTool, String[] toolArgs, FileDescriptor stdin_fd, FileDescriptor stdout_fd, FileDescriptor stderr_fd, int[] pid);
 *
 * @Author: Gregory Guerin
 * @Author: Aurélien Jeoffray
 */

JNIEXPORT jint JNICALL
Java_org_ajdeveloppement_macosx_MacOSXAuthProcess_rootExec(
	JNIEnv * env,
	jobject process,
	jlong session,
	jint options,
	jstring pathToTool,
	jobjectArray toolArgs,
	jobject stdin_fd,
	jobject stdout_fd,
	jobject stderr_fd,
	jintArray pid)
{
	const jbyte * utfToolName;
	jboolean isCopy;
	char **args;
	FILE * commPipe = NULL;
	FILE * errPipe = NULL;
	pid_t * processid;
	OSStatus result;
	jint in_fd;
	jint out_fd;
	jint err_fd;

	if ( session == 0 )
		return ( BAD_AUTHREF );

	// Always get utfToolName as UTF8 nul-terminated bytes.
	utfToolName = (*env)->GetStringUTFChars( env, pathToTool, &isCopy );
	if ( utfToolName == NULL )
		return ( -1 );  // OutOfMemoryError will be thrown

	// Convert args to C form.
	args = getArgs( env, toolArgs );
	if ( args == NULL )
	{
		(*env)->ReleaseStringUTFChars( env, pathToTool, utfToolName );
		return ( -1 );  // OutOfMemoryError will be thrown
	}

	// Fork and exec the process...
	//result = AuthorizationExecuteWithPrivileges(
	//		(AuthorizationRef) session, utfToolName, options, args, &commPipe );
	result = AuthorizationExecuteWithPrivilegesStdErrAndPid(
			(AuthorizationRef) session, utfToolName, options, args, &commPipe, &errPipe, &processid);
	// On success or failure, release and deallocate everything.
	(*env)->ReleaseStringUTFChars( env, pathToTool, utfToolName );
	freeArgs( args );

	// Get the filedes from commPipe.
	// Could use dup() or fcntl(), but there's no point, since both filedes's will still refer to
	// the same pipe.  Returning the identical filedes twice is the least problematic way.
	if ( result == SUCCESS )
	{
		jclass FileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jfieldID IO_fd_fdID = (*env)->GetFieldID(env, FileDescriptor, "fd", "I");

		in_fd = out_fd = fileno( commPipe );
		err_fd = fileno( errPipe );
		(*env)->SetIntField(env, stdin_fd,  IO_fd_fdID, in_fd);
		(*env)->SetIntField(env, stdout_fd, IO_fd_fdID, out_fd);
		(*env)->SetIntField(env, stderr_fd, IO_fd_fdID, err_fd);
		(*env)->SetIntArrayRegion( env, pid, 0, 1, (jint *) &processid );
	}

	return ( result );
}

/**
 *	 Wait the end of the child process
 */
JNIEXPORT jint JNICALL
Java_org_ajdeveloppement_macosx_MacOSXAuthProcess_waitForProcessExit(JNIEnv* env,
		jclass ignored, jint pid) {
	/* We used to use waitid() on Solaris, waitpid() on Linux, but
	 * waitpid() is more standard, so use it on all POSIX platforms. */
	int status;
	/* Wait for the child process to exit.  This returns immediately if
	 the child has already exited. */
	while (waitpid(pid, &status, 0) < 0) {
		switch (errno) {
			case ECHILD: return 0;
			case EINTR: break;
			default: return -1;
		}
	}

	if (WIFEXITED(status)) {
		/*
		 * The child exited normally; get its exit code.
		 */
		return WEXITSTATUS(status);
	} else if (WIFSIGNALED(status)) {
		/* The child exited because of a signal.
		 * The best value to return is 0x80 + signal number,
		 * because that is what all Unix shells do, and because
		 * it allows callers to distinguish between process exit and
		 * process death by signal.
		 */
		return 0x80 + WTERMSIG(status);
	} else {
		/*
		 * Unknown exit code; pass it through.
		 */
		return status;
	}
}

/**
 *	 Kill the child process
 *
 *	 @Author: Aurélien Jeoffray
 */
JNIEXPORT void JNICALL
Java_org_ajdeveloppement_macosx_MacOSXAuthProcess_destroyProcess(JNIEnv *env, jclass ignored, jint pid)
{
    kill(pid, SIGTERM);
}
