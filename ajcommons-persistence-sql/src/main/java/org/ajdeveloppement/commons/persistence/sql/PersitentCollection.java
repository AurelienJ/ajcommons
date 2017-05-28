/*
 * Créé le 18 oct. 2012 à 20:49:38 pour AjCommons (Bibliothèque de composant communs)
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.Session;



/**
 * TODO
 * 1-Identification du type precis des elements de la collection
 * 2-Identification de la pk du type
 * 3-si ajout/suppression avant iteration
 * 3 - historisation de l'action d'ajout/suppression avec les pks
 * 4-a l'iteration envoi de la requete de chargement en base et application des actions historisé sur la
 *   collection resultante. Les éléments ajouté sont ajouté à la collection les éléments supprimé retiré
 * 5-au save, application des actions historisé selon le scenario le plus performant en fonction
 *   - du nombre d'élement supprimé
 *   - du nombre d'élement ajouté
 *   - de la complexité de la clé primaire (simple ou composé)
 *   
 *   les scenarios seront : 
 *   	pour l'ajout: systematiquement individuel
 *      pour la suppression:
 *      	individuel si clé composé ou petit nombre d'element en clé simple et suppression partiel
 *      	groupé si suppression total de la collection ou nombre réduit d'élement avec une clé simple (requete in)
 *      	groupé par lots si grand nombre d'élément et clé simple
 * 
 * @author Aurelien JEOFFRAY
 * @param <E> Type of persistence object
 */
public class PersitentCollection<E extends ObjectPersistence> extends AbstractCollection<E> {
	/**
	 * Database source collection
	 */
	private Collection<E> collection;
	/**
	 * Memory source collection (before save)
	 */
	private List<E> unsavedCollection;
	/**
	 * Database source pk (after modify action and before collection iteration)
	 */
	private List<Map<String, Object>> persistentPk;
	/**
	 * Memory delete pk
	 */
	private List<Map<String, Object>> deletedPk;
	/**
	 * if clear collection
	 */
	private boolean cleared = false;
	
	private Map<String, Object> foreignKey;
	private Class<?> persistentClass;
	private String tableName;
	private String domain;
	private String[] primaryKeyFieldsName = null;
	
	
	
