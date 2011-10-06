package org.jdamico.jhu.xml;

import java.util.ArrayList;
import java.util.List;

import org.jdamico.jhu.dataobjects.FileMap;
import org.jdamico.jhu.dataobjects.NativeFile;
import org.jdamico.jhu.dataobjects.PartialFile;
import org.jdamico.jhu.dataobjects.SourceFile;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/*
 *<?xml version="1.0" encoding="UTF-8"?>
<filemap>
<sourcefile name="" md5="" offset=""/>
<pfiles>
<pfile name="" md5="" uploaded=""/>
</pfiles>
</filemap>
 */

public class FileMapDataProcessor extends DefaultHandler implements DefaultDataProcessor{

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */

	private FileMap message = new FileMap();
	private SourceFile sourcefile = new SourceFile();
	private PartialFile pfile = null;
	private NativeFile nfile = null;
	private List<PartialFile> partialFileList = new ArrayList<PartialFile>(); 
	private List<NativeFile> nativeFileList = new ArrayList<NativeFile>(); 

	private StringBuffer buffer = null;
	private boolean isTagActive = false;
	private String activeTag = null;

	public static final String TAG_sourcefile = "sourcefile";

	public static final String TAG_pfile = "pfile";
	
	public static final String TAG_nfile = "nfile";



	public void startElement (String namespaceUri, String localName, String qualifiedName, Attributes attributes) {


		if(qualifiedName.equals(TAG_sourcefile)){
			activeTag = TAG_sourcefile;

			sourcefile.setMd5(attributes.getValue("md5"));
			sourcefile.setAfterMd5(attributes.getValue("aftermd5"));
			sourcefile.setName(attributes.getValue("name"));
			sourcefile.setTransferPath(attributes.getValue("transferpath"));
			//sourcefile.setOffset(Integer.parseInt(attributes.getValue("offset")));
			sourcefile.setUUID(attributes.getValue("uuid"));
			if (attributes.getValue("stage") != null)
				sourcefile.setStage(Integer.parseInt(attributes.getValue("stage")));
			if (attributes.getValue("totalsize") !=null)
				sourcefile.setTotalSize(Long.parseLong(attributes.getValue("totalsize")));
			if (attributes.getValue("zipsize") !=null)
				sourcefile.setZipSize(Long.parseLong(attributes.getValue("zipsize")));
			

			isTagActive = true;
		}else if(qualifiedName.equals(TAG_pfile)){

			activeTag = TAG_pfile;

			pfile = new PartialFile();
			if (!attributes.getValue("aftermd5").equals("null"))
				pfile.setAfterMd5(attributes.getValue("aftermd5"));
			pfile.setMd5(attributes.getValue("md5"));
			pfile.setName(attributes.getValue("name"));
			if (attributes.getValue("size") !=null)
				pfile.setSize(Long.parseLong(attributes.getValue("size")));

			try{
				pfile.setUploaded(Boolean.parseBoolean(attributes.getValue("uploaded")));
			}catch(Exception e){
				pfile.setUploaded(false);
			}

			partialFileList.add(pfile);


			isTagActive = true;

		}else if(qualifiedName.equals(TAG_nfile)){

			activeTag = TAG_nfile;

			nfile = new NativeFile();
			if (!attributes.getValue("aftermd5").equals("null"))
				nfile.setAfterMd5(attributes.getValue("aftermd5"));
			nfile.setMd5(attributes.getValue("md5"));
			nfile.setName(attributes.getValue("name"));

			

			nativeFileList.add(nfile);


			isTagActive = true;

		}else{
			isTagActive = false;
			buffer = null;
		}

	}

	public void characters(char[] chars,int start,int length){


		buffer = null;


	}

	public void endElement(String uri, String name, String qualifiedName){


		buffer = null;


	}


	@Override
	public FileMap getData() {
		message.setNativeFileList(nativeFileList.toArray(new NativeFile[nativeFileList.size()]));
		message.setPartialFileList(partialFileList.toArray(new PartialFile[partialFileList.size()]));
		message.setSourceFile(sourcefile);

		return message;
	}

}
