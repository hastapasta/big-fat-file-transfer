package org.jdamico.jhu.dataobjects;


public class FileMap {
	private SourceFile sourceFile;
	private PartialFile[] partialFileList;
	private NativeFile[] nativeFileList;
	
	public SourceFile getSourceFile() {
		return sourceFile;
	}
	public void setSourceFile(SourceFile sourceFile) {
		this.sourceFile = sourceFile;
	}
	public PartialFile[] getPartialFileList() {
		return partialFileList;
	}
	
	public NativeFile[] getNativeFileList() {
		return nativeFileList;
	}
	public void setPartialFileList(PartialFile[] partialFileList) {
		this.partialFileList = partialFileList;
	}
	
	public void setNativeFileList(NativeFile[] nativeFileList) {
		this.nativeFileList = nativeFileList;
	}
	public FileMap(SourceFile sourceFile, PartialFile[] partialFileList, NativeFile[] nativeFileList) {
		super();
		this.sourceFile = sourceFile;
		this.partialFileList = partialFileList;
		this.nativeFileList = nativeFileList;
	}
	public FileMap() {
		// TODO Auto-generated constructor stub
	}
	
	
}