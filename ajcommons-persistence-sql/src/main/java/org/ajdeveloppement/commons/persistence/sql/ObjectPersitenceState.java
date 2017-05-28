/*
 * Créé le 18 août 2010 à 11:10:21 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.persistence.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * Afin de réduire le nombre de requête effectué lors d'une opération de sauvegarde d'un graphe d'objet,
 * stock le hash des valeurs de l'objet à sa monté en mémoire et permet de comparer avec le hash de l'objet
 * au moment de l'opération de sauvegarde. Si le hash n'a pas évolué, alors on peut considéré que l'objet n'a
 * pas été modifié et ne nécessite pas d'opération de sauvegarde.  
 * 
 * @author Aurelien JEOFFRAY
 *
 */
class ObjectPersitenceState {
	
	public static enum State {
		NEW,
		UNMODIFIED,
		MODIFIED,
		DELETED
	}
	
	private static class ObjectMetaData {
		private Map<Class<?>, Integer> hashs = new HashMap<Class<?>, Integer>(2);
		private boolean deleted = false;
	}
	
	private static Map<SqlContext, Map<Object, ObjectMetaData>> contextsMetadatas = new WeakHashMap<SqlContext, Map<Object, ObjectMetaData>>(); //500, 0.6f
	
	/**
	 * Calcul et retourne le hash d'un objet sur une class persistante données de l'objet
	 * 
	 * @param objectPersistence l'objet pour lequel calculer le hash
	 * @param persistentClass la class persitante appartenant à l'objet pour laquel calculer le hash. Les propriétés de l'objet
	 * en dehors de cette class ne seront pas pris en compte dans le calcul du hash ne devant pas faire partie de la même table.
	 * @return le hash de l'objet pour la table correspondant au type persistant fournit
	 * @throws ObjectPersistenceException
	 */
	public static int calculateHash(Object objectPersistence, Class<?> persistentClass) throws ObjectPersistenceException {
		return calculateHash(objectPersistence, persistentClass, null, false, null);
	}
	
	/**
	 * Calcul et retourne le hash d'un objet sur une class persistante données de l'objet
	 * 
	 * @param objectPersistence l'objet pour lequel calculer le hash
	 * @param persistentClass la class persistante appartenant à l'objet pour laquelle calculer le hash. Les propriétés de l'objet
	 * en dehors de cette class ne seront pas pris en compte dans le calcul du hash ne devant pas faire partie de la même table.
	 * @param externalValues valeur externe à l'objet (unmappedf field) et venant compléter le calcul du hash
	 * @param saveHash indique si le hash doit être sauvegarder pour comparaison futur
	 * @return le hash de l'objet pour la table correspondant au type persistant fournit
	 * @throws ObjectPersistenceException
	 */
	public static int calculateHash(Object objectPersistence, Class<?> persistentClass, 
			Map<String, Object> externalValues, boolean saveHash, SqlContext context) throws ObjectPersistenceException {
		int valuesHashCode = 11;

		Class<?> persistentType = ReflectionTools.getFirstPersistentClass(objectPersistence.getClass());
		Method m = ReflectionTools.getHashMethod(persistentType);
		if(m == null) {
		
			for(Field field : ReflectionTools.getSqlFields(persistentType)) {
				try {
					Object value = field.get(objectPersistence);
					valuesHashCode = 31 * valuesHashCode + (value != null ? value.hashCode() : 0);
				} catch (IllegalArgumentException e) {
					valuesHashCode = 31 * valuesHashCode;
				} catch (IllegalAccessException e) {
					valuesHashCode = 31 * valuesHashCode;
				}
			}
			
			for(Field field : ReflectionTools.getSqlForeignKeys(persistentType)) {
				try {
					String[] foreignKeyFields = ReflectionTools.getForeignKeyFieldsName(field);
					
					if(externalValues != null) {
						for(String foreignKey : foreignKeyFields) {
							Object foreignValue = externalValues.get(foreignKey);
							
							valuesHashCode = 31 * valuesHashCode + (foreignValue != null ? foreignValue.hashCode() : 0);
						}
					} else {
						Object value = field.get(objectPersistence);
	
						if(value != null) {
							Class<?> foreignPersitentType = ReflectionTools.getFirstPersistentClass(value.getClass());
							if(foreignPersitentType == null)
								throw new ObjectPersistenceException("Foreign key object must contains SqlTable annotation");   //$NON-NLS-1$
							
							String[] valuePK = ReflectionTools.getPrimaryKeyFieldsName(foreignPersitentType);
							if(valuePK == null)
								throw new ObjectPersistenceException("Foreign key object must contains SqlPrimaryKey annotation");  //$NON-NLS-1$
							
							if(foreignKeyFields.length != valuePK.length)
								throw new ObjectPersistenceException("Invalid foreign key length : " + Arrays.deepToString(foreignKeyFields));  //$NON-NLS-1$
							
							QueryData<Object> qdata = new QueryData<Object>(value, true, context);
							for(int i = 0; i < valuePK.length; i++) {
								Object foreignValue = qdata.getObjectIdValues().get(valuePK[i]);
								
								valuesHashCode = 31 * valuesHashCode + (foreignValue != null ? foreignValue.hashCode() : 0);
							}
						} else {
							valuesHashCode = 31 * valuesHashCode;
						}
					}
				} catch (IllegalArgumentException e) {
					valuesHashCode = 31 * valuesHashCode;
				} catch (IllegalAccessException e) {
					valuesHashCode = 31 * valuesHashCode;
				}
			}
		} else {
			try {
				valuesHashCode = (int)m.invoke(objectPersistence);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ObjectPersistenceException(e);
			}
		}
		
		if(saveHash)
			putHash(objectPersistence, persistentClass, valuesHashCode, context);
		
		return valuesHashCode;
	}
	
