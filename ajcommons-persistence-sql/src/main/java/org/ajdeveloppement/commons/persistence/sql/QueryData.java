/*
 * Créé le 9 janv. 2010 à 18:17:48 pour AjCommons (Bibliothèque de composant communs)
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ajdeveloppement.commons.persistence.ObjectData;
import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.sql.ObjectPersitenceState.State;

/**
 * Permet l'extraction des données d'un objet à destination de la base de données
 *
 * @author Aurélien JEOFFRAY
 *
 * @param <T> le type de l'objet pour lequel extraire les données
 */
class QueryData<T> implements ObjectData {
	private T sourceObject;
	
	private Map<String, Object> values = new HashMap<String, Object>();
	private Map<String, Object> primaryKeyValue = new HashMap<String, Object>();

	private Field generatedField;
	private String generatedFieldName;
	private Object generatedId = 0;
	private String[] primaryKeyFieldsName;
	
	private State state = State.UNMODIFIED;

	/**
	 * Extrait les données à mettre en base de l'objet fournit en paramètre
	 *
	 * @param obj l'objet pour lequel extraire les données
	 * @throws ObjectPersistenceException
	 */
	public QueryData(T obj, SqlContext context) throws ObjectPersistenceException {
		this(obj, null, false, false, context);
	}
	
	/**
	 * Extrait les données à mettre en base de l'objet fournit en paramètre
	 *
	 * @param obj l'objet pour lequel extraire les données
	 * @param doNotTrackupdate si true ne doit pas tracker les mise à jour de l'objet.
	 * La méthode {@link #isModified()} renverra toujours false
	 * @throws ObjectPersistenceException
	 */
	public QueryData(T obj, boolean doNotTrackupdate, SqlContext context) throws ObjectPersistenceException {
		this(obj, null, doNotTrackupdate, false, context);
	}
	
	/**
	 * Extrait les données à mettre en base de l'objet fournit en paramètre
	 *
	 * @param obj l'objet pour lequel extraire les données
	 * @param persitentType le type courant ou parent de l'objet gérant la persistence. si null,
	 * déduit le type de l'instance obj
	 * @throws ObjectPersistenceException
	 */
	public QueryData(T obj, Class<?> persitentType, SqlContext context) throws ObjectPersistenceException {
		this(obj, persitentType, false, false, context);
	}
	
	/**
	 * Extrait les données à mettre en base de l'objet fournit en paramètre
	 *
	 * @param obj l'objet pour lequel extraire les données
	 * @param persitentType le type courant ou parent de l'objet gérant la persistence. si null,
	 * déduit le type de l'instance obj
	 * @param doNotTrackupdate si true ne doit pas tracker les mise à jour de l'objet.
	 * La méthode {@link #isModified()} renverra toujours false
	 * @throws ObjectPersistenceException
	 */
	public QueryData(T obj, Class<?> persitentType, boolean doNotTrackupdate, SqlContext context) throws ObjectPersistenceException {
		this(obj, persitentType, doNotTrackupdate, false, context);
	}

