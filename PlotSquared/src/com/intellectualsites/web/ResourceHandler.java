package com.intellectualsites.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by Citymonstret on 2014-09-20.
 */
public class ResourceHandler {

	private File file;
	private BufferedReader reader;

	public ResourceHandler(String filePath, FileType fileType, File folder) throws Exception {
		if (fileType == FileType.CSS) {
			this.file =
					new File(folder.toPath().toString() + File.separator + "web" + File.separator + "css"
							+ File.separator + filePath + "." + fileType.toString());
		}
		else {
			this.file =
					new File(folder.toPath().toString() + File.separator + "web" + File.separator + filePath + "."
							+ fileType.toString());
		}
	}

	public String getHTML() throws Exception {
		StringBuilder html = new StringBuilder();
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
		String line = "";
		while ((line = this.reader.readLine()) != null) {
			html.append(line);
		}
		return html.toString();
	}

	public void done() throws Exception {
		this.reader.close();
	}

	public static enum FileType {
		CSS("css"),
		HTML("html"),
		JS("js");

		private String ext;

		FileType(String ext) {
			this.ext = ext;
		}

		@Override
		public String toString() {
			return this.ext;
		}
	}
}
