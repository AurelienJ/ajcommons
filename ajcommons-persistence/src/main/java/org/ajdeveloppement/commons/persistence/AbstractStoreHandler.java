/**
 * 
 */
package org.ajdeveloppement.commons.persistence;

/**
 * Couche d'abstraction permettant d'implémenter un mode de persistance spécifique (XML, SQL, Binaire, JSON, ...)
 * 
 * @author Aurélien JEOFFRAY
 *
 * @param <T> Le type de l'objet à enregitrer
 */
public abstract class AbstractStoreHandler<T extends ObjectPersistence> {
	
	/**
	 * Analyse un objet pour en collecter les données stoqués. La façon de collecter
	 * dépend de l'implémentation.
	 * 
	 * @param obj l'objet à analyser
	 * @return les données de l'objet devant être stocké par la couche de persistance
	 * @throws ObjectPersistenceException
	 */
	public abstract ObjectData parseObject(T obj) throws ObjectPersistenceException;
	
	/**
	 * Execute la requête d'insertion/mise à jour des données dans la couche de persistance
	 * 
	 * @param datas les données de l'objet à rendre persistant.
	 * @return si la couche de persistance doit générer un identifiant d'enregistrement, retourne celui ci, sinon renvoi null
	 * @throws ObjectPersistenceException
	 */
	public abstract Object executeUpdate(ObjectData datas) throws ObjectPersistenceException;
	
	/**
	 * Execute la requête de suppression d'un enregistrement de la couche de persistance
	 * 
	 * @param datas les données permettant d'obtenir l'identifiant unique de l'enregistrement à supprimer
	 * @throws ObjectPersistenceException
	 */
	public abstract void executeDelete(ObjectData datas) throws ObjectPersistenceException;

}
