/*
 * Créé le 20 mars 2009 pour ajcommons
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
package org.ajdeveloppement.io.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * This class implements extends {@link java.util.zip.InflaterInputStream} to add support
 * of encrypted Zip stream.
 * 
 * @author Aurélien JEOFFRAY
 * 
 */
public class EncryptedInflaterInputStream extends InflaterInputStream {

	protected byte[] encryptedPassword = null;
    protected boolean encrypted = false;
	protected ZipCrc32Cryptograph cryptograph = new ZipCrc32Cryptograph();
	
	private boolean closed = false;
	private boolean reachEOF = false;

	/**
	 * Check to make sure that this stream has not been closed
	 */
	private void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("Stream closed"); //$NON-NLS-1$
		}
	}

	/**
     * Creates a new input stream with the specified uncompressor and
     * buffer size.
     * 
     * @param in the input stream
     * @param inf the uncompressor ("inflater")
     * @param size the input buffer size
     * @exception IllegalArgumentException if size is <= 0
     */
	public EncryptedInflaterInputStream(InputStream in, Inflater inf, int size) {
		super(in, inf, size);
	}
	
	boolean usesDefaultInflater = false;
	
	/**
     * Creates a new input stream with a default uncompressor and buffer size.
     * 
     * @param in the input stream
     */
	public EncryptedInflaterInputStream(InputStream in) {
		super(in);
		
		usesDefaultInflater = true;
	}
	
	/**
	 * Return the password set for encrypted stream
	 * 
	 * @return the encrypted password
	 */
	public byte[] getEncryptedPassword() {
		return encryptedPassword;
	}

	/**
	 * Set the password of stream if this stream is encrypted
	 * 
	 * @param encryptedPassword the encryptedPassword to set
	 */
	public void setEncryptedPassword(byte[] encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
		//cryptograph.cryptInitKeys(encryptedPassword);
	}
	
	/**
	 * Return if the stream is encrypted or not
	 * 
	 * @return <code>true</code> if the stream is encrypted, <code>false</code> else
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

	/**
	 * Fills input buffer with more data to uncompress.
	 * 
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	@Override
	protected void fill() throws IOException {
		ensureOpen();
		byte[] uncryptbuffer = new byte[buf.length];
		len = in.read(buf, 0, buf.length);
		System.arraycopy(buf, 0, uncryptbuffer, 0, len);
		if(encrypted)
			cryptograph.decode(uncryptbuffer, 0, len);
		if (len == -1) {
			throw new EOFException("Unexpected end of ZLIB input stream"); //$NON-NLS-1$
		}
		inf.setInput(uncryptbuffer, 0, len);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		ensureOpen();
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		try {
			int n;
			while ((n = inf.inflate(b, off, len)) == 0) {
				if (inf.finished() || inf.needsDictionary()) {
					reachEOF = true;
					return -1;
				}
				if (inf.needsInput()) {
					fill();
				}
			}
			return n;
		} catch (DataFormatException e) {
			String s = e.getMessage();
			throw new ZipException(s != null ? s : "Invalid ZLIB data format"); //$NON-NLS-1$
		}
	}
	
	/**
     * Returns 0 after EOF has been reached, otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return     1 before EOF and 0 after EOF.
     * @exception  IOException  if an I/O error occurs.
     * 
     */
    @Override
	public int available() throws IOException {
        ensureOpen();
        if (reachEOF)
            return 0;
        return 1;
    }
    
    private byte[] b = new byte[512];

    /**
	 * Skips specified number of bytes of uncompressed data.
	 * 
	 * @param n
	 *            the number of bytes to skip
	 * @return the actual number of bytes skipped.
	 * @exception IOException
	 *                if an I/O error has occurred
	 * @exception IllegalArgumentException
	 *                if n < 0
	 */
	@Override
	public long skip(long n) throws IOException {
		if (n < 0) {
			throw new IllegalArgumentException("negative skip length"); //$NON-NLS-1$
		}
		ensureOpen();
		int max = (int) Math.min(n, Integer.MAX_VALUE);
		int total = 0;
		while (total < max) {
			int len = max - total;
			if (len > b.length) {
				len = b.length;
			}
			len = read(b, 0, len);
			if (len == -1) {
				reachEOF = true;
				break;
			}
			total += len;
		}
		return total;
	}
	
	@Override
	public void close() throws IOException {
		if (!closed) {
            if (usesDefaultInflater)
                inf.end();
	    in.close();
            closed = true;
        }
	}
}
