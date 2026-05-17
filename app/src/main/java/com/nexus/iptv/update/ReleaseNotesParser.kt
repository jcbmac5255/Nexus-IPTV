package com.nexus.iptv.update

/**
 * Sections extracted from a GitHub release's markdown body. Sections are
 * matched loosely on header text so any of `## Features`, `**Features**`,
 * `### New features`, etc. all funnel into [features]; same for changes
 * (matches "changes" / "improvements" / "updates") and fixes (matches
 * "fix" / "fixes" / "bug fixes").
 *
 * Anything that appears before the first recognized header — or in a body
 * that has no recognized headers at all — lands in [other], so the dialog
 * can show a fallback "What's new" block when notes are flat prose rather
 * than structured.
 */
data class ParsedReleaseNotes(
    val features: List<String>,
    val changes: List<String>,
    val fixes: List<String>,
    val other: String
) {
    val isEmpty: Boolean
        get() = features.isEmpty() && changes.isEmpty() && fixes.isEmpty() && other.isBlank()
}

object ReleaseNotesParser {
    private val markdownHeaderRegex = Regex("""^\s*#{1,6}\s*\*{0,2}\s*([^*].*?)\s*\*{0,2}\s*$""")
    private val boldHeaderRegex = Regex("""^\s*\*\*([^*]+?)\*\*\s*:?\s*$""")
    private val horizontalRuleRegex = Regex("""^\s*-{3,}\s*$""")

    private enum class Bucket { FEATURES, CHANGES, FIXES }

    fun parse(markdown: String?): ParsedReleaseNotes {
        if (markdown.isNullOrBlank()) {
            return ParsedReleaseNotes(emptyList(), emptyList(), emptyList(), "")
        }

        val features = mutableListOf<String>()
        val changes = mutableListOf<String>()
        val fixes = mutableListOf<String>()
        val otherLines = mutableListOf<String>()

        var currentBucket: Bucket? = null
        var seenAnyKnownHeader = false

        for (rawLine in markdown.lineSequence()) {
            val line = rawLine.trimEnd()
            if (horizontalRuleRegex.matches(line)) continue

            val headerTitle = markdownHeaderRegex.matchEntire(line)?.groupValues?.get(1)
                ?: boldHeaderRegex.matchEntire(line)?.groupValues?.get(1)

            if (headerTitle != null) {
                currentBucket = bucketFor(headerTitle)
                if (currentBucket != null) seenAnyKnownHeader = true
                continue
            }

            if (line.isBlank()) continue

            val cleaned = line.trimStart()
                .removePrefix("- ")
                .removePrefix("* ")
                .removePrefix("• ")
                .trim()
            if (cleaned.isEmpty()) continue

            when (currentBucket) {
                Bucket.FEATURES -> features.add(cleaned)
                Bucket.CHANGES -> changes.add(cleaned)
                Bucket.FIXES -> fixes.add(cleaned)
                null -> otherLines.add(cleaned)
            }
        }

        return ParsedReleaseNotes(
            features = features,
            changes = changes,
            fixes = fixes,
            // If we recognized any structured section, drop unstructured prose
            // (typically header preamble like "## Compatibility note"). For
            // wholly-unstructured notes, fall back to showing it as the "Other"
            // / "What's new" block.
            other = if (seenAnyKnownHeader) "" else otherLines.joinToString("\n")
        )
    }

    private fun bucketFor(headerTitle: String): Bucket? {
        val title = headerTitle.lowercase()
        return when {
            "feature" in title || "new" in title -> Bucket.FEATURES
            "fix" in title || "bug" in title -> Bucket.FIXES
            "change" in title || "improvement" in title || "update" in title -> Bucket.CHANGES
            else -> null
        }
    }
}
