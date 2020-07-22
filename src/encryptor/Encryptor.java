package encryptor;

import java.nio.charset.Charset;
import java.util.Scanner;
import model.AESCryptor;
import model.Sbox;
import util.Util;

/**
 *
 * @author kashwaa
 */
public class Encryptor {

    public static void main(String[] args) {
//        testSample();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your message: ");
        String message = scanner.nextLine();

        System.out.print("Enter your password: ");
        String pass = scanner.nextLine();

        AESCryptor cryptor = new AESCryptor();
        String cipher = cryptor.encryptECB(message, pass);
        System.out.println("Encrypted message:");
        System.out.println(cipher);

        System.out.println("decrypted message: ");
        System.out.println(cryptor.decryptECB(cipher, pass));

    }


    private static void testSample() {
        byte[] key = {
            (byte) 0x2b, (byte) 0x7e, (byte) 0x15, (byte) 0x16,
            (byte) 0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6,
            (byte) 0xab, (byte) 0xf7, (byte) 0x15, (byte) 0x88,
            (byte) 0x09, (byte) 0xcf, (byte) 0x4f, (byte) 0x3c
        };

//      0x2b, 0x28, 0xab, 0x09,
//      0x7e, 0xae, 0xf7, 0xcf,
//      0x15, 0xd2, 0x15, 0x4f,
//      0x16, 0xa6, 0x88, 0x3c
        byte[] sampleBlock = {
            (byte) 0x32, (byte) 0x43, (byte) 0xf6, (byte) 0xa8,
            (byte) 0x88, (byte) 0x5a, (byte) 0x30, (byte) 0x8d,
            (byte) 0x31, (byte) 0x31, (byte) 0x98, (byte) 0xa2,
            (byte) 0xe0, (byte) 0x37, (byte) 0x07, (byte) 0x34
        };

//      0x32, 0x88, 0x31, 0xe0,
//      0x43, 0x5a, 0x31, 0x37,
//      0xf6, 0x30, 0x98, 0x07,
//      0xa8, 0x8d, 0xa2, 0x34
        byte[] sampleState = Util.getState(sampleBlock);
        byte[] initKey = Util.getState(key);

        AESCryptor cryptor = new AESCryptor();
        byte[] resultCipher = cryptor.encryptState(sampleState, initKey);
        Util.formatMatrix(resultCipher, "Cipher");
    }

}


//"9a 05 26 ed f6 f1 9d 55 a2 7d 02 87 32 3c 2d 73 28 b9 9c 2a 7f 6d ae 49 6d 0c 87 04 51 56 77 4c"
