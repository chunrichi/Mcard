package com.example.mcard.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    BasicText(
        text = parseMarkdown(text),
        modifier = modifier,
        style = style
    )
}

private fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            when {
                // Code block
                line.startsWith("```") -> {
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) {
                        codeLines.add(lines[i])
                        i++
                    }
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp)) {
                        append(codeLines.joinToString("\n"))
                    }
                    append("\n")
                }

                // Headers
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                        append(line.removePrefix("### "))
                    }
                    append("\n")
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append(line.removePrefix("## "))
                    }
                    append("\n")
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp)) {
                        append(line.removePrefix("# "))
                    }
                    append("\n")
                }

                // Unordered list
                line.startsWith("- ") || line.startsWith("* ") -> {
                    append("• ")
                    parseInlineMarkdown(line.removePrefix("- ").removePrefix("* "), this)
                    append("\n")
                }

                // Ordered list
                line.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val number = line.takeWhile { it.isDigit() }
                    val content = line.drop(number.length + 2)
                    append("$number. ")
                    parseInlineMarkdown(content, this)
                    append("\n")
                }

                // Blockquote
                line.startsWith("> ") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color.Gray)) {
                        append(line.removePrefix("> "))
                    }
                    append("\n")
                }

                // Horizontal rule
                line == "---" || line == "***" || line == "___" -> {
                    withStyle(SpanStyle(color = Color.LightGray)) {
                        append("─────────────────────")
                    }
                    append("\n")
                }

                // Regular text
                else -> {
                    parseInlineMarkdown(line, this)
                    if (i < lines.size - 1) append("\n")
                }
            }
            i++
        }
    }
}

private fun parseInlineMarkdown(text: String, builder: AnnotatedString.Builder) {
    var remaining = text
    val boldRegex = Regex("\\*\\*(.+?)\\*\\*")
    val italicRegex = Regex("(?<!\\*)\\*(?!\\*)(.+?)(?<!\\*)\\*(?!\\*)")
    val codeRegex = Regex("`(.+?)`")
    val strikethroughRegex = Regex("~~(.+?)~~")
    val linkRegex = Regex("\\[([^]]+)\\]\\(([^)]+)\\)")

    while (remaining.isNotEmpty()) {
        val match = listOfNotNull(
            boldRegex.find(remaining),
            italicRegex.find(remaining),
            codeRegex.find(remaining),
            strikethroughRegex.find(remaining),
            linkRegex.find(remaining)
        ).minByOrNull { it.range.first } ?: break

        // Append text before match
        if (match.range.first > 0) {
            builder.append(remaining.substring(0, match.range.first))
        }

        // Apply style based on match type
        when (match.value) {
            match.destructured.component1().let { "**$it**" } -> {
                // Bold
                builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    builder.append(match.destructured.component1())
                }
            }
            match.destructured.component1().let { "*$it*" } -> {
                // Italic
                builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    builder.append(match.destructured.component1())
                }
            }
            match.destructured.component1().let { "`$it`" } -> {
                // Inline code
                builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color.LightGray.copy(alpha = 0.3f))) {
                    builder.append(match.destructured.component1())
                }
            }
            match.destructured.component1().let { "~~$it~~" } -> {
                // Strikethrough
                builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    builder.append(match.destructured.component1())
                }
            }
            else -> {
                // Link
                try {
                    val linkText = match.destructured.component1()
                    val linkUrl = match.destructured.component2()
                    builder.withStyle(SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                        builder.append(linkText)
                    }
                } catch (e: Exception) {
                    builder.append(match.value)
                }
            }
        }

        remaining = remaining.substring(match.range.last + 1)
    }

    if (remaining.isNotEmpty()) {
        builder.append(remaining)
    }
}
