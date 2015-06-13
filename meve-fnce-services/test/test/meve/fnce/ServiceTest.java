package test.meve.fnce;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;

import com.meve.fnce.bean.Contexto;
import com.meve.fnce.bean.Propiedades;
import com.meve.fnce.bean.Documento;
import com.meve.fnce.bean.Carpeta;
import com.meve.fnce.bean.DocumentoContenido;
import com.meve.fnce.bean.CriteriosBusqueda;
import com.meve.fnce.util.DateUtils;
import com.meve.fnce.exception.ServiceException;
import com.meve.fnce.service.DocumentService;
import com.meve.fnce.service.FolderService;

import com.ibm.json.java.JSONObject;
import com.ibm.json.java.JSONArray;

public class ServiceTest extends TestCase {
	
	/*
	 * Librerias de FileNet Content Engine requeridas normalmente ubicadas donde FileNet WorkplaceXT se instala en /opt/IBM/FileNet/WebClient/CE_API/lib/
	 * EJB Transport (Weblogic): 
	 * 	- Jace.jar
	 * 	- log4j.jar 
	 * 	- (adicionalmente wlfullclient.jar en caso de invocarse fuera del servidor de aplicaciones, ver documento de referencia "creating a wlfullclient.docx") 
	 * WSI Transport: 
	 * 	- Jace.jar 
	 * 	- log4j.jar
	 * 	- stax-api.jar
	 * 	- xlxpScanner.jar
	 * 	- xlxpScannerUtils.jar
	 */
	
	// Content Engine Context
	private Contexto contexto;
	
	// Content Engine Available Transport Types
	private static final int TRANSPORT_WSI = 0;
	private static final int TRANSPORT_EJB = 1;
	
	// Selected CE Transport
	private static int SELECTED_TRANSPORT = 0;
	
	// Content Engine Object Store
	private static final String objectStoreName = "ExcelECMOS";
	
	// Logger
	private static Logger logger = Logger.getLogger(ServiceTest.class);	
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		// Content Engine Context Parameters
		String username = "p8admin";
		String password = "filenet";
		String servidor = "p8server";
		String puerto = "9080";
		String ceuri = null;
		String stanza = null;
		
		switch (SELECTED_TRANSPORT) 
		{
			case TRANSPORT_WSI: 
				ceuri = "http://" + servidor + ":" + puerto + "/wsi/FNCEWS40MTOM/";
				stanza = "FileNetP8WSI";
				break;				
				
			case TRANSPORT_EJB:
				/**
				 * Las propiedades del JVM requeridas para el transporte EJB son normalmente establecidas a nivel servidor de aplicaciones por dominio (setDomainEnv)
				 * El archivo de configuracion JAAS para Weblogic (jaas.conf.WebLogic) se localiza normalmente donde se instala Content Platform Engine en /opt/IBM/FileNet/ContentEngine/config/samples
				 * 
				 */
				// JVM system properties required by EJB transport (Weblogic)
				System.setProperty("java.security.auth.login.config", "/opt/IBM/FileNet/ContentEngine/config/samples/jaas.conf.WebLogic");
				System.setProperty("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
				ceuri = "t3://" + servidor + ":" + puerto + "/FileNet/Engine";
				stanza = "FileNetP8";
				break;
		}
			
		// Create Content Engine Context
		contexto = new Contexto();
		contexto.setURI(ceuri);
		contexto.setUserName(username);
		contexto.setPassword(password);
		contexto.setStanza(stanza);
		
		// Print Content Engine Context
		logger.info("********************* Contexto *********************");
		logger.info("Tipo de Transporte: " + (SELECTED_TRANSPORT == TRANSPORT_WSI ? "WSI" : "EJB"));
		logger.info("URI: " + ceuri);
		logger.info("Stanza: " + stanza);
		logger.info("Usuario: " + username);
		logger.info("Password: ********");
		logger.info("Oject Store: " + objectStoreName);
		logger.info("********************* Contexto *********************");
	}

