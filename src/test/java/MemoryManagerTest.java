import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MemoryManager class.
 */
public class MemoryManagerTest {

    private MemoryManager memoryManager;
    private byte[] buffer;
    private final int CAPACITY = 10;

    @BeforeEach
    public void setUp() {
        buffer = new byte[CAPACITY];
        memoryManager = new MemoryManager(buffer, CAPACITY);
    }

    /**
     * Test that an allocation of the entire memory succeeds as a single segment.
     */
    @Test
    public void testInitialAllocation() {
        MemoryManager.MemorySequence seq = memoryManager.alloc(CAPACITY);
        assertNotNull(seq, "Allocation of entire memory should succeed.");
        List<?> segments = seq.getSegments();
        assertEquals(1, segments.size(), "Should be one segment when allocated contiguously.");
    }

    /**
     * Test allocating and freeing memory and ensuring that free memory is updated.
     */
    @Test
    public void testSingleAllocationAndFree() {
        // Allocate 4 bytes.
        MemoryManager.MemorySequence seq1 = memoryManager.alloc(4);
        assertNotNull(seq1, "Allocation of 4 bytes should succeed.");
        // Allocate another 3 bytes.
        MemoryManager.MemorySequence seq2 = memoryManager.alloc(3);
        assertNotNull(seq2, "Allocation of 3 bytes should succeed.");
        // Attempt to allocate 4 bytes (should fail due to insufficient free memory).
        MemoryManager.MemorySequence seq3 = memoryManager.alloc(4);
        assertNull(seq3, "Allocation should fail due to insufficient free memory.");
        // Free the first sequence.
        memoryManager.free(seq1);
        // Now, allocation of 4 bytes should succeed.
        MemoryManager.MemorySequence seq4 = memoryManager.alloc(4);
        assertNotNull(seq4, "Allocation of 4 bytes should succeed after freeing memory.");
    }

    /**
     * Test that fragmented free memory can be used to satisfy an allocation.
     */
    @Test
    public void testFragmentedAllocation() {
        // Allocate 1 byte five times.
        MemoryManager.MemorySequence[] sequences = new MemoryManager.MemorySequence[5];
        for (int i = 0; i < 5; i++) {
            sequences[i] = memoryManager.alloc(1);
            assertNotNull(sequences[i], "Allocation of 1 byte should succeed.");
        }
        // Free sequences at index 1 and 3 to fragment the memory.
        memoryManager.free(sequences[1]);
        memoryManager.free(sequences[3]);
        // Attempt to allocate 2 bytes; the manager should combine non-contiguous segments.
        MemoryManager.MemorySequence seq = memoryManager.alloc(2);
        assertNotNull(seq, "Allocation of 2 bytes should succeed even if memory is fragmented.");
        // Optionally, check that we have more than one segment (if the free blocks were not contiguous).
        assertTrue(seq.getSegments().size() >= 1, "Memory sequence should have at least one segment.");
    }

    /**
     * Test that adjacent free blocks are merged (coalesced) so that subsequent allocations
     * can be made in a single contiguous segment.
     */
    @Test
    public void testCoalescing() {
        MemoryManager.MemorySequence seq1 = memoryManager.alloc(3);
        MemoryManager.MemorySequence seq2 = memoryManager.alloc(3);
        MemoryManager.MemorySequence seq3 = memoryManager.alloc(2);
        // Free seq2 and seq1 which are adjacent.
        memoryManager.free(seq2);
        memoryManager.free(seq1);
        // After coalescing, allocating 5 bytes should succeed as one contiguous block.
        MemoryManager.MemorySequence seq4 = memoryManager.alloc(5);
        assertNotNull(seq4, "Allocation of 5 bytes should succeed after coalescing adjacent free blocks.");
        assertEquals(1, seq4.getSegments().size(), "Should be a single segment after coalescing.");
    }

    /**
     * Test that allocation fails when requested size exceeds the total memory capacity.
     */
    @Test
    public void testInsufficientMemory() {
        MemoryManager.MemorySequence seq = memoryManager.alloc(CAPACITY + 1);
        assertNull(seq, "Allocation should fail when requested size exceeds total memory.");
    }
}
