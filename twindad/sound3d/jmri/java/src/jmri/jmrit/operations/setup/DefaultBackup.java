// DefaultBackup.java

package jmri.jmrit.operations.setup;

/**
 * Specific Backup class for backing up and restoring Operations working files
 * to the Default Backup Store. Derived from BackupBase.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 */
public class DefaultBackup extends BackupBase {
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(DefaultBackup.class.getName());

	/**
	 * Creates a DefaultBackup instance and initializes the root directory to
	 * the given name.
	 */
	public DefaultBackup() {
		super("backups");
	}
}
