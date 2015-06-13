package com.meve.fnce.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.meve.fnce.bean.Carpeta;
import com.meve.fnce.bean.Contexto;
import com.meve.fnce.bean.Propiedades;
import com.meve.fnce.exception.ServiceException;

/**
 * Servicio para realizar acciones con folders en FileNet Content Engine
 * @author Juan Saad
 *
 */
public class FolderService extends CEService {
	
	/**
	 * Constructor del servicio mediante contexto de conexion
	 * @param ceContext Contexto de conexion a FileNet Content Engine
	 */		
	public FolderService(Contexto ceContext) {
		super(ceContext);
	}
	
	/**
	 * Crea un nuevo folder
	 * @param objectStoreName Nombre del Object Store
	 * @param className Clase del nuevo folder
	 * @param propertiesMap Mapa de propiedades del folder. Es necesario especificar por lo menos la propiedad "FolderName"
	 * @param parentFolder Id o ruta completa del folder padre donde el nuevo folder sera creado
	 * @return Instancia del folder creado
	 * @throws ServiceException
	 */
	public Carpeta createFolder(String objectStoreName, String className, Propiedades propertiesMap, String parentFolder) throws ServiceException {

		Carpeta carp = null;
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Create Folder
			Folder fnFol = addFolder(objectStoreName, className, propertiesMap, parentFolder);
			
			// Get Carpeta
			carp = getCarpeta(fnFol);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return carp;		
		
	}
	
	/**
	 * Elimina un folder
	 * @param objectStoreName Nombre del Object Store
	 * @param carpeta Instancia del folder
	 * @throws ServiceException
	 */
	public void deleteFolder(String objectStoreName, Carpeta carpeta) throws ServiceException {

		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Folder
			Folder fnFol = retrieveFolder(objectStoreName, carpeta.getId());
			
			// Delete Folder
			deleteFolder(fnFol);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}		
		
	}
	
	/**
	 * Aplica una plantilla de seguridad al folder
	 * @param objectStoreName Nombre del Object Store
	 * @param carpeta Instancia del folder
	 * @param securityTemplateId Id de la plantilla de seguridad
	 * @throws ServiceException En caso de que el folder no incluya la plantilla de seguridad especificada
	 */
	public void setSecurityTemplate(String objectStoreName, Carpeta carpeta, String securityTemplateId)  throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Folder
			Folder fnFol = retrieveFolder(objectStoreName, carpeta.getId());
			
			// Apply Security Template
			setSecurityTemplate(fnFol, securityTemplateId);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}			
		
	}		
	
	/**
	 * Actualiza las propiedades de un folder
	 * @param objectStoreName Nombre del Object Store
	 * @param carpeta Instancia del folder
	 * @param propertiesMap Mapa de propiedades del folder a actualizar
	 * @return Instancia del folder actualizado
	 * @throws ServiceException
	 */
	public Carpeta updateFolder(String objectStoreName, Carpeta carpeta, Propiedades propertiesMap) throws ServiceException {
	
		Carpeta carp = null;
		
		try {

			// Set JAAS Subject
			createSubject();
			
			// Get Folder
			Folder fnFol = retrieveFolder(objectStoreName, carpeta.getId());
			
			// Update Folder Properties
			fnFol = (Folder) setObjectProperties(fnFol, propertiesMap);
			
			// Get Carpeta
			carp = getCarpeta(fnFol);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return carp;			
		
	}
	
	/**
	 * Obtiene la instancia de un folder
	 * @param objectStoreName Nombre del Object Store
	 * @param folderPathOrId Id o ruta completa del folder a obtener
	 * @return Instancia del folder
	 * @throws ServiceException En caso de no localizar la instancia del folder
	 */
	public Carpeta getFolder(String objectStoreName, String folderPathOrId) throws ServiceException {

		Carpeta carp = null;
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Folder
			Folder fnFol = retrieveFolder(objectStoreName, folderPathOrId);
			
			// Get Carpeta
			carp = getCarpeta(fnFol);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return carp;			
		
	}	
	
	/**
	 * Obtiene los valores de las propiedades de un documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del folder
	 * @param propertiesNameList Lista del nombre de las propiedades del folder
	 * @return Valores de las propiedades
	 * @throws ServiceException
	 */
	public Propiedades getPropertiesValue(String objectStoreName, Carpeta carpeta, List<String> propertiesNameList) throws ServiceException {
		
		Propiedades propertiesMap = new Propiedades();
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			if (propertiesNameList != null) {
			
				// Get Folder
				Folder fnFol = retrieveFolder(objectStoreName, carpeta.getId());
				
				// Get Properties Object Values
				com.filenet.api.property.Properties properties = fnFol.getProperties();	
				for (String propertyName : propertiesNameList) {
					if (fnFol.getProperties().isPropertyPresent(propertyName) && !propertiesMap.containsKey(propertyName))
						propertiesMap.put(propertyName, properties.getObjectValue(propertyName));					
				}

			}
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return propertiesMap;
	}	
	
	@SuppressWarnings("unchecked")
	protected Carpeta getCarpeta(Folder fnFol) throws ServiceException {
		Carpeta carpeta = new Carpeta();
		carpeta.setFolderName(fnFol.get_Name());
		carpeta.setId(fnFol.get_Id().toString());
		carpeta.setDateCreated(fnFol.get_DateCreated());
		carpeta.setCreator(fnFol.get_Creator());
		carpeta.setClassName(fnFol.getClassName());
		carpeta.setPathName(fnFol.get_PathName());
		// Set containedDocuments
		DocumentSet docSet = fnFol.get_ContainedDocuments();
		List<String> containedDocuments = new ArrayList<String>();
		for (Iterator<Document> it = docSet.iterator(); it.hasNext(); ) {
			Document fnDoc = it.next();
			containedDocuments.add(fnDoc.get_Id().toString());
		}
		carpeta.setContainedDocuments(containedDocuments);
		// Set subFolders
		FolderSet folSet = fnFol.get_SubFolders();
		List<String> subFolders = new ArrayList<String>();
		for (Iterator<Folder> it = folSet.iterator(); it.hasNext(); ) {
			Folder fol = it.next();
			subFolders.add(fol.get_Id().toString());
		}		
		carpeta.setSubFolders(subFolders);
		carpeta.setParentId(fnFol.get_Parent().get_Id().toString());
		return carpeta;
	}	
	
}
