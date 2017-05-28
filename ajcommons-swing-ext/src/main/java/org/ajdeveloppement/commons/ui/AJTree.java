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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Arbre ayant la capacité de mémoriser l'état (étandu/rétracté) de chacun des noeuds de son
 * arborescence
 *
 * @author Aurélien JEOFFRAY
 * @version 0.1
 *
 */
public class AJTree extends JTree {

	private List<Object> extendedTreePath = new ArrayList<Object>();
	
	private boolean keepExpansionState = false;
	private boolean listenExpansion = true;
	/**
	 * 
	 */
	public AJTree() {
	}

	/**
	 * @param value
	 */
	public AJTree(Object[] value) {
		super(value);
	}

	/**
	 * @param value
	 */
	public AJTree(Vector<?> value) {
		super(value);
	}

	/**
	 * @param value
	 */
	public AJTree(Hashtable<?, ?> value) {
		super(value);
	}

	/**
	 * @param root
	 */
	public AJTree(TreeNode root) {
		super(root);
	}

	/**
	 * @param newModel
	 */
	public AJTree(TreeModel newModel) {
		super(newModel);
	}

	/**
	 * @param root
	 * @param asksAllowsChildren
	 */
	public AJTree(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
	}
	
	/**
	 * Retourne si l'arbre conserve ou non l'état de ses noeuds
	 * 
	 * @return the keepExpansionState true si il conserve l'état de ses noeuds, false sinon
	 */
	public boolean isKeepExpansionState() {
		return keepExpansionState;
	}

	/**
	 * Définit si l'état des noeuds doit être conservé ou non
	 * 
	 * @param keepExpansionState true, si l'on souhaite conserver l'état des noeuds
	 */
	public void setKeepExpansionState(boolean keepExpansionState) {
		this.keepExpansionState = keepExpansionState;
	}

	@Override
	protected void setExpandedState(TreePath path, boolean state) {
		if(path == null)
			return;
		
		super.setExpandedState(path, state);
		if(keepExpansionState && listenExpansion) {
			if(state) {
				if(listenExpansion && !extendedTreePath.contains(path)) {
					Object extandedObj;
					if(path.getLastPathComponent() instanceof DefaultMutableTreeNode)
						extandedObj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
					else
						extandedObj = path;
					extendedTreePath.add(extandedObj);
				}
			} else {
				Object extandedObj;
				if(path.getLastPathComponent() instanceof DefaultMutableTreeNode)
					extandedObj = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
				else
					extandedObj = path;
				extendedTreePath.remove(extandedObj);
			}
		}
	}
	
	@Override
	protected TreeModelListener createTreeModelListener() {
		 return new TreeModelHandler();
	}

	protected class TreeModelHandler extends JTree.TreeModelHandler {
		@Override
		public void treeStructureChanged(TreeModelEvent e) {
			super.treeStructureChanged(e);
			if(keepExpansionState) {
				listenExpansion = false;
				for(Object treePath : extendedTreePath) {
					if(treePath instanceof TreePath) {
						expandPath((TreePath)treePath);
					} else {
						expandMutableTreeNode(treePath, null);
					}
				}
				listenExpansion = true;
			}
		}
		
		private void expandMutableTreeNode(Object node, DefaultMutableTreeNode treeNode) {
			if(treeNode == null)
				treeNode = (DefaultMutableTreeNode)getModel().getRoot();
			if(treeNode.getUserObject() == node) {
				expandPath(new TreePath(treeNode.getPath()));
			} else if(!treeNode.isLeaf()) {
				Enumeration<?> children = treeNode.children(); 
				while(children.hasMoreElements()) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
					expandMutableTreeNode(node, child);
				}
				
			}
		}
	}
}
