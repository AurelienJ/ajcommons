/*
 * Créé le 26 mai 2009 pour ajcommons
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
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA 
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
 * pri connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 *
 *  *** GNU GPL Terms *** 
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.ajdeveloppement.commons.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
/**
 * Utilitaires de manipulation des certificats X509.
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class CertUtil {
	/**
	 * Convertit un certificat au format DER (binaire) dans le format PEM (base64)
	 * 
	 * @param der le certificat au format DER
	 * @return le certificat au format PEM
	 */
	public static byte[] convertDERCertToPEM(byte[] der) {
		StringBuilder sb = new StringBuilder();
		sb.append("-----BEGIN CERTIFICATE-----\n"); //$NON-NLS-1$
		
		sb.append(Base64.getMimeEncoder(76, "\r\n".getBytes()).encodeToString(der)); //$NON-NLS-1$
		sb.append("\n-----END CERTIFICATE-----"); //$NON-NLS-1$
		
		try {
			return sb.toString().getBytes("ASCII"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Lit un certificat X509 stocké au format DER ou PEM et retourne son instance objet
	 * 
	 * @param certFile le fichier contenant le certificat
	 * @return l'objet certificat X509
	 * 
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static Certificate readCert(File certFile) throws CertificateException, IOException {
		FileInputStream certIn = new FileInputStream(certFile);
		
		return readCert(certIn);
	}
	
	/**
	 * Lit un certificat X509 stocké au format DER ou PEM et retourne son instance objet
	 * 
	 * @param in un flux contenant les données d'un certificat X509. Le flux est fermé après la lecture.
	 * @return l'objet certificat X509
	 * 
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static Certificate readCert(InputStream in) throws CertificateException, IOException {
		if(in == null)
			return null;
		
		CertificateFactory factory = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$

		Certificate certificate = null;
		try {
			certificate = factory.generateCertificate(in);
		} finally {
			in.close();
		}
		
		return certificate;
	}
	
	public static PrivateKey readPemPrivateKey(InputStream in, String algorithm) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String temp = ""; //$NON-NLS-1$
		String line = null;
		while((line = reader.readLine()) != null)
			temp += line + "\n"; //$NON-NLS-1$
	      
		 String privKeyPEM = temp.replace("-----BEGIN RSA PRIVATE KEY-----\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		 privKeyPEM = privKeyPEM.replace("\n-----END RSA PRIVATE KEY-----\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
		 //System.out.println("Private key\n"+privKeyPEM);
		
		 byte[] decoded = Base64.getMimeDecoder().decode(privKeyPEM);
		 //RSAPrivateKeySpec
		 RSAPrivateKeySpec spec = null;//new RSAPrivateKeySpec(decoded);
		 KeyFactory kf = KeyFactory.getInstance(algorithm);
		 return kf.generatePrivate(spec);
	}
}
