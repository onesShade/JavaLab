package javalab;

import javalab.utility.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class JavaLab1a2ApplicationTests {

	@Test
	void cacheDoesntDeleteOldEntries() {
		Map<Integer, Integer> c1 = new InMemoryCache<>(2);

		c1.put(1, 1);
		c1.put(2, 2);
		c1.put(3, 3);

		assertThat(c1.containsKey(1)).isFalse();
	}

	@Test
	void cacheEquals() {
		Map<Integer, Integer> c1 = new InMemoryCache<>(2);
		Map<Integer, Integer> c2 = new InMemoryCache<>(2);

		c1.put(1, 1);
		c2.put(2, 2);

		assertThat(c1.equals(c2)).isFalse();

		c2.clear();
		c2.put(1, 1);

		assertThat(c1.equals(c2)).isTrue();
	}
}
