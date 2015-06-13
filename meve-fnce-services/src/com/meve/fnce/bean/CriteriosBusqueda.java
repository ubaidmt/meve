package com.meve.fnce.bean;

public class CriteriosBusqueda {

	private String selectList = "Id";
	private String className = "Document";
	private boolean includeSubClasses = false;
	private String whereClause;
	private int pageSize = 50;
	private int maxRecords = 500;
	private int timeLimit = 180; // seconds
	
	public String getSelectList() {
		return selectList;
	}
	public void setSelectList(String selectList) {
		this.selectList = selectList;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public boolean isIncludeSubClasses() {
		return includeSubClasses;
	}
	public void setIncludeSubClasses(boolean includeSubClasses) {
		this.includeSubClasses = includeSubClasses;
	}
	public String getWhereClause() {
		return whereClause;
	}
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getMaxRecords() {
		return maxRecords;
	}
	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}	
	public int getTimeLimit() {
		return timeLimit;
	}
	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}	
	public String getSQL() {
	    StringBuffer sqlsb = new StringBuffer(); 
	    sqlsb.append("SELECT TOP 500 ");
	    sqlsb.append(getSelectList());
	    sqlsb.append(" FROM ");
	    sqlsb.append(getClassName());
	    sqlsb.append(" WITH ");
	    sqlsb.append((isIncludeSubClasses() ? "INCLUDESUBCLASSES" : "EXCLUDESUBCLASSES"));	    
	    if (getWhereClause() != null) {
	    	sqlsb.append(" WHERE ");
	    	sqlsb.append(getWhereClause());
	    }
	    sqlsb.append(" OPTIONS(TIMELIMIT " + getTimeLimit() + ")");
		return sqlsb.toString();
	}	
}