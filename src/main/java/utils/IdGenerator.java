package utils;

import java.security.SecureRandom;

/**
 * Created by prashanth on 28/1/15.
 */
public class IdGenerator {

    /*
        Avoid create multiple copies of SecureRandom as it can be expensive
     */
    private static SecureRandom random = new SecureRandom();

    public static long generateFileId(){

        long LOWER_RANGE = 1000000000000000L; //assign lower range value
        long UPPER_RANGE = 9999999999999999L; //assign upper range value

        return LOWER_RANGE +
                (long)(random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
    }
}
