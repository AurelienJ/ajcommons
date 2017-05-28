/*
 * Créé le 1 janv. 2011 à 23:37:09 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2011 - Aurélien JEOFFRAY
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * Textfield permettant la suggestion de valeur
 * 
 * @author Aurelien JEOFFRAY
 *
 * @param <T> le type des éléments suggeré
 */
public class AJSuggestTextField<T> extends JTextField implements DocumentListener, HierarchyListener {
	private JDialog suggestDialog;
	private JList<T> jlSuggestion;

	private SuggestModel<T> suggestModel = null;
	
	private boolean initMode = false; 

	/**
	 * Construct a new suggest textfield
	 */
	public AJSuggestTextField() {
		this(null, null, 0);
	}


	/**
	 * Construct a new suggest textfield
	 * 
	 * @param doc le document associé au textfield
	 * @param text le texte initial du textfield
	 * @param columns le nombre de caractères du composant
	 */
	public AJSuggestTextField(Document doc, String text, int columns) {
		super(doc, text, columns);

		addHierarchyListener(this);
	}


	/**
	 * Construct a new suggest textfield
	 * 
	 * @param columns le nombre de caractères du composant
	 */
	public AJSuggestTextField(int columns) {
		this(null, null, columns);
	}


	/**
	 * Construct a new suggest textfield
	 * 
	 * @param text le texte initial du textfield
	 * @param columns le nombre de caractères du composant
	 */
	public AJSuggestTextField(String text, int columns) {
		this(null, text, columns);
	}


	/**
	 * Construct a new suggest textfield
	 * 
	 * @param text le texte initial du textfield
	 */
	public AJSuggestTextField(String text) {
		super(text);
	}

	@Override
	public void setDocument(Document doc) {
		if(getDocument() != null)
			getDocument().removeDocumentListener(this);
		super.setDocument(doc);
		doc.addDocumentListener(this);
	}

	/**
	 * Définit le modele de suggestion associé au textfield
	 * 
	 * @param model le modele de suggestion associé au textfield
	 */
	public void setModel(SuggestModel<T> model) {
		suggestModel = model;
		if(jlSuggestion != null) {
			jlSuggestion.setModel(model);
		}
	}

	/**
	 * Retourne le modele de suggestion associé au textfield
	 * 
	 * @return le modele de suggestion associé au textfield
	 */
	public SuggestModel<T> getModel() {
		return suggestModel;
	}

	/**
	 * 
	 */
	public void beginInit() {
		initMode = true;
	}

	/**
	 * 
	 */
	public void endInit() {
		initMode = false;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if(!initMode)
			updateSuggestList();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if(!initMode)
			updateSuggestList();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if(!initMode)
			updateSuggestList();
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		initSuggestList();
	}

	/**
	 * Set preferred size for the drop-down that will appear.
	 *
	 * @param size
	 *            Preferred size of the drop-down list
	 */
	public void setPreferredSuggestSize(Dimension size) {
		suggestDialog.setPreferredSize(size);
	}

	/**
	 * Set minimum size for the drop-down that will appear.
	 *
	 * @param size
	 *            Minimum size of the drop-down list
	 */
	public void setMinimumSuggestSize(Dimension size) {
		suggestDialog.setMinimumSize(size);
	}

	/**
	 * Set maximum size for the drop-down that will appear.
	 *
	 * @param size
	 *            Maximum size of the drop-down list
	 */
	public void setMaximumSuggestSize(Dimension size) {
		suggestDialog.setMaximumSize(size);
	}

	/**
	 *
	 */
	private void relocate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(suggestDialog != null && isShowing()) {
					Point textFieldPosition = getLocationOnScreen();
					textFieldPosition.y += getHeight();
					suggestDialog.setLocation(textFieldPosition);
				}
			}
		});
	}

	private void initSuggestList() {
		Window parentWindow = SwingUtilities.getWindowAncestor(this);

		if(parentWindow != null) {
			if(suggestDialog == null) {
				suggestDialog = new JDialog(parentWindow);
				suggestDialog.setUndecorated(true);
				suggestDialog.setFocusableWindowState(false);
				//suggestDialog.setFocusable(false);

				jlSuggestion = new JList<T>();
				jlSuggestion.setModel(suggestModel);
				jlSuggestion.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(e.getClickCount() == 2) {
							setText(jlSuggestion.getSelectedValue().toString());
							suggestDialog.setVisible(false);
						}

					}
				});

				suggestDialog.setLayout(new BorderLayout());
				suggestDialog.add(new JScrollPane(jlSuggestion), BorderLayout.CENTER);
				suggestDialog.pack();

				parentWindow.addComponentListener(new ComponentListener() {
					@Override
					public void componentShown(ComponentEvent e) {
						relocate();
					}

					@Override
					public void componentResized(ComponentEvent e) {
						relocate();
					}

					@Override
					public void componentMoved(ComponentEvent e) {
						relocate();
					}

					@Override
					public void componentHidden(ComponentEvent e) {
						relocate();
					}
				});

				addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent e) {
						if(suggestDialog != null)
							suggestDialog.setVisible(false);
					}

					@Override
					public void focusGained(FocusEvent e) {
					}
				});

				addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased (KeyEvent e) {
						if(e.getKeyCode() == KeyEvent.VK_ENTER && jlSuggestion.getSelectedValue() != null) {
							setText(jlSuggestion.getSelectedValue().toString());
							suggestDialog.setVisible(false);
						} else if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
							jlSuggestion.setSelectedIndex(jlSuggestion.getSelectedIndex() + (e.getKeyCode() == KeyEvent.VK_DOWN ? 1 : -1));
							jlSuggestion.ensureIndexIsVisible(jlSuggestion.getSelectedIndex());
						} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
							suggestDialog.setVisible(false);
						} else if(e.getKeyCode() == KeyEvent.VK_SPACE && e.getModifiers() == KeyEvent.CTRL_MASK) {
							if(!suggestDialog.isVisible()) {
								updateSuggestList();
								suggestDialog.setVisible(true);
							}
						}
					};
				});
			}
		}
	}

	private void updateSuggestList() {
		if(suggestModel != null) {
			suggestModel.setSearchSuggestPattern(getText());

		if(suggestDialog != null)
			if((suggestModel.getSize() > 1 && !getText().isEmpty())
					|| (suggestModel.getSize() == 1 && !getText().equalsIgnoreCase(suggestModel.getElementAt(0).toString()))) {
				suggestDialog.setVisible(true);
				jlSuggestion.setSelectedIndex(0);
				relocate();
			} else {
				suggestDialog.setVisible(false);
			}
		}
	}

}
