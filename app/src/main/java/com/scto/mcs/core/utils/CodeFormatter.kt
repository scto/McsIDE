package com.scto.mcside.core.utils

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

object CodeFormatter {

    /**
     * Formatiert Code basierend auf der Dateiendung mit anpassbarer Einrückungsgröße.
     */
    fun format(code: String, extension: String, indentSize: Int = 2): String {
        if (code.isBlank()) return ""

        return try {
            when (extension.lowercase()) {
                "html", "htm" -> formatHtml(code, indentSize)
                "xml" -> formatXml(code, indentSize)
                "json" -> formatJson(code, indentSize)
                "css" -> formatCss(code, indentSize)
                "js" -> formatJs(code, indentSize)
                "java", "kt", "gradle", "kts" -> formatBraceBased(code, indentSize)
                else -> code
            }
        } catch (e: Exception) {
            e.printStackTrace()
            code
        }
    }

    private fun formatHtml(code: String, indentSize: Int): String {
        val doc = Jsoup.parse(code)
        doc.outputSettings()
            .indentAmount(indentSize)
            .prettyPrint(true)
        return doc.html()
    }

    private fun formatXml(code: String, indentSize: Int): String {
        val doc = Jsoup.parse(code, "", Parser.xmlParser())
        doc.outputSettings()
            .indentAmount(indentSize)
            .prettyPrint(true)
        return doc.outerHtml()
    }

    private fun formatJson(code: String, indentSize: Int): String {
        val trimmed = code.trim()
        return if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(indentSize)
        } else {
            JSONObject(trimmed).toString(indentSize)
        }
    }

    private fun formatCss(code: String, indentSize: Int = 4): String {
        val indent = " ".repeat(indentSize)
        return code
            .replace(Regex("\\s*\\{\\s*"), " {\n$indent")
            .replace(Regex("\\s*;\\s*"), ";\n$indent")
            .replace(Regex("\\s*\\}\\s*"), "\n}\n")
            .replace(Regex("(?m)^\\s+$"), "")
            .replace(Regex("\\n\\s*\\n"), "\n")
            .trim()
    }

    private fun formatJs(code: String, indentSize: Int): String = formatBraceBased(code, indentSize)

    /**
     * Generischer Formatter für C-Style Syntax (Java, Kotlin, JS, Groovy).
     * Handhabt Einrückungen basierend auf geschweiften Klammern und Block-Elementen.
     */
    private fun formatBraceBased(code: String, indentSize: Int): String {
        val lines = code.split("\n")
        val result = StringBuilder()
        var indentLevel = 0
        val indentStr = " ".repeat(indentSize)

        for (line in lines) {
            var trimmed = line.trim()
            if (trimmed.isEmpty()) {
                result.append("\n")
                continue
            }
            
            // Verringere Einrückung, wenn die Zeile mit einer schließenden Klammer beginnt
            if (trimmed.startsWith("}") || trimmed.startsWith("]") || trimmed.startsWith(")")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            result.append(indentStr.repeat(indentLevel)).append(trimmed).append("\n")
            
            // Erhöhe Einrückung für den nächsten Block
            if (trimmed.endsWith("{") || trimmed.endsWith("[") || trimmed.endsWith("(")) {
                indentLevel++
            }
        }
        
        // Bereinigung von leeren Zeilen-Kaskaden
        return result.toString().replace(Regex("\\n{3,}"), "\n\n").trim()
    }
}