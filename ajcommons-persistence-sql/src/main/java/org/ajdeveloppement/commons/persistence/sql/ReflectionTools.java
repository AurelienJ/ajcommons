/*
 * Créé le 10 janv. 2010 à 14:19:26 pour AjCommons (Bibliothèque de composant communs)
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

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ajdeveloppement.commons.Beta;
import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlChildCollection;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlField;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlForeignKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlHash;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlLoadBuilder;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlPrimaryKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlTable;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlUnmappedFields;
/**
 * Permet de récuperer les informations de persistance par reflection à
 * l'aide des annotations placé dans les class bindé
 * 
 * @author Aurelien JEOFFRAY
 *
 */
class ReflectionTools {
	private static Map<Class<?>,Class<? extends ObjectPersistence>> cachePeristentType = new HashMap<>();
	
	private static Map<Class<?>,ReflectionCache> cachePersistentTypeProperties = new HashMap<>();
	
	private static Map<Field,String> cacheFieldName = new HashMap<>();
	
	/**
	 * Retourne le nom de la table associé à une class en lisant la valeur
	 * de l'annotation {@link SqlTable} si elle est présente ou le nom de
	 * la class si l'annotation est absente.
	 * 
	 * @param persistentClass la class associé à une table de base de donnée
	 * 
	 * @return le nom de la table associé
	 */
	public static <T> String getTableName(Class<T> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.tableName != null)
			return reflectionCache.tableName;
		
		String tableName = null;
		SqlTable table = persistentClass.getAnnotation(SqlTable.class);
		if(table != null) {
			tableName = table.name();
			if(tableName.isEmpty())
				tableName = persistentClass.getSimpleName();
		}
		
		reflectionCache.tableName = tableName;
		
