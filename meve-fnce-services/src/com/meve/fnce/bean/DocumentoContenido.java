package com.meve.fnce.bean;

import java.io.InputStream;

public class DocumentoContenido {
	
	private String contentFileName;
	private InputStream contentStream;
	private double contentSize;	
	private String contentType;
	
	public String getContentFileName() {
		return contentFileName;
	}
	public void setContentFileName(String contentFileName) {
		this.contentFileName = contentFileName;
	}
	public InputStream getContentStream() {
		return contentStream;
	}
	public void setContentStream(InputStream contentStream) {
		this.contentStream = contentStream;
	}
	public double getContentSize() {
		return contentSize;
	}
	public void setContentSize(double contentSize) {
		this.contentSize = contentSize;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}	
}