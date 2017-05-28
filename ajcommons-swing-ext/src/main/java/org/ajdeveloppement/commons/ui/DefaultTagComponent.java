/*
 * Créé le 23 mai 2010 à 17:07:45 pour AjCommons (Bibliothèque de composant communs)
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.ajdeveloppement.commons.Beta;

/**
 * @author aurelien
 * @param <T>
 */
@Beta
public class DefaultTagComponent<T> extends TagComponent<T> implements MouseListener, ActionListener{
	private int indexTag = -1;
	private String tagText = ""; //$NON-NLS-1$

	private JLabel jlTextTag = new JLabel();
	private JButton jbDelTag = new JButton();
	
	/**
	 * 
	 */
	public DefaultTagComponent() {
		init();
	}
	
	/**
	 * 
	 * @param text
	 * @param index
	 */
	public DefaultTagComponent(String text, int index) {
		init();
		
		indexTag = index;
		setText(text);
	}
	
	private void init() {
		addMouseListener(this);

		setOpaque(false);
		
		jbDelTag.setIcon(getRessourceImageIcon("remove.png")); //$NON-NLS-1$
		jbDelTag.setRolloverIcon(getRessourceImageIcon("remove_over.png")); //$NON-NLS-1$
		jbDelTag.setPressedIcon(getRessourceImageIcon("remove_active.png")); //$NON-NLS-1$
		jbDelTag.setDisabledIcon(getRessourceImageIcon("remove_disable.png")); //$NON-NLS-1$
		jbDelTag.setBorderPainted(false);
		jbDelTag.setFocusPainted(false);
		jbDelTag.setMargin(new Insets(0, 0, 0, 0));
		jbDelTag.setContentAreaFilled(false);
		jbDelTag.setOpaque(false);
		jbDelTag.setVisible(false);
		jbDelTag.addActionListener(this);

		jlTextTag.setFont(jlTextTag.getFont().deriveFont(Font.PLAIN));
		jlTextTag.setBorder(new EmptyBorder(new Insets(2, 0, 2, 0)));
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(jlTextTag);
		add(jbDelTag);
	}
	
	private ImageIcon getRessourceImageIcon(String fileName) {
		ImageIcon imageIcon = new ImageIcon(new ImageIcon(getClass().getResource(fileName)).getImage().getScaledInstance(12, 12,  java.awt.Image.SCALE_SMOOTH));
		
		return imageIcon;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D graphics2d = (Graphics2D)g;
		
		graphics2d.setColor(new Color(200,254,200));
		graphics2d.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
	}
	
	public String getText() {
		return tagText;
	}
	
	public void setText(String text) {
		this.tagText = text;
		
		jlTextTag.setText(tagText);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		jbDelTag.setVisible(true);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (! getVisibleRect().contains(e.getPoint()) ) {
			jbDelTag.setVisible(false);
	    }
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbDelTag) {
			if(parentTagsPanel != null)
				parentTagsPanel.fireRemovalRequired(indexTag);
		}
	}
}
