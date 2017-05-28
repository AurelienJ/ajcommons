/*
 * Créé le 9 avr. 2010 à 23:34:04 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2010 - Aurélien JEOFFRAY
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
package org.ajdeveloppement.commons;

/**
 * Collection de méthode de conversion Binaire&lt;-&gt;Hexa, Centimetre&lt;-&gt;Pouce, Centimetre&lt;-&gt;Point
 * 
 * @author Aurelien JEOFFRAY
 * @version 1.0
 */
public class Converters {
	/**
	 * Convertit les centimetres en pouces
	 * 
	 * @param centimeter les centimetres à convertir
	 * @return les pouces retourné
	 */
	public static double centimeterToInch(double centimeter) {
		return centimeter / 2.54; //1 pouce = 2.54cm
	}
	
	/**
	 * Convertit les pouces en centimetres
	 * 
	 * @param inch les pouces à convertir
	 * @return les centimetres convertit
	 */
	public static double inchToCentimeter(double inch) {
		return inch * 2.54; //1 pouce = 2.54cm
	}
	
	/**
	 * Convertit des centimetres en Point par Pouce
	 * 
	 * @param centimeter les centimetres à convertir
	 * @return les point par pouce retourné
	 */
	public static int centimeterToDpi(double centimeter) {
		return Math.round((int)(centimeterToInch(centimeter) * 72)); //1 pouce = 72 points
	}
	
	/**
	 * Convertit les points par pouce en centimetre
	 * 
	 * @param dpi les points par pouce à convertir
	 * @return le nombre de centimetre resultant
	 */
	public static double dpiToCentimeter(int dpi) {
		return inchToCentimeter(dpi / 72.0); //1 pouce = 72 points
	}
	
	/**
	 * Convertit un tableau d'octet en chaine chaine hexadecimal
	 * 
	 * @param buffer le tableau d'octet à convertir en hexadecimal
	 * @return la chaine hexadecimal representant le tableau d'octet
	 */
	public static String byteArrayToHexString(byte[] buffer) {
		String result = ""; //$NON-NLS-1$
		for (byte b : buffer) {
			result += String.format("%02x", b & 0xff); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * Convertit une chaine hexadecimal en tableau d'octet
	 * 
	 * @param s la chaine à convertir
	 * @return le tableau d'octet correspondant
	 * @throws NumberFormatException 
	 */
	public static byte[] hexStringToByteArray(String s) throws NumberFormatException {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	    	data[i / 2] = Integer.decode("#" + s.substring(i, i+2)).byteValue(); //$NON-NLS-1$
	    }
	    return data;
	}
}
