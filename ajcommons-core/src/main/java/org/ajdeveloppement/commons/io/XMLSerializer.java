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
package org.ajdeveloppement.commons.io;

import java.beans.DefaultPersistenceDelegate;
import java.beans.ExceptionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

/**
 * Serialise/Désérialise un objet en structure XML en exploitant soit
 * java.beans.XMLDecoder/java.beans.XMLEncoder,
 * 	soit JAXBContext Marshall/Unmarshall.
 * 
 * @author Aurélien JEOFFRAY
 * @version 1.0
 */
public class XMLSerializer {
	/**
	 * <p>Charge un objet serialisé sous la forme d'une structure XML compressé ou non</p>
	 * <p>Utilise la methode <i>XMLDecoder</i> pour la deserialisation</p>
	 * 
	 * @param path -
	 *            le chemin du fichier XML de la structure de class
	 * @param gzip true si la structure est compressé au format gzip, false sinon
	 * 
	 * @return l'objet recomposé résultant de la déserialisation ou null si non trouvé
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadXMLStructure(File path, boolean gzip)
			throws IOException {

		java.beans.XMLDecoder d = null;
		T structure = null;

		try {
			if (path.exists()) {
				InputStream is;
				FileInputStream fis = new FileInputStream(path);

				if (gzip) {
					is = new GZIPInputStream(fis);
				} else {
					is = fis;
				}

				d = new java.beans.XMLDecoder(new BufferedInputStream(is), null, null, XMLSerializer.class.getClassLoader());
				d.setExceptionListener(new ExceptionListener() {
				    @Override
					public void exceptionThrown(Exception ex) {
				        ex.printStackTrace();
				    }
				});
				structure = (T)d.readObject();
			} else {
				System.err.println("Erreur de chargement: " + path.getAbsolutePath()); //$NON-NLS-1$
			}
		} catch (IOException io) {
			throw io;
		} finally {
			try {
				if (d != null)
					d.close();
			} catch (Exception exc) {
			}
		}

		return structure;
	}

	/**
	 * Serialise sous la forme d'une structure XML compressé ou non
	 * 
	 * @param path le chemin du fichier XML de la structure de class
	 * @param structure la structure à serialise
	 * @param gzip true si la structure doit être compressé au format gzip, false sinon
	 * @throws IOException 
	 */
	public static void saveXMLStructure(File path, Object structure, boolean gzip) 
			throws IOException {
		java.beans.XMLEncoder e = null;

		try {
			FileOutputStream fos = new FileOutputStream(path);

			OutputStream os = null;

			if (gzip) {
				os = new GZIPOutputStream(fos);
			} else {
				os = fos;
			}

			e = new java.beans.XMLEncoder(new BufferedOutputStream(os));
			e.setExceptionListener(new ExceptionListener() {
			    @Override
				public void exceptionThrown(Exception ex) {
			        ex.printStackTrace();
			    }
			});
			e.setPersistenceDelegate(UUID.class, new DefaultPersistenceDelegate(new String[]{"mostSignificantBits", "leastSignificantBits"})); //$NON-NLS-1$ //$NON-NLS-2$
			e.writeObject(structure);
		} catch (IOException io) {
			throw io;
		} finally {
			try {
				if (e != null)
					e.close();
			} catch (Exception exc) {
			}
		}
	}
	
