package model;

import java.nio.charset.Charset;
import util.Util;

/**
 *
 * @author kashwaa
 */
public class AESCryptor {

    public String encryptECB(String message, String key) {
        String cipher = "";
        byte[][] byteBlocks = Util.createByteBlocks(message, 16);
        byte[] initKey = Util.createByteBlocks(key, 16)[0];
        initKey = Util.getState(initKey);

        byte[][] ciphers = new byte[byteBlocks.length][16];
        for (int i = 0; i < byteBlocks.length; i++) {
            byte[] state = Util.getState(byteBlocks[i]);
            ciphers[i] = Util.getState(encryptState(state, initKey));
            cipher += printCipher(ciphers[i]);
        }

        return cipher;
    }

    private String printCipher(byte[] cipher) {
        String result = "";
        for (byte b : cipher) {
            result += String.format("%02x", b & 0xFF);
        }
        return result;
    }

    public String decryptECB(String cipher, String key) {
        String result = "";
//        byte[][] ciphers = Util.createByteBlocks(cipher, 16);
        byte[][] ciphers = Util.hexStringToByteArrays(cipher, 16);
        
        byte[] initKey = Util.getState(Util.createByteBlocks(key, 16)[0]);
        for (int i = 0; i < ciphers.length; i++) {
            byte[] cipherState = Util.getState(ciphers[i]);
            ciphers[i] = Util.getState(decryptState(cipherState, initKey));
            result += new String(ciphers[i], Charset.forName("UTF-8"));
        }
        return result;
    }

    public byte[] encryptState(byte[] state, byte[] key) {

        byte[][] keys = scheduleKeys(key);

        //initial round (add initial round key)
        state = addRoundKey(state, key);

        //main 9 rounds:
        for (int i = 1; i < 10; i++) {
            subBytes(state);
            shiftRows(state);
            state = mixColumns(state);
            state = addRoundKey(state, keys[i]);
        }

        //final round (without mixing columns)
        subBytes(state);
        shiftRows(state);
        state = addRoundKey(state, keys[10]);

        return state;
    }

    public byte[] decryptState(byte[] state, byte[] key) {
        byte[][] keys = scheduleKeys(key);

        //last add round-key
        state = addRoundKey(state, keys[10]);

        //main 9 rounds inversed
        for (int i = 9; i > 0; i--) {
            invShiftRows(state);
            invSubBytes(state);
            state = addRoundKey(state, keys[i]);
            state = invMixColumns(state);
        }

        //final round (without mixing columns)
        invShiftRows(state);
        invSubBytes(state);
        state = addRoundKey(state, key);
        return state;
    }

    private byte[][] scheduleKeys(byte[] initialKey) {
        byte[][] keys = new byte[11][16];
        keys[0] = initialKey;

        for (int i = 1; i < 11; i++) {
            keys[i] = getNextKey(keys[i - 1], i - 1);
        }
        return keys;
    }

    public byte[] getNextKey(byte[] currentKey, int roundNum) {
        byte[][] nextKey = new byte[4][4];

        byte[][] columns = Util.getColumns(currentKey);

        nextKey[0] = rotWord(columns[3]);
        nextKey[0] = subBytes(nextKey[0]);
        nextKey[0] = addRoundKey(nextKey[0], columns[0]);

        byte[][] rcons = Util.getColumns(rcon);
        nextKey[0] = addRoundKey(nextKey[0], rcons[roundNum]);

        for (int i = 0; i < 3; i++) {
            nextKey[i + 1] = addRoundKey(nextKey[i], columns[i + 1]);
        }

        return Util.mergeColumns(nextKey);
    }

    private byte[] rotWord(byte[] col) {
        byte[] res = new byte[4];
        res[3] = col[0];
        for (int i = 0; i < 3; i++) {
            res[i] = col[i + 1];
        }
        return res;
    }

