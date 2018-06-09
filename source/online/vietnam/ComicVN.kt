package com.bigberry.comicvn.source.online.vietnam

import com.bigberry.comicvn.Constants
import com.bigberry.comicvn.network.POST
import com.bigberry.comicvn.source.model.*
import com.bigberry.comicvn.source.online.HttpSource
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import rx.Observable
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Created by ThangBK on 5/18/17.
 */
class ComicVN : HttpSource() {
    override val id: Long = 13

    override val name = "TruyenTranh.Net"

//    override val baseUrl = "http://192.168.1.5:8100/api"
    override val baseUrl = "https://truyentranhviet.org"

    val baseUrlApi = "https://truyentranhviet.org:8888/api"

    override val lang = "vi"

    override val supportsLatest = true

    override fun popularMangaRequest(page: Int): Request {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val jsonObject = JSONObject()
        jsonObject.put("token", "1234567899")
        jsonObject.put("page", page)
        jsonObject.put("source", id)

        val body = RequestBody.create(JSON, jsonObject.toString());

        return POST(baseUrlApi + "/manga/get", body = body)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val json = response.body()!!.string()
        val jsonObject = JSONObject(json)
        val code = jsonObject["code"]
        val mangas = ArrayList<SManga>()
        if (code == 200) {
            val mangaJsonArray = jsonObject.getJSONArray("message")
            for (i in 0..mangaJsonArray.length() - 1) {
                val mangaJson = mangaJsonArray.getJSONObject(i)
                val manga = SManga.create()
                manga.setUrlWithoutDomain(mangaJson.getString("url"))
                manga.mangaId = mangaJson.getLong("id")
//                manga.url = mangaJson.getString("url")
                manga.title = mangaJson.getString("name")
                manga.author = mangaJson.getString("author")
                manga.genre = mangaJson.getString("genre")
                manga.status = mangaJson.getInt("status")
                manga.description = mangaJson.getString("summary")
                manga.thumbnail_url = mangaJson.getString("thumb")
                mangas.add(manga)
            }
        }

        return MangasPage(mangas, mangas.size >= 30)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val jsonObject = JSONObject()
        jsonObject.put("token", "1234567899")
        jsonObject.put("page", page)
        jsonObject.put("source", id)
        val params = JSONObject()

        (if (filters.isEmpty()) getFilterList() else filters).forEach { filter ->
            when (filter) {
                is TextField -> {
                    if (filter.key == "au") {
                        params.put("au", filter.state)
                    }
                }

                is StatusList -> {

                    var statusValue = 0
                    filter.state.forEachIndexed { index, status ->
                        if (status.state) {
                            when (status.name) {
                                "Đang tiến hành" -> {
                                    statusValue = statusValue.or(1)
                                }

                                "Hoàn thành" -> {
                                    statusValue = statusValue.or(2)
                                }

                                "Tạm ngừng" -> {
                                    statusValue = statusValue.or(4)
                                }
                            }
                        }
                    }

                    params.put("status", statusValue)
                }

                is RatingList -> {
                    var rating = 0
                    filter.state.forEachIndexed { index, status ->
                        if (status.state) {
                            when (status.name) {
                                "5 sao" -> rating = rating.or(16)
                                "4 sao" -> rating = rating.or(8)
                                "3 sao" -> rating = rating.or(4)
                                "2 sao" -> rating = rating.or(2)
                                "1 sao" -> rating = rating.or(1)
                            }
                        }
                    }

                    params.put("rating", rating)
                }

                is OrderBy -> {
                    if (filter.state != null) {
                        val order = JSONObject()
                        order.put("type", filter.state?.index)
                        order.put("asc", filter.state?.ascending)
                        params.put("order", order)
                    }
                    /*when (filter.state?.index)
                    {
                        0 -> url = url.addPathSegment("tim-kiem.tall.html") //HttpUrl.parse("$baseUrl/tim-kiem.tall.html?").newBuilder()
                        1 -> url = url.addPathSegment("tim-kiem.txemnhieu.html")
                        2 -> url = url.addPathSegment("tim-kiem.txemnhieu.html")
                        3 -> url = url.addPathSegment("tim-kiem.tmoinhat.html")
                    }*/
                }

                is GenreList -> {
                    val genreList = JSONArray()
                    filter.state.forEachIndexed { index, genre ->
                        if (genre.state) {
                            genreList.put(genre.name)
                        }
                    }

                    params.put("genre", genreList)
                }
            }
        }

        params.put("name", query)
        jsonObject.put("query", params)
        val body = RequestBody.create(JSON, jsonObject.toString())
        return POST(baseUrlApi + "/manga/search", body = body)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        return popularMangaParse(response)
    }

