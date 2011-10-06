package org.jdamico.jhu.dataobjects;

import org.jdamico.jhu.runtime.BaseEntity;

public class SourceFile {
	
	private String name;
	private String uuid;
	private String md5;
	private String afterMd5;
	private String transferpath;
	private long lTotalSize;
	private long lZipSize;
	private int stage;

	private int offset;
	
	
	public long getTotalSize() {
		return lTotalSize;
	}
	
	public void setTotalSize(long input) {
		this.lTotalSize = input;
	}
	
	public long getZipSize() {
		return lZipSize;
	}
	
	public void setZipSize(long input) {
		this.lZipSize = input;
	}
	
	public String getName() {
		return name;
	}
	
	public String getUUID() {
		return uuid;
	}
	
	public void setUUID(String input) {
		this.uuid = input;
	}
	
	public String getTransferPath() {
		return transferpath;
	}
	
	public int getStage() {
		return stage;
	}
	
	public void setStage(int input) {
		this.stage = input;
	}
	
	public void setTransferPath(String input) {
		this.transferpath = input;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getMd5() {
		return md5;
	}
	public String getAfterMd5() {
		return afterMd5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public void setAfterMd5(String input) {
		this.afterMd5 = input;
	}
	/*public long getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}*/
	public SourceFile(String name, String transferpath,String uuid,String md5) {
		super();
		this.name = name;
		this.uuid = uuid;
		this.md5 = md5;
		//this.offset = offset;
		this.transferpath = transferpath;
	}
	public SourceFile() {
		// TODO Auto-generated constructor stub
	}


}
