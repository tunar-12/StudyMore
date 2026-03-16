package StudyMore.models;

public class SnowflakeIDGenerator {

    // Snowflake ID configuration
    private static final long EPOCH = 1700000000000L;

    private static final int WORKER_ID_BITS   = 5;
    private static final int DATACENTER_BITS  = 5;
    private static final int SEQUENCE_BITS    = 12;

    private static final long MAX_WORKER_ID    = ~(-1L << WORKER_ID_BITS);   
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_BITS);  
    private static final long MAX_SEQUENCE     = ~(-1L << SEQUENCE_BITS);     

    private static final int WORKER_ID_SHIFT    = SEQUENCE_BITS;              
    private static final int DATACENTER_SHIFT   = SEQUENCE_BITS + WORKER_ID_BITS; 
    private static final int TIMESTAMP_SHIFT    = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_BITS; 

    // Default node identifiers (can be changed per instance/service)
    private static final long WORKER_ID    = 1L;
    private static final long DATACENTER_ID = 1L;

    private static long lastTimestamp = -1L;
    private static long sequence      = 0L;

    /**
     * Generates a unique 64-bit Snowflake ID.
     *
     * Bit layout (64 bits total):
     *   [63]       - sign bit (always 0, keeps ID positive)
     *   [62..22]   - 41 bits: milliseconds since custom EPOCH
     *   [21..17]   - 5 bits:  datacenter ID (0-31)
     *   [16..12]   - 5 bits:  worker ID (0-31)
     *   [11..0]    - 12 bits: sequence number (0-4095 per millisecond)
     *
     * @return a unique, time-sortable long ID
     */
    public static synchronized long generate() {
        long currentTimestamp = System.currentTimeMillis();

        // Clock moved backwards — reject to prevent duplicate IDs
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException(
                "Clock moved backwards. Refusing to generate ID for "
                + (lastTimestamp - currentTimestamp) + " ms."
            );
        }

        if (currentTimestamp == lastTimestamp) {
            // Same millisecond: increment sequence
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // Sequence exhausted — wait for the next millisecond
            if (sequence == 0) {
                currentTimestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            // New millisecond: reset sequence
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
             | (DATACENTER_ID              << DATACENTER_SHIFT)
             | (WORKER_ID                  << WORKER_ID_SHIFT)
             | sequence;
    }

    /** Spins until the clock advances past the given timestamp. */
    private static long waitForNextMillis(long lastTs) {
        long ts = System.currentTimeMillis();
        while (ts <= lastTs) {
            ts = System.currentTimeMillis();
        }
        return ts;
    }
}