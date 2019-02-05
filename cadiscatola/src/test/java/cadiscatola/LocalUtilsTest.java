package cadiscatola;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.LocalStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.utils.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class LocalUtilsTest {
	private static User u;
	private static User v; 
	private static SharedSpace z; 
	
	@BeforeClass
	public static void createThings() {
		try {
			u = CloudStorageUtils.createUser("utentea", "PasswordA");
			v = CloudStorageUtils.createUser("utenteb", "PasswordB");
			z = CloudStorageUtils.createSharedSpace("repository", u);
		} catch (UserAlreadyExistsException | InternalException | SharedSpaceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void destroyThings() {
		try {
			CloudStorageUtils.deleteSharedSpace(z);
			CloudStorageUtils.deleteUser(u);
			CloudStorageUtils.deleteUser(v);
		} catch (SharedSpaceDoesNotExistException | InternalException | UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test(expected=InternalException.class)
	public void cloningWithoutAccessTest() throws SharedSpaceDoesNotExistException, InternalException {
		try {
			CloudStorageUtils.removeCollaborator(v, z);
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SharedSpaceDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File f = null; 
		try {
			f = File.createTempFile("/paperino/", "");
			f.delete(); 
			LocalStorageUtils.downloadSharedSpace(z, v, f.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test(expected=InternalException.class)
	public void pushingWithoutAccessTest() throws IOException, SharedSpaceDoesNotExistException, InternalException {
		try {
			CloudStorageUtils.setReadOnlyUser(u, z);
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SharedSpaceDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File f = null; 
		f = File.createTempFile("tmp-repo", "");

		LocalStorageUtils.downloadSharedSpace(z, v, f.getAbsolutePath());
		
		File new_file = new File(f, "a_file");
		LocalStorageUtils.updateSharedSpace(f.getAbsolutePath(), v);
		try {
			FileUtils.delete(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test
	public void cloningWithAccessTest() throws UserDoesNotExistException, SharedSpaceDoesNotExistException, InternalException, IOException {
		try {
			CloudStorageUtils.setReadOnlyUser(v, z);
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SharedSpaceDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File f = null; 
		try {
			f = File.createTempFile("/paperino/", "");
			f.delete(); 
			LocalStorageUtils.downloadSharedSpace(z, v, f.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	@Test
	public void pushingWithAccessTest() {
		try {
			CloudStorageUtils.setReadWriteUser(v, z);
	
			File f = null; 
			f = File.createTempFile("topolino", "");
			f.delete(); 

			LocalStorageUtils.downloadSharedSpace(z, v, f.getAbsolutePath());
			File q = new File(f.getAbsolutePath(), "figlio-di-topolino");
			q.createNewFile();

			LocalStorageUtils.updateSharedSpace(f.getAbsolutePath(), v);
			
			
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SharedSpaceDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void syncingTest() throws UserDoesNotExistException, SharedSpaceDoesNotExistException, InternalException, IOException {
		CloudStorageUtils.setReadWriteUser(v, z);
		
		File vlocalcopy = File.createTempFile("vlocalrepo", "");
		vlocalcopy.delete(); 
		LocalStorageUtils.downloadSharedSpace(z, v, vlocalcopy.getAbsolutePath());
		
		File ulocalcopy = File.createTempFile("ulocalrepo", "");
		ulocalcopy.delete(); 
		LocalStorageUtils.downloadSharedSpace(z, u, ulocalcopy.getAbsolutePath());
		
		File newFile = new File(vlocalcopy, "newFile");
		newFile.createNewFile();
		
		LocalStorageUtils.updateSharedSpace(vlocalcopy.getAbsolutePath(), v);
		
		LocalStorageUtils.synchronizeSharedSpace(ulocalcopy.getAbsolutePath(), u);
		
		assertTrue( (new File(ulocalcopy, "newFile").exists() ));
	}
}
