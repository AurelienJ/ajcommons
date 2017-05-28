/*
 * Créé le 25 juil. 2009 à 11:58:03 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2009 - Aurélien JEOFFRAY
 *
 * http://www.ajdeveloppement.org
 *
 * *** CeCILL Terms *** 
 *
 * FRANCAIS:
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA 
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
 * pri connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 *
 * ENGLISH:
 * 
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 *
 *  *** GNU GPL Terms *** 
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.ajdeveloppement.apps.localisation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.UncheckedException;

/**
 * Utilitaire de localisation d'interfaces utilisateurs. Les champs localisable de la class à localiser
 * doivent être annoté avec l'annotation {@link Localizable}
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 *
 */
public class Localizator {
	
	private static Map<Class<?>, LocalizationHandler> handlers = new HashMap<Class<?>, LocalizationHandler>();
	
	private static DefaultLocalizationHandler defaultHandler = new DefaultLocalizationHandler();
	
	static {
		handlers.put(JTabbedPane.class, new JTabbedPaneLocalizationHandler());
		handlers.put(LocalizableString.class, new LocalizableStringLocalisationHandler());
	}
	
	/**
	 * Ajoute un handler de localisation pour un type donné
	 * 
	 * @param localisableClass le type à interpréter
	 * @param handler l'interpréteur associé.
	 */
	public static void addLocalisationHandler(Class<?> localisableClass, LocalizationHandler handler) {
		handlers.put(localisableClass, handler);
	}
	
	/**
	 * Retire le handler de localisation associé au type donné
	 * 
	 * @param localisableClass le type pour lequel retirer le handler si il existe
	 */
	public static void removeLocalisationHandler(Class<?> localisableClass) {
		handlers.remove(localisableClass);
	}
	
	/**
	 * <p>Localise un écran en fonction du système de ressource de localisation fournit en paramètre.</p>
	 * <p>Les erreurs durant le processus de sérialisation sont transmise silencieusement à la
	 * console d'erreur standard pour ne pas interrompre l'éxecution de l'application.</p>
	 * 
	 * @param localisableObj l'écran à localisé. Les champs à localisé doivent être marqué avec
	 * l'annotation {@link Localizable} et doivent être géré par les handlers su localisateur. Par défaut sont géré:<br>
	 * - les champs possedant une méthode d'affectation de chaîne "setText" ou autre méthode dont le nom est sépcifié dans l'annotation {@link Localizable}<br>
	 * - les champs de type {@link JTabbedPane} dans ce cas, c'est le libéllé de chaque onglet qui est localisé. Voir {@link JTabbedPaneLocalizationHandler} pour
	 * plus de précision<br>
	 * - les champs de type {@link LocalizableString}<br>
	 * Il est possible de rajouter des handler personnalisé avec {@link #addLocalisationHandler(Class, LocalizationHandler)}
	 * 
	 * @param localisableReader l'objet de localisation à utiliser
	 */
	public static void localize(Object localisableObj, AjResourcesReader localisableReader) {
		localize(localisableObj, localisableReader, null);
	}
	
	/**
	 * <p>Localise un écran en fonction du système de ressource de localisation fournit en paramètre.</p>
	 * <p>Les erreurs durant le processus de sérialisation sont transmise silencieusement à la
	 * console d'erreur standard pour ne pas interrompre l'éxecution de l'application.</p>
	 * 
	 * @param localisableObj l'écran à localisé. Les champs à localisé doivent être marqué avec
	 * l'annotation {@link Localizable} et doivent être géré par les handlers su localisateur. Par défaut sont géré:<br>
	 * - les champs possedant une méthode d'affectation de chaîne "setText" ou autre méthode dont le nom est sépcifié dans l'annotation {@link Localizable}<br>
	 * - les champs de type {@link JTabbedPane} dans ce cas, c'est le libéllé de chaque onglet qui est localisé. Voir {@link JTabbedPaneLocalizationHandler} pour
	 * plus de précision<br>
	 * - les champs de type {@link LocalizableString}<br>
	 * Il est possible de rajouter des handler personnalisé avec {@link #addLocalisationHandler(Class, LocalizationHandler)}
	 * 
	 * @param localisableReader l'objet de localisation à utiliser
	 * 
	 * @param handlers interpréteur de localisation spécifique à l'objet que l'on souhaite localiser
	 */
	public static void localize(Object localisableObj, AjResourcesReader localisableReader, Map<Class<?>, LocalizationHandler> handlers) {
		if(localisableObj.getClass().isAnnotationPresent(Localizable.class)) {
			Localizable loc = localisableObj.getClass().getAnnotation(Localizable.class);
			String LocalizableString = loc.value();
			
			LocalizationHandler handler = getObjectLocalisationHandler(localisableObj, handlers);
			
			if(!LocalizableString.isEmpty()) {
				handler.processLocalisation(localisableObj, loc.textMethod(), localisableReader, LocalizableString);
			}
			if(!loc.tooltip().isEmpty() && !loc.tooltipTextMethod().isEmpty()) {
				handler.processLocalisation(localisableObj, loc.tooltipTextMethod(), localisableReader, loc.tooltip());
			}

		}
		for(Field field : localisableObj.getClass().getDeclaredFields()) {
			if(field.isAnnotationPresent(Localizable.class)) {
				Localizable loc = field.getAnnotation(Localizable.class);
				String LocalizableString = loc.value();
				try {
					field.setAccessible(true);
					Object fieldObj = field.get(localisableObj);
					
					LocalizationHandler handler = getObjectLocalisationHandler(fieldObj, handlers);
					
					if(!LocalizableString.isEmpty()) {
						handler.processLocalisation(fieldObj, loc.textMethod(), localisableReader, LocalizableString);
					}
					if(!loc.tooltip().isEmpty() && !loc.tooltipTextMethod().isEmpty()) {
						handler.processLocalisation(fieldObj, loc.tooltipTextMethod(), localisableReader, loc.tooltip());
					}
				} catch (IllegalArgumentException e) {
					throw new UncheckedException(e);
				} catch (IllegalAccessException e) {
					throw new UncheckedException(e);
				} catch (SecurityException e) {
					throw new UncheckedException(e);
				}
			}
		}
	}
	
	/**
	 * Retourne le handler associé à l'objet à localiser
	 * 
	 * @param localisableObj l'objet à localiser
	 * @return le handler de localisation associé à l'objet annoté comme localisable
	 */
	private static LocalizationHandler getObjectLocalisationHandler(Object localisableObj, Map<Class<?>, LocalizationHandler> customHandlers) {
		if(customHandlers != null) {
			for(Class<?> clazz : customHandlers.keySet()) {
				if(localisableObj.getClass().isAssignableFrom(clazz))
					return customHandlers.get(clazz);
			}
		}
		for(Class<?> clazz : handlers.keySet()) {
			if(localisableObj.getClass().isAssignableFrom(clazz))
				return handlers.get(clazz);
		}
		return defaultHandler;
	}
}