	@Test
	public void testCreateDocument() {
		
		String localFile = "c:/temp/prueba.txt";
		String className = "DocumentoPrueba";
		String fileIn = "/Documentos"; // (optional)
		
		InputStream is = null;
		DocumentoContenido docContenido = null;

		logger.info("Creando documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}
		
		// Assign Document Content (optional)
		try
		{
			is = new FileInputStream(localFile);
			docContenido = new DocumentoContenido();
			docContenido.setContentStream(is);
			docContenido.setContentFileName(FilenameUtils.getName(localFile));
			docContenido.setContentType(new MimetypesFileTypeMap().getContentType(localFile));
		} 
		catch (FileNotFoundException fne) 
		{
			logger.error("Error al intentar obtener el archivo para crear el nuevo documento", fne);
			return;
		}
		
		// Set Properties
		// For document creation "DocumentTitle" property is mandatory		
		Propiedades propertiesMap = new Propiedades();
		propertiesMap.put("DocumentTitle", "Prueba");		
		
		// Create Document
		try 
		{				
			Documento doc = docService.createDocument(objectStoreName, className, docContenido, propertiesMap, fileIn);
			logger.info("El documento " + doc.getDocumentTitle() + " ha sido creado");
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar crear el nuevo documento", se);
			return;
		} 
		finally 
		{
			IOUtils.closeQuietly(is);
		}			
		
	}
	
	
	@Test
	public void testUpdateDocument() {
		
		String localFile = "c:/temp/prueba.txt";
		String documentPathOrId = "/Documentos/Prueba";
		
		InputStream is = null;
		DocumentoContenido docContenido = null;
		boolean overrideCheckOut = false;		
		
		logger.info("Actualizando documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}			
		
		// Get Document
		Documento doc = null;
		try 
		{
			doc = docService.getDocument(objectStoreName, documentPathOrId);
		} 
		catch (ServiceException se) 
		{
			logger.error("No se pudo obtener el documento", se);
			return;				
		}
		
		// Document Content (optional)
		try 
		{				
			is = new FileInputStream(localFile);
			docContenido = new DocumentoContenido();
			docContenido.setContentStream(is);
			docContenido.setContentFileName(FilenameUtils.getName(localFile));
			docContenido.setContentType(new MimetypesFileTypeMap().getContentType(localFile));
		} 
		catch (FileNotFoundException fne) 
		{
			logger.error("Error al intentar obtener el archivo para actualizar el documento", fne);	
			return;
		} 	
		
		// Set Properties (optional)		
		Propiedades propertiesMap = new Propiedades();

		// Update Document
		try
		{			
			doc = docService.updateDocument(objectStoreName, doc, docContenido, propertiesMap, overrideCheckOut);		
			logger.info("Se actualiz� el documento " + doc.getDocumentTitle() + ". La versi�n actual es " + doc.getMajorVersion() + "." + doc.getMinorVersion());
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar actualizar el documento", se);
			return;
		} 
		finally
		{
			IOUtils.closeQuietly(is);
		}				
			
	}		
	
	@Test
	public void testDeleteDocument() {
		
		String documentPathOrId = "/Documentos/Prueba";
		boolean deleteVersionSeries = true;
			
		logger.info("Eliminando documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}			
		
		// Get Document
		Documento doc = null;
		try 
		{
			doc = docService.getDocument(objectStoreName, documentPathOrId);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el documento", se);
			return;				
		}			
		
		// Delete Document
		try
		{
			docService.deleteDocument(objectStoreName, doc, deleteVersionSeries);
			logger.info("El documento " + doc.getDocumentTitle() + " ha sido eliminado");
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar eliminar el documento", se);
			return;
		} 			
							
	}
	
	@Test
	public void testMoveDocument() {
		
		String documentPathOrId = "/Documentos/Prueba";
		String folderPathOrIdFrom = "/Documentos"; // (optional)
		String folderPathOrIdTo = "/Documentos"; // (optional)
		
		logger.info("Moviendo documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}			
		
		// Get Document
		Documento doc = null;
		try 
		{
			doc = docService.getDocument(objectStoreName, documentPathOrId);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el documento", se);
			return;				
		}				
		
		// Move Document
		try
		{
			docService.moveDocument(objectStoreName, doc, folderPathOrIdFrom, folderPathOrIdTo);
			logger.info("El documento " + doc.getDocumentTitle() + " ha sido movido");
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar mover el documento", se);
			return;
		} 					
		
	}	
	
	@Test
	public void testSearchDocuments() {
		
		List<Propiedades> searchResult = new ArrayList<Propiedades>();
		boolean writeDocsToDisk = false;
		
		logger.info("Consultando documentos...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Build Search Criteria
		try
		{
			CriteriosBusqueda criteriosBusqueda = new CriteriosBusqueda();
			criteriosBusqueda.setSelectList("Id, DocumentTitle"); // (optional - default Id)
			criteriosBusqueda.setClassName("Document"); // (optional - default Document)
			criteriosBusqueda.setWhereClause("isCurrentVersion = TRUE AND DateCreated <= " + DateUtils.convertLocalTimeToUTC(new java.util.Date())); // (optional)
			criteriosBusqueda.setIncludeSubClasses(false); // (optional - default false) 
			criteriosBusqueda.setMaxRecords(500); // (optional - default 500) The maximum number of search results that the query can return. Enter a value of zero to indicate the default limit of 50,000 search results
			criteriosBusqueda.setTimeLimit(180); // (optional - default 180) The maximum length of time that the query can run. Enter a value of zero to indicate no time limit (although other default time limits can apply			
			logger.info("SQL: " + criteriosBusqueda.getSQL());
			
			// Execute Search
			searchResult = docService.search(objectStoreName, criteriosBusqueda);				
		} 
		catch (ParseException pe) 
		{
			logger.error("Error al convertir la fecha como criterio de la busqueda de documentos", pe);
			return;
		} 
		catch (ServiceException se) 
		{
			logger.error("Error ejecutar la consulta de documentos", se);
			return;
		} 			
	
		try
		{
			for (int i = 0; i < searchResult.size(); i++) 
			{
				Propiedades propertiesResult = searchResult.get(i);
				logger.info("Propiedades obtenidas: " + propertiesResult.toString());
				
				// Get Document
				Documento doc = docService.getDocument(objectStoreName, propertiesResult.get("Id").toString());
				logger.info(doc.getDocumentTitle() + " (" + (doc.getDocumentContent() != null ? doc.getDocumentContent().getContentFileName() : "Sin Contenido") + ")");
				
	    	    // Write document to file system
				if (writeDocsToDisk)
				{
					String filePath = "c:/temp";
					FileOutputStream os = null;
					try
					{
						if (doc.getDocumentContent() != null) {
				    	    os = new FileOutputStream(filePath + File.separator + doc.getDocumentContent().getContentFileName());
				    	    IOUtils.copy(doc.getDocumentContent().getContentStream(), os);
				    	    logger.error("Documento " + doc.getDocumentContent().getContentFileName() + " escrito en disco");	
						}
					} 
					catch (Exception e)
					{
						logger.error("Error al intentar escribir el documento " + doc.getDocumentContent().getContentFileName() + " en disco", e);				
					}
					finally
					{
						IOUtils.closeQuietly(os);
					}
				}
	
			}
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar obtener un documento", se);
			return;
		}
				
	}
	
	@Test
	public void testSetSecurityTemplateDocument() {
		
		String documentPathOrId = "/Documentos/Prueba";
		String securityTemplateId = "{6A1A043D-F146-4CEE-8515-FB4035C22593}";
			
		logger.info("Aplicando plantilla de seguridad a documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}			
		
		// Get Document
		Documento doc = null;
		try 
		{
			doc = docService.getDocument(objectStoreName, documentPathOrId);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el documento", se);
			return;				
		}				
		
		// Update Security
		try
		{
			docService.setSecurityTemplate(objectStoreName, doc, securityTemplateId);		
			logger.info("Se aplic� correctamente la plantilla de seguridad al documento " + doc.getDocumentTitle());
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al aplicar la plantilla de seguridad al documento", se);
			return;
		} 				

	}
	
	@Test
	public void testSetSecurityFolder() {
		
		String documentPathOrId = "/Documentos/Prueba";
		String parentFolder = "/Documentos";
		
		logger.info("Aplicando seguridad heredada de folder a documento...");
		
		// Initiate Service
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			docService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}			
		
		// Get Document
		Documento doc = null;
		try 
		{
			doc = docService.getDocument(objectStoreName, documentPathOrId);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el documento", se);
			return;				
		}				
		
		// Update Security
		try
		{
			docService.setSecurityFolder(objectStoreName, doc, parentFolder);
			logger.info("Se aplic� correctamente la herencia de seguridad para el documento " + doc.getDocumentTitle());
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al aplicar la herencia de seguridad al documento", se);
			return;
		} 			

	}	
	
	@Test
	public void testCreateFolder() {
		
		String className = "FolderPrueba";
		String folderName = "Documentos";
		String parentFolder = "/"; // Root

		logger.info("Creando folder...");
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}				
		
		// Get Folder
		Carpeta carpeta = null;
		try 
		{
			carpeta = folService.getFolder(objectStoreName, parentFolder + "/" + folderName);
		} 
		catch (ServiceException se)
		{			
		}				
		
		if (carpeta != null) {
			logger.info("El folder ya existe, saliendo...");
			return;
		}
		
		// Set Properties
		// For folder creation "FolderName" property is mandatory			
		Propiedades propertiesMap = new Propiedades();
		propertiesMap.put("FolderName", folderName); 
		
		// Create Folder
		try
		{
			carpeta = folService.createFolder(objectStoreName, className, propertiesMap, parentFolder);
			logger.info("El folder " + carpeta.getPathName() + " ha sido creado");
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intenter crear el folder", se);
			return;
		} 					
		
	}	
	
	@Test
	public void testSetSecurityTemplateFolder() {
		
		String folderPathName = "/Documentos";
		String securityTemplateId = "{6A1A043D-F146-4CEE-8515-FB4035C22593}";
		
		logger.info("Aplicando plantilla de seguridad a folder...");
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}				
					
		// Get Folder
		Carpeta carpeta = null;
		try 
		{
			carpeta = folService.getFolder(objectStoreName, folderPathName);
		} 
		catch (ServiceException se) 
		{
			logger.error("No se pudo obtener el folder", se);
			return;				
		}			
		
		// Update Security
		try
		{
			folService.setSecurityTemplate(objectStoreName, carpeta, securityTemplateId);
			logger.info("Se aplic� correctamente la plantilla de seguridad al folder " + carpeta.getFolderName());
		}
		catch (ServiceException se) 
		{
			logger.error("Error al aplicar la plantilla de seguridad al folder", se);
			return;
		} 				

	}		
	
	@Test
	public void testUpdateFolder() {
		
		String folderPathName = "/Documentos";

		logger.info("Actualizando folder...");
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}				
					
		// Get Folder
		Carpeta carpeta = null;
		try 
		{
			carpeta = folService.getFolder(objectStoreName, folderPathName);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el folder", se);
			return;				
		}	
		
		// Set Properties (optional)			
		Propiedades propertiesMap = new Propiedades(); 		
		
		// Update Folder Name
		try
		{					
			carpeta = folService.updateFolder(objectStoreName, carpeta, propertiesMap);
			logger.info("El folder " + carpeta.getPathName() + " ha sido actualizado");				
		}
		catch (ServiceException se) 
		{
			logger.error("Error al intentar actualizar el folder", se);
			return;
		} 			
		
	}	
	
	@Test
	public void testDeleteFolder() {
		
		String folderPathName = "/Documentos";
		
		logger.info("Eliminando folder...");
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}				
					
		// Get Folder
		Carpeta carpeta = null;
		try 
		{
			carpeta = folService.getFolder(objectStoreName, folderPathName);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el folder", se);
			return;				
		}				
		
		// Validate Contained Documents
		if (carpeta.getContainedDocuments().size() > 0) {
			logger.info("El folder " + carpeta.getFolderName() + " contiene documentos, no es posible eliminarlo");
			return;
		}
		
		// Validate Subfolders
		if (carpeta.getSubFolders().size() > 0) {
			logger.info("El folder " + carpeta.getFolderName() + " contiene subfolders, no es posible eliminarlo");
			return;			
		}
		
		// Delete Folder
		try
		{
			folService.deleteFolder(objectStoreName, carpeta);
			logger.info("El folder " + carpeta.getFolderName() + " ha sido eliminado");
		}
		catch (ServiceException se) 
		{
			logger.error("Error al intentar eliminar el folder", se);
			return;
		} 			
				
	}	
	
	@Test
	public void testSearchFolders() {
			
		List<Propiedades> searchResult = new ArrayList<Propiedades>();
		
		logger.info("Consultando folders...");
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		DocumentService docService = new DocumentService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}				
					
		// Build Search Criteria
		try
		{
			CriteriosBusqueda criteriosBusqueda = new CriteriosBusqueda();
			criteriosBusqueda.setSelectList("Id, FolderName, PathName"); // (optional - default Id)
			criteriosBusqueda.setClassName("FolderPrueba"); // (optional - default Document)
			//criteriosBusqueda.setWhereClause("Parent = '{F368886A-D80A-43EB-8BEC-628729342CE9}'");
			criteriosBusqueda.setIncludeSubClasses(false); // (optional - default false) 
			criteriosBusqueda.setMaxRecords(500); // (optional - default 500) The maximum number of search results that the query can return. Enter a value of zero to indicate the default limit of 50,000 search results
			criteriosBusqueda.setTimeLimit(180); // (optional - default 180) The maximum length of time that the query can run. Enter a value of zero to indicate no time limit (although other default time limits can apply			
			
			logger.info("SQL: " + criteriosBusqueda.getSQL());
			
			// Execute Search
			searchResult = folService.search(objectStoreName, criteriosBusqueda.getSQL(), criteriosBusqueda.getPageSize());
		} 
		catch (ServiceException se) 
		{
			logger.error("Error ejecutar la consulta de folders", se);
			return;
		}
		
		try
		{
			for (int i=0; i < searchResult.size(); i++) 
			{
				Propiedades propertiesResult = searchResult.get(i);
				logger.info("Propiedades obtenidas: " + propertiesResult.toString());
				
				// Get Folder
				Carpeta carpeta = folService.getFolder(objectStoreName, propertiesResult.get("Id").toString());
				
				logger.info(carpeta.getFolderName() + " (" + carpeta.getPathName() + ")");
				
				if (carpeta.getContainedDocuments().size() > 0)
					logger.info("La carpeta incluye los siguientes documentos:");
				
				// Get property values from contained documents
				List<String> propertiesNameList = new ArrayList<String>();
				propertiesNameList.add("DocumentTitle");
				for (String docId : carpeta.getContainedDocuments()) {
					Documento doc = docService.getDocument(objectStoreName, docId);
					Propiedades propertiesValue = docService.getPropertiesValue(objectStoreName, doc, propertiesNameList);
					logger.info(propertiesValue.get("DocumentTitle"));
				}
			}
		} 
		catch (ServiceException se) 
		{
			logger.error("Error al intentar obtener un folder", se);
			return;
		} 				
		
	}
	
