/*
 * Créé le 12 juin 2010 à 11:57:16 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Session de traitement de persitance d'un graphe d'objet. Conserve les références
 * des objets déjà traité par l'API de persitance afin d'éviter de les traiter plusieurs
 * fois dans une même session.
 * 
 * @author Aurelien JEOFFRAY
 *
 */
public class Session implements AutoCloseable {
	private List<ObjectPersistence> processedObjects;
	
	/**
	 * Construct a new free persistence session
	 */
	public Session() {
		
	}
	
	/**
	 * Retourne la collection d'objets déjà traité dans la session
	 * 
	 * @return the savedObject la collection d'objets déjà traité dans la session
	 */
	public List<?> getProcessedObjects() {
		if(processedObjects == null)
			return null;
		
		return Collections.unmodifiableList(processedObjects);
	}
	
	/**
	 * Ajoute un objet traité à la session
	 * 
	 * @param treatyObject l'objet à ajouter à la session
	 */
	public void addProcessedObject(ObjectPersistence treatyObject) {
		if(processedObjects == null)
			processedObjects = new ArrayList<ObjectPersistence>();
		processedObjects.add(treatyObject);
	}
	
	/**
	 * Add an instance to list of threaty instance object for a session
	 * 
	 * @param session the persitence session used to to treat object
	 * @param threatyObject the object which has been treated
	 */
	public static void addProcessedObject(Session session, ObjectPersistence threatyObject) {
		if(session != null)
			session.addProcessedObject(threatyObject);
	}
	
	/**
	 * Test si l'objet en parametre à déjà été traité dans la session
	 * 
	 * @param testedObject l'objet à tester
	 * @return <code>true</code> si l'objet est déjà traité.
	 */
	public boolean contains(ObjectPersistence testedObject) {
		if(processedObjects == null)
			return false;
		
		return processedObjects.contains(testedObject);
	}
	
	/**
	 * Retourne l'instance déjà enregistré de l'objet en référence
	 * si elle existe.
	 * 
	 * @param reference
	 * @return retourne l'instance sauvegardé, si elle existe de l'objet fournit en paramètre.
	 * Se base sur {@link Object#equals(Object)} et {@link Object#hashCode()}
	 */
	@SuppressWarnings("unchecked")
	public <T extends ObjectPersistence> T getSavedInstanceOf(T reference) {
		if(processedObjects.contains(reference))
			return (T)processedObjects.get(processedObjects.indexOf(reference));
		return null;
	}
	
	/**
	 * Termine la session et vide la collection d'objet traité.
	 * A utiliser pour éviter des fuites de mémoire.
	 */
	@Override
	public void close() {
		if(processedObjects != null) {
			processedObjects.clear();
			processedObjects = null;
		}
	}
	
	/**
	 * Test, in case of session is not null, if testedObject is not already threaty in
	 * the session.
	 * 
	 * @param session the session for test object
	 * @param testedObject the object to test in the session
	 * @return <code>false</code> only if session is not null and testedObject is already
	 * threaty in the given session.
	 */
	public static boolean canExecute(Session session, ObjectPersistence testedObject) {
		return (session == null || !session.contains(testedObject));
	}
	
	/**
	 * Return if session is enable or not. It's just a syntax sugar to
	 * test if session object is not null.
	 * 
	 * @param session the tested session reference
	 * @return <code>true</code> if and only if session is not null
	 */
	public static boolean isEnable(Session session) {
		return session != null;
	}
}
