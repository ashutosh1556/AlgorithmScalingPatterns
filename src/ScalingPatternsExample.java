import java.util.*;
import java.util.concurrent.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class ScalingPatternsExample {

    /**
     * PATTERN 1: Early Termination Pattern
     * Demonstrates: Failing fast when negative conditions are detected early
     * Applied to: Permutation checking with heuristic exit conditions
     */
    static boolean permutationWithEarlyTermination(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        if (s1.length() != s2.length()) return false;
        
        // Quick hash check - if hashes differ, likely not permutations
        if (s1.hashCode() != s2.hashCode()) {
            // Note: Hash collision possible, so this is just an optimization
        }
        
        Map<Character, Integer> charCount = new HashMap<>();
        int uniqueChars = 0;
        
        // Process both strings simultaneously with early termination
        for (int i = 0; i < s1.length(); i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            
            // Update counts
            charCount.put(c1, charCount.getOrDefault(c1, 0) + 1);
            if (charCount.get(c1) == 1) uniqueChars++;
            else if (charCount.get(c1) == 0) uniqueChars--;
            
            charCount.put(c2, charCount.getOrDefault(c2, 0) - 1);
            if (charCount.get(c2) == -1) uniqueChars++;
            else if (charCount.get(c2) == 0) uniqueChars--;
            
            // Early termination: if we have too many unique chars, not a permutation
            if (uniqueChars > s1.length() / 2) {
                return false; // Heuristic: too many unmatched characters
            }
        }
        
        return uniqueChars == 0;
    }

    /**
     * PATTERN 2: Parallel Processing Pattern
     * Demonstrates: Dividing work across multiple CPU cores
     * Applied to: Chunk-based character frequency counting
     */
    static boolean permutationParallel(String s1, String s2) throws InterruptedException, ExecutionException {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        if (s1.length() != s2.length()) return false;
        
        int length = s1.length();
        int numThreads = Runtime.getRuntime().availableProcessors();
        int chunkSize = Math.max(1000, length / numThreads);
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Map<Character, Integer>>> futures = new ArrayList<>();
        
        // Process chunks in parallel
        for (int start = 0; start < length; start += chunkSize) {
            int end = Math.min(start + chunkSize, length);
            final int startIndex = start;
            final int endIndex = end;
            
            Future<Map<Character, Integer>> future = executor.submit(() -> {
                Map<Character, Integer> chunkCount = new HashMap<>();
                for (int i = startIndex; i < endIndex; i++) {
                    char c1 = s1.charAt(i);
                    char c2 = s2.charAt(i);
                    chunkCount.put(c1, chunkCount.getOrDefault(c1, 0) + 1);
                    chunkCount.put(c2, chunkCount.getOrDefault(c2, 0) - 1);
                }
                return chunkCount;
            });
            futures.add(future);
        }
        
        // Merge results from all chunks
        Map<Character, Integer> totalCount = new HashMap<>();
        for (Future<Map<Character, Integer>> future : futures) {
            Map<Character, Integer> chunkResult = future.get();
            for (Map.Entry<Character, Integer> entry : chunkResult.entrySet()) {
                totalCount.put(entry.getKey(), 
                    totalCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        
        executor.shutdown();
        
        // Check if all counts are zero
        for (int count : totalCount.values()) {
            if (count != 0) return false;
        }
        return true;
    }

    /**
     * PATTERN 3: Streaming/Chunking Pattern
     * Demonstrates: Processing large datasets in memory-bounded chunks
     * Applied to: Buffer-based string processing with periodic cleanup
     */
    static boolean permutationStreaming(String s1, String s2, int bufferSize) {
        if (s1.length() != s2.length()) return false;
        
        Map<Character, Integer> charCount = new HashMap<>();
        int length = s1.length();
        
        // Process in chunks to avoid memory issues
        for (int start = 0; start < length; start += bufferSize) {
            int end = Math.min(start + bufferSize, length);
            
            // Process chunk
            for (int i = start; i < end; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                charCount.put(c1, charCount.getOrDefault(c1, 0) + 1);
                charCount.put(c2, charCount.getOrDefault(c2, 0) - 1);
            }
            
            // Periodic cleanup to prevent memory bloat
            if (start % (bufferSize * 10) == 0) {
                charCount.entrySet().removeIf(entry -> entry.getValue() == 0);
            }
        }
        
        return charCount.values().stream().allMatch(count -> count == 0);
    }

    /**
     * PATTERN 4: Signature/Hashing Pattern
     * Demonstrates: Creating compact representations for expensive comparisons
     * Applied to: Cryptographic signatures of character frequency patterns
     */
    static boolean permutationHashBased(String s1, String s2) {
        if (s1.length() != s2.length()) return false;
        
        try {
            String signature1 = createFrequencySignature(s1);
            String signature2 = createFrequencySignature(s2);
            return signature1.equals(signature2);
        } catch (Exception e) {
            // Fallback to regular method
            return permutationWithEarlyTermination(s1, s2);
        }
    }
    
    private static String createFrequencySignature(String str) throws Exception {
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : str.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }
        
        // Create sorted signature: char1:count1,char2:count2,...
        StringBuilder signature = new StringBuilder();
        freq.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> signature.append(entry.getKey()).append(":").append(entry.getValue()).append(","));
        
        // Hash the signature for fixed-size comparison
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(signature.toString().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * PATTERN 5: Distributed Processing Pattern (MapReduce)
     * Demonstrates: Scaling horizontally across multiple machines
     * Applied to: Distributed character counting with result aggregation
     */
    static class DistributedProcessingPattern {
        public void demonstratePattern() {
            System.out.println("Distributed Processing Pattern:");
            System.out.println("1. Partition data across N machines");
            System.out.println("2. Each machine: process its chunk independently");
            System.out.println("3. Master: aggregate all partial results");
            System.out.println("4. Result: final computation on aggregated data");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== ALGORITHM SCALING PATTERNS DEMONSTRATION ===\n");
        
        // Test all patterns with performance comparison
        demonstratePatterns();
        
        // Show distributed pattern concept
        new DistributedProcessingPattern().demonstratePattern();
    }
    
    static void demonstratePatterns() throws Exception {
        System.out.println("Performance comparison of scaling patterns:\n");
        
        // Generate large test strings for demonstration
        String large1 = generateLargeString(100000, "abcdefghijklmnopqrstuvwxyz");
        String large2 = shuffleString(large1);
        
        // Test each pattern
        long start, end;
        
        // Pattern 1: Early Termination
        start = System.nanoTime();
        boolean result1 = permutationWithEarlyTermination(large1, large2);
        end = System.nanoTime();
        System.out.printf("Early Termination Pattern: %b in %.2f ms%n", result1, (end - start) / 1_000_000.0);
        
        // Pattern 2: Parallel Processing
        start = System.nanoTime();
        boolean result2 = permutationParallel(large1, large2);
        end = System.nanoTime();
        System.out.printf("Parallel Processing Pattern: %b in %.2f ms%n", result2, (end - start) / 1_000_000.0);
        
        // Pattern 3: Streaming/Chunking
        start = System.nanoTime();
        boolean result3 = permutationStreaming(large1, large2, 10000);
        end = System.nanoTime();
        System.out.printf("Streaming/Chunking Pattern: %b in %.2f ms%n", result3, (end - start) / 1_000_000.0);
        
        // Pattern 4: Signature/Hashing
        start = System.nanoTime();
        boolean result4 = permutationHashBased(large1, large2);
        end = System.nanoTime();
        System.out.printf("Signature/Hashing Pattern: %b in %.2f ms%n", result4, (end - start) / 1_000_000.0);
        
        System.out.println("\nAll patterns agree: " + 
            (result1 == result2 && result2 == result3 && result3 == result4));
    }
    
    static String generateLargeString(int length, String alphabet) {
        Random random = new Random(42); // Fixed seed for reproducibility
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
    
    static String shuffleString(String str) {
        char[] chars = str.toCharArray();
        Random random = new Random(24); // Different seed for shuffling
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}