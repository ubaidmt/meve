package com.meve.fnce.bean;

public class Contexto {
	
	private String stanza = "FileNetP8WSI";		
	private String userName;
	private String password;
	private String uri;	
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getURI() {
		return uri;
	}
	public void setURI(String uri) {
		this.uri = uri;
	}
	public String getStanza() {
		return stanza;
	}
	public void setStanza(String stanza) {
		this.stanza = stanza;
	}	

}
