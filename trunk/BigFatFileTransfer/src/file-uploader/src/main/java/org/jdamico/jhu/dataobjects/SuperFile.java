package org.jdamico.jhu.dataobjects;

public class SuperFile {

	private String name;
	private String md5;
	private String afterMd5;
	private boolean transferred;
	private long size;
	
	
	
	public boolean isTransferred() {
		return transferred;
	}
	public void setUploaded(boolean transferred) {
		this.transferred = transferred;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setAfterMd5(String afterMd5) {
		this.afterMd5 = afterMd5;
	}
	
	public String getAfterMd5() {
		return afterMd5;
	}
	
	public long getSize() {
		return this.size;
	}
	
	public void setSize(long input) {
		this.size = input;
	}
	
	
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public SuperFile(String name, String md5) {
	
		this.name = name;
		this.md5 = md5;
		this.afterMd5 = null;
	}
	public SuperFile() {
		// TODO Auto-generated constructor stub
	}

	
}
