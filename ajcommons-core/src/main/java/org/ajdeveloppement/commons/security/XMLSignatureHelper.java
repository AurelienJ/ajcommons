/*
 * Créé le 23 sept. 2009 à 21:26:47 pour AjCommons (Bibliothèque de composant communs)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utilitaire de signature de fichier XML
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class XMLSignatureHelper {
	
	private KeyStore keyStore;
	
	/**
	 * Initialise l'utilitaire de signature XML
	 */
	public XMLSignatureHelper() {
	}
	
	/**
	 * Initialise l'utilitaire de signature XML en précisant le KeyStore
	 * hébergeant la paire clé privée/Certificat
	 * 
	 * @param keyStore le KeyStore hébergeant la paire clé privée/Certificat
	 */
	public XMLSignatureHelper(KeyStore keyStore) {
		this.keyStore = keyStore;
	}
	
	/**
	 * Retourne le KeyStore hébergeant la paire clé privée/Certificat
	 * 
	 * @return le KeyStore hébergeant la paire clé privée/Certificat
	 */
	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * Définit le KeyStore hébergeant la paire clé privée/Certificat
	 * 
	 * @param keyStore le KeyStore hébergeant la paire clé privée/Certificat
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}
	
	/**
	 * Signe un fichier XML
	 * 
	 * @param xml le fichier XML à signer
	 * @param signedXml le fichier XML signé
	 * @param signKeysAlias l'alias de la clé de signature
	 * @param signKeysAliasPassword le mot de passe de l'entrée
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableEntryException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * @throws TransformerException
	 */
	public void signXMLFile(File xml, File signedXml, String signKeysAlias, char[] signKeysAliasPassword)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, 
				IOException, UnrecoverableEntryException, SAXException, ParserConfigurationException, MarshalException, XMLSignatureException, 
				TransformerException {
		
		InputStream in = new FileInputStream(xml);
		OutputStream out = new FileOutputStream(signedXml);
		try {
			signXMLStream(in, out, signKeysAlias, signKeysAliasPassword);
		} finally {
			in.close();
			out.close();
		}
	}

	/**
	 * Signe un flux XML fournit en paramètre.
	 * 
	 * @param xml le flux xml à signé
	 * @param signedXml le flux xml signé
	 * @param signKeysAlias l'alias de la clé servant à générer la signature
	 * @param signKeysAliasPassword le mot de passe de l'entrée
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableEntryException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws MarshalException
	 * @throws XMLSignatureException
	 * @throws TransformerException
	 */
	public void signXMLStream(InputStream xml, OutputStream signedXml, String signKeysAlias, char[] signKeysAliasPassword)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, 
				IOException, UnrecoverableEntryException, SAXException, ParserConfigurationException, MarshalException, XMLSignatureException, 
				TransformerException {
		
		if(keyStore == null)
			throw new KeyStoreException("No key store is defined to sign the XML"); //$NON-NLS-1$
		
		// Create a DOM XMLSignatureFactory that will be used to
		// generate the enveloped signature.
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM"); //$NON-NLS-1$

		// Create a Reference to the enveloped document (in this case,
		// you are signing the whole document, so a URI of "" signifies
		// that, and also specify the SHA256 digest algorithm and
		// the ENVELOPED Transform.
		Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null), //$NON-NLS-1$
				Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);

		// Create the SignedInfo.
		SignedInfo si = fac.newSignedInfo(
				fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
				fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
				Collections.singletonList(ref));
		

		KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(signKeysAlias,
				new KeyStore.PasswordProtection(signKeysAliasPassword));
		X509Certificate cert = (X509Certificate) keyEntry.getCertificate();

		// Create the KeyInfo containing the X509Data.
		KeyInfoFactory kif = fac.getKeyInfoFactory();
		List<Object> x509Content = new ArrayList<Object>();
		x509Content.add(cert.getSubjectX500Principal().getName());
		x509Content.add(cert);
		X509Data xd = kif.newX509Data(x509Content);
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

		// Instantiate the document to be signed.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		
		Document doc = dbf.newDocumentBuilder().parse(xml);

		if(doc != null) {
			// Create a DOMSignContext and specify the RSA PrivateKey and
			// location of the resulting XMLSignature's parent element.
			DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc.getDocumentElement());
	
			// Create the XMLSignature, but don't sign it yet.
			XMLSignature signature = fac.newXMLSignature(si, ki);
	
			// Marshal, generate, and sign the enveloped signature.
			signature.sign(dsc);
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.transform(new DOMSource(doc), new StreamResult(signedXml));
		}
	}
}