	/**
	 * Désérialise un objet sauvegarder dans une structure XML
	 * 
	 * @param <T> le type objet correspondant au fichier XML sérialisé
	 * @param path le chemin de la structure XML à charger
	 * @param destClass la class de l'objet serialisé
	 * @return l'objet désérialisé
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static <T> T loadMarshallStructure(File path, Class<T> destClass)
			throws JAXBException, IOException {
		return loadMarshallStructure(path, destClass, false);
	}
	
	/**
	 * Désérialise un objet sauvegarder dans une structure XML
	 * 
	 * @param <T> le type objet correspondant au fichier XML sérialisé
	 * @param path le chemin de la structure XML à charger
	 * @param destClass la class de l'objet serialisé
	 * @param gzip true si la structure est compressé au format gzip, false sinon
	 * @return l'objet désérialisé
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static <T> T loadMarshallStructure(File path, Class<T> destClass, boolean gzip)
			throws JAXBException, IOException {
		return loadMarshallStructure(new FileInputStream(path), destClass, gzip, true);
	}

	/**
	 * Désérialise un objet sauvegarder dans une structure XML
	 * 
	 * @param <T> le type objet correspondant au fichier XML sérialisé
	 * @param url le chemin de la structure XML à charger
	 * @param destClass la class de l'objet serialisé
	 * @return l'objet désérialisé
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static <T> T loadMarshallStructure(URL url, Class<T> destClass) 
			throws JAXBException, IOException {
		return loadMarshallStructure(url, destClass, false);
	}
	
	/**
	 * Désérialise un objet sauvegarder dans une structure XML
	 * 
	 * @param <T> le type objet correspondant au fichier XML sérialisé
	 * @param url le chemin de la structure XML à charger
	 * @param destClass la class de l'objet serialisé
	 * @param gzip true si la structure est compressé au format gzip, false sinon
	 * @return l'objet désérialisé
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static <T> T loadMarshallStructure(URL url, Class<T> destClass, boolean gzip) 
			throws JAXBException, IOException {
		return loadMarshallStructure(url.openStream(), destClass, gzip, true);
	}
	
	/**
	 * Désérialise un objet sauvegarder dans une structure XML
	 * 
	 * @param <T> le type objet correspondant au fichier XML sérialisé
	 * @param xmlStream le flux contenant la structure XML à charger
	 * @param destClass la class de l'objet serialisé
	 * @param gzip true si la structure est compressé au format gzip, false sinon
	 * @param closeStream indique si le flux doit être ou non fermé à la fin de la sérialisation
	 * @return l'objet désérialisé
	 * @throws JAXBException
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadMarshallStructure(InputStream xmlStream, Class<T> destClass, boolean gzip, boolean closeStream) 
			throws JAXBException, IOException {
		T structure = null;

		// on crée un contexte JAXB
		JAXBContext context = JAXBContext.newInstance(destClass);

		// on crée un Unmarshaller à partir du contexte
		Unmarshaller um = context.createUnmarshaller();
		
		InputStream is = null;
		try {
			if(gzip)
				is = new GZIPInputStream(xmlStream);
			else
				is = xmlStream;
	
			// on demande au marshaller de générer la structure
			if(!destClass.isAnnotationPresent(XmlRootElement.class)) {
				JAXBElement<T> root = um.unmarshal(new StreamSource(is), destClass);
				structure = root.getValue();
			} else {
				structure = (T)um.unmarshal(is);
			}
		} finally {
			if(closeStream && is != null)
				is.close();
		}

		return structure;
	}

	/**
	 * Sérialise un objet en structure XML
	 * 
	 * @param path le chemin du fichier XML de destination
	 * @param structure la structure à sérialiser
	 * 
	 * @throws JAXBException
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public static void saveMarshallStructure(File path, Object structure) 
			throws JAXBException, IOException {
		saveMarshallStructure(path, structure, false);
	}
	
	/**
	 * Sérialise un objet en structure XML éventuelement compressé en gnu-zip
	 * 
	 * @param path le chemin du fichier XML de destination
	 * @param structure la structure à sérialiser
	 * @param gzip true si la structure doit être compressé au format gzip, false sinon
	 * 
	 * @throws JAXBException
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public static void saveMarshallStructure(File path, Object structure, boolean gzip) 
			throws JAXBException, IOException {
		saveMarshallStructure(new FileOutputStream(path), structure, gzip, true);
	}
	
	/**
	 * Sérialise un objet en un flux XML.
	 * 
	 * @param xmlStream le flux dans lequel envoyer la serialisation
	 * @param structure la structure à sérialiser
	 * @param gzip true si la structure doit être compressé au format gzip, false sinon
	 * @param closeStream indique si le flux doit être ou non fermé à la fin de la sérialisation
	 * 
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public static <T> void saveMarshallStructure(OutputStream xmlStream, T structure, boolean gzip, boolean closeStream) 
			throws JAXBException, IOException {
		@SuppressWarnings("unchecked")
		Class<T> structureClass = (Class<T>)structure.getClass();
		
		// on crée un contexte JAXB
		JAXBContext context = JAXBContext.newInstance(structureClass);

		// on crée un marshaller à partir du contexte
		Marshaller m = context.createMarshaller();

		// on veut un affichage formatté
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		OutputStream os = null;
		try {
			if(gzip)
				os = new GZIPOutputStream(xmlStream);
			else
				os = xmlStream;
			
			// on demande au marshaller de générer le XML
			// m.marshal(structure, os);
			
			if(!structureClass.isAnnotationPresent(XmlRootElement.class)) {
				QName qName = new QName(structureClass.getPackage().getName(), structureClass.getName());
				JAXBElement<T> root = new JAXBElement<T>(qName, structureClass, structure);
		        
		        m.marshal(root, os);
			} else {
				m.marshal(structure, os);
			}
		} finally {
			if(closeStream && os != null)
				os.close();
		}
	}
	
	/**
	 * Sérialise un objet en structure XML et retourne la chaîne XML produite
	 * 
	 * @param structure la structure à sérialisé
	 * @return la chaîne XML sérialisé
	 * 
	 * @throws JAXBException if an error was encountered while creating the JAXBContext
	 */
	public static <T> String createMarshallStructure(T structure) throws JAXBException {
		
		StringWriter writer = new StringWriter();
		
		@SuppressWarnings("unchecked")
		Class<T> structureClass = (Class<T>)structure.getClass();
		
		// on crée un contexte JAXB
		JAXBContext context = JAXBContext.newInstance(structureClass);
		
		// on crée un marshaller à partir du contexte
		Marshaller m = context.createMarshaller();

		// on veut un affichage formatté
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		if(!structureClass.isAnnotationPresent(XmlRootElement.class)) {
			QName qName = new QName(structureClass.getPackage().getName(), structureClass.getName());
			JAXBElement<T> root = new JAXBElement<T>(qName, structureClass, structure);
	        
	        m.marshal(root, writer);
		} else {
			m.marshal(structure, writer);
		}
		
		return writer.toString();
	}
}
