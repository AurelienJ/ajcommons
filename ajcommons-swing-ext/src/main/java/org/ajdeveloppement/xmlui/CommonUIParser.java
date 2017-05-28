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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parser generic de structure XML. Interpréte les noeuds XML "method" afin d'invoquer par reflexion
 * les méthodes java correspondantes.
 * 
 * Exemple
 * <code><pre>
 * 	&lt;content class="javax.swing.JEditorPane" name="jepHome"&gt;
 * 		&lt;method name="setEditorKit"&gt;
 *			&lt;param paramclass="javax.swing.text.EditorKit" class="javax.swing.text.html.HTMLEditorKit" /&gt;
 * 		&lt;/method&gt;
 *		&lt;method name="setEditable"&gt;
 * 			&lt;param paramclass="boolean" value="false" /&gt;
 *		&lt;/method&gt;
 *	&lt;/content&gt;
 * </pre></code>
 * 
 * @deprecated Use JavaFX API instead
 * 
 * @author Aurélien JEOFFRAY
 *
 */
@Deprecated
public class CommonUIParser {
	
	protected static void parseMethod(Node node, Object methodObj) {
		NamedNodeMap namedNodeMap = node.getAttributes();
		
		Node methodName = namedNodeMap.getNamedItem("name"); //$NON-NLS-1$
		
		NodeList childNodes = node.getChildNodes();
		
		ArrayList<Object> params = new ArrayList<>();
		ArrayList<Class<?>> classParams = new ArrayList<>();
		for(int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			
			if(childNode.getNodeName().equals("param")) { //$NON-NLS-1$			
				Class<?> paramClass = parseParamClass(childNode);
				if(paramClass != null) {
					classParams.add(paramClass);
					params.add(parseParam(childNode, paramClass));
				}
			}
		}
		
		
		
		//place tous les parametres dans un tableau d'objet
		Object[] objectsParam = new Object[params.size()];
		objectsParam = params.toArray(objectsParam);
		
		//en extrait leurs class
		Class<?>[] objectsClass = new Class<?>[params.size()];
		objectsClass = classParams.toArray(objectsClass);
		
		try {
			Method method = methodObj.getClass().getMethod(methodName.getNodeValue(), objectsClass);
			method.invoke(methodObj, objectsParam);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {	
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	protected static Class<?> parseParamClass(Node node) {
		NamedNodeMap namedNodeMapParam = node.getAttributes();
		Node paramClassName = namedNodeMapParam.getNamedItem("paramclass"); //$NON-NLS-1$
		
		Class<?> paramClass = null;
		if(paramClassName != null) {
			try {
				if(paramClassName.getNodeValue().equals(boolean.class.getName()))
					paramClass = boolean.class;
				else if(paramClassName.getNodeValue().equals(short.class.getName()))
					paramClass = short.class;
				else if(paramClassName.getNodeValue().equals(int.class.getName()))
					paramClass = int.class;
				else if(paramClassName.getNodeValue().equals(long.class.getName()))
					paramClass = long.class;
				else if(paramClassName.getNodeValue().equals(float.class.getName()))
					paramClass = float.class;
				else if(paramClassName.getNodeValue().equals(double.class.getName()))
					paramClass = double.class;
				else if(paramClassName.getNodeValue().equals(byte.class.getName()))
					paramClass = byte.class;
				else
					paramClass = Class.forName(paramClassName.getNodeValue());
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return paramClass;
	}
	
	protected static Object parseParam(Node node, Class<?> supertype) {
		NamedNodeMap namedNodeMap = node.getAttributes();
		
		Node objValue = namedNodeMap.getNamedItem("value"); //$NON-NLS-1$
		Node objType = namedNodeMap.getNamedItem("type"); //$NON-NLS-1$
		
		Object obj = null;
	
		//si on a un champs value alors c'est un type primitif
		if(objValue != null) {
			obj = objValue.getNodeValue();
			if(objType != null && objType.getNodeValue().equals("staticfield")) { //$NON-NLS-1$
				String className = objValue.getNodeValue();
				Class<?> clazz;
				try {
					clazz = Class.forName(className.substring(0, className.lastIndexOf('.')));
					obj = clazz.getField(className.substring(className.lastIndexOf('.')+1)).get(null);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			} else {
				if(supertype == boolean.class)
					obj = Boolean.parseBoolean((String)obj);
				else if(supertype == short.class)
					obj = Short.parseShort((String)obj);
				else if(supertype == int.class)
					obj = Integer.parseInt((String)obj);
				else if(supertype == long.class)
					obj = Long.parseLong((String)obj);
				else if(supertype == float.class)
					obj = Float.parseFloat((String)obj);
				else if(supertype == double.class)
					obj = Double.parseDouble((String)obj);
				else if(supertype == byte.class)
					obj = Byte.parseByte((String)obj);
			}
			return obj;
		}

		//sinon on a affaire à une class à instancier
		Node className = namedNodeMap.getNamedItem("class"); //$NON-NLS-1$
		
		//charge la class en question
		Class<?> cParam = null;
		try {
			cParam = Class.forName(className.getNodeValue());
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		//si chargé avec succès
		if(cParam != null) {
			List<Object> params = new ArrayList<Object>();
			List<Class<?>> classParams = new ArrayList<Class<?>>(); 
			//regarde si il à des parametresenfants
			NodeList childNodes = node.getChildNodes();
			
			//si c'est le cas boucle dessus pour les extraires
			if(childNodes.getLength() > 0) {
				for(int i = 0; i < childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					if(childNode.getNodeName().equals("param")) { //$NON-NLS-1$					
						Class<?> paramClass = parseParamClass(childNode);
						classParams.add(paramClass);
						params.add(parseParam(childNode, paramClass));
					}
				}
			}
			
			//place tous les parametres dans un tableau d'objet
			Object[] objectsParam = new Object[params.size()];
			objectsParam = params.toArray(objectsParam);
			
			//en extrait leurs class
			Class<?>[] objectsClass = new Class<?>[params.size()];
			objectsClass = classParams.toArray(objectsClass);
			
			try {
				Constructor<?> constructor = cParam.getConstructor(objectsClass);
				
				obj = constructor.newInstance(objectsParam);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			if(childNodes.getLength() > 0) {
				for(int i = 0; i < childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					if(childNode.getNodeName().equals("method")) { //$NON-NLS-1$					
						parseMethod(childNode, obj);
					}
				}
			}
		}
		
		return obj;
		
	}
}
