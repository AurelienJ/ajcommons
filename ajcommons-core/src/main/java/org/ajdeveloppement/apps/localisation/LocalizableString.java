/*
 * Créé le 4 déc. 2009 à 19:36:06 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
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
 * pris connaissance de la licence CeCILL-C, et que vous en avez accepté les
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
package org.ajdeveloppement.apps.localisation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * <div lang="fr">
 * 	<p>Un <code>LocalizableString</code> est une chaîne non immuable contrairement <code>String</code>
 * 	permettant un changement de libellé en conservant la même référence mémoire.</p>
 * </div>
 * <div lang="en">
 *  <p>A <code>LocalizableString</code> isn't immutable string unlike <code>String</code> object
 * 	allowing change lable while retaining the same memory reference</p>
 * </div>
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.1
 *
 */
public class LocalizableString {
	private String localizedString;
	private Object[] replaces = null;
	
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	/**
	 */
	public LocalizableString() {
	}
	
	/**
	 * Init localizable string without default localized string, but eventually with java string format replacement. 
	 * 
	 * @param replaces dynamic replacement for string format var args
	 */
	public LocalizableString(Object... replaces) {
		this(null, replaces);
	}
	
	/**
	 * Init an LocalizableString with a default localized inner string.
	 * 
	 * @param defaultLocalizedString the default localized inner string
	 */
	public LocalizableString(String defaultLocalizedString) {
		this(defaultLocalizedString, (Object[])null);
	}
	
	/**
	 * Init an LocalizableString with a default localized inner string and a collection of format var args.
	 * In this case, string must use java string format synthax
	 * 
	 * @param defaultLocalizedString the default localized inner string
	 * @param replaces dynamic replacement for string format var args
	 */
	public LocalizableString(String defaultLocalizedString, Object... replaces) {
		this.localizedString = defaultLocalizedString;
		this.replaces = replaces;
	}
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
	 * 
	 * @param l the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}
	
	/**
	 * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
	 * 
	 * @param l the listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	/**
	 * Return the localized string in brut form (without string format replacement)
	 * 
	 * @return the localizedString the localized string in brut form
	 */
	public String getLocalizedString() {
		return localizedString;
	}

	/**
	 * Set the inner localized string. Localized string can use java string format synthax
	 * 
	 * @param localizedString the inner localized string
	 */
	public void setLocalizedString(String localizedString) {
		String oldValue = this.localizedString;
		
		this.localizedString = localizedString;
		
		pcs.firePropertyChange("localizedString", oldValue, localizedString); //$NON-NLS-1$
	}
	
	
	/**
	 * Return the inner localized string formated if necessary
	 */
	@Override
	public String toString() {
		String outString = localizedString;
		if(replaces != null)
			outString = String.format(localizedString, replaces);
		return outString;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (localizedString == null) ? 0 : localizedString.hashCode();
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
		LocalizableString other = (LocalizableString) obj;
		if (localizedString == null) {
			if (other.localizedString != null)
				return false;
		} else if (!localizedString.equals(other.localizedString))
			return false;
		return true;
	}
}
