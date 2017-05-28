/*
 * This source is on public domain.
 */
package org.ajdeveloppement.commons.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Affiche une image en semi transparence sur le
 * premier plan à la position fournit en parametre
 * 
 * Basé sur le travail de Romain Guy (http://gfx.developpez.com/tutoriel/java/swing/drag/)
 * @author Romain Guy
 * @author Aurélien JEOFFRAY
 *
 */
public class GhostGlassPane extends JPanel {
	private AlphaComposite composite;
	private BufferedImage dragged = null;
	private Point location = new Point(0, 0);

	/**
	 * Crée un nouveau panneau de premier plan en précisant le taux de transparence
	 * 
	 * @param alpha le taux de transparance du panneau de premier plan
	 */
	public GhostGlassPane(float alpha) {
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**
	 * L'image à afficher en transparence sur le panneau de premier plan
	 * 
	 * @param dragged l'image à afficher
	 */
	public void setImage(BufferedImage dragged) {
		this.dragged = dragged;
	}

	/**
	 * La position à partir de laquel afficher l'image en transparence
	 * 
	 * @param location la position de l'image à afficher
	 */
	public void setPoint(Point location) {
		this.location = location;
	}

	@Override
	public void paintComponent(Graphics g) {
		if (dragged == null)
			return;

		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(composite);
		g2.drawImage(dragged,
				(int) (location.getX() - (dragged.getHeight(this) / 2)),
				(int) (location.getY() - (dragged.getHeight(this) / 2)), null);
	}
}