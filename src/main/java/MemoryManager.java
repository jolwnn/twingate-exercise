import java.util.ArrayList;
import java.util.List;

/**
 * MemoryManager is a virtual memory manager that takes a large contiguous block of memory and manages allocations and deallocations on it.
 */
public class MemoryManager {
    private byte[] buffer;
    private final List<Block> blocks;
    private int freeMemory; // Total free memory available

    /**
     * Constructs a MemoryManager to manage a given memory buffer.
     *
     * @param buffer   the underlying byte array representing the memory
     * @param numBytes size of the buffer
     * @throws IllegalArgumentException if numBytes exceeds buffer length
     */
    public MemoryManager(byte[] buffer, int numBytes) {
        if (numBytes > buffer.length) {
            throw new IllegalArgumentException("numBytes cannot exceed buffer length");
        }
        this.buffer = buffer;
        this.freeMemory = numBytes;
        blocks = new ArrayList<>();
        blocks.add(new Block(0, numBytes, true)); // Initially, consider entire memory as one free block
    }

    /**
     * Allocates memory of the specified size.
     * This method supports non-contiguous allocation by combining segments from free blocks.
     * If the total free memory is sufficient (even if fragmented), the allocation succeeds.
     *
     * @param size the number of bytes to allocate
     * @return a MemorySequence containing one or more segments if allocation succeeds;
     *         null if there is not enough free memory
     * @throws IllegalArgumentException if size is not positive
     */
    public synchronized MemorySequence alloc(int size) { // Synchronized to provide thread-safety
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        // Check if overall free memory is sufficient.
        if (freeMemory < size) {
            return null;
        }
        
        int remaining = size;
        List<Segment> segments = new ArrayList<>();

        // Iterate over blocks to allocate segments until the requested size is met.
        for (int i = 0; i < blocks.size() && remaining > 0; i++) {
            Block block = blocks.get(i);
            if (!block.free) {
                continue;
            }
            if (block.size <= remaining) {
                // Use the entire block.
                segments.add(new Segment(block.start, block.size));
                remaining -= block.size;
                block.free = false;
                freeMemory -= block.size; // Update free memory.
            } else {
                // Use first part of the block and split it.
                segments.add(new Segment(block.start, remaining));
                int allocStart = block.start;
                int allocSize = remaining;
                int leftover = block.size - allocSize;
                Block allocatedBlock = new Block(allocStart, allocSize, false);
                Block freeBlock = new Block(allocStart + allocSize, leftover, true);
                blocks.set(i, allocatedBlock);
                blocks.add(i + 1, freeBlock);
                freeMemory -= remaining; // Update free memory.
                remaining = 0;
            }
        }
        return new MemorySequence(segments);
    }

    /**
     * Frees the memory associated with the provided MemorySequence.
     * Each segment within the sequence is freed, and adjacent free blocks are merged to reduce fragmentation.
     *
     * @param sequence the MemorySequence to free
     * @throws IllegalArgumentException if the sequence is null or invalid
     */
    public synchronized void free(MemorySequence sequence) {
        if (sequence == null || sequence.getSegments() == null) {
            throw new IllegalArgumentException("Invalid memory sequence provided for free.");
        }
        for (Segment seg : sequence.getSegments()) {
            freeSegment(seg);
        }
    }

    /**
     * Helper method that frees a segment by marking its corresponding block as free and merging adjacent free blocks.
     *
     * @param seg the segment to free
     * @throws IllegalArgumentException if the segment is not found in the memory blocks
     */
    private void freeSegment(Segment seg) {
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            if (!block.free && block.start == seg.start && block.size == seg.size) {
                block.free = true;
                freeMemory += block.size; // Update free memory.
                // Merge with previous block if free.
                if (i > 0) {
                    Block prev = blocks.get(i - 1);
                    if (prev.free) {
                        prev.size += block.size;
                        blocks.remove(i);
                        i--;
                        block = prev;
                    }
                }
                // Merge with next block if free.
                if (i < blocks.size() - 1) {
                    Block next = blocks.get(i + 1);
                    if (next.free) {
                        block.size += next.size;
                        blocks.remove(i + 1);
                    }
                }
                return;
            }
        }
        throw new IllegalArgumentException("Segment not found in memory blocks");
    }

    public synchronized void printBlocks() {
        System.out.println("\nCurrent memory blocks:");
        for (Block block : blocks) {
            System.out.printf("Start: %d, Size: %d, %s%n",
                    block.start, block.size, block.free ? "free" : "allocated");
        }
        System.out.println("Total free memory: " + freeMemory);
    }

    /**
     * Block represents a contiguous region of the memory buffer for internal management.
     */
    private static class Block {
        int start;
        int size;
        boolean free;

        /**
         * Constructs a Block with the specified start, size, and free status.
         *
         * @param start the starting index of the block
         * @param size  the size of the block in bytes
         * @param free  true if block is free, false if allocated
         */
        Block(int start, int size, boolean free) {
            this.start = start;
            this.size = size;
            this.free = free;
        }
    }

    /**
     * Segment represents a contiguous allocated portion of memory that will be returned.
     */
    private static class Segment {
        int start;
        int size;

        /**
         * Constructs a Segment with the specified start and size.
         *
         * @param start the starting index of the segment
         * @param size  the size of the segment in bytes
         */
        Segment(int start, int size) {
            this.start = start;
            this.size = size;
        }

        @Override
        public String toString() {
            return "Segment{start=" + start + ", size=" + size + "}";
        }
    }

    /**
     * MemorySequence contains one or more allocated segments and is used for non-contiguous memory allocation.
     */
    public static class MemorySequence {
        private final List<Segment> segments;

        /**
         * Constructs a MemorySequence with the specified list of segments.
         *
         * @param segments the list of segments allocated for this sequence
         */
        public MemorySequence(List<Segment> segments) {
            this.segments = segments;
        }

        /**
         * Returns the list of segments contained in this MemorySequence.
         *
         * @return a list of segments
         */
        public List<Segment> getSegments() {
            return segments;
        }

        @Override
        public String toString() {
            return "MemorySequence{" + "segments=" + segments + '}';
        }
    }
}

