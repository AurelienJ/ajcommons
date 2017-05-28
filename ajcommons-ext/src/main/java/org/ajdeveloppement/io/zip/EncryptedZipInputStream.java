/*
 * Copyright 1996-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package org.ajdeveloppement.io.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * <p>This class implements an input stream filter for reading files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.</p>
 * <p>It support the traditional PKWARE encrypted archive by set the {@link EncryptedInflaterInputStream#setEncryptedPassword(byte[])}
 * before call the method {@link EncryptedZipInputStream#getNextEntry()}</p>
 *
 * @author      David Connelly
 * @author      Aurélien JEOFFRAY
 */
public class EncryptedZipInputStream extends EncryptedInflaterInputStream implements ZipConstants {
    private ZipEntry entry;
    private int flag;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];

    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;

    private boolean closed = false;
    // this flag is set to true after EOF has reached for
    // one entry
    private boolean entryEOF = false;

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed"); //$NON-NLS-1$
        }
    }

    /**
     * Creates a new ZIP input stream.
     * @param in the actual input stream
     */
    public EncryptedZipInputStream(InputStream in) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null"); //$NON-NLS-1$
        }
    }

    /**
     * Reads the next ZIP file entry and positions the stream at the
     * beginning of the entry data.
     * @return the next ZIP file entry, or null if there are no more entries
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();
        }
        crc.reset();
        inf.reset();
        cryptograph.cryptInitKeys(encryptedPassword);
        if ((entry = readLOC()) == null) {
            return null;
        }
        if (entry.getMethod() == STORED) {
            remaining = entry.getSize();
        }
        
        entryEOF = false;
        return entry;
    }

    /**
     * Closes the current ZIP entry and positions the stream for reading the
     * next entry.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    @SuppressWarnings("all")
    public void closeEntry() throws IOException {
        ensureOpen();
        while (read(tmpbuf, 0, tmpbuf.length) != -1);
        entryEOF = true;
    }

    /**
     * Returns 0 after EOF has reached for the current entry data,
     * otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return     1 before EOF and 0 after EOF has reached for current entry.
     * @exception  IOException  if an I/O error occurs.
     *
     */
    @Override
	public int available() throws IOException {
        ensureOpen();
        if (entryEOF)
            return 0;
        return 1;
    }

    /**
     * Reads from the current ZIP entry into an array of bytes.
     * If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     * @param b the buffer into which the data is read
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         entry is reached
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    @Override
	public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        if (entry == null) {
            return -1;
        }
        switch (entry.getMethod()) {
	        case DEFLATED:
	            len = super.read(b, off, len);
	            if (len == -1) {
	                readEnd(entry);
	                entryEOF = true;
	                entry = null;
	            } else {
	                crc.update(b, off, len);
	            }
	            return len;
	        case STORED:
	            if (remaining <= 0) {
	                entryEOF = true;
	                entry = null;
	                return -1;
	            }
	            if (len > remaining) {
	                len = (int)remaining;
	            }
	            len = in.read(b, off, len);
	            
	            if (len == -1) {
	                throw new ZipException("unexpected EOF"); //$NON-NLS-1$
	            }
	            
	            //support of PKZIP traditional crypted archive
	            if(encrypted)
	    			cryptograph.decode(b, off, len);
	            crc.update(b, off, len);
	            remaining -= len;
	            if (remaining == 0 && entry.getCrc() != crc.getValue()) {
	                throw new ZipException(
	                    "invalid entry CRC (expected 0x" + Long.toHexString(entry.getCrc()) + //$NON-NLS-1$
	                    " but got 0x" + Long.toHexString(crc.getValue()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	            }
	            return len;
	        default:
	            throw new ZipException("invalid compression method"); //$NON-NLS-1$
        }
    }

    /**
     * Skips specified number of bytes in the current ZIP entry.
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException if n < 0
     */
    @Override
	public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length"); //$NON-NLS-1$
        }
        ensureOpen();
        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > tmpbuf.length) {
                len = tmpbuf.length;
            }
            len = read(tmpbuf, 0, len);
            if (len == -1) {
                entryEOF = true;
                break;
            }
            total += len;
        }
        return total;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * @exception IOException if an I/O error has occurred
     */
    @Override
	public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }

    private byte[] b = new byte[256];

    /*
     * Reads local file (LOC) header for next entry.
     */
    private ZipEntry readLOC() throws IOException {
        try {
            readFully(tmpbuf, 0, LOCHDR);
        } catch (EOFException e) {
            return null;
        }
        if (get32(tmpbuf, 0) != LOCSIG) {
            return null;
        }
        // get the entry name and create the ZipEntry first
        int len = get16(tmpbuf, LOCNAM);
        int blen = b.length;
        if (len > blen) {
            do
                blen = blen * 2;
            while (len > blen);
            b = new byte[blen];
        }
        readFully(b, 0, len);
        ZipEntry e = createZipEntry(new String(b, 0, len, "UTF-8")); //$NON-NLS-1$
        // now get the remaining fields for the entry
        flag = get16(tmpbuf, LOCFLG);
        encrypted = (flag & 1) == 1;

        e.setMethod(get16(tmpbuf, LOCHOW));
        e.setTime(get32(tmpbuf, LOCTIM));
        if ((flag & 8) == 8) {
            /* "Data Descriptor" present */
            if (e.getMethod() != DEFLATED) {
                throw new ZipException(
                        "only DEFLATED entries can have EXT descriptor"); //$NON-NLS-1$
            }
            e.setSize(get32(tmpbuf, LOCLEN));
        } else {
            e.setCrc(get32(tmpbuf, LOCCRC));
            e.setCompressedSize(get32(tmpbuf, LOCSIZ));
            e.setSize(get32(tmpbuf, LOCLEN));
        }
        len = get16(tmpbuf, LOCEXT);
        if (len > 0) {
            byte[] bb = new byte[len];
            readFully(bb, 0, len);
            e.setExtra(bb);
        }
        if(encrypted) {
        	//If encrypted entry 12 random byte must be read to init the crypt key
        	if(encryptedPassword != null && encryptedPassword.length > 0) {
	        	byte[] randbuff = new byte[RANDHEADSIZE];
	        	readFully(randbuff, 0, RANDHEADSIZE);
	        	cryptograph.decode(randbuff, 0, RANDHEADSIZE);
        	} else {
        		throw new ZipException("Encrypted password must defined for entry"); //$NON-NLS-1$
        	}
        }
        return e;
    }

    /**
     * Creates a new <code>ZipEntry</code> object for the specified
     * entry name.
     *
     * @param name the ZIP file entry name
     * @return the ZipEntry just created
     */
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    /*
     * Reads end of deflated entry as well as EXT descriptor if present.
     */
    private void readEnd(ZipEntry e) throws IOException {
        int n = inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream)in).unread(buf, len - n, n);
        }
        if ((flag & 8) == 8) {
            /* "Data Descriptor" present */
            readFully(tmpbuf, 0, EXTHDR);
            long sig = get32(tmpbuf, 0);
            if (sig != EXTSIG) { // no EXTSIG present
            	if(sig != LOCSIG) {
            		e.setCrc(sig);
	                e.setCompressedSize(get32(tmpbuf, EXTSIZ - EXTCRC) + ((encrypted) ? -RANDHEADSIZE : 0));
	                e.setSize(get32(tmpbuf, EXTLEN - EXTCRC));
	                
	                ((PushbackInputStream)in).unread(
                            tmpbuf, EXTHDR - EXTCRC - 1, EXTCRC);
            	} else {
            		((PushbackInputStream)in).unread(
                            tmpbuf, 0, EXTHDR);
            	}
                
            } else {
                e.setCrc(get32(tmpbuf, EXTCRC));
                e.setCompressedSize(get32(tmpbuf, EXTSIZ) + ((encrypted) ? -RANDHEADSIZE : 0));
                e.setSize(get32(tmpbuf, EXTLEN));
            }
        }
        if (e.getSize() != inf.getBytesWritten()) {
        	//Tente de relocaliser le curseur sur l'entrée suivante
        	
        	byte[] b = new byte[8];
        	long sig = 0;
        	int offset = 0;
        	int readSize = 8;
        	//int nbRead = 0;
        	while(sig != LOCSIG && in.read(b, offset, readSize) != -1) {
        		int pos = 0;
        		for(; pos <= 4 && sig != LOCSIG; pos++) {
        			sig = get32(b, pos);
        		}
        		
        		if(sig == LOCSIG) {
        			((PushbackInputStream)in).unread(b, pos-1, 8-(pos-1));
        		}
        		
        		b[0] = b[4];
        		b[1] = b[5];
        		b[2] = b[6];
        		b[3] = b[7];
        		offset = 4;
        		readSize = 4;
        	}
        	
        	//in.read(b, 0, 4);
        	//System.out.println(get32(b, 0));
        	
        	entry = null;
        	
        	//((PushbackInputStream)in).r
        	
            throw new ZipException(
                "invalid entry size (expected " + e.getSize() + //$NON-NLS-1$
                " but got " + inf.getBytesWritten() + " bytes)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (e.getCompressedSize() != 0 && e.getCompressedSize() != inf.getBytesRead()) {
        	entry = null;
        	
            throw new ZipException(
                "invalid entry compressed size (expected " + e.getCompressedSize() + //$NON-NLS-1$
                " but got " + inf.getBytesRead() + " bytes)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (e.getCrc() != 0 && e.getCrc() != crc.getValue()) {
            throw new ZipException(
                "invalid entry CRC (expected 0x" + Long.toHexString(e.getCrc()) + //$NON-NLS-1$
                " but got 0x" + Long.toHexString(crc.getValue()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * Reads bytes, blocking until all bytes are read.
     */
    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }

    /*
     * Fetches unsigned 16-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    private static final int get16(byte b[], int off) {
        return (b[off] & 0xff) | ((b[off+1] & 0xff) << 8);
    }

    /*
     * Fetches unsigned 32-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    private static final long get32(byte b[], int off) {
        return get16(b, off) | ((long)get16(b, off+2) << 16);
    }
}