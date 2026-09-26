package com.example.data.remote

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class NewsItem(
    val title: String,
    val summary: String,
    val link: String,
    val pubDate: String,
    val source: String,
    val isInternational: Boolean
)

data class NewsBriefing(
    val international: NewsItem?,
    val national1: NewsItem?,
    val national2: NewsItem?,
    val allItems: List<NewsItem>
)

class NewsApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun getNewsBriefing(): NewsBriefing = withContext(Dispatchers.IO) {
        val intlList = fetchRss("https://news.google.com/rss/search?q=internacional&hl=es-419&gl=US&ceid=US:es-419", true)
        val natList = fetchRss("https://news.google.com/rss/search?q=colombia&hl=es-419&gl=CO&ceid=CO:es-419", false)

        val intl = intlList.firstOrNull()
        val nat1 = natList.getOrNull(0)
        val nat2 = natList.getOrNull(1)

        val combined = mutableListOf<NewsItem>()
        if (intl != null) combined.add(intl)
        if (nat1 != null) combined.add(nat1)
        if (nat2 != null) combined.add(nat2)
        combined.addAll(intlList.drop(1).take(2))
        combined.addAll(natList.drop(2).take(2))

        NewsBriefing(
            international = intl,
            national1 = nat1,
            national2 = nat2,
            allItems = combined
        )
    }

    private fun fetchRss(url: String, isInternational: Boolean): List<NewsItem> {
        val items = mutableListOf<NewsItem>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) AtlasAssistant/1.1")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return items
                val xml = response.body?.string() ?: return items
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val parser = factory.newPullParser()
                parser.setInput(StringReader(xml))

                var eventType = parser.eventType
                var insideItem = false
                var currentTag = ""
                var title = ""
                var link = ""
                var desc = ""
                var pubDate = ""
                var source = if (isInternational) "Google News Internacional" else "Google News Colombia"

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name.lowercase()
                            if (currentTag == "item") {
                                insideItem = true
                                title = ""
                                link = ""
                                desc = ""
                                pubDate = ""
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (insideItem) {
                                val text = parser.text?.trim().orEmpty()
                                if (text.isNotEmpty()) {
                                    when (currentTag) {
                                        "title" -> title += text
                                        "link" -> link += text
                                        "description" -> desc += text
                                        "pubdate" -> pubDate += text
                                        "source" -> source = text
                                    }
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name.equals("item", ignoreCase = true)) {
                                insideItem = false
                                if (title.isNotBlank()) {
                                    val cleanDesc = cleanHtml(desc)
                                    items.add(
                                        NewsItem(
                                            title = cleanHtml(title),
                                            summary = cleanDesc.ifBlank { "Sin resumen disponible." },
                                            link = link,
                                            pubDate = pubDate,
                                            source = source,
                                            isInternational = isInternational
                                        )
                                    )
                                }
                            }
                            currentTag = ""
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    private fun cleanHtml(html: String): String {
        return try {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (e: Exception) {
            html.replace(Regex("<.*?>"), "").trim()
        }
    }
}
