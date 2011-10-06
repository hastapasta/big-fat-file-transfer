package org.jdamico.jhu.xml;

import java.io.IOException;

import org.jdamico.jhu.dataobjects.FileMap;
import org.xml.sax.SAXException;

public class ObjConverter {
	public FileMap convertToFileMap(String xml) throws Exception {
		Parser parser = null;
		FileMap fileMap =null;
		try {
			parser = new Parser(xml);
			FileMapDataProcessor dp = parser.getRawFileMapData();
			fileMap = dp.getData();
		} catch (SAXException e) {

			System.out.println("FileMap not found at remote host.");
			
			
		} catch (IOException e) {

			System.out.println("FileMap not found at remote host.");
		}
		
		return fileMap;
	}
}
