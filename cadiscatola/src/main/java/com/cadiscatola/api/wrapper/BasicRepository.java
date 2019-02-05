package com.cadiscatola.api.wrapper;

import java.util.Date;

import com.gitblit.Constants;
import com.gitblit.Constants.AccessRestrictionType;
import com.gitblit.models.RepositoryModel;

/** Un RepositoryModel con con alcuni campi impostati a valori di default: 
 *  	-> visibilità VIEW (inizialmente visibile solo all'Owner e all'Admin)
 *  	-> HEAD, mergeTo a valori tali da permettere il push su un bare repository su GitBlit.
 */
public class BasicRepository extends RepositoryModel{
	private static final long serialVersionUID = 1L;
	
	public BasicRepository(String name, String description, String owner, Date date) {
		super(name, description, owner, date);
		this.HEAD = Constants.R_MASTER;
		this.mergeTo = Constants.MASTER;
		this.accessRestriction = AccessRestrictionType.VIEW;
	}
}
