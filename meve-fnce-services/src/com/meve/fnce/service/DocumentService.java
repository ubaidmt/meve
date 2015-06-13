package com.meve.fnce.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Folder;
import com.meve.fnce.bean.DocumentoContenido;
import com.meve.fnce.bean.Contexto;
import com.meve.fnce.bean.Documento;
import com.meve.fnce.bean.Propiedades;
import com.meve.fnce.exception.ServiceException;

/**
 * Servicio para realizar acciones con documentos en FileNet Content Engine
 * @author Juan Saad
 *
 */
public class DocumentService extends CEService {
	
	/**
	 * Constructor del servicio mediante contexto de conexion
	 * @param ceContext Contexto de conexion a FileNet Content Engine
	 */	
	public DocumentService(Contexto ceContext) {
		super(ceContext);
	}
	
	/**
	 * Crea un nuevo documento
	 * @param objectStoreName Nombre del Object Store
	 * @param className Clase del nuevo documento
	 * @param docContent Contenido del documento (opcional)
	 * @param propertiesMap Mapa de propiedades del documento. Es necesario especificar por lo menos la propiedad "DocumentTitle"
	 * @param fileIn Id o ruta completa del folder a relacionar con el documento  (opcional)
	 * @return Instancia del documento creado
	 * @throws ServiceException
	 */
	public Documento createDocument(String objectStoreName, String className,  DocumentoContenido docContent, Propiedades propertiesMap, String fileIn) throws ServiceException {

		Documento doc = null;
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// If DocumentoContenido is null, create empty instance
			if (docContent == null)
				docContent = new DocumentoContenido();
			
			// Create Document
			Document fnDoc = addDocument(objectStoreName, className, docContent.getContentStream(), docContent.getContentFileName(), docContent.getContentType(), propertiesMap, fileIn);			
			
			// Get Documento
			doc = getDocumento(fnDoc);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return doc;		
		
	}
	
	/**
	 * Elimina un documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param deleteVersionSeries Indica si se eliminan todas la versiones asociadas al documento o unicamente la version correspondiente
	 * @throws ServiceException
	 */
	public void deleteDocument(String objectStoreName, Documento documento, boolean deleteVersionSeries) throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
			
