package edu.upenn.cis455.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	
	public static File envHome = null;
	public static Environment myEnv;
	public static EntityStore store;
	
	/**
	 * Initializing Db Environment
	 * @param directory
	 */
	public void initializeDB(String directory){
		try {
			EnvironmentConfig myEnvConfig = new EnvironmentConfig();
			StoreConfig storeConfig = new StoreConfig();
			myEnvConfig.setAllowCreate(true);
			storeConfig.setAllowCreate(true);
			myEnvConfig.setTransactional(true);
			storeConfig.setTransactional(true);
//			System.out.println("directory" + directory);

			// Open the environment and entity store
			envHome = new File(directory);
			myEnv = new Environment(envHome, myEnvConfig);
			store = new EntityStore(myEnv, "EntityStore", storeConfig);
		} catch(DatabaseException dbe) {
			System.err.println("Error opening environment and store: " +
					dbe.toString());
			System.exit(-1);
		} 
	}
	
	/**
	 * Closing Berkeley DB
	 */
	public void closeDB(){
		if (store != null) {
			try {
				store.close();
			} catch(DatabaseException dbe) {
				System.err.println("Error closing store: " +
						dbe.toString());
				System.exit(-1);
			}
		}
		if (myEnv != null) {
			try {
				// Finally, close environment.
				myEnv.close();
			} catch(DatabaseException dbe) {
				System.err.println("Error closing MyDbEnv: " +
						dbe.toString());
				System.exit(-1);
			}
		} 
	}
}
