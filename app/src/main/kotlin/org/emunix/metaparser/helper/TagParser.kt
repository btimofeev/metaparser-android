/*
 * Copyright (c) 2019, 2021 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.metaparser.helper

// This file based on android.text.Html

import android.graphics.Typeface
import android.text.*
import android.text.style.AlignmentSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TagParser @Inject constructor() : DefaultHandler() {
    private val reader: XMLReader = org.ccil.cowan.tagsoup.Parser()
    private lateinit var spannableStringBuilder: SpannableStringBuilder

    init {
        reader.contentHandler = this
    }

    suspend fun parse(source: String): Spanned = withContext(Dispatchers.IO) {
        spannableStringBuilder = SpannableStringBuilder()
        try {
            reader.parse(InputSource(StringReader(source)))
        } catch (e: IOException) {
            // We are reading from a string. There should not be IO problems.
            throw RuntimeException(e)
        } catch (e: SAXException) {
            // TagSoup doesn't throw parse exceptions.
            throw RuntimeException(e)
        }

        return@withContext spannableStringBuilder
    }

    private fun handleStartTag(tag: String) {
        when {
            tag.equals("b", ignoreCase = true) -> {
                start(spannableStringBuilder, Bold())
            }
            tag.equals("i", ignoreCase = true) -> {
                start(spannableStringBuilder, Italic())
            }
            tag.equals("u", ignoreCase = true) -> {
                start(spannableStringBuilder, Underline())
            }
            tag.equals("st", ignoreCase = true) -> {
                start(spannableStringBuilder, Strikethrough())
            }
            tag.equals("center", ignoreCase = true) -> {
                start(spannableStringBuilder, Center())
            }
            tag.equals("right", ignoreCase = true) -> {
                start(spannableStringBuilder, Right())
            }
        }
    }

    private fun handleEndTag(tag: String) {
        when {
            tag.equals("b", ignoreCase = true) -> {
                end(spannableStringBuilder, Bold::class.java, StyleSpan(Typeface.BOLD))
            }
            tag.equals("i", ignoreCase = true) -> {
                end(spannableStringBuilder, Italic::class.java, StyleSpan(Typeface.ITALIC))
            }
            tag.equals("u", ignoreCase = true) -> {
                end(spannableStringBuilder, Underline::class.java, UnderlineSpan())
            }
            tag.equals("st", ignoreCase = true) -> {
                end(spannableStringBuilder, Strikethrough::class.java, StrikethroughSpan())
            }
            tag.equals("center", ignoreCase = true) -> {
                end(spannableStringBuilder, Center::class.java, AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER))
            }
            tag.equals("right", ignoreCase = true) -> {
                end(spannableStringBuilder, Right::class.java, AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE))
            }
        }
    }

    private fun getLast(text: Spanned, kind: Class<*>): Any? {
        /*
        * This knows that the last returned object from getSpans()
        * will be the most recently added.
        */
        val objects = text.getSpans(0, text.length, kind)
        return if (objects.isEmpty()) {
            null
        } else {
            objects[objects.size - 1]
        }
    }

    private fun setSpanFromMark(text: Spannable, mark: Any, vararg spans: Any) {
        val where = text.getSpanStart(mark)
        text.removeSpan(mark)
        val len = text.length
        if (where != len) {
            for (span in spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun start(text: Editable, mark: Any) {
        val len = text.length
        text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    private fun end(text: Editable, kind: Class<*>, repl: Any) {
        val len = text.length
        val obj = getLast(text, kind)
        obj?.let { setSpanFromMark(text, it, repl) }
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        handleStartTag(localName)
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, qName: String) {
        handleEndTag(localName)
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        val sb = StringBuilder()
        spannableStringBuilder.append(sb.append(ch, start, length))
    }

    private class Bold
    private class Italic
    private class Underline
    private class Strikethrough
    private class Center
    private class Right

}
