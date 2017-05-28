/*
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
package org.ajdeveloppement.xmlui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Interface d'interprétation des noeuds XML de composant de type panel
 * 
 * @author Aurélien JEOFFRAY
 * @deprecated Use JavaFX API instead
 */
@SuppressWarnings("deprecation")
@Deprecated
public class PanelParser implements ComponentParser {

	private JComponent panel;
	private String panelName;

	private final Map<String, JComponent> namedComponents = new HashMap<String, JComponent>();

	@SuppressWarnings("unused")
	private final AjResourcesReader ajResourcesReaderLibelle;

	/**
	 * Construit le parser de panel
	 * 
	 * @param ajResourcesReaderLibelle l'objet de localisation du panel
	 */
	public PanelParser(AjResourcesReader ajResourcesReaderLibelle) {
		this.ajResourcesReaderLibelle = ajResourcesReaderLibelle;
	}

	/**
	 * Retourne le panel sur lequel les composants produits sont appliqué
	 * 
	 * @return panel
	 */
	@Override
	public JComponent getComponent() {
		return panel;
	}

	/**
	 * Définit le panel sur lequel appliquer les composants produits
	 * 
	 * @param panel
	 */
	public void setComponent(JComponent panel) {
		this.panel = panel;
	}

	/**
	 * Retourne le nom du composant généré
	 * 
	 * @return name le nom du composant généré
	 */
	@Override
	public String getName() {
		return panelName;
	}

	/**
	 * Donne un nom au composant généré
	 * 
	 * @param panelName le nom à donner au composant généré
	 */
	public void setName(String panelName) {
		this.panelName = panelName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ajinteractive.standard.ui.ComponentParser#getNamedComponents()
	 */
	@Override
	public Map<String, JComponent> getNamedComponents() {
		return namedComponents;
	}

	/**
	 * Analyse du noeud XML et conversion en composant
	 */
	@Override
	public void parse(Node panelnode) {
		NamedNodeMap namedNodeMap = panelnode.getAttributes();
		NodeList childNodes = panelnode.getChildNodes();
		
		Node componentName = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
		if(componentName != null)
			panelName = componentName.getNodeValue();
		panel = new JPanel();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("layout")) { //$NON-NLS-1$
				panel.setLayout(parseLayoutManager(childNode));
			} else if (childNode.getNodeName().equals("component")) { //$NON-NLS-1$
				ComponentEntry cmptEntry = parseComponent(childNode);
				panel.add(cmptEntry.getComponent(), cmptEntry.getConstraints());
			}
		}
	}
	
	private LayoutManager parseLayoutManager(Node layoutnode) {
		LayoutManager lManager = null;
		
		Class<?> cLayoutManager;
		try {
			cLayoutManager = CommonUIParser.parseParamClass(layoutnode);
			lManager = (LayoutManager)CommonUIParser.parseParam(layoutnode, cLayoutManager);
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return lManager;
	}
	
	private ComponentEntry parseComponent(Node componentnode) {
		NamedNodeMap namedNodeMap = componentnode.getAttributes();

		Node type = namedNodeMap.getNamedItem("type"); //$NON-NLS-1$

		JComponent component = null;
		Object constraints = null;

		if (type != null && type.getNodeValue().equals("intern")) { //$NON-NLS-1$
			Node name = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
			namedComponents.put(name.getNodeValue(), new JPanel());
		} else {
			Node componentName = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
			
			component = (JComponent)CommonUIParser.parseParam(componentnode, null);
			if(componentName != null) {
				component.setName(componentName.getNodeValue());
				namedComponents.put(componentName.getNodeValue(), component);
			}
		}

		if (component != null) {
			NodeList childNodes = componentnode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);

				if (childNode.getNodeName().equals("constraints")) { //$NON-NLS-1$
					constraints = parseConstraints(childNode); 
				}
			}
		}
		
		return new ComponentEntry(component, constraints);
	}
	
	private Object parseConstraints(Node constraintsnode) {
		Object constraints = null;
		
		Class<?> cConstraints;
		try {
			cConstraints = CommonUIParser.parseParamClass(constraintsnode);
			constraints = CommonUIParser.parseParam(constraintsnode, cConstraints);
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return constraints;
	}
	
	private class ComponentEntry {
		private Component component;
		private Object constraints;
		
		/**
		 * @param component
		 * @param constraints
		 */
		public ComponentEntry(Component component, Object constraints) {
			this.component = component;
			this.constraints = constraints;
		}
		
		/**
		 * @return the component
		 */
		public Component getComponent() {
			return component;
		}
		/**
		 * @param component the component to set
		 */
		/*public void setComponent(Component component) {
			this.component = component;
		}*/
		/**
		 * @return the constraints
		 */
		public Object getConstraints() {
			return constraints;
		}
		/**
		 * @param constraints the constraints to set
		 */
		/*public void setConstraints(Object constraints) {
			this.constraints = constraints;
		}*/
		
	}
}
