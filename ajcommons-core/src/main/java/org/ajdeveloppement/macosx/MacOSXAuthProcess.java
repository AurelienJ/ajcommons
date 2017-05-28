/*
 * Créer le 7 août 2008 à 16:10:00 pour AjCommons (Bibliothèque de composant communs)
 * 
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL-C Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL-C telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 
 * 
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pri connaissance de la licence CeCILL-C, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package org.ajdeveloppement.macosx;

import java.io.*;

/**
 * <p>AuthProcess represents the privileged child-process created by
 * Mac OS X Method AuthorizationExecuteWithPrivileges().</p>
 * <p>This class is linked with JNI library AuthKit (libAuthKit.jnilib) and 
 * work only on x86_64 Mac OS X computer. The JNI source is available along
 * ajcommons project in c-src directory. A build.sh bash is available to build
 * native source.</p>
 * 
 * @author Aurélien JEOFFRAY
 */

public final class MacOSXAuthProcess extends Process {
	/**
	 ** Authorization options, from "Security/Authorization.h".
	 */
	private static final int DEFAULT = 0x00;
	//private static final int INTERACT = 0x01;
	//private static final int EXTEND = 0x02;
	// private static final int PARTIAL = 0x04; // ## unneeded, since this imp never asks for partial
	// rights
	//private static final int DESTROY = 0x08;
	// private static final int PREAUTH = 0x10; // ## ineffective. See getCapabilities() doc-comment.
	
	private static final int[] paramErrors;

	static {
		// Perform once-only static initialization for class.
		// If the list of illegal-arg errors was much longer, a Hashtable would
		// be worth using.
		// As it is, a sequential search is not so bad, even if all items are
		// skipped.
		paramErrors = new int[] { -1, -60001, -60002, -60003, -60004, -60011 };
	}
	
	private OutputStream in;
	private InputStream out;
	private InputStream err;
	private final int pid;
	
	private int exitcode;
    private boolean hasExited = false;

    /**
     * Create an privilege process session on Mac OS X System.
     * 
     * @param session id of privilege session generate by {@link PrivilegedRuntime#exec(String...)}
     * call.
     * @param command the command to be executed
     * @param args command line arguments 
     * @throws IOException
     */
	protected MacOSXAuthProcess(long session, String command, String... args) throws IOException {
		super();
		
		FileDescriptor stdin_fd = new FileDescriptor();
		FileDescriptor stdout_fd = new FileDescriptor();
		FileDescriptor stderr_fd = new FileDescriptor();
		int[] pid = new int[1];
		check(rootExec(session, DEFAULT, command, args, stdin_fd, stdout_fd, stderr_fd, pid));
		
		this.pid = pid[0];
		
		in = new BufferedOutputStream(new FileOutputStream(stdin_fd));
		out = new BufferedInputStream(new FileInputStream(stdout_fd));
		err = new FileInputStream(stderr_fd);
		
		Thread t = new Thread() {
			@Override
			public void run() {
				int res = waitForProcessExit(MacOSXAuthProcess.this.pid);
				synchronized (MacOSXAuthProcess.this) {
				    hasExited = true;
				    exitcode = res;
				    MacOSXAuthProcess.this.notifyAll();
				}
			}
		};
		t.start();
	}
	
	/**
	 ** Check the result-code, throwing an appropriate unchecked exception for
	 * failures, or returning normally for success. An IllegalArgumentException
	 * is thrown for bad-parameter failures. An UnauthorizedCancellation is
	 * thrown if authentication was cancelled. An UnauthorizedException is
	 * thrown for authentication failures or any other error.
	 */
	private void check(int result) throws IOException{
		if (result == 0)
			return;

		// This result-code value is either completely wrong or is mysteriously
		// undocumented.
		// It should be -60010: "The Security Server denied internalization..."
		// or perhaps -60002: "The authorization parameter is invalid".
		// I'm using -60010 as the replacement, so an UnauthorizedException will
		// be thrown.
		if (result == 0x80010001)
			result = -60010;

		// Filter out parameter errors and throw an IllegalArgumentException for
		// 'em.
		for (int i = 0; i < paramErrors.length; ++i) {
			if (result == paramErrors[i])
				throw new IllegalArgumentException("Authorization error: " //$NON-NLS-1$
						+ result);
		}

		// Choose which exception to throw, with appropriate message.
		if (result == -60006)
			throw new IOException("Authorization cancelled"); //$NON-NLS-1$

		throw new IOException("Authorization denied: " + result); //$NON-NLS-1$
	}

	/**
	 ** Return the stream feeding stdin of the subprocess.
	 */
	@Override
	public OutputStream getOutputStream() {
		return in;
	}

	/**
	 ** Return the stream fed by stdout of the subprocess.
	 */
	@Override
	public InputStream getInputStream() {
		return out;
	}

	/**
	 ** Return the stream fed by stderr of the subprocess.
	 */
	@Override
	public InputStream getErrorStream() {
		return err;
	}

	/**
	 * Wait for the subprocess to terminate (exit).
	 * 
	 */
	@Override
	public synchronized int waitFor() throws InterruptedException {
		while (!hasExited) {
		    wait();
		}
		return exitcode;
	}

	/**
	 ** Return the exit value, or throw an IllegalThreadStateException if this
	 * Process has not terminated (exited) yet.
	 */
	@Override
	public int exitValue() {
		if (!hasExited) {
		    throw new IllegalThreadStateException("process hasn't exited"); //$NON-NLS-1$
		}
		return exitcode;
	}

	/**
	 * Forcibly terminate this Process.
	 */
	@Override
	public void destroy() {
		synchronized (this) {
			if (!hasExited)
				destroyProcess(pid);
		}
		try {
			in.close();
			out.close();
			err.close();
		} catch (IOException e) {
			// ignore
		}
	}
	
	protected static native synchronized int rootExec(long session, int options,
			String pathToTool, String[] toolArgs, FileDescriptor stdin_fd, FileDescriptor stout_fd, FileDescriptor stderr_fd, int[] pid);
	/**
	 * Wait the End of the child process
	 * 
	 * @param pid the processus id of waiting process
	 * @return the exit return status code
	 */
	protected static native int waitForProcessExit(int pid);
	
	/**
	 * kill the current running process
	 * @param pid the pid of process to kill
	 */
	protected static native void destroyProcess(int pid);
}