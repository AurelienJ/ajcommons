/*
 * Créé le 9 oct. 2009 à 20:45:32 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
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
 * pris connaissance de la licence CeCILL-C, et que vous en avez accepté les
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
package org.ajdeveloppement.commons.net.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Représente un paramètre de méthode HTTP POST
 * 
 * @author Aurélien JEOFFRAY
 * @version 0.5
 */
public class PostParameter {
	/**
	 * Le type de paramètre passé. Soit Chaîne, soit fichier
	 * @author Aurélien JEOFFRAY
	 *
	 */
	public enum Type {
		/**
		 * Représente un paramètre de type chaîne.
		 */
		STRING,
		/**
		 * Représente un paramètre de type fichier
		 */
		FILE
	}
	
	private Type type = Type.STRING;
	private String paramName;
	private String stringParam;
	
	private String mimeType = "application/octet-stream"; //$NON-NLS-1$
	private String fileName;
	private InputStream fileStream;
	
	/**
	 * Initialise un paramètre de type chaîne
	 * 
	 * @param paramName le nom du paramètre
	 * @param stringParam la valeur du paramètre
	 */
	public PostParameter(String paramName, String stringParam) {
		this.paramName = paramName;
		this.stringParam = stringParam;
		
		type = Type.STRING;
	}
	
	/**
	 * Initialise un paramètre de type fichier
	 * 
	 * @param paramName le nom du paramètre
	 * @param fileName le nom du fichier associé
	 * @param fileStream le flux vers le fichier
	 */
	public PostParameter(String paramName, String fileName, InputStream fileStream) {
		this(paramName, fileName, fileStream, null);
	}
	
	public PostParameter(String paramName, String fileName, InputStream fileStream, String mimeType) {
		this.paramName = paramName;
		this.fileStream = fileStream;
		if(mimeType != null && !mimeType.isEmpty())
			this.mimeType = mimeType;
		this.fileName = fileName;
		
		type = Type.FILE;
	}
	
	public Type getParameterType() {
		return type;
	}

	/**
	 * @return the paramName
	 */
	public String getParameterName() {
		return paramName;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the stringParam
	 */
	public String getStringValue() {
		try {
			return URLEncoder.encode(stringParam, "UTF-8") ; //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 
		
		return null;
	}

	/**
	 * @return the octetStreamParam
	 */
	public InputStream getFileValue() {
		return fileStream;
	}
	
	public InputStream getValue() {
		if(type == Type.FILE)
			return getFileValue();

		try {
			return new ByteArrayInputStream(getStringValue().getBytes("ASCII")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	@SuppressWarnings("nls")
	public void appendParameterToStream(OutputStream out, String boundary) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		sb.append("--" + boundary + "\r\n");
		
		try {
			if(type == Type.STRING) {
				sb.append(String.format("Content-Disposition: form-data; name=\"%s\"\r\n", paramName));
				sb.append("\r\n");
				sb.append(stringParam);
				sb.append("\r\n");
				
				out.write(sb.toString().getBytes("UTF-8"));
			} else if(type == Type.FILE) {
				sb.append(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", paramName, fileName));
				sb.append(String.format("Content-Type: %s\r\n", mimeType));
				sb.append("\r\n");
				
				//ByteArrayOutputStream stream = new ByteArrayOutputStream();
				out.write(sb.toString().getBytes("UTF-8"));
				
				byte[] buffer = new byte[512];
				int nbRead = 0;
				while((nbRead = fileStream.read(buffer)) > -1) {
					out.write(buffer, 0, nbRead);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
