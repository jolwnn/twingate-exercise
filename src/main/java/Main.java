/**
 * Main class to test the MemoryManager implementation.
 */
public class Main {
    public static void main(String[] args) {
        // Create a buffer of 5 bytes.
        byte[] buffer = new byte[5];
        MemoryManager mm = new MemoryManager(buffer, buffer.length);

        // Allocate 5 blocks of 1 byte each.
        MemoryManager.MemorySequence[] sequences = new MemoryManager.MemorySequence[5];
        for (int i = 0; i < 5; i++) {
            sequences[i] = mm.alloc(1);
            if (sequences[i] == null) {
                System.out.println("Allocation failed at index " + i);
            } else {
                System.out.println("Allocated block " + i + ": " + sequences[i]);
            }
        }
        mm.printBlocks();

        // Free the 2nd and 4th blocks (indexes 1 and 3).
        mm.free(sequences[1]);
        mm.free(sequences[3]);
        System.out.println("\nAfter freeing blocks 1 and 3:");
        mm.printBlocks();

        // Attempt to allocate a block of 2 bytes.
        // Even though free memory is fragmented (one byte at index 1 and one at index 3),
        // the manager will allocate them as two segments in the MemorySequence.
        MemoryManager.MemorySequence sequence2 = mm.alloc(2);
        if (sequence2 == null) {
            System.out.println("Allocation of 2 bytes failed due to insufficient free memory.");
        } else {
            System.out.println("\nAllocated 2 bytes across non-contiguous segments: " + sequence2);
        }
        mm.printBlocks();
    }
}
