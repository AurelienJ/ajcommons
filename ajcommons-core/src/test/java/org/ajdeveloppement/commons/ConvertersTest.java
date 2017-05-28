/*
 * Créé le 29 nov. 2009 à 16:31:02 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aurelien JEOFFRAY
 *
 */
public class ConvertersTest extends TestCase {

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#centimeterToInch(double)}.
	 */
	@Test
	public void testCentimeterToInch() {
		assertEquals(4.724409449, Converters.centimeterToInch(12), 1e-5); //12 cm = 4.72 pouces
		assertEquals(10.62992126, Converters.centimeterToInch(27), 1e-5); //27 cm = 10.63 pouces
	}

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#inchToCentimeter(double)}.
	 */
	@Test
	public void testInchToCentimeter() {
		assertEquals(11.9888, Converters.inchToCentimeter(4.72), 1e-5);
		assertEquals(27.0002, Converters.inchToCentimeter(10.63), 1e-5);
	}

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#centimeterToDpi(double)}.
	 */
	@Test
	public void testCentimeterToDpi() {
		assertEquals(340, Converters.centimeterToDpi(12), 0);
		assertEquals(765, Converters.centimeterToDpi(27), 0);
	}

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#dpiToCentimeter(int)}.
	 */
	@Test
	public void testDpiToCentimeter() {
		assertEquals(12, Converters.dpiToCentimeter(340), 1e-1);
		assertEquals(27, Converters.dpiToCentimeter(765), 1e-1);
	}

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#byteArrayToHexString(byte[])}.
	 */
	@Test
	public void testByteArrayToHexString() {
		assertEquals(Converters.byteArrayToHexString(new byte[] { 10, 5, 32, 45, -16, -126, 123, 85} ), "0a05202df0827b55"); //$NON-NLS-1$
	}

	/**
	 * Test method for {@link org.ajdeveloppement.commons.Converters#hexStringToByteArray(java.lang.String)}.
	 */
	@Test
	public void testHexStringToByteArray() {
		Assert.assertArrayEquals(new byte[] { 10, 5, 32, 45, -16, -126, 123, 85}, Converters.hexStringToByteArray("0a05202df0827b55")); //$NON-NLS-1$
	}
}
