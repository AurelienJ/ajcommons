/*
 * Créé le 12 juin 2012 à 21:57:18 pour AjCommons (Bibliothèque de composant communs)
 *
 * Copyright 2002-2012 - Aurélien JEOFFRAY
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
package org.ajdeveloppement.commons.persistence.sql.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.ajdeveloppement.commons.persistence.sql.annotations.SqlField;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlForeignKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlPrimaryKey;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlTable;
import org.ajdeveloppement.commons.persistence.sql.annotations.SqlUnmappedFields;

/**
 * @author aurelien
 *
 */
@SupportedAnnotationTypes(value={"org.ajdeveloppement.commons.persistence.sql.annotations.SqlTable"}) //
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class QueryableProcessor extends AbstractProcessor {
	
	private static String log = ""; //$NON-NLS-1$
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		
		
	}
	

	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	@SuppressWarnings({ "nls", "unchecked" })
	public boolean process(Set<? extends TypeElement> elements,
			RoundEnvironment env) {
		
		if(!env.processingOver()) {
			if(elements == null)
				return false;
			
			for (Element element : env.getElementsAnnotatedWith(SqlTable.class)) { //ElementFilter.typesIn(env.getRootElements())
				SqlTable table = element.getAnnotation(SqlTable.class);
				if(table != null) {
					String packageName = ((TypeElement)element).getQualifiedName().toString().replace("."+element.getSimpleName(), "");
					try {
		
						SqlPrimaryKey pk = element.getAnnotation(SqlPrimaryKey.class);
						
						String baseClassName = element.getSimpleName().toString(); 
						String className = "T_" + baseClassName;
						String tableName = table.name();
						if(tableName == null || tableName.isEmpty())
							tableName = element.getSimpleName().toString();
						
						log += "Processing " + baseClassName + "...\n";
						
						Map<String, String> primaryKeyTypes = new HashMap<>();
						
						String contentClass = String.format("package %s;\n\n"
								+ "import javax.annotation.processing.Generated;\n"
								+ "import java.sql.ResultSet;\n"
								+ "import java.sql.SQLException;\n"
								+ "import java.util.Map;\n"
								+ "import java.util.HashMap;\n"
								+ "import org.ajdeveloppement.commons.persistence.sql.QResults;\n"
								+ "import org.ajdeveloppement.commons.persistence.sql.QField;\n"
								+ "import org.ajdeveloppement.commons.persistence.ObjectPersistenceException;\n\n"
								+ "@Generated(value=\"Generated by ajcommons\")\n"
								+ "@SuppressWarnings({\"nls\",\"javadoc\"})\n"
								+ "public class %s {\n",packageName,className);
						
						contentClass += String.format("\tpublic static final String TABLE_NAME = \"%s\";\n\n", tableName);
						
						List<String> createdColumns = new ArrayList<>();
						
						for(VariableElement fieldElem : ElementFilter.fieldsIn(element.getEnclosedElements())) {
							SqlField field = fieldElem.getAnnotation(SqlField.class);
							if(field != null) {
								TypeMirror fieldType = fieldElem.asType();
								if (isPrimitive(fieldType)) {
									TypeElement typeElement = processingEnv
											.getTypeUtils().boxedClass(
													(PrimitiveType) fieldType);
									if (typeElement != null)
										fieldType = typeElement.asType();
								}
								
								String fieldName = getFieldName(fieldElem);
								String fieldNameWithoutEscape = fieldName.replaceAll("[\\[\\]`]", "");
								
								log += "\tprocessing " + fieldName + "...\n";
								
								if(!createdColumns.contains(fieldName)) {
									contentClass += String.format("\t/**\n"
											+ "\t * Object Binder for %s field of table %s\n"
											+ "\t */\n", fieldName, tableName);
									contentClass += String.format("\tpublic static final QField<%s> %s = new QField<>(TABLE_NAME,\"%s\");\n\n",
											fieldType.toString(),
											fieldNameWithoutEscape.replace(" ", "_").toUpperCase(),
											fieldName);
									
									createdColumns.add(fieldName);
	
									//The field is in primary key
									if(pk != null && Arrays.stream(pk.fields()).anyMatch(f -> f.equalsIgnoreCase(fieldName))) {
										String key = Arrays.stream(pk.fields()).filter(f -> f.equalsIgnoreCase(fieldName)).findFirst().orElse(null);
										if(key != null)
											primaryKeyTypes.put(key, fieldElem.asType().toString());
									}
								}
							} else {
								SqlForeignKey foreignKey = fieldElem.getAnnotation(SqlForeignKey.class);
								if(foreignKey != null) {
									contentClass += extractForeignKeyFieldType(foreignKey, pk, primaryKeyTypes, fieldElem, createdColumns);
								}
							}
						}
						
						SqlUnmappedFields unmappedFields = element.getAnnotation(SqlUnmappedFields.class);
						
						AnnotationMirror unmappedFieldsMirror = element.getAnnotationMirrors().stream()
							.filter(am -> am.getAnnotationType().toString().equals(SqlUnmappedFields.class.getName())).findFirst().orElse(null);
						if(unmappedFields != null) {
							String[] fields = unmappedFields.fields();
							List<AnnotationValue> typeFields = null;
							
							if(unmappedFieldsMirror!= null) {
								for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : unmappedFieldsMirror.getElementValues().entrySet()) {
									if(entry.getKey().getSimpleName().toString().equals("typeFields")) {
										if(entry.getValue().getValue() instanceof List) {
											typeFields = (List<AnnotationValue>)entry.getValue().getValue();
										}
									}
								}
							}
							
							for(int i = 0; i < fields.length; i++) {
								TypeMirror fieldType = null;
								if(typeFields != null && i < typeFields.size()) {
									fieldType = (TypeMirror)typeFields.get(i).getValue();
									if (isPrimitive(fieldType)) {
										TypeElement typeElement = processingEnv
												.getTypeUtils().boxedClass(
														(PrimitiveType) fieldType);
										if (typeElement != null)
											fieldType = typeElement.asType();
									}
								}
								
								String fieldName = fields[i];
								
								contentClass += String.format("\t/**\n"
										+ "\t * Object Binder for %s field of table %s\n"
										+ "\t */\n", fieldName, tableName);
								contentClass += String.format("\tpublic static final QField<%s> %s = new QField<>(TABLE_NAME,\"%s\");\n\n",
										fieldType != null ? fieldType.toString() : "Object",
										fieldName.toUpperCase(),
										fieldName);
								
								//The field is in primary key
								if(pk != null && Arrays.stream(pk.fields()).anyMatch(f -> f.equalsIgnoreCase(fieldName))) {
									primaryKeyTypes.put(fields[i], fieldType != null ? ((TypeMirror)typeFields.get(i).getValue()).toString() : "Object");
								}
							}
						}
						
						//empty constructor
						//contentClass += String.format("\tpublic %s() {\n"
						//	+ "\t}\n\n",
						//	className
						//	);
						
						contentClass += String.format("\t/**\n"
								+ "\t * Return all instance in database as QResults iterator\n"
								+ "\t */\n");
						contentClass += String.format("\tpublic static QResults<%s, Void> all() {\n"
								+ "\t\treturn QResults.from(%s.class);\n"
								+ "\t}\n\n",
								baseClassName, baseClassName
								);
						
						
						
						if(pk != null) {
							
							String pkFilter = "";
							String params = "";
							for(int i = 0; i < pk.fields().length; i++) {
								String filter = pk.fields()[i].toUpperCase().replace("[", "").replace("]", "").replace("`", "") + ".equalTo(" + pk.fields()[i].toLowerCase().replace("[", "").replace("]", "").replace("`", "") + ")";
								String param = (primaryKeyTypes.containsKey(pk.fields()[i]) ? primaryKeyTypes.get(pk.fields()[i]) : "Object")
										+ " " + pk.fields()[i].toLowerCase().replace("[", "").replace("]", "").replace("`", "");
								if(i == 0) {
									pkFilter = filter;
									params = param;
								} else {
									pkFilter += ".and(" + filter + ")";
									params += ", " + param;
								}
								
							}
							contentClass += String.format("\tpublic static %s getInstanceWithPrimaryKey(%s) {\n"
									+ "\t\treturn QResults.from(%s.class).where(%s).first();\n"
									+ "\t}\n\n",
									baseClassName, params, baseClassName,
									pkFilter
									);
							
							//static method primary key map value
							contentClass += "\tpublic static Map<String, Object> getPrimaryKeyMap(ResultSet rs) throws SQLException, ObjectPersistenceException {\n"
									+ "\t\treturn getPrimaryKeyMap(getPrimaryKeyValues(rs));\n\t}\n\n";
							
							//static method primary key map value
							contentClass += String.format("\tpublic static Map<String, Object> getPrimaryKeyMap(Object... pkValues) {\n"
									+ "\t\tif(pkValues == null || pkValues.length != %s)\n"
									+ "\t\t\treturn null;\n\n"
									+ "\t\tMap<String, Object> persistenceInformations = new HashMap<String, Object>();\n",pk.fields().length);
							for(int i = 0; i < pk.fields().length; i++) {
								contentClass += String.format("\t\tpersistenceInformations.put(\"%s\",pkValues[%s]);\n",pk.fields()[i], i);
							}
							contentClass += "\t\treturn persistenceInformations;\n\t}\n\n";
			
							//static method primary key value
							contentClass += "\tpublic static Object[] getPrimaryKeyValues(ResultSet rs) throws SQLException, ObjectPersistenceException {\n"
									+ "\t\tObject[] pkValues = new Object[] {\n";
							boolean first = true;
							for(String pkField : pk.fields()) {
								if(!first)
									contentClass += ",\n";
								else
									first = false;
								contentClass += String.format("\t\t\t%s.getValue(rs)",pkField.toUpperCase().replace("[", "").replace("]", "").replace("`", ""));
								//		+ "\t\tpersistenceInformations.put();",
								//		"");
							}
							contentClass += "\n\t\t};\n\t\treturn pkValues;\n\t}\n";
						}
						
						//end of class
						contentClass += "}";
						JavaFileObject jfo = processingEnv.getFiler().createSourceFile(packageName+"."+className, element);
						jfo.openWriter().append(contentClass).close();
						
						//BuildC
					} catch (Exception e) {
						try {
							StringWriter strStackTraceWriter = new StringWriter();
							PrintWriter stackTraceWriter = new PrintWriter(strStackTraceWriter);
							e.printStackTrace(stackTraceWriter);
							
							FileObject fo = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, "erreur.log");
							fo.openWriter().append(log + strStackTraceWriter.toString()).close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						processingEnv.getMessager().printMessage(Kind.ERROR, getStackTrace(e));
					}
				}
			}
		}
		return false;
	}
	
	private String extractForeignKeyFieldType(SqlForeignKey foreignKey, SqlPrimaryKey primaryKey, Map<String, String> primaryKeyTypes, Element fieldElem, List<String> createdColumns) {
		return extractForeignKeyFieldType(foreignKey, primaryKey, primaryKeyTypes, fieldElem, null, createdColumns);
	}
	
	@SuppressWarnings({ "nls", "unchecked" })
	private String extractForeignKeyFieldType(SqlForeignKey foreignKey, SqlPrimaryKey primaryKey, Map<String, String> primaryKeyTypes, Element fieldElem, String[] keys, List<String> createdColumns) {
		String contentClass = ""; //$NON-NLS-1$
		if(foreignKey != null) {
			if(keys == null)
				keys = foreignKey.mappedTo();
			
			log += "\tprocessing foreign " + Arrays.toString(keys) + "...\n";
			
			TypeMirror fieldType = fieldElem.asType();
			
			Element element = processingEnv.getTypeUtils().asElement(fieldType);
			SqlTable table = element.getAnnotation(SqlTable.class);
			String tableName = table.name();
			if(tableName == null || tableName.isEmpty())
				tableName = element.getSimpleName().toString();
			
			//contentClass += "//" + tableName + ": " + Arrays.toString(foreignKey.mappedTo()) + "\n";
			
			SqlPrimaryKey foreignPK = processingEnv.getTypeUtils().asElement(fieldType).getAnnotation(SqlPrimaryKey.class);
			if(foreignPK != null) {
				List<String> pkFieldsName = Arrays.asList(foreignPK.fields());
				
				SqlUnmappedFields unmappedFields = processingEnv.getTypeUtils().asElement(fieldType).getAnnotation(SqlUnmappedFields.class);
				AnnotationMirror unmappedFieldsMirror = processingEnv.getTypeUtils().asElement(fieldType).getAnnotationMirrors().stream()
						.filter(am -> am.getAnnotationType().toString().equals(SqlUnmappedFields.class.getName())).findFirst().orElse(null);
				
				if(unmappedFields != null) {
					for(int i = 0; i < unmappedFields.fields().length; i++) {
						String unmappedField = unmappedFields.fields()[i];
						if(pkFieldsName.contains(unmappedField)) {
							TypeMirror unmappedfieldType = null;
							
							List<AnnotationValue> typeFields = null;
							
							if(unmappedFieldsMirror!= null) {
								for(Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : unmappedFieldsMirror.getElementValues().entrySet()) {
									if(entry.getKey().getSimpleName().toString().equals("typeFields")) {
										if(entry.getValue().getValue() instanceof List) {
											typeFields = (List<AnnotationValue>)entry.getValue().getValue();
										}
									}
								}
							}
							
							if(typeFields != null && i < typeFields.size()) {
								unmappedfieldType = (TypeMirror)typeFields.get(i).getValue();
								if (isPrimitive(fieldType)) {
									TypeElement typeElement = processingEnv.getTypeUtils().boxedClass((PrimitiveType) unmappedfieldType);
									if (typeElement != null)
										unmappedfieldType = typeElement.asType();
								}
							}
							
							contentClass += String.format("\t/**\n"
									+ "\t * Object Binder for %s field of table %s\n"
									+ "\t */\n", unmappedField, tableName);
							contentClass += String.format("\tpublic static final QField<%s> %s = new QField<>(TABLE_NAME,\"%s\");\n\n",
									unmappedfieldType != null ? unmappedfieldType.toString() : "Object",
									unmappedField.replaceAll("[\\[\\]`]", "").toUpperCase(),
									unmappedField);
							
							//The field is in primary key
							if(primaryKey != null && Arrays.stream(primaryKey.fields()).anyMatch(f -> f.equalsIgnoreCase(unmappedField))) {
								String key = Arrays.stream(primaryKey.fields()).filter(f -> f.equalsIgnoreCase(unmappedField)).findFirst().orElse(null);
								if(key != null)
									primaryKeyTypes.put(key, unmappedfieldType != null ? unmappedfieldType.toString() : "Object");
							}
						}
					}
				}
				
				for(VariableElement childFieldElem : ElementFilter.fieldsIn(processingEnv.getTypeUtils().asElement(fieldType).getEnclosedElements())) {
					SqlField childField = childFieldElem.getAnnotation(SqlField.class);
					if(childField != null && pkFieldsName.contains(getFieldName(childFieldElem))) {
						int index = pkFieldsName.indexOf(getFieldName(childFieldElem));
						
						TypeMirror childFieldType = childFieldElem.asType();
						if(isPrimitive(childFieldType))
							childFieldType = processingEnv.getTypeUtils().boxedClass((PrimitiveType)childFieldType).asType();
						
						String fieldName = keys[index];

						if(!createdColumns.contains(fieldName)) {
							contentClass += String.format("\t/**\n" //$NON-NLS-1$
									+ "\t * Object Binder for external %s field of table %s\n" //$NON-NLS-1$
									+ "\t */\n", fieldName, tableName); //$NON-NLS-1$
							contentClass += String.format("\tpublic static final QField<%s> %s = new QField<>(TABLE_NAME,\"%s\");\n\n", //$NON-NLS-1$
									childFieldType.toString(),
									fieldName.replaceAll("[\\[\\]`]", "").toUpperCase(),
									fieldName);
							
							createdColumns.add(fieldName);
							
							//The field is in primary key
							if(primaryKey != null && Arrays.stream(primaryKey.fields()).anyMatch(f -> f.equalsIgnoreCase(fieldName))) {
								String key = Arrays.stream(primaryKey.fields()).filter(f -> f.equalsIgnoreCase(fieldName)).findFirst().orElse(null);
								if(key != null)
									primaryKeyTypes.put(key, childFieldElem.asType().toString());
							}
						}
					} else {
						SqlForeignKey childForeignKey = childFieldElem.getAnnotation(SqlForeignKey.class);
						if(childForeignKey != null && childForeignKey.mappedTo().length > 0) {
							if(pkFieldsName.containsAll(Arrays.asList(childForeignKey.mappedTo()))) {
								//1 prend premier champs de childForeignKey.mappedTo()
								String childFKFirstKey = childForeignKey.mappedTo()[0];
								//2 cherche son index dans pkFieldsName
								int index = pkFieldsName.indexOf(childFKFirstKey);
								//3 extrait de keys à partir de index(2) et avec comme taille le tableau de 1
								String[] newKeys = Arrays.copyOfRange(keys, index, index+childForeignKey.mappedTo().length);
								log += "\t--> FPK=" + pkFieldsName + "\n";
								log += "\t--> processing foreign " + Arrays.toString(keys) + "->" + Arrays.toString(childForeignKey.mappedTo()) + " (index " + index + ")\n";
								//4 envoi en recursion le nouveau tableau
								contentClass += extractForeignKeyFieldType(childForeignKey, primaryKey, primaryKeyTypes, childFieldElem, newKeys, createdColumns);
							}
						}
					}
				}
			}
		}
		
		return contentClass;
	}
	
	private static boolean isPrimitive(TypeMirror type) {
		TypeKind kind = type.getKind();
		return kind == TypeKind.BOOLEAN || kind == TypeKind.BYTE
				|| kind == TypeKind.CHAR || kind == TypeKind.DOUBLE
				|| kind == TypeKind.FLOAT || kind == TypeKind.INT
				|| kind == TypeKind.LONG || kind == TypeKind.SHORT;
	}
	
	private String getFieldName(VariableElement element) {
		String fieldName = null;
		SqlField field = element.getAnnotation(SqlField.class);
		if(field != null) {
			fieldName = field.name();
			if(fieldName.isEmpty())
				fieldName = element.getSimpleName().toString();
		}
		
		return fieldName;
	}

	private static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}
}
