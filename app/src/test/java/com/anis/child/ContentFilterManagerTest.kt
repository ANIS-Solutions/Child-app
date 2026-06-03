package com.anis.child

import com.anis.child.data.ContentFilterManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentFilterManagerTest {

    @Test
    fun matchPattern_exactMatch() {
        assertTrue(ContentFilterManager.matchPattern("hello world", "hello"))
        assertTrue(ContentFilterManager.matchPattern("test", "test"))
        assertFalse(ContentFilterManager.matchPattern("hello world", "xyz"))
    }

    @Test
    fun matchPattern_wildcardBothSides() {
        assertTrue(ContentFilterManager.matchPattern("visit pornhub.com", "*porn*"))
        assertTrue(ContentFilterManager.matchPattern("porn", "*porn*"))
        assertTrue(ContentFilterManager.matchPattern("xxx content here", "*xxx*"))
        assertTrue(ContentFilterManager.matchPattern("gambling is bad", "*gambling*"))
        assertFalse(ContentFilterManager.matchPattern("hello world", "*porn*"))
    }

    @Test
    fun matchPattern_wildcardPrefix() {
        assertTrue(ContentFilterManager.matchPattern("visiting casino.com", "*casino.com"))
        assertTrue(ContentFilterManager.matchPattern("endswith_test", "*_test"))
        assertFalse(ContentFilterManager.matchPattern("test_prefix", "*_suffix"))
    }

    @Test
    fun matchPattern_wildcardSuffix() {
        assertTrue(ContentFilterManager.matchPattern("https://bad-site.com", "https://*"))
        assertTrue(ContentFilterManager.matchPattern("startswith_", "startswith_*"))
        assertFalse(ContentFilterManager.matchPattern("/safe/path", "/blocked*"))
    }

    @Test
    fun matchPattern_caseSensitive() {
        assertTrue(ContentFilterManager.matchPattern("visit pornhub.com", "*porn*"))
        assertTrue(ContentFilterManager.matchPattern("hello world", "*hello*"))
        assertTrue(ContentFilterManager.matchPattern("gambling site", "*gambling*"))
        assertFalse(ContentFilterManager.matchPattern("VISIT PORNHUB.COM", "*porn*"))
    }

    @Test
    fun matchPattern_urlPatterns() {
        assertTrue(ContentFilterManager.matchPattern("https://porn-site.com/video", "*porn*"))
        assertTrue(ContentFilterManager.matchPattern("https://www.casino.com/play", "*casino*"))
        assertTrue(ContentFilterManager.matchPattern("http://extremist-forum.org", "*extremist*"))
        assertFalse(ContentFilterManager.matchPattern("https://www.wikipedia.org", "*porn*"))
    }

    @Test
    fun matchPattern_defaultKeywords() {
        val keywords = ContentFilterManager.DEFAULT_BLOCKED_KEYWORDS
        assertTrue(keywords.contains("*porn*"))
        assertTrue(keywords.contains("*gambling*"))
        assertTrue(keywords.contains("*violence*"))
    }

    @Test
    fun matchPattern_defaultUrls() {
        val urls = ContentFilterManager.DEFAULT_BLOCKED_URLS
        assertTrue(urls.contains("*porn*"))
        assertTrue(urls.contains("*casino*"))
        assertTrue(urls.contains("*extremist*"))
    }

    @Test
    fun matchPattern_emptyPattern() {
        assertTrue(ContentFilterManager.matchPattern("anything", ""))
        assertTrue(ContentFilterManager.matchPattern("", ""))
        assertFalse(ContentFilterManager.matchPattern("", "*nonempty*"))
    }

    @Test
    fun matchPattern_edgeCases() {
        assertTrue(ContentFilterManager.matchPattern("a", "a"))
        assertFalse(ContentFilterManager.matchPattern("a", "b"))
        assertTrue(ContentFilterManager.matchPattern("abc", "a*"))
        assertTrue(ContentFilterManager.matchPattern("abc", "*c"))
        assertFalse(ContentFilterManager.matchPattern("abc", "d*"))
    }
}
