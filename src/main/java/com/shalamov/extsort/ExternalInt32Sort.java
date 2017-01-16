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
            System.out.println(checkFileTobeSorted(new File(file)) ? "OK" : "FAILURE!");

        } else if (mode.equals("-s")) {
            Sort sort = new Sort();
            try {
                System.gc();
                sort.sort(new File(file), new File(file + ".sorted"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(mode.equals("-t")){
            // test mode:
            Sort sort = new Sort();
            try {
                System.out.println("4Mb warmup:");
                String f = "file.4m.bin";
                System.gc();
                sort.sort(new File(f), new File(f + ".sorted"));
                System.out.println("real tests:");
                f = "file.40m.bin";
                System.gc();
                sort.sort(new File(f), new File(f + ".sorted"));
                f = "file.400m.bin";
                System.gc();
                sort.sort(new File(f), new File(f + ".sorted"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean checkFileTobeSorted(File file) {
        try {
            BinaryFileBufferOfInts bufferOfInts = new BinaryFileBufferOfInts(file);
            int prev = Integer.MIN_VALUE;
            int cur;

            while (!bufferOfInts.isEmpty()) {
                cur = bufferOfInts.pop();
                if (prev > cur) {
                    return false;
                }
                prev = cur;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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
