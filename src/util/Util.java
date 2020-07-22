
package util;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author kashwaa
 */
public class Util {
    
    /**
     * Converts the plain-text provided into byte-blocks of equal size determined
     * by the second parameter.
     * @param plainText The plain text to be converted.
     * @param blockSize This desired block size
     * @return Array of blocks, each is a byte array of the defined blockSize
     */
    public static byte[][] createByteBlocks(String plainText, int blockSize){
        byte[] original = plainText.getBytes(Charset.forName("UTF-8"));
        
        int length = original.length;
        int numBlocks = original.length / blockSize;
        int remainingBytes = original.length % blockSize;
        int addedBytes = blockSize - remainingBytes;
        
        if (remainingBytes > 0) {
            numBlocks++;
            
            length += addedBytes;
            
            original = Arrays.copyOf(original, length);
        }
        
        byte[][] blocks = new byte[numBlocks][blockSize];

        for (int i = 0; i < numBlocks; i++) {
            blocks[i] = Arrays.copyOfRange(original, i * blockSize, (i + 1) * blockSize);
        }
        
        return blocks;
    }
    
    /**
     * Converts a hexadecimal String representation of a byte array into 
     * separate byte arrays of the same size.
     * @param hexString the hexadecimal string representation of the bytes.
     * @param blockSize the size of each individual array.
     * @return an array of blocks, each is a byte array of the defined blockSize.
     */
    public static byte[][] hexStringToByteArrays(String hexString, int blockSize){
        int size = blockSize * 2; 
        int numBytes = hexString.length();
        int numBlocks = numBytes/size;
        
        byte[][] blocks = new byte[numBlocks][size];
        for (int i = 0; i < numBlocks; i++) {
            blocks[i] = hexStringToByteArray(hexString.substring(i * size, 
                    (i+1) * size));
        }
        
        return blocks;
    }
    
    public static byte[] hexStringToByteArray(String hexString){
        int length = hexString.length();
        byte[] data = new byte[length/2];
        for (int i = 0; i < length; i+=2) {
            data[i/2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * Transforms the given 16 byte block into a State by turning the columns of
     * the 4x4 matrix into row and vice-versa.
     * It can also be used to do the reverse.
     * @param block a 16 bytes length array to be transformed.
     * @return a State as required by the AES specifications.
     * 
     * NOTE: This version is designed to deal with block of size 16 bytes only.
     * Implementation should be extended to deal with larger blocks later.
     */
    public static byte[] getState(byte[] block){
        byte[] state = new byte[block.length];
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                state[i * 4 + j] = block[j * 4 + i];
            }
        }
        return state;
    }
    
    /**
     * Divide the state into separate columns, each consists of 4 bytes. it deals
     * with any state size and can be used with blocks larger than 16 bytes.
     * @param state The byte array to be divided.
     * @return An array of columns, each is an array of four bytes.
     */
    public static byte[][] getColumns(byte[] state) {
        final int columnLenght = 4;
        int rowLenght = state.length / columnLenght;
        byte[][] result = new byte[rowLenght][columnLenght];
        for (int i = 0; i < rowLenght; i++) {
            byte[] column = new byte[columnLenght];
            for (int j = 0; j < columnLenght; j++) {
                column[j] = state[j * rowLenght + i];
            }
            result[i] = column;
        }
        return result;
    }
    
    /**
     * Merges four columns, each contains 4 bytes into a single array of 16 bytes.
     * @param columns An array of columns, each is an array of 4 bytes.
     * @return A 16 byte array of the merged columns.
     */
    public static byte[] mergeColumns(byte[][] columns) {
        byte[] result = new byte[columns.length * 4];
        int k = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[k] = columns[j][i];
                k++;
            }
        }
        return result;
    }
    
    
    public static byte ByteModulo(byte a, byte b){
        
        byte aa = a, bb = b, result = 0, t;
        
        while(aa != 0){
            //check if the LSB of aa is 0
            if ((aa & 1) !=0) {//LSB of aa is 1
                result = (byte) (result ^ bb); 
            }
            t = (byte) (bb & 0x80); //get the MSB of bb.
            bb = (byte) (bb << 1); //shift bb to the left by 1.
            if(t != 0){ //the removed bit of bb was set. 
                bb = (byte) (bb ^ 0x1b); //apply the mod operation round with 0x1b;
            }
            aa = (byte) ((aa & 0xff) >> 1);//reduce a by 1 shift to the right.
        }
        
        return result;
    }

    public static void formatMatrix(byte[] matrix, String header) {
        System.out.println("---" + header + "---");
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(String.format("%02X", matrix[i] & 255) + "   ");
            if ((i + 1) % 4 == 0 && i != 0) {
                System.out.println("");
            }
        }
    }

}