		return tableName;
	}
	
	/**
	 * Return the database connection domain for given persistent type
	 * 
	 * @param persistentClass the persistent type
	 * @return the database connection domain
	 */
	public static <T> String getTableDomain(Class<T> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.domain != null)
			return reflectionCache.domain;
		
		String tableDomain = null;
		SqlTable table = persistentClass.getAnnotation(SqlTable.class);
		if(table != null) {
			tableDomain = table.domain();
			if(tableDomain.isEmpty())
				tableDomain = SqlContext.DEFAULT_DOMAIN;
		}
		
		reflectionCache.domain = tableDomain;
		
		return tableDomain;
	}
	
	/**
	 * Return the binder class to bind resultset to Object
	 * 
	 * @param persistentClass the persistent type to find binding
	 * @return the binder
	 */
	@SuppressWarnings("unchecked")
	public static <T,K> Class<? extends ResultSetRowToObjectBinder<T, K>> getBinderClass(Class<T> persistentClass) {
		Class<? extends ResultSetRowToObjectBinder<T, K>> binderClass = null;
		
		SqlTable table = persistentClass.getAnnotation(SqlTable.class);
		if(table != null) {
			binderClass = (Class<? extends ResultSetRowToObjectBinder<T, K>>)table.loadBuilder();
		} else {
			SqlLoadBuilder loadBuilder = persistentClass.getAnnotation(SqlLoadBuilder.class);
			if(loadBuilder != null)
				binderClass = (Class<? extends ResultSetRowToObjectBinder<T, K>>)loadBuilder.value();
		}
		
		return binderClass;
	}
	
	/**
	 * Retourne le nom du champs de la base de données associé au champs de la class
	 * fourni. Prend la valeur de l'annotation {@link SqlField} si présente ou le nom
	 * du champs dans le cas contraire.
	 * 
	 * @param field le champs pour lequel récupéré le nom en base
	 * @return le nom du champs de base associé
	 */
	public static String getFieldName(Field field) {
		String fieldName = cacheFieldName.get(field);
		if(fieldName == null) {
			SqlField sqlField = field.getAnnotation(SqlField.class);
			if(sqlField != null) {
				fieldName = sqlField.name();
				if(fieldName.isEmpty())
					fieldName = field.getName();
				
				cacheFieldName.put(field, fieldName);
			}
		}

		return fieldName;
	}
	
	/**
	 * Retourne la liste des nom de champs de clé étrangère pointant vers une autre Table/Class et
	 * référencé par l'annotation {@link SqlForeignKey} du champs
	 * 
	 * @param field le champs pour lequel récupéré la clé étrangère
	 * @return les champs de clé étrangère associé aux champs de la class
	 */
	public static String[] getForeignKeyFieldsName(Field field) {
		String[] fieldsName = field.getAnnotation(SqlForeignKey.class).mappedTo();

		return fieldsName;
	}
	
	public static String[] getPrimaryKeyFieldsName(Class<?> persitentType) {
		if(cachePersistentTypeProperties.containsKey(persitentType) && cachePersistentTypeProperties.get(persitentType).primaryKeyFieldsName != null)
			return cachePersistentTypeProperties.get(persitentType).primaryKeyFieldsName;
		
		SqlPrimaryKey pkAnnotation = persitentType.getAnnotation(SqlPrimaryKey.class);
		if(pkAnnotation == null)
			return null;
		
		String[] primaryKey = pkAnnotation.fields();
		
		if(!cachePersistentTypeProperties.containsKey(persitentType))
			cachePersistentTypeProperties.put(persitentType, new ReflectionCache());
		cachePersistentTypeProperties.get(persitentType).primaryKeyFieldsName = primaryKey;
		
		return primaryKey;
	}
	
	public static String getGeneratedIdFieldName(Class<?> persitentType) {
		if(cachePersistentTypeProperties.containsKey(persitentType) && cachePersistentTypeProperties.get(persitentType).generatedId != null)
			return cachePersistentTypeProperties.get(persitentType).generatedId;
		
		SqlPrimaryKey pkAnnotation = persitentType.getAnnotation(SqlPrimaryKey.class);
		if(pkAnnotation == null)
			return null;
		
		String generatedId = pkAnnotation.generatedidField().name();
		
		if(!cachePersistentTypeProperties.containsKey(persitentType))
			cachePersistentTypeProperties.put(persitentType, new ReflectionCache());
		cachePersistentTypeProperties.get(persitentType).generatedId = generatedId;
		
		return generatedId;
	}
	
	/**
	 * Pour une class donnée, recherche dans la class et les super-class
	 * la première contenant l'annotation de persistance SQL
	 * 
	 * @param highLevelClass la class pour laquelle rechercher la couche de persitance
	 * @return la class persistante retourne null si dans la pile d'heritage, aucune ne
	 * contient d'annotation {@link SqlTable}
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends ObjectPersistence> getFirstPersistentClass(Class<?> highLevelClass) {
		if(highLevelClass == null)
			return null;
		
		if(!ObjectPersistence.class.isAssignableFrom(highLevelClass))
			return null;
		
		Class<? extends ObjectPersistence> persistentClass = cachePeristentType.get(highLevelClass);
		if(persistentClass != null)
			return persistentClass;
		
		if(highLevelClass.isAnnotationPresent(SqlTable.class)) {
			cachePeristentType.put(highLevelClass, (Class<? extends ObjectPersistence>)highLevelClass);
			
			return (Class<? extends ObjectPersistence>)highLevelClass;
		}
		
		Class<? extends ObjectPersistence> parentPersistentClass = getParentPersistentClass(highLevelClass, false);
		if(parentPersistentClass != null)
			cachePeristentType.put(highLevelClass, parentPersistentClass);
		
		return parentPersistentClass;
	}
	
	/**
	 * Retourne l'ensemble de la pile de class impliqué dans la persistence
	 * 
	 * @param highLevelClass
	 * @return
	 */
	public static List<Class<? extends ObjectPersistence>> getPersistentsClassStack(Class<?> highLevelClass) {
		List<Class<? extends ObjectPersistence>> persitentsClassStack = new ArrayList<>();
		
		Class<? extends ObjectPersistence> topClass = getFirstPersistentClass(highLevelClass);
		if(topClass != null) {
			persitentsClassStack.add(0, topClass);
			
			while(topClass.getSuperclass() != null && topClass.getSuperclass() != Object.class) {
				Class<?> superclass = topClass.getSuperclass();
				topClass = getFirstPersistentClass(superclass);
				if(topClass != null)
					persitentsClassStack.add(0, topClass);
			}
		}
		
		return persitentsClassStack;
	}
	
	public static Method getHashMethod(Class<?> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.haveHashMethod == null) {
			for(Method m : persistentClass.getMethods()) {
				if(m.isAnnotationPresent(SqlHash.class)) {
					reflectionCache.haveHashMethod = true;
					reflectionCache.hashMethod = m;
					
					return m;
				}
			}
			reflectionCache.haveHashMethod = false;
		}
		
		return reflectionCache.hashMethod;
	}
	
	/**
	 * Retourne la liste des colonnes connue de la table associé à une class donnée
	 * 
	 * @param persistentClass
	 * @return la liste des colonnes connue de la table associé à une class donnée
	 */
	public static List<String> getTableColumns(Class<?> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.sqlTableColumns == null) {
			List<String> bindedFieldsName = new ArrayList<>();
			for(Field field : getSqlFields(persistentClass)) {
				bindedFieldsName.add(getFieldName(field));
			}
			
			for(Field field : getSqlForeignKeys(persistentClass)) {
				String[] fieldsName = ReflectionTools.getForeignKeyFieldsName(field);
				bindedFieldsName.addAll(Arrays.asList(fieldsName));
			}
			
			if(persistentClass.isAnnotationPresent(SqlUnmappedFields.class))
				bindedFieldsName.addAll(Arrays.asList(persistentClass.getAnnotation(SqlUnmappedFields.class).fields()));
			
			reflectionCache.sqlTableColumns = bindedFieldsName;
		}
		
		return reflectionCache.sqlTableColumns;
	}
	
	/**
	 * Retourne les champs mappés avec Sql
	 * 
	 * @param persistentClass la class persitente pour laquelle retourner la liste des
	 * champs mappés
	 * @return la liste des champs mappés
	 */
	public static List<Field> getSqlFields(Class<?> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.sqlFields != null)
			return reflectionCache.sqlFields;
		
		List<Field> fields = new ArrayList<>();
		
		for(Field field : persistentClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(SqlField.class)) {
				if(!field.isAccessible())
					field.setAccessible(true);
				
				fields.add(field);
			}
		}
		
		reflectionCache.sqlFields = fields;
		
		return fields;
	}
	
	/**
	 * Retourne les champs mappés avec Sql en clé étrangère
	 * 
	 * @param persistentClass la class persitente pour laquelle retourner la liste des
	 * champs mappés
	 * @return la liste des champs mappés
	 */
	public static List<Field> getSqlForeignKeys(Class<?> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.sqlForeignKeys != null)
			return reflectionCache.sqlForeignKeys;
		
		List<Field> fields = new ArrayList<>();
		
		for(Field field : persistentClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(SqlForeignKey.class)) {
				field.setAccessible(true);
				
				fields.add(field);
			}
		}
		
		reflectionCache.sqlForeignKeys = fields;
		
		return fields;
	}
	
	/**
	 * Retourne les collections enfants de l'objet devant être persité
	 * 
	 * @param persistentClass la class persitente pour laquelle retourner la liste des
	 * champs mappés
	 * @return la liste des collections persistante
	 */
	public static List<Field> getSqlChildCollections(Class<?> persistentClass) {
		ReflectionCache reflectionCache = getReflectionCache(persistentClass);
		if(reflectionCache.sqlChildCollections != null)
			return reflectionCache.sqlChildCollections;
		
		List<Field> fields = new ArrayList<>();
		
		for(Field field : persistentClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(SqlChildCollection.class)) {
				field.setAccessible(true);
				
				fields.add(field);
			}
		}
		
		reflectionCache.sqlChildCollections = fields;
		
		return fields;
	}
	
	/**
	 * Recherche la méthode <code>addPropertyChangeListener</code>
	 * 
	 * @param persistentClass la class dans laquelle rechercher la méthode
	 * @return la méthode addPropertyChangeListener
	 */
	public static Method getAddPropertyChangeListenerMethod(Class<?> persistentClass) {
		try {
			Method m = persistentClass.getMethod("addPropertyChangeListener", PropertyChangeListener.class); //$NON-NLS-1$
			if(m != null)
				return m;
		} catch (NoSuchMethodException | SecurityException e) {
		}
		
		return null;
	}
	
	/**
	 * Do not use - not finish
	 * 
	 * @param persistentClass
	 * @return The create table script
	 */
	@Beta
	public static String getCreateTableScript(Class<?> persistentClass) {
		String createTableScript = String.format("CREATE TABLE %s (", (Object)getTableName(persistentClass)); //$NON-NLS-1$
		
		for (Field field : getSqlFields(persistentClass)) {
			SqlField sqlField = field.getAnnotation(SqlField.class);
			createTableScript += String.format("%s %s", //$NON-NLS-1$
					getFieldName(field),
					sqlField.sqlType());
			if(sqlField.size() > 0)
				createTableScript += "(" + sqlField.size() + (sqlField.scale() > 0 ? ", " + sqlField.scale() : "") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if(!sqlField.nullable())
				createTableScript += " NOT NULL"; //$NON-NLS-1$
			createTableScript += ",\n"; //$NON-NLS-1$
		}
		
		for (Field field : getSqlForeignKeys(persistentClass)) {
			SqlForeignKey sqlForeignKey = field.getAnnotation(SqlForeignKey.class);
			//getPrimaryKeyFieldsName(field.getType());
			
			
			/*createTableScript += String.format("%s %s", //$NON-NLS-1$
					sqlForeignKey.name(),
					sqlField.sqlType());
			if(sqlField.size() > 0)
				createTableScript += "(" + sqlField.size() + (sqlField.scale() > 0 ? ", " + sqlField.scale() : "") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			if(!sqlField.nullable())
				createTableScript += " NOT NULL"; //$NON-NLS-1$*/
			createTableScript += ",\n"; //$NON-NLS-1$
		}
		
		String[] pk = getPrimaryKeyFieldsName(persistentClass);
		if(pk != null) {
			createTableScript += "PRIMARY KEY ("; //$NON-NLS-1$
			boolean first = true;
			for (String pkColumn : pk) {
				if(!first)
					createTableScript += ","; //$NON-NLS-1$
				createTableScript += pkColumn;
			}
			createTableScript += ")"; //$NON-NLS-1$
		}

		createTableScript += ");"; //$NON-NLS-1$
		
		return createTableScript;
	}
	
	/**
	 * Recherche dans les super-class la première contenant l'annotation de persistance SQL.
	 * Contrairement à {@link #getFirstPersistentClass(Class)}, ne regarde pas dans la class
	 * de plus haut niveau fournit en paramètre.
	 * 
	 * @param highLevelClass la class pour laquelle rechercher un éventuel parent
	 * intervenant dans la persistance
	 * @param highLevelIsPersistent indique si la class fournit en parametre est déjà un type persistent ou non
	 * @return la class persistante retourne null si dans la pile d'heritage, aucune ne
	 * contient d'annotation {@link SqlTable}
	 */
	@SuppressWarnings("unchecked")
	private static Class<? extends ObjectPersistence> getParentPersistentClass(Class<?> highLevelClass, boolean highLevelIsPersistent) {
		ReflectionCache reflectionCache = null;
		
		//Si la class fournit est un type persistent, on extrait ses informations
		//de persistence
		if(highLevelIsPersistent)
			reflectionCache = getReflectionCache(highLevelClass);
		
		if(reflectionCache == null || reflectionCache.haveParentPersistentClass == null) {
			Class<?> parentClass = highLevelClass.getSuperclass();
			while(parentClass != null && parentClass != Object.class) {
				if(parentClass.isAnnotationPresent(SqlTable.class) && ObjectPersistence.class.isAssignableFrom(parentClass)) {
					if(reflectionCache != null) {
						reflectionCache.haveParentPersistentClass = true;
						reflectionCache.parentPersistentClass = (Class<? extends ObjectPersistence>)parentClass;
					}
					
					return (Class<? extends ObjectPersistence>)parentClass;
				}
				
				parentClass = parentClass.getSuperclass();
			}
			if(reflectionCache != null)
				reflectionCache.haveParentPersistentClass = false;
		}
		
		return reflectionCache != null ? reflectionCache.parentPersistentClass : null;
	}
	
	private static ReflectionCache getReflectionCache(Class<?> persistentClass) {
		ReflectionCache reflectionCache = cachePersistentTypeProperties.get(persistentClass);
		if(reflectionCache == null) {
			reflectionCache = new ReflectionCache();
			cachePersistentTypeProperties.put(persistentClass, reflectionCache);
		}
		return reflectionCache;
	}

	private static class ReflectionCache {
		private String tableName = null;
		private String domain = null;
		private String[] primaryKeyFieldsName = null;
		private String generatedId = null;
		
		private List<Field> sqlFields = null;
		private List<Field> sqlForeignKeys = null;
		private List<Field> sqlChildCollections = null;
		private List<String> sqlTableColumns = null;
		
		private Boolean haveParentPersistentClass = null;
		private Class<? extends ObjectPersistence> parentPersistentClass = null;
		
		private Boolean haveHashMethod = null;
		private Method hashMethod = null;
	}
}
