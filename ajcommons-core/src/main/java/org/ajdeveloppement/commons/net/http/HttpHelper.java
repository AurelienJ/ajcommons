/*
 * Créé le 9 oct. 2009 à 20:25:30 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL-C telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.CeCILL.info".
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
 * pris connaissance de la licence CeCILL-C, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.CeCILL.info". 
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
 *
 */
package org.ajdeveloppement.commons.net.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

/**
 * Permet l'envoie d'une requête HTTP POST avec des fichiers joints
 * 
 * @author Aurélien JEOFFRAY
 * @version 0.5
 */
public class HttpHelper {
	
	private static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy"; //$NON-NLS-1$
	private static final String POST = "POST"; //$NON-NLS-1$

	/**
	 * 
	 */
	public HttpHelper() {

	}
	
	/**
	 * Envoie une requête post à un serveur web
	 * 
	 * @param urlConnection la connexion HTTP sur laquelle effectué la requête POST
	 * @param params les paramètres de la requête
	 * @return la réponse à la requête
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	public static InputStream sendPostRequest(URLConnection urlConnection, List<PostParameter> params) throws IOException {
		
		if(!(urlConnection instanceof HttpURLConnection))
			throw new IOException("Bad Connection type.");
		
		HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setRequestMethod(POST);
		httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
		
		OutputStream dout = httpURLConnection.getOutputStream();
		
		for(PostParameter parameter : params) {
			parameter.appendParameterToStream(dout, BOUNDARY);
		}
		dout.write(new String("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8"));
		dout.flush();
		
		return new BufferedInputStream(httpURLConnection.getInputStream());
	}
}