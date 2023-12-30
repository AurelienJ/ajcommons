/*
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
package org.ajdeveloppement.swingxext.error;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.SwingUtilities;

import org.ajdeveloppement.apps.ApplicationContext;
import org.ajdeveloppement.commons.AjResourcesReader;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorReporter;

/**
 * <p>Permet de reporter une erreur sur un serveur Web après avoir demandé à l'utilisateur des
 * informations complémentaire sur les évenements ayant conduit à l'erreur.</p>
 * <p>Les informations sont transmisent au serveur Web via la commande HTTP POST</p>
 * <p>Est transmis un fichier zip contenant:
 * <ul>
 * 	<li>La sérialisation XML (au format JAXB) de l'objet {@link ErrorReportMessage}</li>
 * 	<li>Tous fichiers définit par le développeur pouvant facilité l'opération d'analyse</li>
 * </ul>
 * </p>
 * <p><i>En cas de collecte de données utilisateurs, le développeur utilisant cette class doit
 * s'engager  à les utiliser les données collecté à la strict fin de débugage et les supprimer
 * dès la fin de l'opération. L'interface utilisateur demande l'accord explicite de celui-ci
 * aavnt d'autoriser l'adjonction de toutes données utilisateurs.</i></p>
 * 
 * @author Aurelien JEOFFRAY
 *
 */
public class WebErrorReporter implements ErrorReporter {
	private URL reportUrl;
	private ApplicationContext context;
	
	/**
	 * Intialise le service de rapport d'erreur
	 * 
	 * @param reportUrl l'url ou poster le rapport
	 * @param context Information on applicatgion (app name, version, os, jvm, ... to complete error report)
	 * @param context le contexte d'execution de l'application ayant généré l'erreur
	 */
	public WebErrorReporter(URL reportUrl, ApplicationContext context) {
		this.reportUrl = reportUrl; 
		this.context = context;
	}
	
	/**
	 * Reporte l'exception
	 * 
	 * @see org.jdesktop.swingx.error.ErrorReporter#reportError(org.jdesktop.swingx.error.ErrorInfo)
	 */
	@Override
	public void reportError(final ErrorInfo errorInfo) throws NullPointerException {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				StringWriter strStackTraceWriter = new StringWriter();
				PrintWriter stackTraceWriter = new PrintWriter(strStackTraceWriter);
				errorInfo.getErrorException().printStackTrace(stackTraceWriter);
				
				try {
					String detail = "Crash on " + context.getApplicationName() + " v" + context.getApplicationVersion();
					detail += "\nEnvironment: " + context.getOSName() + " " + context.getArchitecture() + " (JVM " + context.getJVMVendor() + " " + context.getJVMVersion() + ")\n";
					detail += "\nStacktrace: \n" + strStackTraceWriter.toString();
					
					String uri = reportUrl + "?title=" + URLEncoder.encode("JVM Exception : " + errorInfo.getErrorException().getMessage(), "UTF-8") + "&body=" + URLEncoder.encode(detail, "UTF-8");
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(new URI(uri));
					}
				} catch (IOException e1) {
				
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		} else {
			r.run();
		}
	}
}