    override fun latestUpdatesRequest(page: Int): Request {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val jsonObject = JSONObject()
        jsonObject.put("token", "1234567899")
        jsonObject.put("page", page)
        jsonObject.put("source", id)

        val body = RequestBody.create(JSON, jsonObject.toString())

        return POST(baseUrlApi + "/manga/latest", body = body)
    }

    override fun latestUpdatesParse(response: Response): MangasPage {
        return popularMangaParse(response)
    }

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return Observable.just(manga).map { manga -> manga.apply { initialized = true } }
    }

    override fun chapterListRequest(manga: SManga): Request {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val jsonObject = JSONObject()
        jsonObject.put("token", "1234567899")
        jsonObject.put("mangaid", manga.mangaId)

        val body = RequestBody.create(JSON, jsonObject.toString())
        return POST(baseUrlApi + "/manga/chapter", body = body)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val json = response.body()!!.string()
        val jsonObject = JSONObject(json)
        val code = jsonObject["code"]
        val chapters = ArrayList<SChapter>()
        if (code == 200) {
            val chapterJsonArray = jsonObject.getJSONArray("message")

            for (i in 0..chapterJsonArray.length() - 1) {
                val chapterJson = chapterJsonArray.getJSONObject(i)
                val chapter = SChapter.create()
                chapter.chapterId = chapterJson.getLong("id")
                chapter.name = chapterJson.getString("name")
//                chapter.url = chapterJson.getString("url")
                chapter.setUrlWithoutDomain(chapterJson.getString("url"))
                chapter.date_upload = chapterJson.getLong("time") * 1000
                chapters.add(chapter)
            }
        }

        return chapters
    }

    override fun pageListRequest(chapter: SChapter): Request {
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val jsonObject = JSONObject()
        jsonObject.put("token", "1234567899")
        jsonObject.put("chapterid", chapter.chapterId)

        val body = RequestBody.create(JSON, jsonObject.toString())
        return POST(baseUrlApi + "/manga/page", body = body)
    }

    override fun pageListParse(response: Response): List<Page> {
        val json = response.body()!!.string()
        val jsonObject = JSONObject(json)
        val code = jsonObject["code"]
        val pages = mutableListOf<Page>()
        if (code == 200) {
            val pageJsonArray = jsonObject.getJSONArray("message")
            for (i in 0..pageJsonArray.length() - 1) {
                val pageUrl = pageJsonArray[i].toString()
                if (pageUrl.contains(Constants.GOOGLE_PROXY)) {
                    val tempUrl = Jsoup.parse(URLDecoder.decode(pageUrl)).text()
                    val realPageUrl = getUrlParameters(url = tempUrl, key = "url")
                    pages.add(Page(i, "", realPageUrl))
                    continue
                }

                pages.add(Page(i, "", pageUrl))
            }
        }
        // Encode page
        return pages
    }

    @Throws(UnsupportedEncodingException::class)
    fun getUrlParameters(url: String, key: String): String {
        val urlParts = url.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (urlParts.size > 1) {
            val query = urlParts[1]
            for (param in query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val pair = param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val keyPair = URLDecoder.decode(pair[0], "UTF-8")
                if (keyPair != key) {
                    continue
                }
                var value = ""
                if (pair.size > 1) {
                    value = URLDecoder.decode(pair[1], "UTF-8")
                    return value
                }
            }
        }
        return ""
    }

    override fun imageUrlParse(response: Response): String {
        val json = response.body()!!.string()
        return json
    }


    var type = arrayOf("Khác", "Manga", "Manhwa", "Manhua", "Tất cả")
    var status = arrayOf("Đang tiến hành", "Hoàn thành", "Tạm ngừng")

    private class Status(name: String, value: Int) : Filter.CheckBox(name)
    private class Rate(name: String, value: Int) : Filter.CheckBox(name)
    private class RatingList(rates: List<Rate>) : Filter.Group<Rate>("Đánh giá", rates)

    private class TextField(name: String, val key: String) : Filter.Text(name)
    private class StatusList(status: List<Status>) : Filter.Group<Status>("Trạng thái", status)
    private class OrderBy : Filter.Sort("Sắp xếp",
            arrayOf("Tên truyện", "Xem nhiều", "Điểm cao nhất", "Mới nhất"),
            Filter.Sort.Selection(0, true))

    private class Genre(name: String) : Filter.CheckBox(name)
    private class GenreList(genres: List<Genre>) : Filter.Group<Genre>("Thể loại", genres)

    override fun getFilterList() = FilterList(
            TextField("Tác giả", "au"),
            StatusList(getStatusList()),
            RatingList(getRatingList()),
            OrderBy(),
            GenreList(getGenreList())
    )

    private fun getStatusList() = listOf(
            Status("Đang tiến hành", 1),
            Status("Hoàn thành", 2),
            Status("Tạm ngừng", 3)
    )

    private fun getRatingList() = listOf(
            Rate("5 sao", 5),
            Rate("4 sao", 4),
            Rate("3 sao", 3),
            Rate("2 sao", 2),
            Rate("1 sao", 1)
    )

    private fun getGenreList() = listOf(
            Genre("18+"),
            Genre("4 koma"),
            Genre("Action"),
            Genre("Adventure"),
            Genre("Award winning"),
            Genre("Comedy"),
            Genre("Comic"),
            Genre("Cooking"),
            Genre("Cổ đại"),
            Genre("Demons"),
            Genre("Doujinshi"),
            Genre("Drama"),
            Genre("Ecchi"),
            Genre("Fantasy"),
            Genre("Gay cấn"),
            Genre("Gender bender"),
            Genre("Giống game"),
            Genre("Hay Nhức Nhói"),
            Genre("Historical"),
            Genre("Horror"),
            Genre("Huyền Huyễn"),
            Genre("Magic"),
            Genre("Manga"),
            Genre("Manhua"),
            Genre("Manhwa"),
            Genre("Martial Arts"),
            Genre("Mature"),
            Genre("Mecha"),
            Genre("Medical"),
            Genre("Military"),
            Genre("Music"),
            Genre("Mystery"),
            Genre("One shot"),
            Genre("Oneshot"),
            Genre("Psychological"),
            Genre("Romance"),
            Genre("School Life"),
            Genre("Sci-Fi"),
            Genre("Seinen"),
            Genre("Shoujo"),
            Genre("Shoujo Ai"),
            Genre("Shounen"),
            Genre("Shounen Ai"),
            Genre("Slice of Life"),
            Genre("Smut"),
            Genre("Sports"),
            Genre("Staff pick"),
            Genre("Super power"),
            Genre("Supernatural"),
            Genre("Tragedy"),
            Genre("Trap (Crossdressing)"),
            Genre("Trinh Thám"),
            Genre("VNComic"),
            Genre("Vampire"),
            Genre("Webtoon"),
            Genre("Xuyên Không"),
            Genre("Yaoi"),
            Genre("Yuri"),
            Genre("boy love"),
            Genre("kinh dị"),
            Genre("Đam Mỹ")
    )

    private fun getGenre(genre: String): String {
        return genre
    }
}