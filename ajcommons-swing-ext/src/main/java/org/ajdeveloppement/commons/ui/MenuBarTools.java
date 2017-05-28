/*
 * Créer le 23 mars 08 à 17:09:27 pour AjCommons (Bibliothèque de composant communs)
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
package org.ajdeveloppement.commons.ui;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Outils de manipulation simplifié des barres d'outils
 * 
 * @author Aurélien JEOFFRAY
 *
 */
public class MenuBarTools {
	
	/**
	 * Ajoute un élément à une barre d'outil en le plaçant à la position définit par le path
	 * 
	 * @param menuItem l'élément à ajouter
	 * @param menubar la barre de menu dans laquelle ajouter l'élément
	 * @param path le chemin de positionnement de l'élément
	 */
	public static void addItem(JMenuItem menuItem, JMenuBar menubar, String[] path) {
		menuItem.setName(path[path.length - 1]);
		for(int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			if(menu.getName().equals(path[0])) {
				if(path.length > 2)
					addItem(menuItem, menu, Arrays.copyOfRange(path, 1, path.length));
				else
					menu.add(menuItem);
			}
		}
	}
	
	/**
	 * Cache les menus sans éléments enfant
	 * 
	 * @param menubar la barre de menu pour laquelle cacher les éléments enfant
	 */
	public static void hideEmptyMenu(JMenuBar menubar) {
		for(int i = 0; i < menubar.getMenuCount(); i++) {
			hideEmptyMenu(menubar.getMenu(i));
		}
	}
	
	/**
	 * Ajoute un séparateur à une barre d'outil en le plaçant à la position définit par le path
	 * 
	 * @param menubar la barre de menu dans laquelle ajouter l'élément
	 * @param path le chemin de positionnement de l'élément
	 */
	public static void addSeparator(JMenuBar menubar, String[] path) {
		for(int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			if(menu.getName().equals(path[0])) {
				if(path.length > 2)
					addSeparator(menu, Arrays.copyOfRange(path, 1, path.length));
				else
					menu.addSeparator();
			}
		}
	}
	
	public static void removeItem(JMenuBar menubar, String[] path) {
		for(int i = 0; i < menubar.getMenuCount(); i++) {
			JMenu menu = menubar.getMenu(i);
			if(menu.getName().equals(path[0])) {
				if(path.length > 1)
					removeItem(menu, Arrays.copyOfRange(path, 1, path.length));
				else
					menubar.remove(menu);
			}
		}
	}
	
	private static void addItem(JMenuItem menuItem, JMenu rootMenu, String[] path) {
		boolean find = false;
		for(Component component : rootMenu.getMenuComponents()) {
			if (component instanceof JMenu) {
				JMenu menu = (JMenu) component;
				if(menu.getName().equals(path[0])) {
					if(path.length > 2)
						addItem(menuItem, menu, Arrays.copyOfRange(path, 1, path.length - 1));
					else
						menu.add(menuItem);
					find = true;
					break;
				}
			}
		}
		if(!find) {
			JMenu menu = new JMenu(path[0]);
			menu.setName(path[0]);
			if(path.length > 2)
				addItem(menuItem, menu, Arrays.copyOfRange(path, 1, path.length - 1));
			else
				menu.add(menuItem);
			rootMenu.add(menu);
		}
	}
	
	private static void hideEmptyMenu(JMenu menu) {
		if(menu.getItemCount() == 0)
			menu.setVisible(false);
		else {
			for(int i = 0; i < menu.getItemCount(); i++) {
				if(menu.getItem(i) instanceof JMenu) {
					hideEmptyMenu((JMenu)menu.getItem(i));
				}
			}
		}
	}
	
	private static void addSeparator(JMenu rootMenu, String[] path) {
		boolean find = false;
		for(Component component : rootMenu.getMenuComponents()) {
			if (component instanceof JMenu) {
				JMenu menu = (JMenu) component;
				if(menu.getName().equals(path[0])) {
					if(path.length > 2)
						addSeparator(menu, Arrays.copyOfRange(path, 1, path.length - 1));
					else
						menu.addSeparator();
					find = true;
					break;
				}
			}
		}
		if(!find) {
			JMenu menu = new JMenu(path[0]);
			menu.setName(path[0]);
			if(path.length > 2)
				addSeparator(menu, Arrays.copyOfRange(path, 1, path.length - 1));
			else
				menu.addSeparator();
			rootMenu.add(menu);
		}
	}
	
	private static void removeItem(JMenu rootMenu, String[] path) {
		for(Component component : rootMenu.getMenuComponents()) {
			if (component instanceof JMenu) {
				JMenu menu = (JMenu) component;
				if(menu.getName().equals(path[0])) {
					if(path.length > 1)
						removeItem(menu, Arrays.copyOfRange(path, 1, path.length));
					else
						rootMenu.remove(menu);
					
					break;
				}
			} else if(component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem)component;
				if(menuItem.getName() != null && menuItem.getName().equals(path[0])) {
					rootMenu.remove(component);
				
					break;
				}
			}
		}
	}
}
