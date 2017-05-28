/*
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

import java.nio.ByteBuffer;



/**
 * Methodes permettant de simplifier la manipulation des tableaux
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class ArraysUtils {
	/**
	 * Interverti deux elements d'un tableau représenté par
	 * leurs indices
	 * 
	 * @param <T> le type du tableau à manipuler
	 * @param table le tableau contenant les éléments à intervertir
	 * @param index1 l'index du premier element
	 * @param index2 l'index du second element
	 */
	public static <T> void swap(T[] table, int index1, int index2) {
		T tempObject = table[index1];
		table[index1] = table[index2];
		table[index2] = tempObject;
	}
	
	/**
	 * Recherche si le tableau fournit en parametre contient la valeur searchValue
	 * @param <T> le type du tableau à traiter
	 * @param table le tableau de référence
	 * @param searchValue la valeur à rechercher dans le tableau
	 * 
	 * @return true si la valeur à été trouvé, false sinon
	 */
	public static <T> boolean contains(T[] table, T searchValue) {
		for(T element : table) {
			if(element.equals(searchValue) && element.hashCode() == searchValue.hashCode())
				return true;
		}
		return false;
	}
	
	/**
	 * Recherche un pattern binaire dans un ByteBuffer
	 * 
	 * @param source le buffer dans lequel rechercher lepattern
	 * @param searchPattern le pattern à trouver
	 * @param fromIndex la positionde départ de la recherche
	 * 
	 * @return la position du pattern ou -1 si non trouvé
	 */
	public static int binaryIndexOf(ByteBuffer source, byte[] searchPattern, int fromIndex) {
		boolean find = false;
		int i;
		for(i = fromIndex; i < source.limit() - searchPattern.length; i++) {
			if (source.get(i) == searchPattern[0]) {
				find = true;
				
				for(int j = 0; j < searchPattern.length; j++) {
					if(source.get(i + j) != searchPattern[j]) {
						find = false;
						break;
					}
				}
				
				if(find)
					return i;
			}			
		}
		return -1;
	}
	
	/**
	 * Recherche un pattern binaire dans un tableau d'octet
	 * 
	 * @param source le buffer dans lequel rechercher lepattern
	 * @param searchPattern le pattern à trouver
	 * @param fromIndex la positionde départ de la recherche
	 * 
	 * @return la position du pattern ou -1 si non trouvé
	 */
	public static int binaryIndexOf(byte[] source, byte[] searchPattern, int fromIndex) {
		boolean find = false;
		int i;
		for(i = fromIndex; i < source.length - searchPattern.length; i++) {
			if (source[i] == searchPattern[0]) {
				find = true;
				
				for(int j = 0; j < searchPattern.length; j++) {
					if(source[i + j] != searchPattern[j]) {
						find = false;
						break;
					}
				}
				
				if(find)
					return i;
			}			
		}
		return -1;
	}
}
