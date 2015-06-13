package com.meve.fnce.bean;

import java.util.Date;
import java.util.List;

public class Carpeta {

	private String Id;
	private Date dateCreated;
	private String creator;
	private String folderName;
	private String className;
	private String pathName;	
	private List<String> containedDocuments;
	private List<String> subFolders;
	private String parentId;

	public String getId() {
		return Id;
	}
	public void setId(String Id) {
		this.Id = Id;
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
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getPathName() {
		return pathName;
	}
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}
	public List<String> getContainedDocuments() {
		return containedDocuments;
	}
	public void setContainedDocuments(List<String> containedDocuments) {
		this.containedDocuments = containedDocuments;
	}
	public List<String> getSubFolders() {
		return subFolders;
	}
	public void setSubFolders(List<String> subFolders) {
		this.subFolders = subFolders;
	}	
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}	
}
