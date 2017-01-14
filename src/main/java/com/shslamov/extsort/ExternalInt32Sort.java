package com.shslamov.extsort;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

import static com.shslamov.extsort.Utils.toByteArray;
import static com.shslamov.extsort.Utils.toIntArray;


/**
 * This is a prototype of external sort algorithm.
 * This should sort only 32bit integers got from file and use 2-way merge.
 * Created by viacheslav on 2017-01-14.
 */
public class ExternalInt32Sort {

    public static final int BLOCK_SIZE = 4 * 1024;
    public static final int GENERATE_BLOCKS = 1;

    public static void main(String[] args) {
        String mode = args[0];
        String fileName = args[1];


        if(mode.equals("-gr")){
            generateRandomFile(fileName, GENERATE_BLOCKS);
        } else if(mode.equals("-gb")){
            Utils.writeIncreasingBytes(fileName);
        } else if(mode.equals("-gint")){
            Utils.generateInts(fileName, BLOCK_SIZE/4);
        } else if(mode.equals("-gintsh")){
            Utils.generateShuffledInts(fileName, BLOCK_SIZE/4);
        } else if(mode.equals("-s")){
            internalBlocksSort(fileName);
        } else if(mode.equals("-t")){
            return;
        }
    }

    private static void generateRandomFile(String fileName, int blocks) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            byte[] buffer = new byte[BLOCK_SIZE];
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

    private static void internalBlocksSort(String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(fileName + ".tmp");
            byte[] buffer = new byte[BLOCK_SIZE];



            int offset = 0;
            int read;
            while ((read = fileInputStream.read(buffer, offset, BLOCK_SIZE)) != -1) {
                if(read %4 != 0) {
                    System.err.println("Fucked up! read " + read + "bytes");
                }

                int[] ints = toIntArray(buffer, read / 4);
                Arrays.sort(ints);
                byte[] sorted = toByteArray(ints);
//
//                Arrays.sort(buffer);
//                byte[] sorted = buffer;

                if(sorted.length != read) {
                    System.err.println("Fucked up! read-write size does not match!: " + read + " vs " + sorted.length);
                }
                fileOutputStream.write(sorted);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
