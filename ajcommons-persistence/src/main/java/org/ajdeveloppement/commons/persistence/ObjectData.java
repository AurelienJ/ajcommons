package org.ajdeveloppement.commons.persistence;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 
 * Interface représentant les données d'un objet tel que devant être présenté à la couche
 * de persistance
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public interface ObjectData {
	/**
	 * Ajoute une map de champs/valeurs aux données de l'objet
	 * 
	 * @param values les valeurs à ajouter à la persistance
	 */
	public void putValuesMap(Map<String, Object> values);
	
	/**
	 * Fixe la valeur de l'identifiant de l'objet
	 * 
	 * @param generatedId l'identifiant de l'objet généré à l'insertion (typiquement en base)
	 */
	public void setGeneratedId(Object generatedId);
	
	/**
	 * Retourne une map de champs/valeurs représentant les données de l'objet
	 * 
	 * @return les données de l'objet
	 */
	public Map<String, Object> getValues();
	
	/**
	 * Retourne une map de champs/valeurs identifiant l'objet de manière unique
	 * 
	 * @return une map de champs/valeurs identifiant l'objet de manière unique
	 */
	public Map<String, Object> getObjectIdValues();
	
	/**
	 * Retourne le champs dont la valeur doit être généré au moment
	 * de l'action de persistance
	 * 
	 * @return le champs dont la valeur doit être généré
	 */
	public Field getGeneratedField();
	
	/**
	 * Indique si les données de l'objet ont été modifiées ou non
	 * depuis leurs chargement de la couche de persistance.
	 * 
	 * @return true si les données ont été modifiées, false sinon
	 */
	public boolean isModified();
	
	/**
	 * Retourne l'objet à la source des données de persistances
	 * 
	 * @return l'objet à la source des données de persistances
	 */
	public Object getSourceObject();
}
