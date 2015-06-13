package com.meve.fnce.service;

import javax.security.auth.Subject;

import com.meve.fnce.bean.Contexto;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;

class CEConnection {
	
	// CE Connection parameters
	private Contexto ceContext;
	private Connection con;
	private UserContext uc;
	private Subject subject;
	private boolean isConnected;	
	
	protected CEConnection(Contexto ceContext)
	{
		this.ceContext = ceContext;
		con = Factory.Connection.getConnection(ceContext.getURI());
		uc = UserContext.get();
		isConnected = false;
	}	
	
	/*
	 * Establishes the JAAS subject required by Content Engine
	 */
	protected void createSubject() {
	    Subject sub = UserContext.createSubject(con, ceContext.getUserName(), ceContext.getPassword(), ceContext.getStanza());
	    setSubject(sub);
	    uc.pushSubject(sub);
	    isConnected = true;			
	}
	
	/*
	 * Release JAAS subject
	 */	
	protected void releaseSubject() {
		try
		{
			setSubject(null);
			uc.popSubject();
		} catch (Exception e) {}
		finally {
			isConnected = false;
		}
	}	
	
	/*
	 * Returns Domain object.
	 */
	protected Domain fetchDomain()
	{
		Domain dom = Factory.Domain.fetchInstance(con, null, null);
	    return dom;
	}

	/*
	 * Returns ObjectStore object for supplied
	 * object store name.
	 */
	protected ObjectStore fetchOS(String name)
	{
	    ObjectStore os = Factory.ObjectStore.fetchInstance(Factory.Domain.getInstance(con,null), name, null);
	    return os;
	}
	
	/*
	 * Returns ObjectStore instance for supplied
	 * object store name.
	 */
	protected ObjectStore getOS(String name)
	{
	    ObjectStore os = Factory.ObjectStore.getInstance(Factory.Domain.getInstance(con,null), name);
	    return os;
	}	

	/*
	 * Returns the subject.
	 */
	protected Subject getSubject()
	{
		return subject;
	}

	/*
	 * Sets the subject to the Subject object created using
	 * the username and password passed to the establishConnection method.
	 */
	protected void setSubject(Subject sub)
	{
		this.subject = sub;
	}
	
	/*
	 * Checks whether JAAS subject has been pushed or not
	 */
	protected boolean isConnected() 
	{
		return isConnected;
	}
	
	protected Contexto getContexto() {
		return ceContext;
	}

	protected void setContexto(Contexto ceContext) {
		this.ceContext = ceContext;
	}		

}
