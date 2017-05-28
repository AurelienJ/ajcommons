/*
 * Créé le 14 déc. 2014 à 13:33:54 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2014 - Aurélien JEOFFRAY
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
package org.ajdeveloppement.commons.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aurelien
 *
 */
public class HttpPostMultipartInputStream extends InputStream {

	private HttpInputStream innerStream;
	
	private Deque<Integer> lastReadsByte = new ArrayDeque<>();
	private int[] boundaryArray;
	private boolean boundaryRead = false;

	/**
	 * 
	 * @param the inner inputstream
	 */
	public HttpPostMultipartInputStream(HttpInputStream in) {
		this.innerStream = in;
	}
	
	/**
	 * 
	 * @param boundary
	 * @throws IOException 
	 */
	@SuppressWarnings("nls")
	public Entry nextEntry(String boundary) throws IOException {
		char[] boundaryAsChars = ("--" + boundary).toCharArray();
		boundaryArray = new int[boundaryAsChars.length];
		//Conversion en int
		for(int i = 0; i < boundaryArray.length; i++) {
			boundaryArray[i] = boundaryAsChars[i];
		}
		
		String line = "--" + boundary;
		if(!boundaryRead)
			line = innerStream.readLine(1024);
		
		boundaryRead = false;
		if(line.equals("--" + boundary)) { //$NON-NLS-1$
			Entry entry = new Entry(boundary); //$NON-NLS-1$
			while(!(line = innerStream.readLine(1024)).isEmpty()) {
				int separatorIndex = line.indexOf(":"); //$NON-NLS-1$
				if(separatorIndex > -1) {
					String key = line.substring(0, separatorIndex).trim().toLowerCase();
					String value = line.substring(separatorIndex+1).trim();
					
					entry.headers.put(key, value);
				} else if(line.equals("--")) {
					return null;
				}
			}
			
			return entry;
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		//lire jusqu'a remplissage du buffer
		int[] buffer;
		
		while(lastReadsByte.size() < boundaryArray.length) {
			lastReadsByte.addLast(innerStream.read());
		}
		
		//une fois remplit retourne le 1er octet lu
		//si le buffer != --boundary
		//sinon retoune -1 (EOF)
		buffer = lastReadsByte.stream().mapToInt(i -> i).toArray();
		
		if(Arrays.equals(buffer, boundaryArray)) {
			boundaryRead = true;
			return -1;
		}
		return lastReadsByte.pollFirst();
	}

	public class Entry {
		private String boundary;
		private Map<String, String> headers = new HashMap<>();
		
		/**
		 * @param boundary
		 * @param name
		 */
		public Entry(String boundary) {
			super();
			this.boundary = boundary;
		}

		/**
		 * @return the headers
		 */
		public Map<String, String> getHeaders() {
			return headers;
		}

		@SuppressWarnings("nls")
		public boolean isMultipartEntry() {
			if(getHeaders().containsKey("content-type"))
				return getHeaders().get("content-type").toLowerCase().contains("multipart/mixed");
			
			return false;
		}
		
		@SuppressWarnings("nls")
		public boolean isFormData() {
			if(getHeaders().containsKey("content-disposition"))
				return getHeaders().get("content-disposition").toLowerCase().contains("form-data");
			
			return false;
		}
		
		@SuppressWarnings("nls")
		public boolean isAttachedContent() {
			if(getHeaders().containsKey("content-disposition"))
				return getHeaders().get("content-disposition").toLowerCase().contains("attachment");
			
			return false;
		}
		
		@SuppressWarnings("nls")
		public String getMultipartBoundary() {
			if(getHeaders().containsKey("content-type") && getHeaders().get("content-type").toLowerCase().contains("multipart/mixed")
					&& getHeaders().get("content-type").toLowerCase().contains("boundary=")) {
				String contentType = getHeaders().get("content-type");
				String subBoundary;
				String[] values = contentType.split(",");
				for(String value : values) {
					if(value.trim().toLowerCase().startsWith("boundary=")) {
						return value.trim().substring(9);
					}
				}
			}
			return null;
		}
		
		@SuppressWarnings("nls")
		public String getFormDataName() {
			if(getHeaders().containsKey("content-disposition") && getHeaders().get("content-disposition").toLowerCase().contains("form-data")
					&& getHeaders().get("content-disposition").toLowerCase().contains("name=")) {
				String contentType = getHeaders().get("content-disposition");
				String subBoundary;
				String[] values = contentType.split(";");
				for(String value : values) {
					if(value.trim().toLowerCase().startsWith("name=")) {
						return value.trim().substring(5);
					}
				}
			}
			return null;
		}
		
		@SuppressWarnings("nls")
		public String getFileName() {
			if(getHeaders().containsKey("content-disposition") /*&& getHeaders().get("content-disposition").toLowerCase().contains("attachment")*/
					&& getHeaders().get("content-disposition").toLowerCase().contains("filename=")) {
				String contentType = getHeaders().get("content-disposition");
				String subBoundary;
				String[] values = contentType.split(";");
				for(String value : values) {
					if(value.trim().toLowerCase().startsWith("filename=")) {
						String fileName = value.trim().substring(9);
						if(fileName.startsWith("\""))
							fileName = fileName.substring(1);
						if(fileName.endsWith("\""))
							fileName = fileName.substring(0, fileName.length() - 1);
						return fileName;
					}
				}
			}
			return null;
		}

		/**
		 * @return the boundary
		 */
		public String getBoundary() {
			return boundary;
		}
	}
}
