package org.vikulin.utils;

import org.jdamico.jhu.components.Splitter;
import org.vikulin.runtime.Configuration;

public abstract interface Constants {
	public static final Configuration conf = Configuration
			.getInstance();
	public static final String PATH_SEPARATOR = Splitter
			.returnPathSeparator(conf.getFileDirectory());
}