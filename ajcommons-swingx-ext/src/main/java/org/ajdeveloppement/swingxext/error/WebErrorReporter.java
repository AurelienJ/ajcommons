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

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;

import org.ajdeveloppement.apps.ApplicationContext;
import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.io.XMLSerializer;
import org.ajdeveloppement.commons.net.http.HttpHelper;
import org.ajdeveloppement.commons.net.http.PostParameter;
import org.ajdeveloppement.swingxext.error.ui.ErrorReporterDialog;
import org.ajdeveloppement.swingxext.error.ui.ErrorReporterDialog.Action;
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
	
	private JDialog dialog = null;
	
	private AjResourcesReader localisation = new AjResourcesReader(WebErrorReporter.class.getPackage().getName() + ".libelle"); //$NON-NLS-1$
	
	/**
	 * Intialise le service de rapport d'erreur
	 * 
	 * @param reportUrl l'url ou poster le rapport
	 * @param context le contexte d'execution de l'application ayant généré l'erreur
	 */
	public WebErrorReporter(URL reportUrl, ApplicationContext context) {
		this.reportUrl = reportUrl; 
		this.context = context;
	}
	
	/**
	 * Définit la boite de dialogue d'erreur parent afin
	 * d'avoir un modal correct.
	 * 
	 * @param dialog la boite de dialogue d'erreur parent
	 */
	public void setParentErrorDialog(JDialog dialog) {
		this.dialog = dialog;
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
				ErrorReportMessage message = new ErrorReportMessage();
				
				//ErrorInfo errInf = errorInfo;
				
				StringWriter strStackTraceWriter = new StringWriter();
				PrintWriter stackTraceWriter = new PrintWriter(strStackTraceWriter);
				errorInfo.getErrorException().printStackTrace(stackTraceWriter);
				
				message.setErrorMessage(strStackTraceWriter.toString());
				message.setContext(context);
				
				Dialog d = dialog;

				ErrorReporterDialog dialog = new ErrorReporterDialog(d);
				if(dialog.showErrorReporterDialog(message) == Action.SEND) {
					File tempErrMessage = null;
					try {
						tempErrMessage = File.createTempFile("errmsg", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			
						ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempErrMessage));
						try {
							ZipEntry entry = new ZipEntry("errorMessage.xml"); //$NON-NLS-1$
				
							zipOut.putNextEntry(entry);
							zipOut.write(XMLSerializer.createMarshallStructure(message).getBytes("UTF-8")); //$NON-NLS-1$
							zipOut.closeEntry();
							zipOut.flush();
						} finally {
							zipOut.close();
						}
						
						PostParameter errorMessagePost = new PostParameter(
								"errorReport", //$NON-NLS-1$
								"report-" +	System.currentTimeMillis() + ".zip", //$NON-NLS-1$ //$NON-NLS-2$
								new FileInputStream(tempErrMessage),
								"application/zip"); //$NON-NLS-1$
						
						URLConnection urlConnection = reportUrl.openConnection();
			
						String response = ""; //$NON-NLS-1$
						BufferedReader reader = new BufferedReader(new InputStreamReader(HttpHelper.sendPostRequest(
								urlConnection, Collections.singletonList(errorMessagePost))));
						try {
							
							String line;
							while((line = reader.readLine()) != null) {
								response += line;
							}
						} finally {
							reader.close();
						}
						
						if(response.startsWith("OK ")) { //$NON-NLS-1$
							JOptionPane.showMessageDialog(null, 
									localisation.getResourceString("weberrorreporter.confirmation"), "", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							JOptionPane.showMessageDialog(null, localisation.getResourceString("weberrorreporter.error"), "", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						
						JOptionPane.showMessageDialog(null, localisation.getResourceString("weberrorreporter.error"), "", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (MalformedURLException e) {
						e.printStackTrace();
						
						JOptionPane.showMessageDialog(null, localisation.getResourceString("weberrorreporter.error"), "", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (IOException e) {
						e.printStackTrace();
						
						JOptionPane.showMessageDialog(null, localisation.getResourceString("weberrorreporter.error"), "", JOptionPane.ERROR_MESSAGE);  //$NON-NLS-1$//$NON-NLS-2$
					} catch (JAXBException e) {
						e.printStackTrace();
						
						JOptionPane.showMessageDialog(null, localisation.getResourceString("weberrorreporter.error"), "", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
					} finally {
						if(tempErrMessage != null)
							tempErrMessage.delete();
					}
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
