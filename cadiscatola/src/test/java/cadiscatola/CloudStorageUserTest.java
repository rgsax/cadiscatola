package cadiscatola;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cadiscatola.api.model.User;
import com.cadiscatola.api.utils.CloudStorageUtils;
import com.cadiscatola.api.utils.exceptions.PasswordMismatchException;
import com.cadiscatola.api.utils.exceptions.UserAlreadyExistsException;
import com.cadiscatola.api.utils.exceptions.UserDoesNotExistException;
import com.cadiscatola.api.wrapper.exceptions.InternalException;

public class CloudStorageUserTest {
	private static int NUM_USERS = 4;
	private static User[] users = new User[NUM_USERS];
	
	@BeforeClass
	public static void createTestUsers() throws UserAlreadyExistsException, InternalException {
		for(int i = 0 ; i < NUM_USERS ; ++i)
			users[i] = CloudStorageUtils.createUser("utente" + i + "_test", "password" + i);
	}
	
	@AfterClass
	public static void deleteOtherTestUsers() throws UserDoesNotExistException, InternalException {
		for(int i = 0 ; i < NUM_USERS ; ++i)
			if(users[i] != null)
				CloudStorageUtils.deleteUser(users[i]);
	}
	
	@Test
	public void createUserTest() throws UserAlreadyExistsException, InternalException, UserDoesNotExistException {
		User user = null;
		user = CloudStorageUtils.createUser("new_user_test", "password");
		assertNotNull(user);
		CloudStorageUtils.deleteUser(user);
	}
	
	@Test
	public void deleteUserTest() throws InternalException {
		boolean status = false;
		try {
			status = CloudStorageUtils.deleteUser(users[0]);
			users[0] = null;
		} catch (UserDoesNotExistException e) {
			//empty
		}
		assertTrue(status);
	}
	
	@Test
	public void getUserTest() throws InternalException {
		User user = null;
		
		try {
			user = CloudStorageUtils.getUser(users[2].getName(), users[2].getPassword());
		} catch (PasswordMismatchException | UserDoesNotExistException e) {
			//empty
		}
		
		assertNotNull(user);
		
	}
	
	@Test(expected=UserAlreadyExistsException.class)
	public void UserAlreadyExistsExceptionTest() throws UserAlreadyExistsException, InternalException {
		CloudStorageUtils.createUser(users[NUM_USERS - 1].getName(), "password");
	}
	
	@Test(expected=UserDoesNotExistException.class)
	public void UserDoesNotExistExceptionTest() throws UserDoesNotExistException, InternalException {
		User user = new User("random_user_test", null);
		CloudStorageUtils.deleteUser(user);
	}
	
	@Test(expected=PasswordMismatchException.class)
	public void PasswordMismatchExceptionTest() throws UserDoesNotExistException, PasswordMismatchException, InternalException {
		users[1].setPassword("wrong_password");
		CloudStorageUtils.modifyPassword(users[1], "another_password");
	}

}