	/**
	 * Extrait les données à mettre en base de l'objet fournit en paramètre
	 *
	 * @param obj l'objet pour lequel extraire les données
	 * @param persitentType le type courant ou parent de l'objet gérant la persistence. si null,
	 * déduit le type de l'instance obj
	 * @param doNotTrackupdate si true ne doit pas tracker les mise à jour de l'objet.
	 * La méthode {@link #isModified()} renverra toujours false
	 * @param extractOnlyPrimaryKeyValue si true, s'arreter à la récupération de la clé primaire. En outre la recherche de mise
	 * à jour n'est jamais effectué dans ce cas
	 * @throws ObjectPersistenceException
	 */
	public QueryData(T obj, Class<?> persitentType, boolean doNotTrackupdate, boolean extractOnlyPrimaryKeyValue, SqlContext context) throws ObjectPersistenceException {
		boolean stopParsing = false;
		
		sourceObject = obj;
		
		try {
			if(persitentType == null)
				persitentType = obj.getClass();

			persitentType = ReflectionTools.getFirstPersistentClass(persitentType);
			if(persitentType == null)
				throw new ObjectPersistenceException("The object is not a SqlPersitentObject!"); //$NON-NLS-1$

			primaryKeyFieldsName = ReflectionTools.getPrimaryKeyFieldsName(persitentType);
			generatedFieldName = ReflectionTools.getGeneratedIdFieldName(persitentType);
			
			List<String> primaryKeyFieldsNameList = null;
			if(primaryKeyFieldsName != null)
				primaryKeyFieldsNameList = Arrays.asList(primaryKeyFieldsName);
			
			for(Field field : ReflectionTools.getSqlFields(persitentType)) {
				String fieldName = ReflectionTools.getFieldName(field);
				boolean isInPk = primaryKeyFieldsNameList != null && primaryKeyFieldsNameList.contains(fieldName); //test if field is in primary key
				
				if(!extractOnlyPrimaryKeyValue || isInPk) {
					Object value = field.get(obj);
	
					if(field.getType().isEnum()) {
						value = value.toString();
					}
					
					if(generatedFieldName != null && !generatedFieldName.isEmpty() && fieldName.equals(generatedFieldName)) {
						if(value == null) {
							if(field.getType().isAssignableFrom(UUID.class)) {
								//value = UUID.randomUUID();
							}
						} else if((value.getClass().isAssignableFrom(Byte.class) && (Byte)value == 0) //Id 8bit
								|| (value.getClass().isAssignableFrom(Short.class) && (Short)value == 0 )//Id 16bit
								|| (value.getClass().isAssignableFrom(Integer.class) && (Integer)value == 0)//Id 32bit
								|| (value.getClass().isAssignableFrom(Long.class) && (Long)value == 0)) { //Id 64bit
							value = null;
						}
	
						generatedId = value;
						generatedField = field;
					}
	
					values.put(fieldName, value);
					
					if(isInPk) {
						primaryKeyValue.put(fieldName, value);
						
						if(extractOnlyPrimaryKeyValue && primaryKeyValue.size() == primaryKeyFieldsName.length) {
							stopParsing = true;
							
							break;
						}
					}
				}
			}
			
			if(!stopParsing) {
				//Extraction of foreign keys
				for(Field field : ReflectionTools.getSqlForeignKeys(persitentType)) {
					if(stopParsing)
						break;
					
					String[] foreignKeyFields = ReflectionTools.getForeignKeyFieldsName(field);
					
					Object value = field.get(obj);
					
					if(value != null) {
						Class<?> foreignPersitentType = ReflectionTools.getFirstPersistentClass(value.getClass());
						if(foreignPersitentType == null)
							throw new ObjectPersistenceException("Foreign key object must contains SqlTable annotation");   //$NON-NLS-1$
						
						String[] valuePK = ReflectionTools.getPrimaryKeyFieldsName(foreignPersitentType);
						if(valuePK == null)
							throw new ObjectPersistenceException("Foreign key object must contains SqlPrimaryKey annotation");  //$NON-NLS-1$
						
						if(foreignKeyFields.length != valuePK.length)
							throw new ObjectPersistenceException("Invalid foreign key length : " + Arrays.deepToString(foreignKeyFields));  //$NON-NLS-1$

						//extract primary key value in sub-object
						QueryData<Object> qdata = new QueryData<Object>(value, null, true, true, context);
						
						for(int i = 0; i < valuePK.length; i++) {
							if(!values.containsKey(foreignKeyFields[i])) {
								boolean isInPk = primaryKeyFieldsNameList != null && primaryKeyFieldsNameList.contains(foreignKeyFields[i]);
								
								if(!extractOnlyPrimaryKeyValue || isInPk) {
									Object fkFieldValue = qdata.getObjectIdValues().get(valuePK[i]);
									values.put(foreignKeyFields[i], fkFieldValue);
									
									if(isInPk) {
										primaryKeyValue.put(foreignKeyFields[i], fkFieldValue);
										
										//If all pk fields are parsed do not continue
										if(extractOnlyPrimaryKeyValue && primaryKeyValue.size() == primaryKeyFieldsName.length) {
											stopParsing = true;
											
											break;
										}
									}
								}
							}
						}
					} else {
						for(String foreignKey : foreignKeyFields) {
							if(stopParsing)
								break;
							
							boolean isInPk = primaryKeyFieldsNameList != null && primaryKeyFieldsNameList.contains(foreignKey);
							
							if(!extractOnlyPrimaryKeyValue || isInPk) {
								if(!values.containsKey(foreignKey))
									values.put(foreignKey, null);
								
								if(isInPk) {
									primaryKeyValue.put(foreignKey, null);
									
									if(extractOnlyPrimaryKeyValue && primaryKeyValue.size() == primaryKeyFieldsName.length) {
										stopParsing = true;
										
										break;
									}
								}
							}
						}
					}
				}
			}
			
			if(primaryKeyFieldsNameList != null && primaryKeyValue.size() < primaryKeyFieldsNameList.size()) {
				Class<? extends ObjectPersistence> superPersitentType = ReflectionTools.getFirstPersistentClass(persitentType.getSuperclass());
				if(superPersitentType != null) {
					//for inheritance extract primary key value in super-object
					QueryData<Object> qdata = new QueryData<Object>(obj, superPersitentType, true, true, context);
					qdata.getObjectIdValues().forEach((key, value) -> {
						if(!primaryKeyValue.containsKey(key))
							primaryKeyValue.put(key, value);
					});
				}
			}

			if(!extractOnlyPrimaryKeyValue || !doNotTrackupdate)
				state = ObjectPersitenceState.getState(obj, persitentType, context);
		} catch (SecurityException e) {
			throw new ObjectPersistenceException(e);
		} catch (IllegalArgumentException e) {
			throw new ObjectPersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new ObjectPersistenceException(e);
		}
	}

