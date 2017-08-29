/*
 * Créé le 19 août 2012 à 11:32:54 pour AjCommons (Bibliothèque de composant communs)
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

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.ajdeveloppement.commons.persistence.LoadHelper;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlForeignKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlSubTables;

/**
 * @author Aurelien JEOFFRAY
 * @param <T> The return type
 * @param <K> The binder map
 *
 */
public class DefaultSqlBuilder<T, K> implements ResultSetRowToObjectBinder<T, K> {

	protected static Map<Class<?>, Map<SqlContext, LoadHelper<?, ResultSet>>> resultSetLoadHelpers = new HashMap<>();
	
	private Class<?> persistentType;
	private Class<T> returnType;
	private boolean useGlobalCache = true;
	
	private Map<Class<?>, Map<String, Object>> lastForeignKeys;
	
	/**
	 * @param returnType
	 */
	public DefaultSqlBuilder(Class<T> returnType) {
		this.returnType = returnType;
		this.persistentType = ReflectionTools.getFirstPersistentClass(returnType);
	}

	/**
	 * @return the useGlobalCache
	 */
	public boolean isUseGlobalCache() {
		return useGlobalCache;
	}

	/**
	 * @param useGlobalCache the useGlobalCache to set
	 */
	public void setUseGlobalCache(boolean useGlobalCache) {
		this.useGlobalCache = useGlobalCache;
	}
	
	/**
	 * @return the lastForeignKeys
	 */
	public Map<Class<?>, Map<String, Object>> getLastForeignKeys() {
		return lastForeignKeys;
	}
	

	/**
	 * @param lastForeignKeys the lastForeignKeys to set
	 */
	public void setLastForeignKeys(
			Map<Class<?>, Map<String, Object>> lastForeignKeys) {
		this.lastForeignKeys = lastForeignKeys;
	}
	

	/**
	 * @param primaryKeyValues
	 * @param sessionCache
	 * @param binderRessourcesMap
	 * @return the binded type new instance
	 * @throws ObjectPersistenceException
	 */
	@Override
	public T get(SqlContext context, SqlLoadingSessionCache sessionCache,
			K binderRessourcesMap,
			Object... primaryKeyValues) throws ObjectPersistenceException {
		T newInstance = getCachedReturnTypeInstance(primaryKeyValues, context, sessionCache);
		if(newInstance == null) {
			QFilter pkFilter = getReturnTypePrimaryKeyFilter(primaryKeyValues);
			
			newInstance = QResults.from(returnType, sessionCache, binderRessourcesMap)
					.useContext(context)
					.where(pkFilter)
					.first();
		}
		
		return newInstance;
	}

	/**
	 * Build an entity with given ResultSet
	 * 
	 * @param rs the ResultSet row that contains data to build entity
	 * @param sessionCache the entity cache of loading session
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T get(ResultSet rs, SqlContext context, SqlLoadingSessionCache sessionCache,
			K binderRessourcesMap) throws ObjectPersistenceException {
		Object[] primaryKey = getPrimaryKeyValue(rs, persistentType);
		
		T newInstance = getCachedReturnTypeInstance(primaryKey, context, sessionCache);
		if(newInstance == null) {
			Class<?> realReturnType = getHighLevelType(rs, returnType);
			
			newInstance = (T)createReturnTypeInstance(realReturnType);
			
			lastForeignKeys = ((LoadHelper<T, ResultSet>)getResultSetLoadHelper(context, realReturnType)).load(newInstance, rs);

			putInstanceInCache(newInstance, context, sessionCache, primaryKey);
			
			loadForeignFields(newInstance, lastForeignKeys, context, sessionCache, binderRessourcesMap);
			
		}
		return newInstance;
	}
	
	/**
	 * @param primaryKeyValues
	 * @return
	 * @throws ObjectPersistenceException
	 */
	private QFilter getReturnTypePrimaryKeyFilter(Object... primaryKeyValues) throws ObjectPersistenceException {
		String tableName = ReflectionTools.getTableName(persistentType);
		Map<String, Object> pkMap = new HashMap<>(primaryKeyValues.length,1);
		List<QField<Object>> pkFields = Arrays.stream(ReflectionTools.getPrimaryKeyFieldsName(persistentType))
				.map(fieldName -> new QField<Object>(tableName, fieldName))
				.collect(Collectors.toList());
		
		QFilter pkFilter = null;
		
		if(pkFields != null) {
			int i = 0;
			for(QField<Object> pkFieldName : pkFields) {
				if(pkFilter == null)
					pkFilter = pkFieldName.equalTo(primaryKeyValues[i++]);
				else
					pkFilter = pkFilter.and(pkFieldName.equalTo(primaryKeyValues[i++]));
			}
		} else {
			throw new ObjectPersistenceException(
					String.format("An SqlPrimaryKey must be defined on '%s' to return an entity",returnType.getName())); //$NON-NLS-1$
		}
		return pkFilter;
	}
	
