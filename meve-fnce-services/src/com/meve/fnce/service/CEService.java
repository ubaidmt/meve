package com.meve.fnce.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.collection.FolderSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.constants.VersionStatus;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.VersionSeries;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.meve.fnce.bean.Contexto;
import com.meve.fnce.bean.CriteriosBusqueda;
import com.meve.fnce.bean.Propiedades;
import com.meve.fnce.exception.ServiceException;

class CEService extends CEConnection {

	protected CEService(Contexto ceContext) {
		super(ceContext);
	}
	
	/**
	 * Valida la conexion al dominio de P8
	 * @throws ServiceException En caso de no realizar correctamente la conexion al dominio de P8
	 */
	public void validateConnection() throws ServiceException {
		
		try 
		{
			// Set JAAS Subject
			createSubject();
			
			// Fecha P8 Domain
			fetchDomain();
		}
		catch (Exception e)
		{
			throw new ServiceException (e);
		}
		finally
		{
			// Release JAAS Subject
			releaseSubject();
		}
		
	}
	
	/**
	 * Consulta de objetos
	 * @param objectStoreName Nombre del Object Store
	 * @param searchCriteria Criterios de la consulta
	 * @return Lista de propiedades de los objetos regresados por la consulta 
	 * @throws ServiceException
	 */	
	public List<Propiedades> search(String objectStoreName, CriteriosBusqueda searchCriteria) throws ServiceException {
		
		try {
			
			// Set JAAS Subject
			createSubject();
			
			// Fetch Rows
			RepositoryRowSet rowSet = fetchRows(objectStoreName, searchCriteria.getSelectList(), searchCriteria.getClassName(), searchCriteria.getWhereClause(), searchCriteria.isIncludeSubClasses(), searchCriteria.getMaxRecords(), searchCriteria.getTimeLimit(), searchCriteria.getPageSize());
			
			// Get Search Result
			List<Propiedades> searchResult = getSearchResults(rowSet);
			
			return searchResult;
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}			
		
	}
	
	/**
	 * Consulta de objetos
	 * @param objectStoreName Nombre del Object Store
	 * @param sqlStatement Sentencia SQL
	 * @param pageSize Tama�o de p�gina
	 * @return Lista de propiedades de los objetos regresados por la consulta 
	 * @throws ServiceException
	 */	
	public List<Propiedades> search(String objectStoreName, String sqlStatement, int pageSize) throws ServiceException {

		try {
			
			// Set JAAS Subject
			createSubject();

			// Fetch Rows
			RepositoryRowSet rowSet = fetchRows(objectStoreName, sqlStatement, pageSize);
						
			// Get Search Result
			List<Propiedades> searchResult = getSearchResults(rowSet);
			
			return searchResult;
			
		} catch (Exception e) {
			throw new ServiceException(e);
		
		} finally {
			
			// Release JAAS Subject
			releaseSubject();
		}			

	}		
		
	@SuppressWarnings("unchecked")
	protected Document addDocument(String objectStoreName, String className, InputStream documentContent, String fileName, String mimeType, Propiedades propertiesMap, String fileIn) throws Exception {

		// Create Document
		Document newDoc = Factory.Document.createInstance(getOS(objectStoreName), className);
		
		// Set Content
		if (documentContent != null && fileName != null && mimeType != null) {
			ContentElementList contentList = Factory.ContentElement.createList();
			ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
			contentTransfer.setCaptureSource(documentContent);
			contentTransfer.set_RetrievalName(fileName);
			contentTransfer.set_ContentType(mimeType);
			contentList.add(contentTransfer);
			newDoc.set_ContentElements(contentList);
		}
		
		// CheckIn Document
		newDoc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		
		// Set Mime Type
		if (mimeType != null)
			newDoc.set_MimeType(mimeType);			
	
		// Assign Properties
		newDoc = (Document) setObjectProperties(newDoc, propertiesMap);
	
		// Filed In Folder
		fileDocument(objectStoreName, newDoc, fileIn);
		
		return newDoc;
	}	
	
	protected Document checkoutDocument(Document document, boolean override) throws Exception {
		
		if (document.get_IsCurrentVersion().booleanValue()== false)
			document = (Document)document.get_CurrentVersion();
		
		if (document.get_IsReserved().booleanValue() == true && override)
			document = cancelCheckoutDocument(document);
		
		if (document.get_IsReserved().booleanValue() == false) {
			document.checkout(ReservationType.EXCLUSIVE, null, null, null);
			document.save(RefreshMode.REFRESH);
		} else {
			throw new Exception("El documento ya se encuentra en estado de check out.");
		}
		
		return document;
	}
	
