package javalab;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import javalab.utility.Cache;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.Test;


class CacheTests {

	@Test
    void deleteOldEntries() {
		Map<Integer, Integer> c1 = new Cache<>(2);

		c1.put(1, 1);
		c1.put(2, 2);
		c1.put(3, 3);

		MapAssert.assertThatMap(c1).doesNotContainKey(1);
		MapAssert.assertThatMap(c1).containsKey(2);
		MapAssert.assertThatMap(c1).containsKey(3);
	}

	@Test
	void cacheEquals() {
		Map<Integer, Integer> c1 = new Cache<>(2);
		Map<Integer, Integer> c2 = new Cache<>(2);

		c1.put(1, 1);
		c2.put(2, 2);

		assertThat(c1.equals(c2)).isFalse();

		c2.clear();
		c2.put(1, 1);

		assertThat(c1.equals(c2)).isTrue();
	}
}
