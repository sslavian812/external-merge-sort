package com.shalamov.extsort;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static com.shalamov.extsort.Utils.toByteArray;
import static com.shalamov.extsort.Utils.toIntArray;


/**
 * This is a prototype of external sort algorithm.
 * This should sort only 32bit integers got from file and use 2-way merge.
 * Created by viacheslav on 2017-01-14.
 */
public class ExternalInt32Sort {


    private static int BLOCK_SIZE = 4*1024;

    public static void main(String[] args) {
        String mode = args[0];
        String file = args[1];


        if (mode.equals("-g")) {
            //generate some files
            String numberOfInts = args[2];
            Utils.generateRandomFile(file, BLOCK_SIZE, Integer.parseInt(numberOfInts));
        } else if (mode.equals("-c")) {
            // check file to be sorted
            System.out.println(checkFileTobeSorted(file) ? "OK" : "FAILURE!");

        } else if (mode.equals("-s")) {
            Sort sort = new Sort();
            try {
                sort.sort(new File(file), new File(file + ".sorted"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static boolean checkFileTobeSorted(String file) {
        return true;
    }

    private static void renameFile(String currentName, String newName) {
        File file = new File(currentName);
        File nFile = new File(newName);
        if (!file.renameTo(nFile)) {
            throw new IllegalStateException("Unable to rename file");
        }
    }

    private static int internalBlocksSort(String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(fileName + ".tmp");
            byte[] buffer = new byte[4*1024];

            int acc = 0;

            int read;
            while ((read = fileInputStream.read(buffer)) != -1) {
                acc += read;
                if (read % 4 != 0) {
                    throw new IllegalStateException("Fucked up! read " + read + "bytes");
                }

                int[] ints = toIntArray(buffer, read / 4);
                Arrays.sort(ints);
                byte[] sorted = toByteArray(ints);

                if (sorted.length != read) {
                    throw new IllegalStateException("Fucked up! read-write size does not match!: " + read + " vs " + sorted.length);
                }
                fileOutputStream.write(sorted);
            }
            fileOutputStream.flush();
            fileInputStream.close();
            return acc;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