    private byte[] invRotWord(byte[] col) {
        byte[] result = new byte[4];
        result[0] = col[3];
        for (int i = 0; i < 3; i++) {
            result[i + 1] = col[i];
        }
        return result;
    }

    private byte[] addRoundKey(byte[] block, byte[] key) {
        byte[] result = new byte[block.length];
        for (int i = 0; i < block.length; i++) {
            result[i] = (byte) (block[i] ^ key[i]);
        }
        return result;
    }

    private byte[] subBytes(byte[] state) {
        for (int i = 0; i < state.length; i++) {
            state[i] = Sbox.substitute(state[i]);
        }
        return state;
    }

    private byte[] invSubBytes(byte[] state) {
        for (int i = 0; i < state.length; i++) {
            state[i] = Sbox.inverse(state[i]);
        }
        return state;
    }

    private byte[] shiftRows(byte[] state) {
        int rowLenght = state.length / 4;
        for (int i = 0; i < state.length; i += rowLenght) {
            int rowNum = i / rowLenght;
            for (int j = 0; j < rowNum; j++) {
                byte temp = state[i];
                for (int k = 0; k < rowLenght - 1; k++) {
                    state[i + k] = state[i + k + 1];
                }
                state[i + rowLenght - 1] = temp;
            }
        }
        return state;
    }

    private byte[] invShiftRows(byte[] state) {
        int rowLenght = state.length / 4;
        for (int i = 0; i < state.length; i += rowLenght) {
            int rowNum = i / rowLenght;
            for (int j = 0; j < rowNum; j++) {
                byte temp = state[i + rowLenght - 1];
                for (int k = rowLenght - 2; k >= 0; k--) {
                    state[i + k + 1] = state[i + k];
                }
                state[i] = temp;
            }
        }
        return state;
    }

    private byte[] mixColumns(byte[] state) {
        byte[][] columns = Util.getColumns(state);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = mixColumn(columns[i]);
        }
        return Util.mergeColumns(columns);
    }

    private byte[] invMixColumns(byte[] state) {
        byte[][] columns = Util.getColumns(state);
        for (int i = 0; i < columns.length; i++) {
            columns[i] = invMixColumn(columns[i]);
        }
        return Util.mergeColumns(columns);
    }

    private byte[] mixColumn(byte[] column) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i] ^= Util.ByteModulo(AESCryptor.galoisField[i][j], (byte) column[j]);
            }
        }
        return result;
    }

    private byte[] invMixColumn(byte[] column) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i] ^= Util.ByteModulo(AESCryptor.invGaloisField[i][j], column[j]);
            }
        }
        return result;
    }

    public static byte[][] galoisField = {
        {0x02, 0x03, 0x01, 0x01},
        {0x01, 0x02, 0x03, 0x01},
        {0x01, 0x01, 0x02, 0x03},
        {0x03, 0x01, 0x01, 0x02}
    };

    public static byte[][] invGaloisField = {
        {0x0E, 0x0B, 0x0D, 0x09},
        {0x09, 0x0E, 0x0B, 0x0D},
        {0x0D, 0x09, 0x0E, 0x0B},
        {0x0B, 0x0D, 0x09, 0x0E}
    };

    private static byte[] rcon = {
        0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80, 0x1b, 0x36,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};

    public byte[] getPrevKey(byte[] currentKey, int roundNum) {
        byte[][] prevKey = new byte[4][4];

        byte[][] columns = Util.getColumns(currentKey);

        for (int i = 0; i < 3; i++) {
            prevKey[i + 1] = addRoundKey(columns[i], columns[i + 1]);
        }

        byte[] tempCol3 = prevKey[3];
        tempCol3 = rotWord(tempCol3);
        tempCol3 = subBytes(tempCol3);
        tempCol3 = addRoundKey(tempCol3, columns[0]);

        byte[][] rcons = Util.getColumns(rcon);
        prevKey[0] = addRoundKey(tempCol3, rcons[roundNum]);

        return Util.mergeColumns(prevKey);
    }

}
