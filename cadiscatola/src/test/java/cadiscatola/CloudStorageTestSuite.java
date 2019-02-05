package cadiscatola;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CloudStorageSharedSpaceTest.class, CloudStorageUserTest.class })
public class CloudStorageTestSuite {

}
