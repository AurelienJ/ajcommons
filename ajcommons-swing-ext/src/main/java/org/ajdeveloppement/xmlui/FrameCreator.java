/*
 * Créer le 13 déc. 06 - 18:50:36
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
package org.ajdeveloppement.xmlui;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ajdeveloppement.commons.AjResourcesReader;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Construit une fenetre à partir d'un fichier XML de description
 * 
 * @author Aurélien JEOFFRAY
 * @deprecated Use JavaFX API instead
 */
@Deprecated
public class FrameCreator {

	private JFrame frame;

	private final EventListenerList listeners = new EventListenerList();

	private Object[] titleReplacement;

	private final Map<String, JComponent> namedComponents = new HashMap<String, JComponent>();

	private AjResourcesReader ajResourcesReaderLibelle;

	/**
	 * Initialise la construction d'une fenetre
	 * 
	 * @param frame l'objet {@link JFrame} servant de base à la construction de la fenetre
	 */
	public FrameCreator(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * Retourne la fenetre construite avec l'objet
	 * 
	 * @return the frame la fentre construite sur la base du XML
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * définit la fenetre sur laquel effectué la construction XML
	 * @param frame
	 *            the frame to set
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * Dans le fichier XML, le titre peut contenir des chaines à formater de type %s ou %d.<br>
	 * Mettre en parametre de cette fonction l'ensemble des chaines et/ou objet de remplacement
	 * 
	 * @param titleReplacement les éléments à remplacer dans le titre
	 */
	public void formatTitle(Object... titleReplacement) {
		this.titleReplacement = titleReplacement;
	}

	/**
	 * Définit l'objet de localisation qui doit être employé pour traduire les chaines à localisé
	 * du fichier XML
	 * 
	 * @param ajResourcesReaderLibelle l'objet de localisation qui doit être employé
	 */
	public void setL10N(AjResourcesReader ajResourcesReaderLibelle) {
		this.ajResourcesReaderLibelle = ajResourcesReaderLibelle;
	}

	/**
	 * Ajoute un composant nommé à la liste des composants nommé
	 * 
	 * @param component le composant à ajouter
	 * @param name le nom du composant à ajouter
	 */
	public void addNamedComponent(JComponent component, String name) {
		namedComponents.put(name, component);
	}

	/**
	 * Retourne le composant représenté par son nom
	 * 
	 * @param name le nom du composant à retourner
	 * 
	 * @return le composant à retourner
	 */
	public JComponent getNamedComponent(String name) {
		return namedComponents.get(name);
	}

	/**
	 * 
	 * @param actionListener
	 */
	public void addActionListener(ActionListener actionListener) {
		listeners.add(ActionListener.class, actionListener);
	}

	/**
	 * Construit la fenetre à partir du fichier XML fournit en parametre
	 *  
	 * @param interfacexml le fichier XML de déscription de la fenetre
	 */
	public void createFrame(File interfacexml) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = documentBuilder.parse(interfacexml);

			Element rootElement = doc.getDocumentElement();
			// l'element racine d'une fenetre est "screen" qui represente un
			// "ecran".
			// si ce n'est pas le cas on effectue aucun traitement
			if (rootElement.getTagName().equals("screen")) { //$NON-NLS-1$
				NodeList nodes = rootElement.getChildNodes();

				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);

					if (node.getNodeName().equals("frame")) { //$NON-NLS-1$
						parseFrame(node);
					} else if (node.getNodeName().equals("window")) { //$NON-NLS-1$
						// TODO à supporter
					}
				}
			}
		} catch (SAXException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param node
	 */
	private void parseFrame(Node node) {
		NamedNodeMap namedNodeMap = node.getAttributes();

		Node width = namedNodeMap.getNamedItem("width"); //$NON-NLS-1$
		Node height = namedNodeMap.getNamedItem("height"); //$NON-NLS-1$
		Node minwidth = namedNodeMap.getNamedItem("minwidth"); //$NON-NLS-1$
		Node minheight = namedNodeMap.getNamedItem("minheight"); //$NON-NLS-1$
		Node extendedstate = namedNodeMap.getNamedItem("extendedstate"); //$NON-NLS-1$

		frame.setSize(Integer.parseInt(width.getNodeValue()), Integer.parseInt(height.getNodeValue()));
		if(minwidth != null && minheight != null)
			frame.setMinimumSize(new Dimension(Integer.parseInt(minwidth.getNodeValue()), Integer.parseInt(minheight.getNodeValue())));

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("title")) { //$NON-NLS-1$
				parseTitle(childNode);
			} else if (childNode.getNodeName().equals("icon")) { //$NON-NLS-1$
				parseIcon(childNode);
			} else if (childNode.getNodeName().equals("menubar")) { //$NON-NLS-1$
				parseMenubar(childNode);
			} else if (childNode.getNodeName().equals("contentpane")) { //$NON-NLS-1$
				parseContentPane(childNode);
			}
		}

		frame.setVisible(true);

		try {
			Field field = frame.getClass().getField(extendedstate.getNodeValue());
			int state = field.getInt(frame);

			frame.setExtendedState(state);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param node
	 */
	private void parseTitle(Node node) {
		String brutTitle = node.getTextContent();
		frame.setTitle(String.format(brutTitle, titleReplacement));
	}

	/**
	 * 
	 * @param node
	 */
	private void parseIcon(Node node) {
		NamedNodeMap namedNodeMap = node.getAttributes();

		Node src = namedNodeMap.getNamedItem("src"); //$NON-NLS-1$
		frame.setIconImage(new ImageIcon(src.getNodeValue()).getImage());
	}

	/**
	 * 
	 * @param node
	 */
	private void parseMenubar(Node node) {
		JMenuBar mb = new JMenuBar();

		frame.setJMenuBar(mb);

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("menu")) { //$NON-NLS-1$
				mb.add(parseMenu(childNode));
			}
		}
	}

	/**
	 * 
	 * @param node
	 * @param mb
	 */
	private JMenu parseMenu(Node node) {
		JMenu menu = new JMenu();

		NamedNodeMap namedNodeMap = node.getAttributes();

		Node visible = namedNodeMap.getNamedItem("visible"); //$NON-NLS-1$
		if (visible != null) {
			menu.setVisible(Boolean.parseBoolean(visible.getNodeValue()));
		}

		Node id = namedNodeMap.getNamedItem("id"); //$NON-NLS-1$
		menu.setName(id.getNodeValue());

		Node name = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
		if (name != null) {
			namedComponents.put(name.getNodeValue(), menu);
		}
		Node enable = namedNodeMap.getNamedItem("enable"); //$NON-NLS-1$
		if (enable != null)
			menu.setEnabled(Boolean.parseBoolean(enable.getNodeValue()));

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("label")) { //$NON-NLS-1$
				parseMenuLabel(childNode, menu);
			} else if (childNode.getNodeName().equals("menuitems")) { //$NON-NLS-1$
				parseMenuItems(childNode, menu);
			}
		}

		return menu;
	}

	/**
	 * 
	 * @param node
	 * @param menu
	 */
	private void parseMenuLabel(Node node, JMenu menu) {
		menu.setText(ajResourcesReaderLibelle.getResourceString(node.getTextContent()));
	}

	/**
	 * 
	 * @param node
	 * @param menu
	 */
	private void parseMenuItems(Node node, JMenu menu) {
		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("menuitem")) { //$NON-NLS-1$
				parseMenuItem(childNode, menu);
			} else if (childNode.getNodeName().equals("menu")) { //$NON-NLS-1$
				menu.add(parseMenu(childNode));
			} else if (childNode.getNodeName().equals("separator")) { //$NON-NLS-1$
				menu.addSeparator();
			}
		}

	}

	/**
	 * 
	 * @param node
	 * @param menu
	 */
	private void parseMenuItem(Node node, JMenu menu) {
		JMenuItem menuItem = new JMenuItem();

		NamedNodeMap namedNodeMap = node.getAttributes();

		Node name = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
		if (name != null)
			namedComponents.put(name.getNodeValue(), menuItem);
		Node enable = namedNodeMap.getNamedItem("enable"); //$NON-NLS-1$
		if (enable != null)
			menuItem.setEnabled(Boolean.parseBoolean(enable.getNodeValue()));

		Node keycode = namedNodeMap.getNamedItem("keycode"); //$NON-NLS-1$
		if (keycode != null) {
			Node modifier = namedNodeMap.getNamedItem("modifier"); //$NON-NLS-1$
			try {
				Field field = KeyEvent.class.getDeclaredField(keycode.getNodeValue());
				int key = field.getInt(null);
				int modif = 0;
				if (modifier != null) {
					field = InputEvent.class.getDeclaredField(modifier.getNodeValue());
					modif = field.getInt(null);
				}

				menuItem.setAccelerator(KeyStroke.getKeyStroke(key, modif));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		menuItem.setText(ajResourcesReaderLibelle.getResourceString(node.getTextContent()));
		menuItem.setActionCommand(node.getTextContent());
		for (ActionListener al : listeners.getListeners(ActionListener.class)) {
			menuItem.addActionListener(al);
		}

		menu.add(menuItem);
	}

	@SuppressWarnings("deprecation")
	private void parseContentPane(Node node) {
		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			ComponentParser componentParser = ComponentParserFactory.getComponentParser(childNode.getNodeName(), ajResourcesReaderLibelle);
			if (componentParser != null) {
				componentParser.parse(childNode);

				if (componentParser.getComponent() != null) {
					namedComponents.put(componentParser.getName(), componentParser.getComponent());
					namedComponents.putAll(componentParser.getNamedComponents());
					frame.getContentPane().add(componentParser.getComponent());
				} else {
					frame.getContentPane().add(namedComponents.get(componentParser.getName()));
				}
			}
		}
	}
}
