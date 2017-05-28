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


/**
 * <p>Permit to crypt and decrypt Zip file encrypted with traditionnal PKWARE encryption</p>
 * <p><b>Here the official encryption explaination</b></p>
 * <p><pre>
 * VII. Traditional PKWARE Encryption
----------------------------------

The following information discusses the decryption steps
required to support traditional PKWARE encryption.  This
form of encryption is considered weak by today's standards
and its use is recommended only for situations with
low security needs or for compatibility with older .ZIP 
applications.

Decryption
----------

PKWARE is grateful to Mr. Roger Schlafly for his expert contribution 
towards the development of PKWARE's traditional encryption.

PKZIP encrypts the compressed data stream.  Encrypted files must
be decrypted before they can be extracted.

Each encrypted file has an extra 12 bytes stored at the start of
the data area defining the encryption header for that file.  The
encryption header is originally set to random values, and then
itself encrypted, using three, 32-bit keys.  The key values are
initialized using the supplied encryption password.  After each byte
is encrypted, the keys are then updated using pseudo-random number
generation techniques in combination with the same CRC-32 algorithm
used in PKZIP and described elsewhere in this document.

The following is the basic steps required to decrypt a file:

1) Initialize the three 32-bit keys with the password.
2) Read and decrypt the 12-byte encryption header, further
   initializing the encryption keys.
3) Read and decrypt the compressed data stream using the
   encryption keys.

Step 1 - Initializing the encryption keys
-----------------------------------------

Key(0) <- 305419896
Key(1) <- 591751049
Key(2) <- 878082192

loop for i <- 0 to length(password)-1
    update_keys(password(i))
end loop

Where update_keys() is defined as:

update_keys(char):
  Key(0) <- crc32(key(0),char)
  Key(1) <- Key(1) + (Key(0) & 000000ffH)
  Key(1) <- Key(1) * 134775813 + 1
  Key(2) <- crc32(key(2),key(1) >> 24)
end update_keys

Where crc32(old_crc,char) is a routine that given a CRC value and a
character, returns an updated CRC value after applying the CRC-32
algorithm described elsewhere in this document.

Step 2 - Decrypting the encryption header
-----------------------------------------

The purpose of this step is to further initialize the encryption
keys, based on random data, to render a plaintext attack on the
data ineffective.

Read the 12-byte encryption header into Buffer, in locations
Buffer(0) thru Buffer(11).

loop for i <- 0 to 11
    C <- buffer(i) ^ decrypt_byte()
    update_keys(C)
    buffer(i) <- C
end loop

Where decrypt_byte() is defined as:

unsigned char decrypt_byte()
    local unsigned short temp
    temp <- Key(2) | 2
    decrypt_byte <- (temp * (temp ^ 1)) >> 8
end decrypt_byte

After the header is decrypted,  the last 1 or 2 bytes in Buffer
should be the high-order word/byte of the CRC for the file being
decrypted, stored in Intel low-byte/high-byte order.  Versions of
PKZIP prior to 2.0 used a 2 byte CRC check; a 1 byte CRC check is
used on versions after 2.0.  This can be used to test if the password
supplied is correct or not.

Step 3 - Decrypting the compressed data stream
----------------------------------------------

The compressed data stream can be decrypted as follows:

loop until done
    read a character into C
    Temp <- C ^ decrypt_byte()
    update_keys(temp)
    output Temp
end loop

 * </pre></p>
 * @author Aurélien JEOFFRAY
 * 
 */
public class ZipCrc32Cryptograph {
	
	private static final int CRCPOLY = 0xedb88320;
	private static int[] crctab = new int[256];
	//private static int[] crcinvtab = new int[256];
	
	private int[] keys = new int[3];
	
	static {
		mkCrcTab();
	}

	/**
	 * 
	 */
	public ZipCrc32Cryptograph() {

	}
	
	/*
	 * Initialise le tableau CRC 32
	 */
	private static void mkCrcTab() {
		int i, j, c;

		for (i = 0; i < 256; i++) {
			c = i;
			for (j = 0; j < 8; j++) {
				if ((c & 1) != 0 )
					c = (c >>> 1) ^ CRCPOLY;
				else
					c = (c >>> 1);
			}
			crctab[i] = c;
			//crcinvtab[c >>> 24] = (c << 8) ^ i;
		}
	}

	/**
	 * Init the triple CRC encryption key with the password given
	 * 
	 * @param password the password to init encryption key
	 */
	public void cryptInitKeys(byte[] password) {
		keys[0] = 305419896;
		keys[1] = 591751049;
		keys[2] = 878082192;
		if(password != null) {
			for (byte c : password) {
				cryptUpdateKeys(c);
			}
		}
	}

	/**
	 * Update the key with the current byte of the encrypted stream
	 * 
	 * @param c the current byte of encrypted stream
	 */
	public void cryptUpdateKeys(byte c) {
		keys[0] = cryptCRC32(keys[0], c);
		keys[1] += keys[0] & 0xff;
		keys[1] = keys[1] * 134775813 + 1;
		c = (byte) (keys[1] >>> 24);
		keys[2] = cryptCRC32(keys[2], c);
	}

	/**
	 * Uncrypt the given buffer
	 * 
	 * @param buffer the  buffer to uncrypt
	 * @param off start offset to uncrypt
	 * @param size the size of data to uncrypt
	 */
	public void decode(byte[] buffer, int off, int size) {
		for (int i = off; i < off+size; i++)
			buffer[i] = cryptDecode(buffer[i]);
	}
	
	/**
	 * Crypt the given buffer with PKZIP traditionnal encoding
	 * 
	 * @param buffer the  buffer to crypt
	 * @param off start offset to crypt
	 * @param size the size of data to crypt
	 */
	void encode(byte[] buffer, int off, int size) {
		for (int i = off; i < size; i++)
			buffer[i] = cryptEncode(buffer[i]);
	}

	/*
	 * Retourne l'octet fournit en paramètre décodé
	 * et met à jour la clé 
	 */
	private byte cryptDecode(byte b) {
		b ^= cryptDecryptByte();
		cryptUpdateKeys(b);
		
		return b;
	}
	
	/*
	 * Retourne l'octet fournit en paramètre codé
	 */
	private byte cryptEncode(byte b) {
		byte t = cryptDecryptByte();
		cryptUpdateKeys(b);
		b ^= t;
		
		return b;
	}

	/*
	 * Retourne le masque de rotation pour l'octet à crypter/décrypter en fonction de la clé 2 
	 */
	private byte cryptDecryptByte() {
		int temp = ((keys[2] & 0xffff) | 2);
		return (byte) (((temp * (temp ^ 1)) >> 8) & 0xff);
	}

	/*
	 * Retourne la valeur CRC32 actualisé
	 * @param l l'ancienne valeur CRC32
	 * @param c l'octet à ajouter au calcul du CRC
	 */
	private int cryptCRC32(int l, byte c) {	
		return (((l)>>>8)^crctab[((l)^(c))&0xff]);
	}
}