	protected Document cancelCheckoutDocument(Document document) throws Exception {
		
		if (document.get_IsCurrentVersion().booleanValue() == false) 
			document = (Document)document.get_CurrentVersion();	
		
		if (document.get_IsReserved().booleanValue() == true) {
			document.cancelCheckout();
			document.save(RefreshMode.REFRESH);
		} else {
			throw new Exception("El documento no se encuentra en estado de checkout");
		}	
		
		return document;
	}	
	
	protected void deleteDocument(Document document, boolean deleteVersionSeries) throws Exception {
		
		if (deleteVersionSeries) 
		{
			VersionSeries vs = document.get_VersionSeries();
			vs.delete();
			vs.save(RefreshMode.REFRESH);						
		}
		else
		{
			document.delete();
			document.save(RefreshMode.REFRESH);	
		}
		
	}
	
	protected Folder fileDocument(String objectStoreName, Document document, String fileIn) throws Exception {
		
		if (fileIn == null)
			return null;
		
		Folder folder = retrieveFolder(objectStoreName, fileIn);		
		ReferentialContainmentRelationship rel = folder.file(document, AutoUniqueName.AUTO_UNIQUE, document.get_Name(), DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		rel.save(RefreshMode.NO_REFRESH);		
		
		return folder;
	
	}
	
	protected void unfileDocument(String objectStoreName, Document document, String unfileFrom) throws Exception {

		if (unfileFrom == null)
			return;
		
		Folder folder = retrieveFolder(objectStoreName, unfileFrom);
		ReferentialContainmentRelationship rel = folder.unfile(document);
		rel.save(RefreshMode.NO_REFRESH);
			
	}
	
	@SuppressWarnings("unchecked")
	protected void updateContainmentName(String objectStoreName, Document document) throws Exception {

		FolderSet folSet = document.get_FoldersFiledIn();
		for (Iterator<Folder> it = folSet.iterator(); it.hasNext(); ) {
			Folder fol = it.next();
			unfileDocument(objectStoreName, document, fol.get_PathName());
			fileDocument(objectStoreName, document, fol.get_PathName());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected Document checkinDocument(Document document, InputStream documentContent, String fileName, String mimeType) throws Exception {
		
		if (document.get_IsCurrentVersion().booleanValue()== false)
			document = (Document)document.get_CurrentVersion();

		if (document.get_IsReserved().booleanValue() == true && (document.get_VersionStatus().getValue() != VersionStatus.RESERVATION_AS_INT)){			
			
			// Get Document Reservation Object
			document = (Document)document.get_Reservation();
			
			// Update Document Content
			if (documentContent != null && fileName != null && mimeType != null) {
				ContentElementList contentList = Factory.ContentElement.createList();
				ContentTransfer contentTransfer = Factory.ContentTransfer.createInstance();
				contentTransfer.set_RetrievalName(fileName);
				contentTransfer.setCaptureSource(documentContent);
				contentTransfer.set_ContentType(mimeType);
				contentList.add(contentTransfer);
				document.set_ContentElements(contentList);	
			}
			
			// Checkin Document
			document.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);											
		
			// Set Mime Type
			document.set_MimeType(mimeType);	
		
			// Save Document				
			document.save(RefreshMode.REFRESH);		
		}

		return document;		
	}
	
	protected Document retrieveDocument(String objectStoreName, String documentPathOrId) throws Exception {
		if (Id.isId(documentPathOrId))
			return Factory.Document.fetchInstance(getOS(objectStoreName), new Id(documentPathOrId), null);
		else
			return Factory.Document.fetchInstance(getOS(objectStoreName), documentPathOrId, null);
	}
		
	protected Folder addFolder(String objectStoreName, String className, Propiedades propertiesMap, String parent) throws Exception {
		
		Folder newFol;	
		
		// Fetch parentFolder
		Folder parentFolder = retrieveFolder(objectStoreName, parent);		
		
		// Create Folder
		newFol = Factory.Folder.createInstance(getOS(objectStoreName), className);
		newFol.set_Parent((Folder) parentFolder);		
	
		// Assign Properties
		newFol = (Folder) setObjectProperties(newFol, propertiesMap);
		
		return newFol;
	}	
	
	protected Folder retrieveFolder(String objectStoreName, String folderPathOrId) throws Exception {
		if (Id.isId(folderPathOrId))
			return Factory.Folder.fetchInstance(getOS(objectStoreName), new Id(folderPathOrId), null);	
		else
			return Factory.Folder.fetchInstance(getOS(objectStoreName), folderPathOrId, null);				
	}
	
	protected void deleteFolder(Folder folder) throws Exception {
		folder.delete();
		folder.save(RefreshMode.REFRESH);
	}	
	
	protected IndependentObject setObjectProperties(IndependentObject obj, Propiedades propertiesMap) throws Exception {
		
		// Update Properties
		com.filenet.api.property.Properties properties = obj.getProperties();
		if (propertiesMap != null) {
			for (Iterator<String> it = propertiesMap.keySet().iterator(); it.hasNext(); ) {
				String propertyName = it.next();
				Object propertyValue = propertiesMap.get(propertyName);
				properties.putObjectValue(propertyName, propertyValue);		
			}
		}
		
		if (obj instanceof VersionSeries) {
			((VersionSeries) obj).save(RefreshMode.REFRESH);
		} else if (obj instanceof Document) {	
			((Document) obj).save(RefreshMode.REFRESH);
		} else if (obj instanceof Folder) {
			((Folder) obj).save(RefreshMode.REFRESH);
		}
		
		return obj;				
		
	}	
	
	protected void setSecurityTemplate(IndependentObject obj, String securityTemplateId) throws Exception {
		
		if (!Id.isId(securityTemplateId))
			throw new Exception("El Id del sercurity template no es valido.");
		
		if (obj instanceof VersionSeries) {
			VersionSeries vs = (VersionSeries) obj;
			Document doc = (Document) vs.get_CurrentVersion();
			doc.applySecurityTemplate(new Id(securityTemplateId));
			doc.save(RefreshMode.REFRESH);	
		} else if (obj instanceof Document) {
			Document doc = (Document) obj;
			doc.applySecurityTemplate(new Id(securityTemplateId));
			doc.save(RefreshMode.REFRESH);			
		} else if (obj instanceof Folder) {
			Folder fol = (Folder) obj;
			fol.applySecurityTemplate(new Id(securityTemplateId));
			fol.save(RefreshMode.REFRESH);		
		}
		
	}
	
	protected void setSecurityFolder(String objectStoreName, Document doc, String parentFolder) throws Exception {
		
		Folder folder = null;
		if (parentFolder != null)
			folder = retrieveFolder(objectStoreName, parentFolder);

		doc.set_SecurityFolder(folder); // hereda seguridad del folder
		doc.save(RefreshMode.REFRESH);		
	}
		
	protected RepositoryRowSet fetchRows(String objectStoreName, String selectList, String className, String whereClause, boolean includeSubclasses, int maxRecords, int timeLimit, int pageSize) throws Exception {
			
		SearchScope search = new SearchScope(getOS(objectStoreName));	
	    SearchSQL sql = new SearchSQL();
	    sql.setSelectList(selectList);
	    
	    if (maxRecords > 0)
	    	sql.setMaxRecords(maxRecords);
	    
	    sql.setFromClauseInitialValue(className, null, includeSubclasses);
	    
	    if (whereClause != null)
	    	sql.setWhereClause(whereClause);
	    
	    sql.setTimeLimit(timeLimit);
	    
		return search.fetchRows(sql, pageSize,  null, true);

	}
	
	protected RepositoryRowSet fetchRows(String objectStoreName, String sqlStatement, int pageSize) throws Exception {
		
		SearchScope search = new SearchScope(getOS(objectStoreName));	
	    SearchSQL sql = new SearchSQL(sqlStatement);	    
		return search.fetchRows(sql, pageSize,  null, true);

	}	
	
	@SuppressWarnings("unchecked")
	protected List<Propiedades> getSearchResults(RepositoryRowSet rowSet) throws Exception {
		
		List<Propiedades> searchResult = new ArrayList<Propiedades>();
		
		for (Iterator<RepositoryRow> it = rowSet.iterator(); it.hasNext(); ) {
			RepositoryRow row = it.next();
			Propiedades propMapTO = new Propiedades();	
			for (Iterator<com.filenet.api.property.Property> it2 = row.getProperties().iterator(); it2.hasNext(); ) {
	    		com.filenet.api.property.Property prop = it2.next();
	    		propMapTO.put(prop.getPropertyName(), prop.getObjectValue());
			}
			searchResult.add(propMapTO);
		}
		
		return searchResult;
		
	}
	
}