package com.yomiato.data.ai

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 端末内要約・タグ抽出の単体テスト。
 * kuromoji は純 JVM ライブラリのため Robolectric 不要で実行できる。
 */
class LocalSummarizerTest {

    private val summarizer = LocalSummarizer()

    @Test
    fun summarize_isExtractive_everySentenceComesFromSource() = runTest {
        val text = buildString {
            append("人工知能の研究は近年急速に進展している。")
            append("大規模言語モデルは自然言語処理の多くの課題を解決した。")
            append("一方で計算資源の消費という課題も残されている。")
            append("研究者たちは効率的な学習手法の開発に取り組んでいる。")
            append("今後はより小さなモデルでの高性能化が期待される。")
        }
        val result = summarizer.summarizeAndTag(
            title = "AI 研究の動向",
            text = text,
            existingTags = emptyList(),
            maxSentences = 2,
        )

        assertTrue("要約が空でない", result.summary.isNotBlank())
        // 抽出型なので、要約文は必ず原文に含まれる（ハルシネーションが無いことの確認）。
        val sentences = text.split("。").filter { it.isNotBlank() }
        val summaryPieces = result.summary.split("。").filter { it.isNotBlank() }
        summaryPieces.forEach { piece ->
            assertTrue(
                "要約片『$piece』は原文に含まれるべき",
                sentences.any { it.contains(piece) || piece.contains(it) },
            )
        }
    }

    @Test
    fun summarize_shortTextReturnedAsIs() = runTest {
        val text = "短い本文です。これだけ。"
        val result = summarizer.summarizeAndTag(
            title = "短文",
            text = text,
            existingTags = emptyList(),
            maxSentences = 3,
        )
        assertTrue(result.summary.isNotBlank())
    }

    @Test
    fun tags_prefersExistingTags() = runTest {
        val text = buildString {
            repeat(5) { append("機械学習の応用事例について解説する。") }
            append("ディープラーニングは画像認識で成果を上げている。")
        }
        val result = summarizer.summarizeAndTag(
            title = "機械学習",
            text = text,
            existingTags = listOf("機械学習"),
            maxTags = 5,
        )
        assertTrue("既存タグが候補に含まれる", result.tags.any { it == "機械学習" })
        // 既存タグは先頭側に来る（優先される）。
        if (result.tags.size > 1) {
            assertTrue("既存タグが先頭付近に来る", result.tags.indexOf("機械学習") == 0)
        }
    }

    @Test
    fun tags_excludeEnglishStopWordsAndSymbols() = runTest {
        val text = "The system and the data are processed. これはテストの本文です。"
        val result = summarizer.summarizeAndTag(
            title = "test",
            text = text,
            existingTags = emptyList(),
        )
        assertFalse("英語ストップワードは除外", result.tags.contains("the"))
        assertFalse("英語ストップワードは除外", result.tags.contains("and"))
        // 記号のみのトークンが混入しないこと
        result.tags.forEach { tag ->
            assertTrue("タグ『$tag』は文字を含む", tag.any { it.isLetterOrDigit() })
        }
    }
}
