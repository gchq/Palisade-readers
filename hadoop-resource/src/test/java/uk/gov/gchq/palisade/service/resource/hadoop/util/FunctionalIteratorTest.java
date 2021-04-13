/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.service.resource.hadoop.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.service.resource.hadoop.util.FunctionalIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionalIteratorTest {
    FunctionalIterator<Integer> testIterator;

    @BeforeEach
    public void setUp() {
        testIterator = FunctionalIterator.fromIterator(List.of(0, 1, 2, 3, 4, 5).iterator());
    }

    @Test
    void testMap() {
        FunctionalIterator<Integer> dslTest = testIterator
                .map(i -> i + 1);
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(1, 2, 3, 4, 5, 6));
    }

    @Test
    void testFilterFirst() {
        var dslTest = testIterator
                .filter(i -> i == 0);
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(0));
    }

    @Test
    void testFilterLast() {
        var dslTest = testIterator
                .filter(i -> i == 5);
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(5));
    }

    @Test
    void testMapLast() {
        var dslTest = testIterator
                .mapLast(i -> 100);
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(0, 1, 2, 3, 4, 100));
    }

    @Test
    void testTwoFlatMap() {
        var dslTest = testIterator
                .flatMap(i -> List.of(i, 2 * i).iterator());
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(0, 0, 1, 2, 2, 4, 3, 6, 4, 8, 5, 10));
    }

    @Test
    void testZeroFlatMap() {
        var dslTest = testIterator
                .flatMap(i -> Collections.emptyIterator());
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(Collections.emptyList());
    }

    @Test
    void testPeek() {
        final AtomicInteger count = new AtomicInteger(0);
        var dslTest = testIterator
                .peek(i -> count.incrementAndGet());
        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(0, 1, 2, 3, 4, 5));

        assertThat(count.intValue())
                .as("Check that the next number in the iterator is correct")
                .isEqualTo(6);
    }

    @Test
    void testWholeDSLTwice() {
        var firstSum = new AtomicInteger(0);
        var dslTest = testIterator
                .map(i -> i + 1) // 1, 2, 3, 4, 5, 6
                .filter(i -> i % 3 != 0 && i % 2 != 0) // 1, 5
                .mapLast(i -> i + 2) // 1, 7
                .flatMap(i -> List.of(i, i * i).iterator()) // 1, 1, 7, 49
                .peek(firstSum::addAndGet) // 58
                .map(i -> i - 1) // 0, 0, 6, 48
                .mapLast(i -> i - 40) // 0, 0, 6, 8
                .filter(i -> i > 0 && i < 8) // 6
                .flatMap(i -> List.of(i * (i + 1)).iterator()) // 42
                .peek(System.out::println); // Print 1

        assertThat(listOf(dslTest))
                .as("Check that the iterator has the correct values returned")
                .isEqualTo(List.of(42));
    }

    private <T> List<T> listOf(final Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport
                .stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }
}