	@Test
	/**
	 * Ejemplo para mostrar actualizacion y consulta de datos binarios estructurados en formato JSON
	 * asociados a un objecto en Content Engine (documento o folder)
	 */
	public void testBinaryDataManagement() {
	
		String folderPathName = "/Documentos";
		JSONObject comentarios = new JSONObject();
		
		logger.info("Ejemplo de manipulacion de datos binarios...");		
		
		// Load JSON resource
		String resource = "comentarios.json";
		InputStream is = null;		
		try
		{
			is = getClass().getClassLoader().getResourceAsStream(resource);
			if (is == null)
				throw new FileNotFoundException("Recurso " + resource  + " no encontrado");
			comentarios = JSONObject.parse(is);
		}
		catch (FileNotFoundException fne)
		{
			logger.error(fne);
			return;
		}
		catch (IOException ioe)
		{
			logger.error("Recurso " + resource  + " no es un documento JSON valido", ioe);
			return;			
		} 
		finally 
		{
			IOUtils.closeQuietly(is);
		}
		
		// Initiate Service
		FolderService folService = new FolderService(contexto);
		
		// Validate Connection to Domain
		try
		{
			folService.validateConnection();
		}
		catch (ServiceException se)
		{
			logger.error("No se pudo realizar la conexion al dominio", se);
			return;
		}		
		
		// Get Folder
		Carpeta carpeta = null;
		try 
		{
			carpeta = folService.getFolder(objectStoreName, folderPathName);
		} 
		catch (ServiceException se)
		{
			logger.error("No se pudo obtener el folder", se);
			return;				
		}	
		
		// Set Property Binary Value
		Propiedades propertiesMap = new Propiedades();
		try
		{
			propertiesMap.put("ClbJSONData", comentarios.serialize().getBytes());
		}
		catch (IOException ioe)
		{
			logger.error("Error al construir mapa de propiedades", ioe);
			return;			
		}
		
		// Update Folder Binary Property Value
		try
		{					
			carpeta = folService.updateFolder(objectStoreName, carpeta, propertiesMap);
			logger.info("Los comentarios del folder " + carpeta.getPathName() + " han sido actualizados");				
		}
		catch (ServiceException se) 
		{
			logger.error("Error al intentar actualizar el folder", se);
			return;
		}
		
		// Parse Folder Binary Property Value
		try
		{
			List<String> propertiesNameList = new ArrayList<String>();
			propertiesNameList.add("ClbJSONData");
			Propiedades propertiesValue = folService.getPropertiesValue(objectStoreName, carpeta, propertiesNameList);
	    	byte[] data = (byte[]) propertiesValue.get("ClbJSONData");
	    	comentarios = JSONObject.parse(new String(data));	
	    	JSONArray jsonHistoriaComentarios = (JSONArray) comentarios.get("historia");
	    	logger.info("La historia de los comentarios asociados al folder " + carpeta.getPathName() + " son:");
	    	for (Object obj : jsonHistoriaComentarios) {
	    		JSONObject historiaEntry = (JSONObject) obj;
	    		logger.info(historiaEntry.get("timestamp").toString() + " - " + historiaEntry.get("usuario").toString() + " - " + historiaEntry.get("comentario").toString());
	    	}
		}
		catch (IOException ioe)
		{
			logger.error("Error al intentar leer el valor binary de la propiedad", ioe);
			return;			
		}
		catch (ServiceException se) 
		{
			logger.error("Error al intentar leer el valor binary de la propiedad", se);
			return;
		}
		
	}
		

}
