package za.co.psybergate.application.core.utility;

import za.co.psybergate.application.core.exception.ApplicationException;

public interface EncryptionUtilities {

    String encryptUsingSHA256(String secret, String body) throws ApplicationException;

    boolean isIdentical(String a, String b);

}
