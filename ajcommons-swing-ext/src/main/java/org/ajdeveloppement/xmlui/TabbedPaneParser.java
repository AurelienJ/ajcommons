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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.ajdeveloppement.commons.ui.AJTabbedPane;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Interface d'interprétation des noeuds XML de composant de type onglet
 * 
 * @author Aurélien JEOFFRAY
 * @deprecated Use JavaFX API instead
 */
@SuppressWarnings("deprecation")
@Deprecated
public class TabbedPaneParser implements ComponentParser {
	private JComponent component;
	private String name;

	private final Map<String, JComponent> namedComponents = new HashMap<>();
	private final AjResourcesReader ajResourcesReaderLibelle;

	/**
	 * Construit le parser de tabbedpane
	 * 
	 * @param ajResourcesReaderLibelle l'objet de localisation
	 */
	public TabbedPaneParser(AjResourcesReader ajResourcesReaderLibelle) {
		this.ajResourcesReaderLibelle = ajResourcesReaderLibelle;
	}

	/**
	 * Retourne le tabbedpane sur lequel les composants produits sont appliqué
	 * 
	 * @return tabbedpane
	 */
	@Override
	public JComponent getComponent() {
		return component;
	}

	/**
	 * Définit le tabbedpane sur lequel appliquer les composants produits
	 * 
	 * @param component
	 */
	public void setComponent(JComponent component) {
		this.component = component;
	}

	/**
	 * Retourne le nom du composant généré
	 * 
	 * @return name le nom du composant généré
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Donne un nom au composant généré
	 * 
	 * @param name le nom à donner au composant généré
	 */
	public void setName(String name) {
		this.name = name;
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
     * 
     * @param panelnode noeud à analyser
	 */
	@Override
	public void parse(Node panelnode) {
		AJTabbedPane tabbedPane = new AJTabbedPane();

		NamedNodeMap namedNodeMap = panelnode.getAttributes();

		Node componentName = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$

		name = componentName.getNodeValue();
		component = tabbedPane;

		NodeList childNodes = panelnode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
            switch (childNode.getNodeName()) {
                case "method": //$NON-NLS-1$
                    //$NON-NLS-1$
                    CommonUIParser.parseMethod(childNode, tabbedPane);
                    break;
                case "tab": //$NON-NLS-1$
                    //$NON-NLS-1$
                    parseTab(childNode, tabbedPane);
                    break;
            }
		}
	}

	private void parseTab(Node node, AJTabbedPane tabbedPane) {
		NamedNodeMap namedNodeMap = node.getAttributes();

		Node closeButton = namedNodeMap.getNamedItem("closebutton"); //$NON-NLS-1$

		// tabbedPane.addTab("", component);
		JComponent tabComponent = null;
		String title = ""; //$NON-NLS-1$

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
            switch (childNode.getNodeName()) {
                case "label":  //$NON-NLS-1$
                    title = ajResourcesReaderLibelle.getResourceString(childNode.getTextContent());
                    break;
                case "content":  //$NON-NLS-1$
                    ComponentParser panelParser = ComponentParserFactory.getComponentParser("panel", ajResourcesReaderLibelle); //$NON-NLS-1$
                    panelParser.parse(childNode);
                    tabComponent = panelParser.getComponent();
                    if(panelParser.getName() != null)
                        namedComponents.put(panelParser.getName(), tabComponent);
                    namedComponents.putAll(panelParser.getNamedComponents());
                    break;
            }
		}

		tabbedPane.addTab(title, tabComponent);

		if (closeButton.getNodeValue().equals("false")) { //$NON-NLS-1$
			tabbedPane.hideIconAt(tabbedPane.getTabCount() - 1);
		}
	}
}
