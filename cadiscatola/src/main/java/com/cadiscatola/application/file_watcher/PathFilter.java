package com.cadiscatola.application.file_watcher;

import java.io.File;

import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * 
 * Filtro sulla cartella da osservare
 * (Non prende le cartelle nascoste di Git)
 *
 */
public class PathFilter extends AbstractFileFilter {
	@Override
	public boolean accept(File file) {
		return file.getAbsolutePath().contains(".git"); //si trova all'interno di una cartella git -> non voglio notificare i cambiamenti 
	}
}
