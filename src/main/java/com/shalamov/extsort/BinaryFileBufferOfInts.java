package com.shalamov.extsort;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * This class wraps a file reader to cache the last object read.
 * Only 4-byte integers are supported for now.
 * <p>
 * Main mithods peek, pop, and popBatch provide integers read from wrapped file.
 * <p>
 * isEmpty method
 */
public class BinaryFileBufferOfInts implements Closeable {

    private static int BUFFERSIZE = 4 * 1024;
    private BufferedReader bufferedReader;
    File originalFile;
    private Integer cache = null;

    private ByteBuffer byteBuffer = null;
    private int availableInts = 0;

    public BinaryFileBufferOfInts(File file) throws IOException {
        originalFile = file;
        bufferedReader = new BufferedReader(new FileReader(file), BUFFERSIZE);
        refresh();
    }

    /**
     * Buffer is empty, iff there are byteByffer is set to null, which happens iff
     * there are no bytes in current byteBuffer and there are no bytes to read from file
     *
     * @return
     */
    public boolean isEmpty() {
        return byteBuffer == null;
    }

    public Integer peek() {
        if (cache == null)
            throw new IllegalStateException("Truing to peek from completely empty buffer");
        return cache;
    }

    /**
     * Returns cashed value and pops it out. Next peek() and pop() calls will provide another element.
     * If there are no other elements available, but the cached one, invalidates cache.
     * @return cached value.
     * @throws IOException
     */
    public Integer pop() throws IOException {
        Integer answer = peek();
        refresh();
        return answer;
    }

    /**
     * Maintains byteBuffer in consistent state.
     * Throws cached value out and replaces ot with next available int in from the buffer.
     * If there are no available ints to be popped from byteBuffer,
     * this method invalidates cache and byteBuffer.
     */
    private void refresh() throws IOException {
        if (availableInts != 0) {
            cache = byteBuffer.getInt();
            --availableInts;
            return;
        }

        // invalidate cache:
        byteBuffer = null;
        cache = null;

        // read something:
        byte[] bytes = new byte[BUFFERSIZE];
        int read = -1; // bufferedReader.read; // TODO read these bytes
        if (read > 0) {
            // update cache and buffer:
            byteBuffer = ByteBuffer.wrap(bytes);
            availableInts = read / 4;
            cache = byteBuffer.getInt();
        }
    }

    /**
     * This method provides a batch of ints read from file.
     * Provided ints are permanently popped from buffer.
     *
     * @param amount the maximal amount of read integers.
     * @return A list of integers of size {@code amount} or less;
     * @throws IOException
     */
    public List<Integer> popBatch(int amount) throws IOException {
        List<Integer> integers = new ArrayList<Integer>();
        while (!isEmpty() && amount > 0) {
            integers.add(pop());
            --amount;
        }
        return integers;
    }


    public void clear() throws IOException {
        originalFile.deleteOnExit();
    }

    public void close() throws IOException {
        bufferedReader.close();
    }
}