	/**
	 * 
	 * @param collection
	 * @param foreignKey
	 */
	public PersitentCollection(Collection<E> collection, Map<String, Object> foreignKey) {
		this.collection = collection;
		this.foreignKey = foreignKey;
		
		if(collection != null && !collection.isEmpty()) {
			E firstElement = collection.iterator().next();
			persistentClass = ReflectionTools.getFirstPersistentClass(firstElement.getClass());
			tableName = ReflectionTools.getTableName(persistentClass);
			domain = ReflectionTools.getTableDomain(persistentClass);
			primaryKeyFieldsName = ReflectionTools.getPrimaryKeyFieldsName(persistentClass);
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		if(collection != null)
			return collection.iterator();
		return null;
	}

	@Override
	public int size() {
		if(collection != null)
			return collection.size();
		return 0;
	}
	
	@Override
	public boolean add(E item) {
		if(collection != null)
			return collection.add(item);
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object item) {
		if(collection != null)
			return collection.remove(item);
		
		return false;
	}
	
	/**
	 * 
	 * @param collection
	 * @param session
	 * @param foreignKey
	 * @throws ObjectPersistenceException
	 */
	public static <E extends ObjectPersistence> void save(Collection<E> collection, Session session, Map<String, Object> foreignKey) throws ObjectPersistenceException {
		PersitentCollection<E> pc = new PersitentCollection<E>(collection, foreignKey);
		pc.save(session);
	}
	
	public static <E extends ObjectPersistence> void delete(Collection<E> collection, Session session, Map<String, Object> foreignKey) throws ObjectPersistenceException {
		PersitentCollection<E> pc = new PersitentCollection<E>(collection, foreignKey);
		pc.delete(session);
	}
	
	
	/**
	 * Save the current collection of {@link ObjectPersistence} in database
	 * and delete element in database not in current database. 
	 * 
	 * @param session
	 * @throws ObjectPersistenceException
	 */
	public void save(Session session) throws ObjectPersistenceException {
		if(collection != null) {
			for(E element : collection) {
				element.save(session);
			}
		}
	}
	
	/**
	 * 
	 * @param session
	 * @throws ObjectPersistenceException
	 */
	public void delete(Session session) throws ObjectPersistenceException {
		delete(session, false);
	}
	
	/**
	 * Delete all elments in collection
	 * 
	 * @param session
	 * @param removeCache
	 * @throws ObjectPersistenceException
	 */
	public void delete(Session session, boolean removeCache) throws ObjectPersistenceException {
		SqlContext context = null;
		if(session instanceof SqlSession)
			context = ((SqlSession)session).getContext();
		
		PrimaryKeysMap pkMap = new PrimaryKeysMap();
		
		if(collection != null) {
			for(E element : collection) {
				PrimaryKeysMapNode[] pkNodes = extractPrimaryKey(element);
				if(pkNodes != null && pkNodes.length > 0)
					pkMap.put(pkNodes);
				
				if(session != null)
					session.addProcessedObject(element);
				
				if(removeCache && context != null)
					context.getCache().remove(element);
			}
		}
		
		executeDelete(session, pkMap, true, false);
	}
	
	/**
	 * Execute delete query on collection table
	 * 
	 * @param pkMap primary key map of object to delete or keep
	 * @param deleteAll if <code>true</code> delete All element in table with instance specified foreign key,
	 * else delete only element in collection or not in collection on function of <code>notInclude</code> param
	 * @param notInclude if <code>true</code> delete element not in primary key map, else delete element in primary key map
	 * @throws ObjectPersistenceException
	 */
	private void executeDelete(Session session, PrimaryKeysMap pkMap, boolean deleteAll, boolean notInclude) throws ObjectPersistenceException {
		if(tableName != null && !tableName.isEmpty()) {
			String query = String.format("delete from %s", tableName); //$NON-NLS-1$
			
			QFilter filter = null;
			if(collection != null && collection.size() > 0 && pkMap.size() > 0) {
				filter = getWhereClause(pkMap, notInclude, deleteAll);
				if(filter != null)
					query += String.format(" where %s", filter.getFilterClause()); //$NON-NLS-1$
			}
			
			try {
				SqlContext context = null;
				if(session instanceof SqlSession)
					context = ((SqlSession)session).getContext();
				
				if(context != null) {
					Connection connection = context.getConnectionForDomain(domain);
					PreparedStatement pstmt = connection.prepareStatement(query);
					
					if(filter != null) {
						int paramIndex = 1;
						for(Object o : filter.getParams())
							pstmt.setObject(paramIndex++, o);
					}
					
					pstmt.executeUpdate();
				}
			} catch (SQLException e) {
				throw new ObjectPersistenceException(e);
			}
		}
	}
	
	private PrimaryKeysMapNode[] extractPrimaryKey(E element) throws ObjectPersistenceException {

		if(primaryKeyFieldsName != null) {
			
			QueryData<E> data = new QueryData<E>(element, persistentClass, true, true, null);
			Map<String,Object> pk = data.getObjectIdValues();
			
			PrimaryKeysMapNode[] pkNodes = new PrimaryKeysMapNode[primaryKeyFieldsName.length];
			for(int i = 0; i < primaryKeyFieldsName.length; i++) {
				pkNodes[i] = new PrimaryKeysMapNode(primaryKeyFieldsName[i], pk.get(primaryKeyFieldsName[i]));
			}
			
			return pkNodes;
		}
		
		return null;
	}
	
	private QFilter getWhereClause(PrimaryKeysMap pkMap, boolean notInclude, boolean deleteAll) {
		Set<PrimaryKeysMapNode> nodes = null;
		Object[] values = null;
		String fieldName = null; 
		
		if(pkMap != null) {
			nodes = pkMap.getNodes();
			values = new Object[nodes.size()];
			int i = 0;
			
			for(PrimaryKeysMapNode node : nodes) {
				if(fieldName == null)
					fieldName = node.getName();
				values[i++] = node.getValue(); 
			}
		}
		
		QFilter foreignKeyClause = null;
		if(foreignKey != null) {
			for(Entry<String, Object> entry : foreignKey.entrySet()) {
				QFilter subClause = new QFilter(String.format("%s = ?", entry.getKey()), Collections.singletonList(entry.getValue()));  //$NON-NLS-1$
				if(foreignKeyClause == null)
					foreignKeyClause = subClause;
				else
					foreignKeyClause.and(subClause);
			}
		}
		
		QFilter qfilter = null;
		if(!deleteAll && values != null && values.length > 0) {
			String[] paramsWildCard = new String[values.length];
			Arrays.fill(paramsWildCard, "?"); //$NON-NLS-1$
			String notInFilterString = String.format("%s %s in (%s)", //$NON-NLS-1$
					fieldName,
					notInclude ? "not " : "", //$NON-NLS-1$ //$NON-NLS-2$
					String.join(",", paramsWildCard) //$NON-NLS-1$
					);
			qfilter = new QFilter(notInFilterString, Arrays.asList(values));
			
			for(PrimaryKeysMapNode node : nodes) {
				PrimaryKeysMap pkSubMap = pkMap.getSubMap(node);
				if(pkSubMap != null && pkSubMap.size() > 0) {
					QFilter subClause = new QFilter(String.format("%s = ?", fieldName), Arrays.asList(new Object[] { node.getValue() }));  //$NON-NLS-1$
					qfilter.or(subClause.and(getWhereClause(pkSubMap, notInclude, deleteAll)));
				}
			}
		}
		
		if(foreignKeyClause != null) {
			if(qfilter == null)
				qfilter = foreignKeyClause;
			else
				qfilter.and(foreignKeyClause);
		}
		
		return qfilter;
	}
	
	private static class PrimaryKeysMap {
		
		private Map<PrimaryKeysMapNode, PrimaryKeysMap> maps = new HashMap<>();
		
		public PrimaryKeysMap() {
		}
		
		public void put(PrimaryKeysMapNode... path) {
			if(path == null || path.length == 0)
				return;
			
			if(!maps.containsKey(path[0])) {
				maps.put(path[0], new PrimaryKeysMap());
			}
			
			if(path.length > 1)
				maps.get(path[0]).put(Arrays.copyOfRange(path, 1, path.length-1));
		}
		
		public int size() {
			return maps.size();
		}
		
		public Set<PrimaryKeysMapNode> getNodes() {
			return maps.keySet();
		}
		
		public PrimaryKeysMap getSubMap(PrimaryKeysMapNode node) {
			return maps.get(node);
		}
	}
	
	private static class PrimaryKeysMapNode {
		private String name;
		private Object value;
		
		/**
		 * @param name
		 * @param value
		 */
		public PrimaryKeysMapNode(String name, Object value) {
			this.name = name;
			this.value = value;
		}
		

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
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
			PrimaryKeysMapNode other = (PrimaryKeysMapNode) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
}
