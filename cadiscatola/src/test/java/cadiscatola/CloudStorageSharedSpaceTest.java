package cadiscatola;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cadiscatola.api.model.SharedSpace;
import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.exceptions.SharedSpaceAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.SharedSpaceDoesNotExistException;
import com.cadiscatola.api.utils.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class CloudStorageSharedSpaceTest {
	private static int NUM_SP = 4;
	private static SharedSpace[] sharedSpaces = new SharedSpace[NUM_SP];
	private static User owner;
	private static User testUser;
	private static User collaborator1;
	private static User collaborator2;
	private static User collaborator3;
	
	@BeforeClass
	public static void createdTestSharedSpaces() throws UserAlreadyExistsException, InternalException, SharedSpaceAlreadyExistsException, UserDoesNotExistException, SharedSpaceDoesNotExistException {
		owner = CloudStorageUtils.createUser("shared_spaces_owner_test", "password");
		testUser = CloudStorageUtils.createUser("another_user_test", "password");
		collaborator1 = CloudStorageUtils.createUser("collaborator1_test", "password");
		collaborator2 = CloudStorageUtils.createUser("collaborator2_test", "password");
		collaborator3 = CloudStorageUtils.createUser("collaborator3_test", "password");
		
		for(int i = 0 ; i < NUM_SP ; ++i)
			sharedSpaces[i] = CloudStorageUtils.
				createSharedSpace("sharedSpace" + i + "_test", owner);
		
		CloudStorageUtils.setReadWriteUser(collaborator1, sharedSpaces[2]);
		CloudStorageUtils.setReadWriteUser(collaborator2, sharedSpaces[3]);
		CloudStorageUtils.setReadOnlyUser(collaborator3, sharedSpaces[3]);
		CloudStorageUtils.setReadWriteUser(collaborator3, sharedSpaces[2]);
	}
	
	@AfterClass
	public static void deleteOtherTestSharedSpaces() throws SharedSpaceDoesNotExistException, InternalException, UserDoesNotExistException {
		for(int i = 0 ; i < NUM_SP ; ++i)
			if(sharedSpaces[i] != null)
				CloudStorageUtils.deleteSharedSpace(sharedSpaces[i]);
		CloudStorageUtils.deleteUser(owner);
		CloudStorageUtils.deleteUser(testUser);
		CloudStorageUtils.deleteUser(collaborator1);
		CloudStorageUtils.deleteUser(collaborator2);
		CloudStorageUtils.deleteUser(collaborator3);
	}
	
	@Test
	public void createSharedSpaceTest() throws InternalException {
		SharedSpace sharedSpace = null;
		try {
			sharedSpace = CloudStorageUtils.createSharedSpace("new_sharedSpace_test", owner);
		} catch (SharedSpaceAlreadyExistsException e) {
			//empty
		}
		
		assertNotNull(sharedSpace);
		
		try {
			CloudStorageUtils.deleteSharedSpace(sharedSpace);
		} catch (SharedSpaceDoesNotExistException e) {
			//empty
		}
	}
	
	@Test
	public void deleteSharedSpaceTest() throws InternalException {
		boolean status = false;
		try {
			status = CloudStorageUtils.deleteSharedSpace(sharedSpaces[0]);
			sharedSpaces[0] = null;
		} catch (SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(status);
	}
	
	@Test
	public void setReadOnlyUserTest() throws InternalException {
		boolean status = false;
		
		try {
			status = CloudStorageUtils.setReadOnlyUser(testUser, sharedSpaces[2]);
		} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(status);		
	}
	
	@Test
	public void setReadWriteUserTest() throws InternalException {
		boolean status = false;
		
		try {
			status = CloudStorageUtils.setReadWriteUser(testUser, sharedSpaces[2]);
		} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(status);		
	}
	
	@Test
	public void removeCollaboratorTest() throws InternalException {
		boolean status = false;
		
		try {
			status = CloudStorageUtils.removeCollaborator(collaborator1, sharedSpaces[2]);
		} catch (UserDoesNotExistException | SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(status);
	}
	
	@Test
	public void getSharedSpaceCollaboratorsTest() throws InternalException {
		Map<String, Boolean> collaborators = null;
		
		try {
			collaborators = CloudStorageUtils.getSharedSpaceCollaborators(sharedSpaces[3]);
		} catch (SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertEquals(2, collaborators.size());
		assertTrue(collaborators.get(collaborator2.getName()).booleanValue());
		assertFalse(collaborators.get(collaborator3.getName()).booleanValue());
	}
	
	@Test
	public void userCanWriteToSharedSpaceTest() throws InternalException {
		boolean canWrite = false, canRead = false;
		
		try {
			canWrite = CloudStorageUtils.userCanWriteToSharedSpace(collaborator2, sharedSpaces[3]);
			canRead = CloudStorageUtils.userCanReadSharedSpace(collaborator2, sharedSpaces[3]);		
		} catch (SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(canRead && canWrite);
	}
	
	@Test
	public void userCanReadSharedSpaceTest() throws InternalException {
		boolean canWrite = false, canRead = false;
		
		try {
			canWrite = CloudStorageUtils.userCanWriteToSharedSpace(collaborator3, sharedSpaces[3]);
			canRead = CloudStorageUtils.userCanReadSharedSpace(collaborator3, sharedSpaces[3]);		
		} catch (SharedSpaceDoesNotExistException e) {
			//empty
		}
		
		assertTrue(canRead && !canWrite);
	}
	
	@Test
	public void getAccessibleSharedSpacesTest() throws InternalException {
		ArrayList<ImmutablePair<SharedSpace, Boolean>> accessibleSharedSpaces = null;
		
		accessibleSharedSpaces = CloudStorageUtils.getAccessibleSharedSpaces(collaborator3);
		
		assertEquals(2, accessibleSharedSpaces.size());
		
		for(ImmutablePair<SharedSpace, Boolean> pair : accessibleSharedSpaces) {
			if(pair.left.equals(sharedSpaces[2])) {
				assertTrue(pair.right.booleanValue());
			}
			else if(pair.left.equals(sharedSpaces[3])) {
				assertFalse(pair.right.booleanValue());
			}
			else {
				fail("found wrong shared space");
			}
		}
	}
	
	@Test(expected=SharedSpaceAlreadyExistsException.class)
	public void SharedSpaceAlreadyExistsExeptionTest() throws SharedSpaceAlreadyExistsException, InternalException {
		CloudStorageUtils.createSharedSpace(sharedSpaces[NUM_SP - 1].getName(), owner);
	}
	
	@Test(expected=SharedSpaceDoesNotExistException.class)
	public void SharedSpaceDoesNotExistExceptionTest() throws SharedSpaceDoesNotExistException, InternalException {
		SharedSpace sharedSpace = new SharedSpace("another_shared_space", owner);
		CloudStorageUtils.deleteSharedSpace(sharedSpace);
	}
}
