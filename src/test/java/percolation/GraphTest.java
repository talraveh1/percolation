package percolation;

import org.junit.jupiter.api.BeforeAll;
import percolation.stats.Stats;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphTest {
    protected static final Stats stats = mock(Stats.class);

    @BeforeAll
    static void beforeAll() {
        Stats.TimeOutput mock = mock(Stats.TimeOutput.class);
        when(stats.spt()).thenReturn(mock);
    }
}