			// Delete Document
			deleteDocument(fnDoc, deleteVersionSeries);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
	}
	
	/**
	 * Mueve un documento de un folder a otro
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param folderFrom Id o ruta completa del folder donde sera removida la relacion del documento (opcional)
	 * @param folderTo Id o ruta completa del folder a relacionar con el documento (opcional)
	 * @throws ServiceException
	 */
	public void moveDocument(String objectStoreName, Documento documento, String folderFrom, String folderTo) throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
			
			// Get Current Version
			if (!fnDoc.get_IsCurrentVersion().booleanValue())
				fnDoc = (Document) fnDoc.get_CurrentVersion();
			
			// Unfile
			if (folderFrom != null)
				unfileDocument(objectStoreName, fnDoc, folderFrom);			
				
			// File
			if (folderTo != null)
				fileDocument(objectStoreName, fnDoc, folderTo);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}		
		
	}
	
	/**
	 * Aplica una plantilla de seguridad al documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param securityTemplateId Id de la plantilla de seguridad
	 * @throws ServiceException En caso de que el documento no incluya la plantilla de seguridad especificada
	 */
	public void setSecurityTemplate(String objectStoreName, Documento documento, String securityTemplateId)  throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
			
			// Get Current Version
			if (!fnDoc.get_IsCurrentVersion().booleanValue())
				fnDoc = (Document) fnDoc.get_CurrentVersion();			
			
			// Apply Security Template
			setSecurityTemplate(fnDoc, securityTemplateId);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}			
		
	}	
	
	/**
	 * Establece la seguridad del documento en base un folder
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param parentFolder Id o ruta completa del folder de donde hereda la seguridad el documento
	 * @throws ServiceException
	 */
	public void setSecurityFolder(String objectStoreName, Documento documento, String parentFolder) throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
			
			// Get Current Version
			if (!fnDoc.get_IsCurrentVersion().booleanValue())
				fnDoc = (Document) fnDoc.get_CurrentVersion();		
			
			// Apply Security Template
			setSecurityFolder(objectStoreName, fnDoc, parentFolder);
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}			
		
	}
	
	/**
	 * Actualiza el contenido y/o propiedades de un documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param docContent Contenido del documento (opcional)
	 * @param propertiesMap Mapa de propiedades del documento a actualizar
	 * @param overrideCheckOut Bandera para indicar la sobreescritura de versionamiento en caso de que el documento se encuentre en estado de "Check Out"
	 * @return Instancia del documento actualizado
	 * @throws ServiceException
	 */
	public Documento updateDocument(String objectStoreName, Documento documento, DocumentoContenido docContent, Propiedades propertiesMap, boolean overrideCheckOut) throws ServiceException {

		Documento doc = null;
		
		try {

			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
			
			// Get Current Version
			if (!fnDoc.get_IsCurrentVersion().booleanValue())
				fnDoc = (Document) fnDoc.get_CurrentVersion();	
			
			// If DocumentoContenido is null, create empty instance
			if (docContent == null)
				docContent = new DocumentoContenido();			
						
			// Checkout Document
			fnDoc = checkoutDocument(fnDoc, overrideCheckOut);
			
			// Checkin Document
			fnDoc = checkinDocument(fnDoc, docContent.getContentStream(), docContent.getContentFileName(), docContent.getContentType());
			
			// Update Document Properties
			fnDoc = (Document) setObjectProperties(fnDoc, propertiesMap);
			
			// Update ContainmentName
			if (propertiesMap.containsKey("DocumentTitle"))
				updateContainmentName(objectStoreName, fnDoc);
			
			// Get Documento
			doc = getDocumento(fnDoc);	
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return doc;			
		
	}
	
	/**
	 * Obtiene la instancia de un documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documentPathOrId Id o ruta completa del documento a obtener
	 * @return Instancia del documento
	 * @throws ServiceException En caso de no localizar la instancia del documento
	 */
	public Documento getDocument(String objectStoreName, String documentPathOrId) throws ServiceException {

		Documento doc = null;
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Get Document
			Document fnDoc = retrieveDocument(objectStoreName, documentPathOrId);				
			
			// Get Documento
			doc = getDocumento(fnDoc);	
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}
		
		return doc;		
		
	}	
	
	/**
	 * Obtiene los valores de las propiedades de un documento
	 * @param objectStoreName Nombre del Object Store
	 * @param documento Instancia del documento
	 * @param propertiesNameList Lista del nombre de las propiedades del folder
	 * @return Valores de las propiedades
	 * @throws ServiceException
	 */
	public Propiedades getPropertiesValue(String objectStoreName, Documento documento, List<String> propertiesNameList) throws ServiceException {
		
		Propiedades propertiesMap = new Propiedades();
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			if (propertiesNameList != null) {
			
				// Get Document
				Document fnDoc = retrieveDocument(objectStoreName, documento.getId());
				
				// Get Properties Object Values
				com.filenet.api.property.Properties properties = fnDoc.getProperties();	
				for (String propertyName : propertiesNameList) {
					if (fnDoc.getProperties().isPropertyPresent(propertyName) && !propertiesMap.containsKey(propertyName))
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
	protected Documento getDocumento(Document fnDoc) throws ServiceException {
		Documento doc = new Documento();
		doc.setDocumentTitle(fnDoc.get_Name());
		doc.setId(fnDoc.get_Id().toString());
		doc.setDateCreated(fnDoc.get_DateCreated());
		doc.setCreator(fnDoc.get_Creator());
		doc.setClassName(fnDoc.getClassName());
		doc.setMimeType(fnDoc.get_MimeType());
		doc.setMajorVersion(fnDoc.get_MajorVersionNumber());
		doc.setMinorVersion(fnDoc.get_MinorVersionNumber());
		doc.setDocumentContent(getContentElement(fnDoc));
		// Set foldersFiledIn
		FolderSet folSet = fnDoc.get_FoldersFiledIn();
		List<String> foldersFiledIn = new ArrayList<String>();
		for (Iterator<Folder> it = folSet.iterator(); it.hasNext(); ) {
			Folder fol = it.next();
			foldersFiledIn.add(fol.get_Id().toString());
		}
		doc.setFoldersFiledIn(foldersFiledIn);
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	protected DocumentoContenido getContentElement(Document fnDoc) throws ServiceException {
		
		List<DocumentoContenido> docContentList = new ArrayList<DocumentoContenido>();
		
		try {
			
			ContentElementList contents = fnDoc.get_ContentElements();
			
			// Get all content elements from document
			for (Iterator<ContentTransfer> it = contents.iterator(); it.hasNext(); ) {
				ContentTransfer contentTransfer = it.next();
				DocumentoContenido docContent = new DocumentoContenido();
				docContent.setContentFileName(contentTransfer.get_RetrievalName());
				docContent.setContentSize(contentTransfer.get_ContentSize().doubleValue());
				docContent.setContentType(contentTransfer.get_ContentType());
				docContent.setContentStream(contentTransfer.accessContentStream());	
				docContentList.add(docContent);
			}
			
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
		// Return the first content element
		if (docContentList.size() > 0)
			return docContentList.get(0);
		else
			return null;
		
	}		

}