	private Class<?> getHighLevelType(ResultSet rs, Class<?> returnType) throws ObjectPersistenceException {
		SqlSubTables subTables = returnType.getAnnotation(SqlSubTables.class);
		if(subTables != null) {
			//Get each childs persistent type
			for(Class<?> subType : subTables.value()) {
				Class<?> persistentSubType = ReflectionTools.getFirstPersistentClass(subType);
				if(persistentSubType != null && persistentSubType != returnType) {
					//test if childs pk is not null
					Object[] primaryKey = getPrimaryKeyValue(rs, persistentSubType);
					if(Arrays.stream(primaryKey).anyMatch(o -> o != null)) {
						return getHighLevelType(rs, subType);
					}
				}
			}
		}
		
		return returnType;
	}
	
	protected LoadHelper<?, ResultSet> getResultSetLoadHelper(SqlContext context, Class<?> returnType) {
		LoadHelper<?, ResultSet> loadHelper = null;
		if(returnType != null) {
			Map<SqlContext, LoadHelper<?, ResultSet>> helpers = resultSetLoadHelpers.get(returnType);
			if(helpers == null) {
				helpers = new WeakHashMap<>();
				
				resultSetLoadHelpers.put(returnType, helpers);
			}
			
			loadHelper = helpers.get(returnType);
			if(loadHelper == null) {
				loadHelper = ResultSetLoadFactory.getLoadHelper(context, returnType);
				
				helpers.put(context, loadHelper);
			}
		}
		return loadHelper;
	}
	
 	protected T getCachedReturnTypeInstance(Object[] primaryKey, SqlContext context, SqlLoadingSessionCache sessionCache)
			throws ObjectPersistenceException {
		T newInstance = null;
		if(primaryKey != null) {
			if(useGlobalCache)
				newInstance = context.getCache().get(returnType, primaryKey);
			else if(sessionCache != null)
				newInstance = sessionCache.get(returnType, new SqlLoadingSessionCache.Key(primaryKey));
		}
		
		return newInstance;
	}
 	
 	protected Object createReturnTypeInstance(Class<?> returnType) throws ObjectPersistenceException {
 		try {
			return returnType.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ObjectPersistenceException(e);
		}
 	}

	protected void loadForeignFields(T newInstance, Map<Class<?>, Map<String, Object>> foreignKeys, SqlContext context, SqlLoadingSessionCache sessionCache,
			K binderRessourcesMap) throws ObjectPersistenceException {
		if(foreignKeys == null || foreignKeys.size() == 0)
			return;
		
		Class<?> persistentType = ReflectionTools.getFirstPersistentClass(newInstance.getClass());
		
		String tableName = ReflectionTools.getTableName(persistentType);
		
		Class<?> currentPersistenceCouch = persistentType;
		
		while(currentPersistenceCouch != null) {
			for(Field foreignField : ReflectionTools.getSqlForeignKeys(currentPersistenceCouch)) {
				SqlForeignKey sqlForeignKey = foreignField.getAnnotation(SqlForeignKey.class);
				String[] externalKey = sqlForeignKey.mappedTo();
				Object[] externalKeyValue = new Object[externalKey.length];

				boolean isDefined = false;
				int i = 0;
				for(String fkFieldName : externalKey) {
					Map<String,Object> values = foreignKeys.get(currentPersistenceCouch);
					if(values != null) {
						externalKeyValue[i] = values.get(fkFieldName);
						if(externalKeyValue[i++] != null)
							isDefined = true;
					}
				}
				
				if(isDefined) {
					ResultSetRowToObjectBinder<?, K> sqlBuilder = ResultSetRowToObjectBinderFactory.getBinder(foreignField.getType());
					try {
						foreignField.set(newInstance, sqlBuilder.get(context, sessionCache, binderRessourcesMap, externalKeyValue));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new ObjectPersistenceException(e);
					}

				}
			}
			
			currentPersistenceCouch = ReflectionTools.getFirstPersistentClass(currentPersistenceCouch.getSuperclass());
		}
	}
	
	protected void putInstanceInCache(T newInstance, SqlContext context, SqlLoadingSessionCache sessionCache, Object... keys)
			throws ObjectPersistenceException {
		if(useGlobalCache) {
			context.getCache().put(newInstance, keys);	
		}
		else if(sessionCache != null)
			sessionCache.put(newInstance);
	}
	
	private Object[] getPrimaryKeyValue(ResultSet rs, Class<?> persistentType) throws ObjectPersistenceException {
		String tableName = ReflectionTools.getTableName(persistentType);
		String[] pkFieldsName = ReflectionTools.getPrimaryKeyFieldsName(persistentType);
		if(pkFieldsName != null) {
			Object[] primaryKey = new Object[pkFieldsName.length];
			try {
				int i = 0;
				for(String pkFieldName : pkFieldsName) {
					QField<?> pkField = new QField<>(tableName, pkFieldName, null, -1);
					primaryKey[i++] = pkField.getValue(rs);
				}
			} catch (SQLException e1) {
				throw new ObjectPersistenceException(e1);
			}
			
			return primaryKey;
		}
		
		return null;
	}
}
