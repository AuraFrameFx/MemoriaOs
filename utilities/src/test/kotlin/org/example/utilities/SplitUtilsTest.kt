package org.example.utilities

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.example.list.LinkedList

@DisplayName("SplitUtils.split")
class SplitUtilsTest {

    private fun linkedListToList(ll: LinkedList): List<String> {
        // Assumes LinkedList implements Iterable<String>
        return ll.toList()
    }

    @Nested
    @DisplayName("Happy paths")
    inner class HappyPaths {
        @Test
        fun `single space-separated tokens`() {
            val result = SplitUtils.split("alpha beta gamma")
            assertEquals(listOf("alpha", "beta", "gamma"), linkedListToList(result))
        }

        @Test
        fun `single token no spaces`() {
            val result = SplitUtils.split("singleton")
            assertEquals(listOf("singleton"), linkedListToList(result))
        }

        @Test
        fun `unicode tokens preserved`() {
            val result = SplitUtils.split("café müller 東京")
            assertEquals(listOf("café", "müller", "東京"), linkedListToList(result))
        }
    }

    @Nested
    @DisplayName("Edge cases and trimming behavior")
    inner class EdgeCases {
        @Test
        fun `leading spaces are ignored (no empty tokens)`() {
            val result = SplitUtils.split("   lead space")
            assertEquals(listOf("lead", "space"), linkedListToList(result))
        }

        @Test
        fun `trailing spaces are ignored (no empty tokens)`() {
            val result = SplitUtils.split("trail space   ")
            assertEquals(listOf("trail", "space"), linkedListToList(result))
        }

        @Test
        fun `consecutive spaces collapse to single split (empty tokens filtered)`() {
            val result = SplitUtils.split("a  b   c")
            assertEquals(listOf("a", "b", "c"), linkedListToList(result))
        }

        @Test
        fun `spaces only yields empty list`() {
            val result = SplitUtils.split("     ")
            assertTrue(linkedListToList(result).isEmpty())
        }

        @Test
        fun `empty string yields empty list`() {
            val result = SplitUtils.split("")
            assertTrue(linkedListToList(result).isEmpty())
        }

        @Test
        fun `non-space whitespace (tabs newlines) are not split`() {
            val result = SplitUtils.split("a\tb\nc")
            // Implementation splits only by literal space " ", so tabs/newlines should remain inside tokens
            assertEquals(listOf("a\tb\nc"), linkedListToList(result))
        }

        @Test
        fun `mixed spaces and tabs results in split only on spaces`() {
            val result = SplitUtils.split("a\tb c\td")
            assertEquals(listOf("a\tb", "c\td"), linkedListToList(result))
        }
    }

    @Nested
    @DisplayName("Robustness")
    inner class Robustness {
        @Test
        fun `very long input does not throw and splits correctly`() {
            val input = (1..1000).joinToString(" ") { "t${it}" }
            val result = SplitUtils.split(input)
            val list = linkedListToList(result)
            assertEquals(1000, list.size)
            assertEquals("t1", list.first())
            assertEquals("t1000", list.last())
        }

        @Test
        fun `tokens containing multiple spaces around them`() {
            val input = "  A    B   C  "
            val result = SplitUtils.split(input)
            assertEquals(listOf("A", "B", "C"), linkedListToList(result))
        }
    }
}