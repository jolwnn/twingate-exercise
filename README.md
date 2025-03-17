# MemoryManager Documentation

MemoryManager is a virtual memory manager that takes a large contiguous block of memory and manages allocations and deallocations on it. The program is written in Java and instead of returning a single pointer, the allocation method returns a **MemorySequence** object that encapsulates one or more segments allocated from available free blocks. This approach helps the memory manager to handle fragmentation.

## Design Choices and Algorithm

### Class Design

- **MemoryManager**  
  This is the main class responsible for managing the memory buffer. It maintains a list of `Block` objects to represent the memory layout and a `freeMemory` field that tracks the total available free memory (needed to check remaining free memory when calling `alloc`). It provides methods for allocating (`alloc`) and freeing (`free`) memory. There is also a contiguous byte array, `buffer`, field appears to be redundant in my solution, but I kept it to follow the template code.

- **Block**  
  An inner class within `MemoryManager` that represents a contiguous region of the memory buffer. A `Block` stores:
  - `start`: The starting index of the block in the byte array.
  - `size`: The size (in bytes) of the block.
  - `free`: A boolean flag indicating whether the block is available for allocation.

- **Segment**  
  Another inner class within `MemoryManager` that represents a contiguous allocated portion of memory. When an allocation request is made and the free memory is fragmented, the memory is allocated as multiple segments. Each segment records its starting index and size. I decided to separate `Block` and `Segment` such that `Block` is used for internal memory management and `Segment` is more of a view object to be returned to the function caller to avoid confusion.

- **MemorySequence**  
  This class encapsulates one or more `Segment` objects and is returned by the `alloc` method. This class is needed to be able to return fragmented memory space.

- **MemoryManagerTest**  
  A comprehensive set of JUnit 5 unit tests (located under `src/test/java`) that validate various scenarios like contiguous allocation, fragmented allocation, and block coalescing.

### Algorithm
- **First-Fit Allocation:**  
  - The allocation method scans the list of blocks for free segments.
  - If a free block is large enough to satisfy the request, it is either used (if its size equals the request) or split into an allocated block and a remaining free block.
  - When memory is fragmented, the algorithm can combine multiple free blocks to fulfill an allocation request by returning a **MemorySequence** that contains multiple segments.
  - Alternative will be Best-Fit Allocation which finds the smallest available space that fits exactly and reduces wasted memory, but it is more expensive to conduct this search.

- **Free Method:**  
  The `free` method processes each segment within a `MemorySequence` as follows:
  1. Search for the block matching the segment's start and size.
  2. Mark the corresponding block as free and update the `freeMemory` field.
  3. Merge adjacent free blocks to reduce fragmentation.
  
- **Thread-Safety:**  
  The key methods (`alloc` and `free`) are synchronized, ensuring that the implementation is thread-safe for concurrent use.

## Pros and Cons

### Pros:
- **Simplicity:** Uses a straightforward first-fit algorithm and ArrayList-based block management, making it easy to understand.
- **Fragmentation Handling:** By returning a MemorySequence with multiple segments, it utilizes fragmented memory effectively.
- **Thread-Safety:** Synchronized methods provide basic thread-safety.
- **Low Overhead:** Maintaining a `freeMemory` field allows for quick checks on available memory.

### Cons:
- **Linear Search Overhead:** The use of an `ArrayList` for managing blocks might not scale efficiently for extremely large buffers or a very high number of allocations.
- **Space Complexity:** Using an ArrayList to store blocks may consume extra memory compared to more optimized data structures.
- **Interface Limitations:** Returning a full MemorySequence object may not be ideal for all real-world applications; a pointer/index-based approach might be more efficient.

## Future Considerations and Optimizations
- **Pointer/Index-Based Interface:**  
  Return a simple pointer or index handle instead of a full MemorySequence. Maintain an internal map that links this index to the allocated MemorySequence.

- **Alternative Allocation Strategies:**  
  Consider implementing other allocation strategies like best-fit allocation strategy.

- **Enhanced Data Structures:**  
  Replacing the `ArrayList` with a more advanced data structure (e.g., trees) might improve search and merge operations in highly fragmented memory.

- **Reallocation Support:**  
  Add a `realloc` method to resize allocated memory without full deallocation.

## Setup and Running Instructions
Compile manually:
     ```bash
     javac -d out src/main/java/MemoryManager.java src/main/java/Main.java
     java -cp out Main
     ```
**Run Unit Tests:**
   - Build the project with Gradle:
     ```bash
     gradle build
     ```
   - To run tests, run:
     ```bash
     gradle test
     ```
