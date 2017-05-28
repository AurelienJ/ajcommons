/*
 * Créé le 14 mai 2010 à 21:13:00 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2010 - Aurélien JEOFFRAY
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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.ajdeveloppement.commons.Beta;
import org.ajdeveloppement.commons.ui.event.TagsDataEvent;
import org.ajdeveloppement.commons.ui.event.TagsDataListener;
import org.ajdeveloppement.commons.ui.event.TagsManagementListener;

/**
 * Special panel to display and manage a fog of tags. This panel can be use
 * to represent and manage tags associate with an element as a picture, document,
 * contact beans, etc.
 * 
 * @author Aurelien JEOFFRAY
 *
 * @param <T> tags type
 */
@Beta
public class TagsPanel<T> extends JPanel implements TagsDataListener {
	
	private TagsModel<T> model = new DefaultTagsModel<T>();
	private TagRenderer<T> renderer = new DefaultTagRenderer<T>();
	private TagEditor<T> editor = null;
	
	private EventListenerList listenerList = new EventListenerList();

	/**
	 * Construct a new TagsPanel
	 */
	public TagsPanel() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(Color.WHITE);
		
		model.addTagsDataListener(this);
	}
	
	@SuppressWarnings("unchecked")
	private void paintTags() {
		for (Component c : getComponents()) {
			if(c instanceof TagComponent)
				((TagComponent<T>)c).setParentTagsPanel(null);
		}
		removeAll();
		repaint();
		
		if(model != null) {
			for(int i = 0; i < model.getSize(); i++) {
				TagComponent<T> tagComponent = renderer.getTagRendererComponent(this, model.getElementAt(i), i, false, false);
				tagComponent.setParentTagsPanel(this);
				
				add(tagComponent);
			}
			
			if(editor != null) {
				TagComponent<T> tagComponent = editor.getTagEditorComponent(this, null, -1, false);
				tagComponent.setParentTagsPanel(this);
				add(tagComponent);
			}
		}
		
		revalidate();
	}
	
	public void addTagsManagementListener(TagsManagementListener<T> l) {
		listenerList.add(TagsManagementListener.class, l);
	}
	
	public void removeTagsManagementListener(TagsManagementListener<T> l) {
		listenerList.remove(TagsManagementListener.class, l);
	}

	/**
	 * Return current model associate to TagsPane
	 * 
	 * @return the model
	 */
	public TagsModel<T> getModel() {
		return model;
	}

	/**
	 * Set model associate to tags panel
	 * 
	 * @param model the model to set
	 */
	public void setModel(TagsModel<T> model) {
		if(this.model != null)
			this.model.removeTagsDataListener(this);
		
		this.model = model;
		if(model != null)
			this.model.addTagsDataListener(this);
		
		paintTags();
	}

	/**
	 * Return the TagRenderer define to display tag elements of panel
	 * 
	 * @return the renderer
	 */
	public TagRenderer<T> getRenderer() {
		return renderer;
	}

	/**
	 * Define the TagRenderer to display tag elements of panel
	 * 
	 * @param renderer the renderer to set
	 */
	public void setRenderer(TagRenderer<T> renderer) {
		this.renderer = renderer;
		
		paintTags();
	}
	
	public TagEditor<T> getEditor() {
		return editor;
	}
	
	public void setEditor(TagEditor<T> editor) {
		this.editor = editor;
		
		paintTags();
	}

	@Override
	public void contentsChanged(TagsDataEvent e) {
		paintTags();
	}

	@Override
	public void tagsAdded(TagsDataEvent e) {
		paintTags();
	}

	@Override
	public void tagsRemoved(TagsDataEvent e) {
		paintTags();
	}
	
	@SuppressWarnings("unchecked")
	protected void fireRemovalRequired(int index) {
		if(model != null) {
			for(TagsManagementListener<T> l : listenerList.getListeners(TagsManagementListener.class)) {
				l.removalRequired(model.getElementAt(index));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void fireInsertingRequired(T insertingTag) {
		if(model != null) {
			for(TagsManagementListener<T> l : listenerList.getListeners(TagsManagementListener.class)) {
				l.insertingRequired(insertingTag);
			}
		}
	}
}
