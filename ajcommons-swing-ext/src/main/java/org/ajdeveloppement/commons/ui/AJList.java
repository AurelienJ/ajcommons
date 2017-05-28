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
package org.ajdeveloppement.commons.ui;

import java.applet.Applet;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.ajdeveloppement.commons.Beta;

/**
 * Liste avec gestion unitaires en ajout/suppression
 * des éléments des élements
 * 
 * @author Aurélien JEOFFRAY
 * @author Santhosh Kumar T - santhosh@in.fiorano.com 
 * 
 * @param <T> type des éléments de la liste
 */
@Beta
public class AJList<T> extends JList<T> implements CellEditorListener {
	
	protected Component editorComp = null; 
    protected int editingIndex = -1; 
    protected ListCellEditor editor = null; 
    private PropertyChangeListener editorRemover = null; 

	private List<T> lstData= new ArrayList<T>();
	
	private boolean selectable = true;
    
	/**
	 * Initialise une nouvelle liste
	 */
    public AJList() {
        super();
        init();
    }
    
    /**
     * Initialise la liste avec le tableau de données fournit en parametre
     * 
     * @param listData tableau de valeurs initialisant la liste
     */
    public AJList(T[] listData) {
        super(listData);
        for(int i = 0; i < listData.length; i++) {
            lstData.add(listData[i]);
        }
        init();
    }
    
    /**
     * Initialise la liste avec une liste fournit en parametre (Wrapping)
     * 
     * @param listData la liste à wrapper
     */
    @SuppressWarnings("unchecked")
	public AJList(List<T> listData) {
        super((T[])listData.toArray());
        lstData = listData;
        init();
    }
    
    @SuppressWarnings("nls")
	private void init() {
		getActionMap().put("startEditing", new StartEditingAction());
		getActionMap().put("cancel", new CancelEditingAction());
		addMouseListener(new MouseListener());
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"startEditing");
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
	
    @Override
	public void setListData(Vector<? extends T> listData) {
        lstData = new ArrayList<T>();
        for(int i = 0; i < listData.size(); i++) {
        	lstData.add(listData.get(i));
        }
		super.setListData(listData);
	}
    
    @Override
	public void setListData(T[] listData) {
		lstData = new ArrayList<T>();
		for(int i = 0; i < listData.length; i++) {
			lstData.add(listData[i]);
		}
		super.setListData(listData);
	}
	
    /**
     * Ajoute un élément à la liste
     * 
     * @param obj l'élément à ajouter à la liste 
     */
	@SuppressWarnings("unchecked")
	public void add(T obj) {
		lstData.add(obj);
		super.setListData((T[])lstData.toArray());
	}
	
	/**
	 * Retourne l'ensemble des éléments de la liste
	 * 
	 * @return la collection des éléments de la liste
	 */
	public List<T> getAllElements() {
		return lstData;
	}
	
	/**
	 * Retourne l'index de l'élément fournit en paramétre
	 * 
	 * @param obj l'élément pour lequel retrouver la position
	 * 
	 * @return la position de l'élément concerné
	 */
	public int getIndex(T obj) {
		return lstData.indexOf(obj);
	}
    
	/**
	 * Retourne l'élément se trouvant à l'index fournit en paramètre
	 * 
	 * @param index l'index de l'élément à retourner
	 * @return l'élément retourné
	 */
    public T getValueAt(int index) {
        return lstData.get(index);
    }
	
    /**
     * Vérifie si l'élément fournit en paramétre existe ou non dans
     * la liste
     * 
     * @param obj l'élément à tester
     * @return <code>true</code> si l'élément existe dans la liste, <code>false</code> sinon
     */
	public boolean contains(T obj) {
		return lstData.contains(obj);
	}
	
	/**
	 * Retire l'élément fournit en paramétre de la liste
	 * 
	 * @param obj l'élément à supprimer
	 */
	@SuppressWarnings("unchecked")
	public void remove(T obj) {
		lstData.remove(obj);
		
		super.setListData((T[])lstData.toArray());
	}
	
	/**
	 * Retire l'élément représenté par son index de la liste
	 * 
	 * @param index l'index de l'élément ) retirer de la liste
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void remove(int index) {
		lstData.remove(index);
		
		super.setListData((T[])lstData.toArray());
	}
	
	/**
	 * Supprime l'ensemble des éléments de la liste
	 */
	@SuppressWarnings("unchecked")
	public void clear() {
		lstData.clear();
		
		super.setListData((T[])lstData.toArray());
	}
    
	/**
	 * Retourne le nombre d'élément stocké dans la liste
	 * 
	 * @return le nombre d'éléments de la liste
	 */
    public int getNumElements() {
        return lstData.size();
    }
    
