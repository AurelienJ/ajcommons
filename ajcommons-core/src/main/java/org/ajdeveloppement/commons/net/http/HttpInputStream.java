/*
 * Créé le 10 mars 2014 à 23:13:07 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * An HTTP stream is a mixed ASCII/binary stream.
 * Expose helper to read ASCII part of stream as HTTP header with line
 * limit to avoid OutOfMemory with an invalid header.
 * 
 * @author "Aurélien JEOFFRAY"
 *
 */
public class HttpInputStream extends InputStream {
	
	private InputStream innerStream;
	
	/**
	 * 
	 * @param in 
	 */
	public HttpInputStream(InputStream in) {
		this.innerStream = in;
	}
	
	/**
	 * Read a text line in stream. A line is terminated by CR, LF or CRLF chars or end of stream.
	 * Use this for read Http headers.
	 * Attempt an us-ascii encoding to convert string
	 * 
	 * @param maxLineLength max size of length. if no end line chars is found before this limit, stop
	 * reading stream at this size. If -1 there is no limit (not recommended for security reason)
	 * @return a text line in stream at current position.
	 * @throws IOException
	 */
	public String readLine(int maxLineLength) throws IOException {
		return readLine(maxLineLength, StandardCharsets.US_ASCII);
	}
	
	/**
	 * Read a text line in stream. A line is terminated by CR, LF or CRLF chars or end of stream.
	 * Use this for read Http headers.
	 * 
	 * @param maxLineLength max size of length. if no end line chars is found before this limit, stop
	 * reading stream at this size. If -1 there is no limit (not recommended for security reason)
	 * @param charset the charset use to convert stream byte to string
	 * @return a text line in stream at current position.
	 * @throws IOException
	 */
	public String readLine(int maxLineLength, Charset charset) throws IOException {
		byte[] lineBuffer = null;
		byte[] buf = lineBuffer;

		if (buf == null) {
			buf = lineBuffer = new byte[128];
		}

		int room = buf.length;
		int offset = 0;
		int c;

		loop: while (true) {
			switch (c = read()) {
				case -1:
				case '\n':
					break loop;
	
				case '\r':
					int c2 = innerStream.read();
					if ((c2 != '\n') && (c2 != -1)) {
						if (!(innerStream instanceof PushbackInputStream)) {
							innerStream = new PushbackInputStream(innerStream);
						}
						((PushbackInputStream) innerStream).unread(c2);
					}
					break loop;
	
				default:
					if (--room < 0) {
						buf = new byte[offset + 128];
						room = buf.length - offset - 1;
						System.arraycopy(lineBuffer, 0, buf, 0, offset);
						lineBuffer = buf;
					}
					buf[offset++] = (byte)c;
					
					if(maxLineLength > -1 && offset >= maxLineLength)
						break loop;
					break;
			}
		}
		if ((c == -1) && (offset == 0)) {
			return null;
		}
		
		return new String(buf, 0, offset, charset);
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return innerStream.read();
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		return innerStream.read(b);
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return innerStream.read(b, off, len);
	}
	
	@Override
	public long skip(long n) throws IOException {
		return innerStream.skip(n);
	}
	
	@Override
	public int available() throws IOException {
		return innerStream.available();
	}
	
	@Override
	public void close() throws IOException {
		innerStream.close();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		innerStream.mark(readlimit);
	}
	
	@Override
	public synchronized void reset() throws IOException {
		innerStream.reset();
	}
	
	@Override
	public boolean markSupported() {
		return innerStream.markSupported();
	}
}