	@Override
	public void putValuesMap(Map<String, Object> foreignValues) {
		values.putAll(foreignValues);

		if(primaryKeyFieldsName != null) {
			for(String pkField : primaryKeyFieldsName)
				primaryKeyValue.put(pkField, values.get(pkField));
		}
	}

	/**
	 * Retourne la map contenant les données de à rendre persistante
	 *
	 * @return la map contenant les données de à rendre persistante
	 */
	@Override
	public Map<String, Object> getValues() {
		return Collections.unmodifiableMap(values);
	}

	/**
	 * Retourne la valeur de la clé primaire sous la forme d'une map de champs, valeur
	 *
	 * @return la valeur de la clé primaire
	 */
	@Override
	public Map<String, Object> getObjectIdValues() {
		return Collections.unmodifiableMap(primaryKeyValue);
	}

	/**
	 * Retourne le champs dont la valeur doit être généré à l'enregistrement en base
	 *
	 * @return le champs dont la valeur doit être généré à l'enregistrement en base
	 */
	@Override
	public Field getGeneratedField() {
		return generatedField;
	}

	/**
	 * Retourne la valeur généré à l'enregistrement en base
	 *
	 * @return la valeur généré à l'enregistrement en base
	 */
	public Object getGeneratedId() {
		return generatedId;
	}

	/**
	 * affecte la valeur généré par le moteur de base de donnée
	 */
	@Override
	public void setGeneratedId(Object id) {
		primaryKeyValue.put(generatedFieldName, id);

		generatedId = id;
	}

	@Override
	public boolean isModified() {
		return state == State.NEW || state == State.MODIFIED;
	}

	@Override
	public Object getSourceObject() {
		return sourceObject;
	}
}