    /**
     * @see javax.swing.JList#setSelectedIndices(int[])
     */
    @Override
    public void setSelectedIndices(int[] indices) {
    	if(selectable) {
    		super.setSelectedIndices(indices);
    	}
    }
    
    /**
     * @see javax.swing.JList#setSelectedValue(java.lang.Object, boolean)
     */
    @Override
    public void setSelectedValue(Object anObject, boolean shouldScroll) {
    	if(selectable) {
    		super.setSelectedValue(anObject, shouldScroll);
    	}
    }
    
    /**
     * @see javax.swing.JList#setSelectionInterval(int, int)
     */
    @Override
    public void setSelectionInterval(int anchor, int lead) {
    	if(selectable) {
    		super.setSelectionInterval(anchor, lead);
    	}
    }
    
    /**
     * @see javax.swing.JList#setSelectedIndex(int)
     */
    @Override
    public void setSelectedIndex(int index) {
    	if(selectable) {
    		super.setSelectedIndex(index);
    	}
    }

	/**
	 * Détermine si les éléments de la liste sont séléctionnable ou non
	 * 
	 * @return  true si séléctionnable, false sinon
	 */
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * Définit si les éléments de la liste sont séléctionnable ou non
	 * 
	 * @param selectable true si séléctionnable, false sinon
	 */
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	/**
	 * parametrage du composant d'édition graphique de l'élément de la liste
	 * 
	 * @param editor le composant d'édition graphique de l'élément de la liste
	 */
	public void setListCellEditor(ListCellEditor editor) {
		this.editor = editor;
	}

	/**
	 * Retourne le composant d'édition graphique de l'élément de la liste
	 * @return le composant d'édition graphique de l'élément de la liste
	 * 	ou <code>null</code> si inexistant
	 */
	public ListCellEditor getListCellEditor() {
		return editor;
	}

	/**
	 * Indique si la liste est en cours d'édition
	 * 
	 * @return <code>true</code> si la liste est en cours d'édition.
	 */
	public boolean isEditing() {
		return (editorComp == null) ? false : true;
	}

	/**
	 * Retourne le composant d'édition
	 * 
	 * @return le composant d'édition
	 */
	public Component getEditorComponent() {
		return editorComp;
	}

	/**
	 * Retourne l'index de l'élément en cours d'édition
	 * 
	 * @return l'index de l'élément en cours d'édition
	 */
	public int getEditingIndex() {
		return editingIndex;
	}

	/**
	 * Prépare le composant d'édition pour l'élément de la liste indiqué par son index
	 * 
	 * @param index l'index de l'élément pour lequel préparer le composant d'édition
	 * 
	 * @return le composant d'édition préparé
	 */
	public Component prepareEditor(int index) {
		Object value = getModel().getElementAt(index);
		boolean isSelected = isSelectedIndex(index);
		Component comp = editor.getListCellEditorComponent(this, value,
				isSelected, index);
		/*if (comp instanceof JComponent) {
			JComponent jComp = (JComponent) comp;
			if (jComp.getNextFocusableComponent() == null) {
				jComp.setNextFocusableComponent(this);
			}
		}*/
		return comp;
	}

