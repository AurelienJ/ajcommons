/*
 * Créé le 1 août 2012 à 11:56:14 pour AjCommons (Bibliothèque de composant communs)
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ajdeveloppement.commons.persistence.AbstractLoadHandler;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlUnmappedFields;

/**
 * Abstract couch for SQL Binders.
 * 
 * This class is declared abstract to protect again direct instanciation
 * 
 * @author Aurelien JEOFFRAY
 * 
 * @param <T> The return type
 * @param <PI> the type of persitenceInformations that permit binding with database
 */
abstract class AbstractSqlLoadHandler<T, PI> extends AbstractLoadHandler<T, PI> {
	
	protected Class<?> persistentClass = null;
	protected String tableName;
	protected String domain;
	protected List<String> foreignFieldsNames = new ArrayList<String>();
	protected List<Field> tableFields = new ArrayList<Field>();
	
	protected AbstractLoadHandler<T, PI> childSqlLoadHandler = null;
	
	private Map<String, QField<?>> cacheFieldsAccessor = new HashMap<>();
	
	/**
	 * Analyse class to extract table name, table columns, foreigns keys and unmapped columns
	 */
	protected void analysePersistentClass() {
		this.tableName = ReflectionTools.getTableName(persistentClass);
		this.domain = ReflectionTools.getTableDomain(persistentClass);
		
		for(Field field : ReflectionTools.getSqlFields(persistentClass)) {
			tableFields.add(field);
		}
		
		for(Field field : ReflectionTools.getSqlForeignKeys(persistentClass)) {
			String[] fieldsName = ReflectionTools.getForeignKeyFieldsName(field);
			foreignFieldsNames.addAll(Arrays.asList(fieldsName));
		}
		
		if(persistentClass.isAnnotationPresent(SqlUnmappedFields.class))
			foreignFieldsNames.addAll(Arrays.asList(persistentClass.getAnnotation(SqlUnmappedFields.class).fields()));
	}
	
	/**
	 * Bind a resultset row into an instance of a binded object 
	 * 
	 * @param obj the instonce of binded object
	 * @param persistenceInformations persistenceInformations that help to realize binding. Use for superclass loading
	 * @param resultSet the resultset row that contains data to bind
	 * @return Map of values of foreigns keys and unmapped field of each persistent class of the binding operation
	 * @throws ObjectPersistenceException 
	 */
	protected Map<Class<?>, Map<String, Object>> fill(T obj,
			PI persistenceInformations, ResultSet resultSet, SqlContext context) throws ObjectPersistenceException {
		
		if(resultSet == null)
			return null;
		
		Map<Class<?>, Map<String, Object>> foreignKeys = new HashMap<Class<?>, Map<String, Object>>(2);
		
		//Si la class hérite d'un parent on récupére les clés étrangére du parent
		if(childSqlLoadHandler != null)
			foreignKeys.putAll(childSqlLoadHandler.fill(obj, persistenceInformations));
		
		try {
			for(Field field : tableFields) {
				String fieldName = ReflectionTools.getFieldName(field);
				
				QField<?> tableField = null;
				if(!cacheFieldsAccessor.containsKey(fieldName)) {
					tableField = new QField<>(tableName, fieldName);
					cacheFieldsAccessor.put(fieldName, tableField);
				} else {
					tableField = cacheFieldsAccessor.get(fieldName);
				}
				
				Object value = tableField.getValue(resultSet);
				
				if(field.getType().isEnum()) {
					for(Object et : field.getType().getEnumConstants()) {
						if(et.toString().equals(value)) {
							field.set(obj, et);
							break;
						}
					}
				} else {
					if(value == null && field.getType().isPrimitive()) {
						if(field.getType() == Boolean.TYPE)
							value = false;
						else if(field.getType() == Long.TYPE || field.getType() == Integer.TYPE || field.getType() == Short.TYPE)
							value = 0;
						else
							value = field.getType().cast(0);
					}
					field.set(obj, value);
				}
			}
			
			if(foreignFieldsNames.size() > 0 && !foreignKeys.containsKey(persistentClass))
				foreignKeys.put(persistentClass, new HashMap<String, Object>());
			
			for(String foreignFieldName : foreignFieldsNames) {
				QField<?> tableField = null;
				if(!cacheFieldsAccessor.containsKey(foreignFieldName)) {
					tableField = new QField<>(tableName, foreignFieldName);
					cacheFieldsAccessor.put(foreignFieldName, tableField);
				} else {
					tableField = cacheFieldsAccessor.get(foreignFieldName);
				}
				
				foreignKeys.get(persistentClass).put(foreignFieldName, tableField.getValue(resultSet));
			}
			
			ObjectPersitenceState.calculateHash(obj, persistentClass, foreignKeys.get(persistentClass), true, context);
		} catch (SQLException | IllegalArgumentException | IllegalAccessException | ClassCastException e) {
			throw new ObjectPersistenceException(e);
		}
		
		return foreignKeys;
	}
}
