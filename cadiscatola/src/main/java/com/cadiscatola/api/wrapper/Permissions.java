package com.cadiscatola.api.wrapper;

import com.gitblit.Constants.AccessPermission;

public class Permissions {
	private Permissions() { }
	
	/**	Permessi di lettura e scrittura sul repository (possibilità di clonare e pushare)
	 * 
	 */
	public static final AccessPermission[] RW = { AccessPermission.PUSH, AccessPermission.DELETE };
	
	/** Permesso di lettura (possibilità di clonare) sul repository 
	 * 
	 */
	public static final AccessPermission[] R = { AccessPermission.CLONE };
	
	/** Nessun permesso (né clonare, né pushare).
	 * 
	 */
	public static final AccessPermission[] HIDDEN = { AccessPermission.NONE };
	
}
