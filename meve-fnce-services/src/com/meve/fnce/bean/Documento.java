package com.meve.fnce.bean;

import java.util.Date;
import java.util.List;

public class Documento {
	
	private String Id;
	private Date dateCreated;
	private String creator;
	private String documentTitle;
	private String mimeType;
	private String className;
	private DocumentoContenido documentContent;	
	private int majorVersion;
	private int minorVersion;
	private List<String> foldersFiledIn;
	
	public String getId() {
		return Id;
	}
	public void setId(String Id) {
		this.Id = Id;
	}
	public String getDocumentTitle() {
		return documentTitle;
	}
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}	
	public DocumentoContenido getDocumentContent() {
		return documentContent;
	}
	public void setDocumentContent(DocumentoContenido documentContent) {
		this.documentContent = documentContent;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}	
	public int getMajorVersion() {
		return majorVersion;
	}
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	public List<String> getFoldersFiledIn() {
		return foldersFiledIn;
	}
	public void setFoldersFiledIn(List<String> foldersFiledIn) {
		this.foldersFiledIn = foldersFiledIn;
	}	
}
