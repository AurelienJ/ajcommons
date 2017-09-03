/*
 * Créé le 2 janv. 2017 à 11:51:10 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2017 - Aurélien JEOFFRAY
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.ajdeveloppement.commons.Lazy;
import org.ajdeveloppement.commons.UncheckedException;
import org.ajdeveloppement.commons.persistence.ObjectPersistence;
import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;
import org.ajdeveloppement.commons.persistence.Session;

/**
 * @author aurelien
 * 
 * TODO Extraire la pk d'un item pour executer une requete contains
 *
 */
public class LazyPersistentCollection<E extends ObjectPersistence, K> implements Collection<E>, ObjectPersistence {
	
	private Lazy<QResults<E, K>> internalQResults;
	
	/**
	 * Added uncommited items
	 */
	private List<E> addedItems = new ArrayList<E>();
	
	/**
	 * Removed uncommited items
	 */
	private List<E> deletedItems = new ArrayList<E>();
	
	/**
	 * Initialize a lazy persistent collection binded on a QResults data source
	 * 
	 * @param qresults the data source for collection
	 */
	public LazyPersistentCollection(Supplier<QResults<E, K>> qresults) {
		this.internalQResults = new Lazy<>(qresults);
	}
	
	/**
	 * Return the type of collection elements
	 * 
	 * @return The type of collection elements
	 */
	private Class<?> getElementType() {
		return internalQResults.get().getReturnType();
	}
	
	private Map<String,Object> extractPrimaryKey(E element) throws ObjectPersistenceException {
		QueryData<E> data = new QueryData<E>(element, getElementType(), true, true, null);
		return data.getObjectIdValues();
	}
	
	public QResults<E, K> getQResults() {
		return internalQResults.get();
	}
	
	/**
	 * Returns all collections elements (committed and uncommitted) as an unoredered iterable
	 * 
	 * @return
	 */
	public Iterable<E> getUncommitedItems() {
		return new Iterable<E>() {
			private Iterable<E> persistentIterable = internalQResults.get();
			private Iterable<E> uncommitedIterable = addedItems;

			@Override
			public Iterator<E> iterator() {
				return new Iterator<E>() {
					private Iterator<E> persistentIterator = persistentIterable.iterator();
					private Iterator<E> uncommitedIterator = uncommitedIterable.iterator();
					
					private E nextElement = null;
					
					private synchronized E getNextElement() {
						if(nextElement == null) {
							if(persistentIterator.hasNext()) {
								E item = persistentIterator.next();
								if(!deletedItems.contains(item) && !addedItems.contains(item)) {
									nextElement = item;
								}
							}
							
							if(nextElement == null && uncommitedIterator.hasNext()) {
								nextElement = uncommitedIterator.next();
							}
						}
						
						return nextElement;
					}

					@Override
					public boolean hasNext() {
						return getNextElement() != null;
					}

					@Override
					public E next() {
						E nextItem = getNextElement();
						
						nextElement = null;
						
						return nextItem;
					}
				};
			}
		};
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return internalQResults.get().count();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		if(getElementType().isAssignableFrom(o.getClass())) {
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> pk = extractPrimaryKey((E)o);
				
				QFilter filter= null;
				
				for(Map.Entry<String, Object> entry : pk.entrySet()) {
					String tableName = ReflectionTools.getTableName(getElementType());
					
					QField<Object> qField = new QField<>(tableName, entry.getKey());
					QFilter entryFilter = qField.equalTo(entry.getValue());
					if(filter == null)
						filter = entryFilter;
					else
						filter.and(entryFilter);
				}
				
				Boolean contains = QResults.from(getElementType()).where(filter)
						.selectOneColumn(QField.<Boolean>custom("1")).iterator().next();
				return contains != null && contains.booleanValue();
				
			} catch (ObjectPersistenceException | SQLException e) {
				throw new UncheckedException(e);
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return internalQResults.get().iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return internalQResults.get().asList().toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return internalQResults.get().asList().toArray(a);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		boolean added = false;

		if(!addedItems.contains(e))
			added = addedItems.add(e);
		
		if(deletedItems.contains(e))
			deletedItems.remove(e);
		
		return added;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		boolean removed = false;
		
		if(getElementType().isAssignableFrom(o.getClass()) && !deletedItems.contains(o))
			removed = deletedItems.add((E)o);
		
		if(addedItems.contains(o))
			addedItems.remove(o);
		
		return removed;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		if(c != null) {
			for (Object object : c) {
				if(!contains(object))
					return false;
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean collectionChanged = false;
		for(E item : c) {
			if(add(item))
				collectionChanged = true;
		}
		return collectionChanged;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean collectionChanged = false;
		for(Object item : c) {
			if(remove(item))
				collectionChanged = true;
		}
		return collectionChanged;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		deletedItems.addAll(internalQResults.get().asList());
		
		return addAll((Collection<? extends E>)c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		removeAll(internalQResults.get().asList());
	}

	/* (non-Javadoc)
	 * @see org.ajdeveloppement.commons.persistence.ObjectPersistence#save()
	 */
	@Override
	public void save() throws ObjectPersistenceException {
		SessionHelper.startSaveSession(this);
	}

	/* (non-Javadoc)
	 * @see org.ajdeveloppement.commons.persistence.ObjectPersistence#save(org.ajdeveloppement.commons.persistence.Session)
	 */
	@Override
	public void save(Session session) throws ObjectPersistenceException {
		for (E e : addedItems) {
			e.save(session);
		}
		
		for (E e : deletedItems) {
			e.delete(session);
		}
	}

	/* (non-Javadoc)
	 * @see org.ajdeveloppement.commons.persistence.ObjectPersistence#delete()
	 */
	@Override
	public void delete() throws ObjectPersistenceException {
		SessionHelper.startDeleteSession(this);
	}

	/* (non-Javadoc)
	 * @see org.ajdeveloppement.commons.persistence.ObjectPersistence#delete(org.ajdeveloppement.commons.persistence.Session)
	 */
	@Override
	public void delete(Session session) throws ObjectPersistenceException {
		clear();
		
		save(session);
	}
}
