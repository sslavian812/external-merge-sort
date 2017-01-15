package com.shalamov.extsort;

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

    public static final int BLOCK_SIZE = 4 * 16;
    public static final double GENERATE_BLOCKS = 8;

    public static void main(String[] args) {
        String mode = args[0];
        String fileName = args[1];


        if (mode.equals("-gr")) {
            Utils.generateRandomFile(fileName, (int) GENERATE_BLOCKS);
        } else if (mode.equals("-gb")) {
            Utils.writeIncreasingBytes(fileName);
        } else if (mode.equals("-gint")) {
            Utils.generateInts(fileName, (int) (BLOCK_SIZE / 4 * GENERATE_BLOCKS));
        } else if (mode.equals("-gintsh")) {
            Utils.generateShuffledInts(fileName, (int) (BLOCK_SIZE / 4 * GENERATE_BLOCKS));
        } else if (mode.equals("-s")) {
            int totalBytes = internalBlocksSort(fileName);
            int totalInts = totalBytes / 4;

            int size = totalBytes / BLOCK_SIZE;
            String inFile = fileName + ".tmp";
            String outFile = fileName + ".tmp.dest";

            while (size * 2 <= totalBytes) {
                externalMergeSort(inFile, outFile, size, totalBytes);
                renameFile(outFile, inFile);
                size *= 2;
            }


            // TODO carefully merge the last part of file.
        }
    }

    private static void renameFile(String currentName, String newName) {
        File file = new File(currentName);
        File nFile = new File(newName);
        if (!file.renameTo(nFile)) {
            throw new IllegalStateException("Unable to rename file");
        }
    }


    /**
     * This method merges all subsequent pairs of blocks of size {@code size} from {@code srcFile}
     * into blocks of size {@code 2*size} and writes down to {@code dstFile}
     *
     * @param srcFile
     * @param dstFile
     * @param size
     */
    private static void externalMergeSort(String srcFile, String dstFile, int size, int maxFileSize) {
        try {
            FileInputStream fileInputStream = new FileInputStream(srcFile);
            FileOutputStream fileOutputStream = new FileOutputStream(dstFile);

            FileChannel channelA = fileInputStream.getChannel();
            FileChannel channelB = fileInputStream.getChannel();


//            TODO: read and merge
//            byte[] buffer = new byte[BLOCK_SIZE];
//            int read;
//            while ((read = fileInputStream.read(buffer)) != -1) {
//                int[] ints = toIntArray(buffer, read / 4);
//                Arrays.sort(ints);
//                byte[] sorted = toByteArray(ints);
//                fileOutputStream.write(sorted);
//            }


            fileOutputStream.flush();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int internalBlocksSort(String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(fileName + ".tmp");
            byte[] buffer = new byte[BLOCK_SIZE];

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
