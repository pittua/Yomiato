package com.yomiato.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** URL 抽出・正規化（重複判定の要）の単体テスト。 */
class UrlUtilsTest {

    // ---- extractFirstUrl ----

    @Test
    fun extractFirstUrl_picksUrlFromSharedText() {
        val text = "面白い記事みつけた https://example.com/a/b ぜひ読んで"
        assertEquals("https://example.com/a/b", UrlUtils.extractFirstUrl(text))
    }

    @Test
    fun extractFirstUrl_trimsTrailingPunctuation() {
        assertEquals("https://example.com/x", UrlUtils.extractFirstUrl("見て(https://example.com/x)."))
    }

    @Test
    fun extractFirstUrl_returnsNullWhenAbsentOrBlank() {
        assertNull(UrlUtils.extractFirstUrl("ただのテキスト"))
        assertNull(UrlUtils.extractFirstUrl(""))
        assertNull(UrlUtils.extractFirstUrl(null))
    }

    // ---- isValidHttpUrl ----

    @Test
    fun isValidHttpUrl_acceptsHttpAndHttps() {
        assertTrue(UrlUtils.isValidHttpUrl("http://example.com"))
        assertTrue(UrlUtils.isValidHttpUrl("https://example.com/path"))
    }

    @Test
    fun isValidHttpUrl_rejectsOtherSchemesAndGarbage() {
        assertFalse(UrlUtils.isValidHttpUrl("ftp://example.com"))
        assertFalse(UrlUtils.isValidHttpUrl("example.com"))
        assertFalse(UrlUtils.isValidHttpUrl("not a url"))
    }

    // ---- host ----

    @Test
    fun host_stripsWwwPrefix() {
        assertEquals("example.com", UrlUtils.host("https://www.example.com/a"))
        assertEquals("news.example.com", UrlUtils.host("https://news.example.com/a"))
    }

    @Test
    fun host_returnsNullForInvalid() {
        assertNull(UrlUtils.host("not a url"))
    }

    // ---- normalize ----

    @Test
    fun normalize_lowercasesSchemeAndHost() {
        assertEquals("https://example.com/Path", UrlUtils.normalize("HTTPS://Example.COM/Path"))
    }

    @Test
    fun normalize_dropsDefaultPortAndFragment() {
        assertEquals("https://example.com/a", UrlUtils.normalize("https://example.com:443/a#section"))
        assertEquals("http://example.com/a", UrlUtils.normalize("http://example.com:80/a"))
    }

    @Test
    fun normalize_keepsNonDefaultPort() {
        assertEquals("https://example.com:8443/a", UrlUtils.normalize("https://example.com:8443/a"))
    }

    @Test
    fun normalize_removesTrailingSlashOnNonRootPath() {
        assertEquals("https://example.com/a", UrlUtils.normalize("https://example.com/a/"))
        // パス無しはホストのみ
        assertEquals("https://example.com", UrlUtils.normalize("https://example.com"))
    }

    @Test
    fun normalize_stripsTrackingParamsAndSortsRest() {
        val input = "https://example.com/a?utm_source=twitter&b=2&a=1&fbclid=xyz"
        assertEquals("https://example.com/a?a=1&b=2", UrlUtils.normalize(input))
    }

    @Test
    fun normalize_treatsTrackingOnlyQueryAsNoQuery() {
        assertEquals("https://example.com/a", UrlUtils.normalize("https://example.com/a?utm_source=x&gclid=y"))
    }

    @Test
    fun normalize_equivalentUrlsCollapseToSameKey() {
        val a = UrlUtils.normalize("https://www.Example.com/post/?utm_medium=email")
        val b = UrlUtils.normalize("https://www.example.com/post")
        // www は host() の表示用処理であり normalize では保持される点に注意（両者とも www 付き）
        assertEquals(a, b)
    }
}
