/*
 * Créé le 5 août 2008 à 11:49:17 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.IOException;

/**
 * <p>
 * Permet l'execution de processus en mode privilégié sous mac os x.
 * Fait appel pour cela à une librairie native chargé de demandé les autorisations
 * nécessaire à l'utilisateur (Popup de demande d'élévation de privilège)
 * </p>
 * <p>
 * Fait appel à la librairie native <code>libAuthKit.jnilib</code> (Sous Mac OS X, compilé seulement en 64bits)
 * </p>
 * @author Aurélien JEOFFRAY
 * @version 1.0
 *
 */
public class PrivilegedRuntime {
	//private static final int DEFAULT = 0x00;
	//private static final int INTERACT = 0x01;
	//private static final int EXTEND = 0x02;
	// private static final int PARTIAL = 0x04; // ## unneeded, since this imp never asks for partial
	// rights
	private static final int DESTROY = 0x08;
	// private static final int PREAUTH = 0x10; // ## ineffective. See getCapabilities() doc-comment.
	
	private long authSession = 0l;
	
	private static PrivilegedRuntime instance = new PrivilegedRuntime();
	
	static {
		System.loadLibrary("AuthKit"); //$NON-NLS-1$
	}
	
	private PrivilegedRuntime() {
		
	}
	
	/**
	 * Returns the runtime object associated with the current Java application. Most of the methods of class
	 * PrivilegedRuntime are instance methods and must be invoked with respect to the current runtime object. 
	 * 
	 * @return l'instance unique du gestionnaire d'execution
	 */
	public static PrivilegedRuntime getRuntime() {
		return instance;
	}
	
	/**
	 * Executes the specified command and arguments in a separate process with administrator
	 * right eventualy after display and confirm elevation popup dialog. !!! Do not use on console or web application.
	 * 
	 * @param cmd array containing the command to call and its arguments. 
	 * 
	 * @return new Process object for managing the subprocess
	 * @throws IOException
	 */
	public Process exec(String... cmd) throws IOException {	
		return execPrivileged(cmd);
	}
	
	private Process execPrivileged(String[] progArray) throws IOException {
		if(authSession == 0)
			authSession = makeSession(); //init admin session
		
		if (progArray == null || progArray.length == 0)
			throw new IllegalArgumentException("Null or empty String[]"); //$NON-NLS-1$
		
		String command = progArray[0];
		if (command == null || command.length() == 0)
			throw new IllegalArgumentException("Null or empty command"); //$NON-NLS-1$
		
		String[] args = new String[progArray.length - 1];
		System.arraycopy(progArray, 1, args, 0, args.length);
		
		return new MacOSXAuthProcess(authSession, command, args);
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		try {
			killSession(authSession, DESTROY);
		} catch (Throwable ignored) { }
	}
	
	private static native synchronized long makeSession();
	
	private static native synchronized int killSession(long session, int options);
}
