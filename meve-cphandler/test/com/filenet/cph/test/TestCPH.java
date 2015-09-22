package com.filenet.cph.test;

import com.filenet.cph.collections.DocumentCreateChangePreprocessor;

public class TestCPH {

	public static void main(String[] args) {
		DocumentCreateChangePreprocessor cph = new DocumentCreateChangePreprocessor();
		boolean retVal = cph.isTransactionValid();
		System.out.println(retVal);
	}

}
