package org.jdamico.jhu.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Joiner {
	private File firstFile;
	private int noOfFiles;
	private String path;
	private String fileNameTemplate;
	private String opSys;
	private boolean deleteFiles;

	public Joiner() {
		this.opSys = "/";
	}

	public void join(File firstFileInp, boolean deleteFiles) {
		this.deleteFiles = deleteFiles;
		this.firstFile = firstFileInp;
		determineOpSys(this.firstFile);
		this.noOfFiles = getNoOfFiles(this.firstFile);
		File[] fileArr = new File[this.noOfFiles];
		File fileToCreate = new File(this.path + this.opSys
				+ this.fileNameTemplate);

		int i = 0;
		int bufferSize = 1024 * 1024;
		byte[] buffer = new byte[bufferSize];
		try {
			for (i = 0; i < this.noOfFiles; ++i) {
				fileArr[i] = new File(this.path + this.opSys
						+ this.fileNameTemplate + ".part_" + i);
			}
			i = 0;
			BufferedOutputStream oBuff = new BufferedOutputStream(
					new FileOutputStream(fileToCreate));
			BufferedInputStream iBuff = new BufferedInputStream(
					new FileInputStream(fileArr[i]));
			while (true) {
				while (iBuff.available() > bufferSize) {
					iBuff.read(buffer);
					oBuff.write(buffer);
				}
				if (iBuff.available() > 0) {
					byte[] bufferTemp = new byte[iBuff.available()];
					iBuff.read(bufferTemp);
					oBuff.write(bufferTemp);
				}
				oBuff.flush();
				iBuff.close();
				if (deleteFiles) {
					boolean success = fileArr[i].delete();
					if (!success)
						System.out.println("Part files were not deleted!");
				}
				++i;
				if (i == this.noOfFiles) {
//					System.out.println("Job Completed....");

					break;
				}
				iBuff = new BufferedInputStream(new FileInputStream(fileArr[i]));
			}
			oBuff.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private int getNoOfFiles(File firstFileInp) {
		int noOfFilesT = 1;
		try {
			this.path = firstFileInp.getAbsolutePath().substring(0,
					firstFileInp.getAbsolutePath().lastIndexOf(this.opSys));

			this.fileNameTemplate = firstFileInp.getName().substring(0,
					firstFileInp.getName().lastIndexOf("."));

			int i = 1;
			while (true) {
				File temp = new File(this.path + this.opSys
						+ this.fileNameTemplate + ".part_" + i);

				if (temp.exists()) {
					++noOfFilesT;
					++i;
				} else {
					temp = null;
					break;
				}
			}

			return noOfFilesT;
		} catch (Exception e) {
			e.printStackTrace();

			return noOfFilesT;
		} finally {
		}
	}

	public void determineOpSys(File temp) {
		if (temp.getAbsolutePath().lastIndexOf("/") != -1) {
			this.opSys = "/";
		} else {
			this.opSys = "\\";
		}
	}
}