package com.shalamov.extsort;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by viacheslav on 2017-01-14.
 */
public class Utils {


    /**
     * Returns <code>intsNum</code> integers in an array.
     *
     * @param bytes
     * @return
     */
    public static int[] toIntArray(byte[] bytes, int intsNum) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);

//        if the file uses little endian as apposed to network
//        (big endian, Java's native) format,
//        then set the byte order of the ByteBuffer
//            if(use_little_endian)
//                bb.order(ByteOrder.LITTLE_ENDIAN);

        int[] ints = new int[intsNum];
        for (int i = 0; i < intsNum; i++) {
            ints[i] = bb.getInt();
        }

        return ints;
    }


    public static byte[] toByteArray(int[] ints) {
        byte[] bytes = new byte[ints.length*4];
        int j = 0;
        for (int t : ints) {
            bytes[j++] = (byte) (t >> 24);
            bytes[j++] = (byte) (t >> 16);
            bytes[j++] = (byte) (t >> 8);
            bytes[j++] = (byte) t;
        }
        return bytes;
    }

    public static void writeIncreasingBytes(String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[256];
            for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE ; i++) {
                buffer[(i-Byte.MIN_VALUE)] = i;
            }
            buffer[255] = Byte.MAX_VALUE;

            fileOutputStream.write(buffer);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] shuffle(int[] ints){
        Random r = new Random();
        int t;
        for (int i = 0; i < ints.length; i++) {
            int j =(int) (Math.random()* (double)ints.length);
            t = ints[i];
            ints[i] = ints[j];
            ints[j] = t;
        }
        return ints;
    }

    public static void generateInts(String fileName, int intsNum) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            int[] ints = new int[intsNum];
            for (int i = 0; i < intsNum; i++) {
                ints[i]=i;
            }
            byte[] buffer = toByteArray(ints);

            fileOutputStream.write(buffer);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateShuffledInts(String fileName, int intsNum) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            int[] ints = new int[intsNum];
            for (int i = 0; i < intsNum; i++) {
                ints[i]=i;
            }
            shuffle(ints);
            byte[] buffer = toByteArray(ints);

            fileOutputStream.write(buffer);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void generateRandomFile(String fileName, int blocks) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[ExternalInt32Sort.BLOCK_SIZE];
            Random r = new Random();
            for (int i = 0; i < blocks; i++) {
                r.nextBytes(buffer);
                fileOutputStream.write(buffer);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