	/**
	 * Stop l'édition de l'élément courant
	 */
	@SuppressWarnings("nls")
	public void removeEditor() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner",
						editorRemover);
		editorRemover = null;

		if (editor != null) {
			editor.removeCellEditorListener(this);

			if (editorComp != null) {
				remove(editorComp);
			}

			Rectangle cellRect = getCellBounds(editingIndex, editingIndex);

			editingIndex = -1;
			editorComp = null;

			repaint(cellRect);
		}
	}

	/**
	 * Edite l'élement à la position fournit
	 * 
	 * @param index l'index de l'élément à éditer
	 * @param e l'evenement lié à la demande d'édition
	 * @return <code>true> si la demande d'édition est validé, <code>false</code> si l'élément
	 * n'est pas éditable
	 */
	@SuppressWarnings("nls")
	public boolean editCellAt(int index, EventObject e) {
		if (editor != null && !editor.stopCellEditing())
			return false;

		if (index < 0 || index >= getModel().getSize())
			return false;

		if (!isCellEditable(index))
			return false;

		if (editorRemover == null) {
			KeyboardFocusManager fm = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			editorRemover = new CellEditorRemover(fm);
			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover); // NOI18N
		}

		if (editor != null && editor.isCellEditable(e)) {
			editorComp = prepareEditor(index);
			if (editorComp == null) {
				removeEditor();
				return false;
			}
			editorComp.setBounds(getCellBounds(index, index));
			add(editorComp);
			editorComp.validate();

			editingIndex = index;
			editor.addCellEditorListener(this);

			return true;
		}
		return false;
	}

	@SuppressWarnings("nls")
	@Override
	public void removeNotify() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner", editorRemover);
		super.removeNotify();
	}

	// This class tracks changes in the keyboard focus state. It is used
	// when the XList is editing to determine when to cancel the edit.
	// If focus switches to a component outside of the XList, but in the
	// same window, this will cancel editing.
	class CellEditorRemover implements PropertyChangeListener {
		KeyboardFocusManager focusManager;

		public CellEditorRemover(KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

		@Override
		@SuppressWarnings("nls")
		public void propertyChange(PropertyChangeEvent ev) {
			if (!isEditing()
					|| getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {
				return;
			}

			Component c = focusManager.getPermanentFocusOwner();
			while (c != null) {
				if (c == AJList.this) {
					// focus remains inside the table
					return;
				} else if ((c instanceof Window)
						|| (c instanceof Applet && c.getParent() == null)) {
					if (c == SwingUtilities.getRoot(AJList.this)) {
						if (!getListCellEditor().stopCellEditing()) {
							getListCellEditor().cancelCellEditing();
						}
					}
					break;
				}
				c = c.getParent();
			}
		}
	}

	/*-------------------------------------------------[ Model Support ]---------------------------------------------------*/

	/**
	 * Indique si l'élément représenté par son index est éditable
	 * 
	 * @param index l'index de l'élément à controler
	 * @return <code>true</code>si l'élement est éditable, <code>false</code> sinon
	 */
	public boolean isCellEditable(int index) {
		if (getModel() instanceof MutableListModel)
			return ((MutableListModel<T>) getModel()).isCellEditable(index);
		return false;
	}

	/**
	 * Modifie la valeur de l'élement représenté par son index
	 * @param value la nouvelle valeur de l'élément
	 * @param index l'index de l'élément à modiifier
	 */
	public void setValueAt(T value, int index) {
		((MutableListModel<T>) getModel()).setValueAt(value, index);
	}

	/*-------------------------------------------------[ CellEditorListener ]---------------------------------------------------*/

	@Override
	public void editingStopped(ChangeEvent e) {
		if (editor != null) {
			@SuppressWarnings("unchecked")
			T value = (T)editor.getCellEditorValue();
			setValueAt(value, editingIndex);
			removeEditor();
		}
	}

	@Override
	public void editingCanceled(ChangeEvent e) {
		removeEditor();
	}

	/*-------------------------------------------------[ Editing Actions]---------------------------------------------------*/

	private static class StartEditingAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			AJList<?> list = (AJList<?>) e.getSource();
			if (!list.hasFocus()) {
				CellEditor cellEditor = list.getListCellEditor();
				if (cellEditor != null && !cellEditor.stopCellEditing()) {
					return;
				}
				list.requestFocus();
				return;
			}
			ListSelectionModel rsm = list.getSelectionModel();
			int anchorRow = rsm.getAnchorSelectionIndex();
			list.editCellAt(anchorRow, null);
			Component editorComp = list.getEditorComponent();
			if (editorComp != null) {
				editorComp.requestFocus();
			}
		}
	}

	private class CancelEditingAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			AJList<?> list = (AJList<?>) e.getSource();
			list.removeEditor();
		}

		@Override
		public boolean isEnabled() {
			return isEditing();
		}
	}

	private class MouseListener extends MouseAdapter {
		private Component dispatchComponent;

		private void setDispatchComponent(MouseEvent e) {
			Component editorComponent = getEditorComponent();
			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(AJList.this, p,
					editorComponent);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(
					editorComponent, p2.x, p2.y);
		}

		private boolean repostEvent(MouseEvent e) {
			// Check for isEditing() in case another event has
			// caused the editor to be removed. See bug #4306499.
			if (dispatchComponent == null || !isEditing()) {
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(AJList.this,
					e, dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			return true;
		}

		private boolean shouldIgnore(MouseEvent e) {
			return e.isConsumed()
					|| (!(SwingUtilities.isLeftMouseButton(e) && isEnabled()));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (shouldIgnore(e))
				return;
			Point p = e.getPoint();
			int index = locationToIndex(p);
			// The autoscroller can generate drag events outside the Table's
			// range.
			if (index == -1)
				return;

			if (editCellAt(index, e)) {
				setDispatchComponent(e);
				repostEvent(e);
			} else if (isRequestFocusEnabled())
				requestFocus();
		}
	} 
}