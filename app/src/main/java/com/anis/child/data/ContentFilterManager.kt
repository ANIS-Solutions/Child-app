package com.anis.child.data

import com.anis.child.data.local.ContentFilterRuleDao
import com.anis.child.data.local.ContentFilterRuleEntity
import javax.inject.Inject
import javax.inject.Singleton

data class ContentFilterResult(
    val isBlocked: Boolean,
    val matchedRule: ContentFilterRuleEntity? = null
)

@Singleton
class ContentFilterManager @Inject constructor(
    private val contentFilterRuleDao: ContentFilterRuleDao
) {
    suspend fun checkUrl(url: String): ContentFilterResult {
        val rules = contentFilterRuleDao.getActiveRules()
        val urlRules = rules.filter { it.type == "url" }
        for (rule in urlRules) {
            if (matchPattern(url.lowercase(), rule.pattern.lowercase())) {
                return ContentFilterResult(isBlocked = true, matchedRule = rule)
            }
        }
        return ContentFilterResult(isBlocked = false)
    }

    suspend fun checkText(text: String): ContentFilterResult {
        val rules = contentFilterRuleDao.getActiveRules()
        val keywordRules = rules.filter { it.type == "keyword" }
        for (rule in keywordRules) {
            if (matchPattern(text.lowercase(), rule.pattern.lowercase())) {
                return ContentFilterResult(isBlocked = true, matchedRule = rule)
            }
        }
        return ContentFilterResult(isBlocked = false)
    }

    suspend fun addRule(pattern: String, type: String): Long {
        return contentFilterRuleDao.insert(
            ContentFilterRuleEntity(
                pattern = pattern,
                type = type,
                isBlocked = true,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRule(rule: ContentFilterRuleEntity) {
        contentFilterRuleDao.delete(rule)
    }

    suspend fun toggleRule(id: Long, enabled: Boolean) {
        contentFilterRuleDao.setRuleEnabled(id, enabled)
    }

    companion object {
        fun matchPattern(text: String, pattern: String): Boolean {
            val escaped = Regex.escape(pattern.removeSurrounding("*"))
            val regex = when {
                pattern.startsWith("*") && pattern.endsWith("*") ->
                    Regex("\\b$escaped\\b", RegexOption.IGNORE_CASE)
                pattern.startsWith("*") ->
                    Regex("\\b${escaped}$", RegexOption.IGNORE_CASE)
                pattern.endsWith("*") ->
                    Regex("^${escaped}\\b", RegexOption.IGNORE_CASE)
                else ->
                    Regex("\\b${escaped}\\b", RegexOption.IGNORE_CASE)
            }
            return regex.containsMatchIn(text)
        }
        val DEFAULT_BLOCKED_KEYWORDS = listOf(
            "*porn*", "*xxx*", "*adult*", "*sex*", "*nude*",
            "*gambling*", "*casino*", "*bet*",
            "*violence*", "*gore*", "*hate*"
        )

        val DEFAULT_BLOCKED_URLS = listOf(
            "*porn*", "*xxx*", "*adult*",
            "*casino*", "*gambling*",
            "*hate*", "*extremist*"
        )
    }
}
