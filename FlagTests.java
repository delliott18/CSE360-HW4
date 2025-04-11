import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FlagTests {

    @Test
    void addFlag_createsNewFlag() {
        int id = MemoryStorage.addFlag("QUESTION", 1, "staffA");
        assertTrue(id > 0);
        assertTrue(MemoryStorage.getActiveFlag("QUESTION", 1).isPresent());
    }

    @Test
    void addFlag_returnsMinusOne_ifAlreadyFlagged() {
        MemoryStorage.addFlag("ANSWER", 2, "staffA");
        int id2 = MemoryStorage.addFlag("ANSWER", 2, "staffB");
        assertEquals(-1, id2);
    }

    @Test
    void resolveFlag_marksResolvedAndAudits() {
        int id = MemoryStorage.addFlag("REVIEW", 3, "staffA");
        MemoryStorage.resolveFlag(id, "staffB");

        Flag f = MemoryStorage.getFlags(true).stream()
                              .filter(fl -> fl.getId() == id).findFirst().get();

        assertTrue(f.isResolved());
        assertEquals("staffB", f.getResolvedBy());
        assertNotNull(f.getResolvedOn());
        assertFalse(MemoryStorage.getActiveFlag("REVIEW", 3).isPresent());
    }
}