	/**
	 * Retourne les métas données d'un objet de persistance en les créants aux besoin
	 * 
	 * @param objectPersistence l'objet pour lequel retourner les métadonnées
	 * @return les métadonnées de l'objet
	 */
	public static ObjectMetaData getObjectMetaData(Object objectPersistence, SqlContext context) {
		Map<Object, ObjectMetaData> metadatas = contextsMetadatas.get(context);
		if(metadatas == null) {
			metadatas = new WeakHashMap<>(500, 0.6f);
			contextsMetadatas.put(context, metadatas);
		}
		
		ObjectMetaData objectMetaData = metadatas.get(objectPersistence);
		if(objectMetaData == null) {
			objectMetaData = new ObjectMetaData();
			metadatas.put(objectPersistence, objectMetaData);
		}

		return objectMetaData;
	}
	
	/**
	 * Ajoute le hash données dans la table des références de hash chargé
	 * 
	 * @param objectPersistence l'objet auquel appartient le hash
	 * @param persistentClass le type persitant lié au hash
	 * @param hash le hash
	 */
	public static synchronized void putHash(Object objectPersistence, Class<?> persistentClass, int hash, SqlContext context) {
		ObjectMetaData objectMetaData = getObjectMetaData(objectPersistence, context);
		objectMetaData.hashs.put(persistentClass, hash);
	}
	
	/**
	 * retourne le hash d'un objet donné sur un type persitant donné
	 * 
	 * @param objectPersistence l'objet auquel appartient le hash à retourner
	 * @param persistentClass la class persistante correspondant au hash
	 * @return 0 si pas de hash trouvé, sinon le hash de la couche demandé
	 */
	public static int getHash(Object objectPersistence, Class<?> persistentClass, SqlContext context) {
		ObjectMetaData objectMetaData = getObjectMetaData(objectPersistence, context);
		if(objectMetaData != null)
			return objectMetaData.hashs.get(persistentClass);
		
		return 0;
	}
	
	/**
	 * Définit l'état "supprimé" de l'objet
	 * 
	 * @param objectPersistence l'objet pour lequel définir l'état
	 * @param deleted l'état de suppression de l'objet
	 */
	public static synchronized void setDeleted(Object objectPersistence, boolean deleted, SqlContext context) {
		ObjectMetaData objectMetaData = getObjectMetaData(objectPersistence, context);
		objectMetaData.deleted = deleted;
	}
	
	/**
	 * Indique si l'objet a été supprimé en base ou non
	 * 
	 * @param objectPersistence l'objet à tester
	 * @return l'état de suppression de l'objet
	 */
	public static boolean isDeleted(Object objectPersistence, SqlContext context) {
		ObjectMetaData objectMetaData = getObjectMetaData(objectPersistence, context);
		if(objectMetaData != null)
			return objectMetaData.deleted;
		
		return false;
	}
	
	/**
	 * Retourne l'état de persistance de l'objet
	 * 
	 * @param objectPersistence l'objet à tester
	 * @param peristentClass la couche de l'objet à tester
	 * @return l'état de persistance
	 * 
	 * @throws ObjectPersistenceException
	 */
	public static State getState(Object objectPersistence, Class<?> peristentClass, SqlContext context) throws ObjectPersistenceException {
		if(context != null) {
			Map<Object, ObjectMetaData> metadatas = contextsMetadatas.get(context);
			if(metadatas != null) {
				ObjectMetaData objectMetaData = metadatas.get(objectPersistence);
				if(objectMetaData != null) {
					if(objectMetaData.deleted)
						return State.DELETED;
					else if(hashHasChanged(objectPersistence, peristentClass, context))
						return State.MODIFIED;
					else
						return State.UNMODIFIED;
				}
			}
		}
		
		return State.NEW;
	}
	
	/**
	 * Supprime une instance de la table des metadatas
	 * 
	 * @param objectPersistence l'objet pour lequel supprimer sons metadatas
	 */
	public static synchronized void remove(Object objectPersistence, SqlContext context) {
		Map<Object, ObjectMetaData> metadatas = contextsMetadatas.get(context);
		if(metadatas != null)
			metadatas.remove(objectPersistence);
	}
	
	/**
	 * indique si le hash d'un objet à changé par rapport à son hash de référence stocké
	 * 
	 * @param objectPersistence
	 * @param peristentClass
	 * @return true si le hash à changé, false sinon
	 * @throws ObjectPersistenceException
	 */
	public static boolean hashHasChanged(Object objectPersistence, Class<?> peristentClass, SqlContext context) throws ObjectPersistenceException {
		Map<Object, ObjectMetaData> metadatas = contextsMetadatas.get(context);
		if(metadatas != null && metadatas.containsKey(objectPersistence) && metadatas.get(objectPersistence).hashs.containsKey(peristentClass)) {
			int currentHash = calculateHash(objectPersistence, peristentClass);
			return metadatas.get(objectPersistence).hashs.get(peristentClass) != currentHash;
		}
		return true;
	}
}
