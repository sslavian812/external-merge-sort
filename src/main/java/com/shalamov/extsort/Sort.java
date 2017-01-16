package com.shalamov.extsort;

import java.io.*;
import java.util.*;

/**
 * Created by viacheslav on 2017-01-16.
 */
public class Sort {
    private static final int TEMP_FILES_LIMIT = 1024;
    private static final long DEFAULT_BLOCK_SIZE = 4 * 1024;


    /**
     * Sorts the content of proveded {@code input} file and stores sorted data to {@code output} file.
     * The only publicly exposed method.
     *
     * @param input
     * @param output
     * @throws IOException
     */
    public void sort(File input, File output) throws IOException {
        long start = System.currentTimeMillis();
        List<File> list = splitToSortedParts(input);
        mergeKWay(list, output);
        System.out.println((System.currentTimeMillis()-start) /1000 + "s" );
    }

    /**
     * This method reads one big file block-by-block,
     * maintains a limited set of sorted temporary files.
     * If file is not finished, and set size exceeds the limit,
     * merges the whole set into one sorted file and continues (with one temporary file)
     * to read input file further.
     * Should be called only once.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private List<File> splitToSortedParts(File file) throws IOException {

        List<File> inputFiles = new ArrayList<>();

        List<File> sortedFiles = new ArrayList<>();

        long blockSize = DEFAULT_BLOCK_SIZE;


        try (BinaryFileBufferOfInts fbr = new BinaryFileBufferOfInts(file)) {
            List<Integer> elements = new ArrayList<>();

            while (!fbr.isEmpty()) {
                // read while block is not filled
                long currentBlockSize = 0;
                while ((currentBlockSize < blockSize) && !fbr.isEmpty()) {
                    elements.add(fbr.pop());
                    currentBlockSize += 4;
                }

                if (inputFiles.size() > TEMP_FILES_LIMIT) {
                    if (sortedFiles.size() > TEMP_FILES_LIMIT) {
                        File mergedFile = merge(sortedFiles);
                        sortedFiles = Arrays.asList(mergedFile);

                    }
                    inputFiles.add(internalSortAndSaveToFile(elements));
                    sortedFiles.add(merge(inputFiles));
                    inputFiles.clear();
                } else {
                    // sort read block (a set of strings), save to file
                    // and add link to this file to input files set.
                    inputFiles.add(internalSortAndSaveToFile(elements));
                }
                elements.clear();
            }

            if (elements.size() > 0) {
                inputFiles.add(internalSortAndSaveToFile(elements));
                elements.clear();
            }

        }

        inputFiles.addAll(sortedFiles);
        return inputFiles;
    }


    /**
     * This method merges given sorted files into one and returns it.
     * @param files
     * @return
     * @throws IOException
     */
    private File merge(List<File> files) throws IOException {
        File comdinedFile = File.createTempFile("mergeTmp", "flatfile");
        comdinedFile.deleteOnExit();
        mergeKWay(files, comdinedFile);
        return comdinedFile;
    }

    /**
     * Internal sorting.
     * Sorts given list of strings (ints) by comparator,
     * Creates new file (overwrites the old one) and
     * writes this sorted list to file.
     *
     * @param unsortedList
     * @return Newly created (overwritten file with stored sorted list of strings).
     * @throws IOException
     */
    private File internalSortAndSaveToFile(List<Integer> unsortedList) throws IOException {

        File file = File.createTempFile("sortTmp", "flatfile");
        file.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        Collections.sort(unsortedList);

        byte[] bytes = Utils.toByteArray(unsortedList);
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        return file;
    }


    /**
     * The most important part: K-way merging of files onto one file.
     *
     * @param files
     * @param outputfile
     * @return
     * @throws IOException
     */
    private void mergeKWay(List<File> files, File outputfile) throws IOException {
        PriorityQueue<BinaryFileBufferOfInts> pq = new PriorityQueue<>(
                Comparator.comparingInt(BinaryFileBufferOfInts::peek));


        for (File f : files) {
            BinaryFileBufferOfInts bfb = new BinaryFileBufferOfInts(f);
            pq.add(bfb);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputfile)) {
            while (pq.size() > 0) {
                BinaryFileBufferOfInts bfb = pq.poll();
                Integer next = bfb.pop();
                fileOutputStream.write(Utils.toBytes(next));

                if (bfb.isEmpty()) {
                    bfb.close();
                    bfb.clear();
                } else {
                    pq.add(bfb);
                }
            }
        } finally {
            for (BinaryFileBufferOfInts bfb : pq) {
                bfb.clear();
            }
        }
    }
}
