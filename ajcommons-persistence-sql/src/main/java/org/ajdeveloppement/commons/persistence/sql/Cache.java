/*
 * Créé le 28 juil. 2012 à 15:41:08 pour AjCommons (Bibliothèque de composant communs)
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

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;

/**
 * Expose an object reference cache for all persitence object couch.
 * 
 * @author Aurelien JEOFFRAY
 *
 */
public class Cache {
	private Map<Class<?>,Map<CacheKey, SoftReference<Object>>> referenceCache = 
			new HashMap<Class<?>,Map<CacheKey, SoftReference<Object>>>();
	
	//Permet de faire disparaitre la référence vers la clé si l'objet mis en cache est libéré
	//de la mémoire. Ainsi sa libère l'entrée dans le cache.
	private Map<Object, CacheKey> keyCache = new WeakHashMap<Object, CacheKey>(500, 0.6f);

	
	protected Cache() {
	}
	
	/**
	 * Return the lower level persistent type (in inheritance case)
	 * @param type
	 * @return
	 */
	private Class<?> getRootPersistentType(Class<?> type) {

		List<?> types = ReflectionTools.getPersistentsClassStack(type);
		if(types != null && types.size()>0)
			return (Class<?>) types.get(types.size()-1);
		
		return null;
	}
	
	/**
	 * Put an object into cache if it not already present
	 * 
	 * @param object instance to put un cache
	 * @param keys 
	 * @throws ObjectPersistenceException
	 */
	public <T> void put(Object object, Object... keys) throws ObjectPersistenceException {
		Class<?> objClass = getRootPersistentType(object.getClass());
		if(objClass == null)
			return;
		
		Map<CacheKey, SoftReference<Object>> classCache = referenceCache.get(objClass);

		if(classCache == null) {
			synchronized (referenceCache) {
				//On recheck pour s'assurer qu'un autre thread ne vient pas d'ajouter la clé
				classCache = referenceCache.get(objClass);
				if(classCache == null) {
					classCache =  new WeakHashMap<CacheKey, SoftReference<Object>>(100, 0.6f);
					referenceCache.put(objClass, classCache);
				}
			}
		}

		if(!keyCache.containsKey(object)) {
			synchronized (keyCache) {
				if(!keyCache.containsKey(object)) {
					
					CacheKey key = null;
					if(keys != null && keys.length > 0)
						key = new CacheKey(keys);
					else
						key = CacheKey.getObjectKey(object);
					
					if(key != null) {
						classCache.put(key, new SoftReference<Object>(object));
						keyCache.put(object, key);
					}
				}
			}
		}
	}
	
	/**
	 * Remove an object from cache
	 * 
	 * @param instance the instance to remove
	 */
	public synchronized void remove(ObjectPersistence instance) {
		keyCache.remove(instance);
	}
	
	/**
	 * Remove an object identify by his type and persistence key from cache
	 * 
	 * @param cacheType the type of the instance to remove
	 * @param keys the identifier key of the instance to remove
	 */
	public <T> void remove(Class<T> cacheType, Object... keys) {
		Class<?> rootType = getRootPersistentType(cacheType);
		
		Map<CacheKey, SoftReference<Object>> classCache = referenceCache.get(rootType);
		if(classCache != null) {
			SoftReference<Object> reference = classCache.remove(new CacheKey(keys));
			if(reference != null) {
				Object removedObject = reference.get();
				if(removedObject != null)
					synchronized (keyCache) {
						keyCache.remove(removedObject);
					}
			}
		}
	}
	
	/**
	 * Return, if prensent in cache, the instence identify by his type and his persistence
	 * identifier key (primary key)
	 * 
	 * @param cacheType the type of the instance to return
	 * @param keys he identifier key of the instance to return
	 * @return instance in cache of object identify if found in cache
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> cacheType, Object... keys) {
		Class<?> rootType = getRootPersistentType(cacheType);
		Map<CacheKey, SoftReference<Object>> classCache = referenceCache.get(rootType);
		if(classCache != null) {
			SoftReference<Object> reference = classCache.get(new CacheKey(keys));
			if(reference != null)
				return (T)reference.get();
		}
		
		return null;
	}
	
	/**
	 * Indicate if given instance if prensent or not in cache.
	 * 
	 * @param instance the instance to test
	 * @return <code>true</code> if present, <code>false</code> else
	 */
	public <T> boolean containsInstance(T instance) {
		return keyCache.containsKey(instance);
	}
	
	/**
	 * Return all perstence object instance in cache as stream collection
	 * 
	 * @return all perstence object instance in cache as stream collection
	 */
	public Stream<Object> getCachedInstancesStream() {
		return keyCache.keySet().stream();
	}
	
	/**
	 * Clear cache
	 */
	public void clear() {
		keyCache.clear();
		referenceCache.clear();
	}
	
	private static class CacheKey {
		

		final private Object[] keys;
		private int hashCode = 0;
		
		public CacheKey(Object... keys) {
			this.keys = keys;
		}

		public static CacheKey getObjectKey(Object obj) throws ObjectPersistenceException {
			Class<?> persistentType = ReflectionTools.getFirstPersistentClass(obj.getClass());
			
			QueryData<Object> qdata = new QueryData<Object>(obj, persistentType, true, true, null);
			Map<String, Object> objectIdValues = qdata.getObjectIdValues();
			
			String[] pkFieldsName = ReflectionTools.getPrimaryKeyFieldsName(persistentType);

			if(pkFieldsName != null) {
				List<Object> values = new ArrayList<>();
				for(String pkColumn : pkFieldsName) {
					values.add(objectIdValues.get(pkColumn));
				}
				
				return new CacheKey(values.toArray());
			}
			
			return null;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			if(hashCode == 0) {
				final int prime = 31;
				int result = 1;
				result = prime * result + Arrays.hashCode(keys);
				
				hashCode = result;
			}
			return hashCode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (!Arrays.equals(keys, other.keys))
				return false;
			return true;
		}
	}
}
