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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JWindow;

/**
 * Splash Screen à afficher au démmarrage d'une application
 * et affichant une image et un éventuel texte de progression
 * du chargement
 * 
 * @deprecated use {@link java.awt.SplashScreen} instead
 *  
 * @author Aurélien JEOFFRAY
 *
 */
@Deprecated
public class SplashScreen extends JWindow {
    
	private BufferedImage image;
    private String message = ""; //$NON-NLS-1$
    
    /**
     * Construit le splash screen à afficher
     * 
     * @param file le chemin de l'image à afficher
     * @param time le temps d'affichage de l'image en ms ou 0 si indéfini
     */
	public SplashScreen(String file, long time) {
		super();
		
		try {
            URL url = new URL("file:" + file); //$NON-NLS-1$
            
			image = ImageIO.read(url);
			setSize(new Dimension(image.getWidth(), image.getHeight()));
			setLocationRelativeTo(null);
			setAlwaysOnTop(false);
			setVisible(true);
		} catch(IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		if(time > 0) {
			TimerTask dispose = new TimerTask(){
                @Override
				public void run(){dispose();}	
			};
			Timer timer = new Timer();
			timer.schedule(dispose, time);
			try {
				Thread.sleep(time);
			} catch(Exception e){
                e.printStackTrace();
            }
		}
	}
	
	/**
	 * Construit le splash screen en spécifiant le chemin de l'image à afficher
	 * 
	 * @param file le chemin de l'image à afficher
	 */
	public SplashScreen(String file){
		this(file,0);	
	}
    
	/**
	 * Définit le texte de progression à afficher
	 * 
	 * @param text le texte de progression
	 */
    public void setProgressionText(String text) {
        message = text;
        repaint();
    }
    
    @Override
	public void paint(Graphics g) {
        update(g);
	}
    
    @Override
    public synchronized void update(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        //Si l'image à ue couche alpha, capture l'arriere plan pour l'afficher
        if(image.getColorModel().hasAlpha()) {
            try {
                Robot robot = new Robot();
                BufferedImage fond = robot.createScreenCapture(getBounds());
                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(fond,0);
                tracker.waitForAll();
                g2d.drawImage(fond, 0,0,null);
            } catch(Exception e){e.printStackTrace();}
        }
        g2d.drawImage(image,0,0,null);
        g2d.setFont(new Font(null, Font.BOLD, 15));
        g2d.drawString(message, image.getWidth() - 200, image.getHeight() - 50);
    }
}