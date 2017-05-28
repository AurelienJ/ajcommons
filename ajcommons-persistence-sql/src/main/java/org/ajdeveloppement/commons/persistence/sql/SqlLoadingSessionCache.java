/*
 * Créé le 23 juil. 2012 à 18:14:23 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2012 - Aurélien JEOFFRAY
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * A temp loading cache associate to the session to prevent
 * multiple instance of same data.
 * 
 * @author Aurelien JEOFFRAY
 */
public class SqlLoadingSessionCache {
	private Map<Class<?>, Map<Key,Object>> cacheContent = new HashMap<Class<?>, Map<Key,Object>>(500, 0.6f);
	
	/**
	 * Return the primary key of a persitent object instance
	 * 
	 * @param obj the persitent object instance
	 * @return the prmary keyj of the instance
	 * @throws ObjectPersistenceException
	 */
	public static Key getObjectKey(Object obj) throws ObjectPersistenceException {
		Class<?> persistentType = ReflectionTools.getFirstPersistentClass(obj.getClass());
		
		QueryData<Object> qdata = new QueryData<Object>(obj, persistentType, true, true, null);
		Map<String, Object> objectIdValues = qdata.getObjectIdValues();
		String[] pkFieldsName = ReflectionTools.getPrimaryKeyFieldsName(persistentType);
		if(pkFieldsName != null) {
			List<Object> values = new ArrayList<>();
			for(String pkColumn : pkFieldsName) {
				values.add(objectIdValues.get(pkColumn));
			}
			
			return new Key(values.toArray());
		}
		
		return null;
	}
	
	/**
	 * Put instance in cache if it is not already present
	 * 
	 * @param obj the instant put in cache
	 * @throws ObjectPersistenceException
	 */
	public void put(Object obj) throws ObjectPersistenceException {
		Map<Key,Object> innerMap = null;
		synchronized (cacheContent) {
			innerMap = cacheContent.get(obj.getClass());
			if(innerMap == null) {
				innerMap = Collections.synchronizedMap(new HashMap<Key,Object>(2));
				cacheContent.put(obj.getClass(), innerMap);
			}
		}
		
		Key key = getObjectKey(obj);
		
		if(key != null) {
			synchronized (innerMap) {
				if(!innerMap.containsKey(key))
					innerMap.put(key, obj);
			}
		}
	}
	
	/**
	 * Return the instance of specified persitent Type in cache if present
	 * 
	 * @param clazz the persitent type
	 * @param key the primary key of search instance
	 * @return the instance in cache if exists
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz, Key key) {
		Map<Key,Object> typeMap = cacheContent.get(clazz);
		if(typeMap != null)
			return (T)typeMap.get(key);
		
		return null;
	}
	
	/**
	 * Return if an instance of specified persitent Type in cache if present or not
	 * 
	 * @param clazz the persitent type
	 * @param key the primary key of search instance
	 * @return <code>true</code> if an instance exists in cache
	 */
	public boolean containsKey(Class<?> clazz, Key key) {
		Map<Key,Object> typeMap = cacheContent.get(clazz);
		if(typeMap != null)
			return typeMap.containsKey(key);

		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		cacheContent = null;
		
		super.finalize();
	}

	/**
	 * The primary Key of an cached object
	 */
	public static class Key {
		private Object[] keyElement = null;
		
		/**
		 * 
		 * @param keyElement
		 */
		public Key(Object... keyElement) {
			this.keyElement = keyElement;
		}

		/**
		 * @return the keyElement
		 */
		public Object[] getKeyElement() {
			return keyElement;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(keyElement);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!Arrays.equals(keyElement, other.keyElement))
				return false;
			return true;
		}
	}
}
