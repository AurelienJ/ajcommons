/*
 * Créé le 1 déc. 2013 à 22:50:58 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2013 - Aurélien JEOFFRAY
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.Session;
import org.ajdeveloppement.commons.persistence.StoreHelper;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlChildCollection;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlTable;

/**
 * @author "Aurélien JEOFFRAY"
 *
 */
public interface SqlObjectPersistence extends ObjectPersistence {

	/**
	 * Data validation before save object
	 * 
	 * @return true if valide for save
	 */
	public default boolean validateBeforeSave() throws ObjectPersistenceException {
		return true;
	}
	
	/**
	 * Data validation before delete object
	 * 
	 * @return true if valide for delete
	 */
	public default boolean validateBeforeDelete() throws ObjectPersistenceException {
		return true;
	}
	
	@Override
	public default void save() throws ObjectPersistenceException {
		SessionHelper.startSaveSession(this);
	}
	
	@Override
	public default void delete() throws ObjectPersistenceException {
		SessionHelper.startDeleteSession(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public default void save(Session session) throws ObjectPersistenceException {
		SqlContext context = null;
		if(session instanceof SqlSession)
			context = ((SqlSession)session).getContext();
		
		if(Session.canExecute(session, this)) {
			if(!validateBeforeSave())
				return;
			
			List<Class<? extends ObjectPersistence>> persitentTypes = ReflectionTools.getPersistentsClassStack(this.getClass());
			Map<Class<? extends ObjectPersistence>, Map<String, Object>> primaryKeyStackValues = new HashMap<>();
			
			Map<String, Object> lastPrimaryKeyMap = null;
			for(Class<? extends ObjectPersistence> persitentType : persitentTypes) {
				// For each inherit stack
				StoreHelper<SqlObjectPersistence> helper = (StoreHelper<SqlObjectPersistence>)SqlStoreHelperCache.getHelper(persitentType, context);
				if(helper != null) {
					
					//Save foreign key
					for(Field f : ReflectionTools.getSqlForeignKeys(persitentType)) {
						try {
							ObjectPersistence value = (ObjectPersistence)f.get(this);
							
							if(value != null) {
								value.save(session);
							}
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new ObjectPersistenceException(e);
						}
					}
					
					//save current stack entry
					Map<String, Object> primaryKeyMap = helper.save(this, lastPrimaryKeyMap);
					primaryKeyStackValues.put(persitentType, primaryKeyMap);
					lastPrimaryKeyMap = primaryKeyMap;
				}
			}
			
			
			SqlTable tableAnnotation = persitentTypes.get(persitentTypes.size()-1).getAnnotation(SqlTable.class);
			if(tableAnnotation != null && !tableAnnotation.disableCache() && context != null)
				context.getCache().put(this);
			
			Session.addProcessedObject(session, this);
			
			for(Class<? extends ObjectPersistence> persitentType : persitentTypes) {
				String[] primaryKey = ReflectionTools.getPrimaryKeyFieldsName(persitentType);
				
				if(primaryKey == null)
					throw new ObjectPersistenceException("The persistent type must contains an SqlPrimaryKey annotation"); //$NON-NLS-1$

				for(Field f : ReflectionTools.getSqlChildCollections(persitentType)) {
					Object collectionValue;
					try {
						collectionValue = f.get(this);
						
						if(collectionValue != null) {
							if(collectionValue instanceof LazyPersistentCollection) {
								((LazyPersistentCollection<?, ?>)collectionValue).save(session);
							} else if(collectionValue instanceof PersitentCollection) {
								((PersitentCollection<?>)collectionValue).save(session);
							} else if(collectionValue instanceof Collection) {
								
								String[] foreignKey = f.getAnnotation(SqlChildCollection.class).foreignFields();
								if(foreignKey.length != primaryKey.length)
									throw new ObjectPersistenceException("Foreign key of child collection must get same size as primary key of parent type"); //$NON-NLS-1$
								
								if(primaryKeyStackValues.containsKey(persitentType)) {
									Map<String, Object> foreignKeyMap = new HashMap<>();
									for(int i = 0; i < foreignKey.length; i++) {
										foreignKeyMap.put(foreignKey[i], primaryKeyStackValues.get(persitentType).get(primaryKey[i]));
									}
									PersitentCollection.save((Collection<ObjectPersistence>)collectionValue, session, foreignKeyMap);
								}
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new ObjectPersistenceException(e);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public default void delete(Session session) throws ObjectPersistenceException {
		SqlContext context = null;
		if(session instanceof SqlSession)
			context = ((SqlSession)session).getContext();
		
		//@SuppressWarnings("unchecked")
		//StoreHelper<SqlObjectPersistence> helper = (StoreHelper<SqlObjectPersistence>)SqlStoreHelperCache.getHelper(this.getClass(), context);

		if(Session.canExecute(session, this)) {
			if(!validateBeforeDelete())
				return;
			
			List<Class<? extends ObjectPersistence>> persitentTypes = ReflectionTools.getPersistentsClassStack(this.getClass());
			Map<Class<? extends ObjectPersistence>, Map<String, Object>> primaryKeyStackValues = new HashMap<>();
			
			Map<String, Object> lastPrimaryKeyMap = null;
			for(Class<? extends ObjectPersistence> persitentType : persitentTypes) {
				String[] primaryKey = ReflectionTools.getPrimaryKeyFieldsName(persitentType);
				
				if(primaryKey == null)
					throw new ObjectPersistenceException("The persistent type must contains an SqlPrimaryKey annotation"); //$NON-NLS-1$
				
				for(Field f : ReflectionTools.getSqlChildCollections(persitentType)) {
					Object collectionValue;
					try {
						collectionValue = f.get(this);
						
						if(collectionValue != null) {
							if(collectionValue instanceof LazyPersistentCollection) {
								((LazyPersistentCollection<?, ?>)collectionValue).delete(session);
							} else if(collectionValue instanceof PersitentCollection) {
								((PersitentCollection<?>)collectionValue).delete(session);
							} else if(collectionValue instanceof Collection) {
								
								String[] foreignKey = f.getAnnotation(SqlChildCollection.class).foreignFields();
								if(foreignKey.length != primaryKey.length)
									throw new ObjectPersistenceException("Foreign key of child collection must get same size as primary key of parent type"); //$NON-NLS-1$
								
								if(primaryKeyStackValues.containsKey(persitentType)) {
									Map<String, Object> foreignKeyMap = new HashMap<>();
									for(int i = 0; i < foreignKey.length; i++) {
										foreignKeyMap.put(foreignKey[i], primaryKeyStackValues.get(persitentType).get(primaryKey[i]));
									}
									PersitentCollection.delete((Collection<ObjectPersistence>)collectionValue, session, foreignKeyMap);
								}
							}
						}
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new ObjectPersistenceException(e);
					}
				}
			}
			
			StoreHelper<SqlObjectPersistence> helper = (StoreHelper<SqlObjectPersistence>)SqlStoreHelperCache.getHelper(
					ReflectionTools.getFirstPersistentClass(this.getClass()), context);
			
			if(helper != null)
				helper.delete(this);
			
			SqlTable tableAnnotation = this.getClass().getAnnotation(SqlTable.class);
			if(tableAnnotation != null && !tableAnnotation.disableCache() && context != null)
				context.getCache().remove(this);
			
			Session.addProcessedObject(session, this);
		}
	}
}
