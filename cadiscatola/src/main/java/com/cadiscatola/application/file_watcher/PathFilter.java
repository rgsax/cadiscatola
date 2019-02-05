package com.cadiscatola.application.file_watcher;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class PathFilter extends AbstractFileFilter {
	@Override
	public boolean accept(File file) {
		return file.getAbsolutePath().contains(".git"); //si trova all'interno di una cartella git -> non voglio notificare i cambiamenti 
	}
}
