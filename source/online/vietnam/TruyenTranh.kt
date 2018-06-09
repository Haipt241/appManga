import com.bigberry.comicvn.network.GET
import com.bigberry.comicvn.source.model.*
import com.bigberry.comicvn.source.online.ParsedHttpSource
import com.bigberry.comicvn.util.asJsoup
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

class TruyenTranh : ParsedHttpSource() {

    override val id: Long = 11

    override val name = "Truyentranh"

    override val baseUrl = "http://truyentranh.net"

    override val lang = "vi"

    override val supportsLatest = true

//    override val client: OkHttpClient = network.cloudflareClient

    override fun popularMangaSelector() = "div.mainpage-manga"

    override fun latestUpdatesSelector() = "div.mainpage-manga"

    override fun popularMangaRequest(page: Int): Request
    {
//            return GET("$baseUrl/the-loai/Co-dai.64.html", headers)
            return GET("http://truyentranhviet.org:8088/manga/genre")
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/danh-sach.tmoinhat.html?p=$page", headers)
    }

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

            element.select("a").first().let {
                manga.setUrlWithoutDomain(it.attr("href"))
                manga.title = it.attr("title")
            }

        return manga
    }

    override fun latestUpdatesFromElement(element: Element): SManga {
        return popularMangaFromElement(element)
    }

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val mangas = document.select(popularMangaSelector()).map { element ->
            popularMangaFromElement(element)
        }

        val lastPage = document.select(popularMangaNextPageSelector()).last()
        val activePage = document.select(popularMangaActivePageSelector()).last()
        val hasNextPage = lastPage != activePage

        return MangasPage(mangas, hasNextPage)
    }

    override fun popularMangaNextPageSelector() = "div.pagination > a"

    fun popularMangaActivePageSelector() = "div.pagination > a.active"

    override fun latestUpdatesParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = document.select(latestUpdatesSelector()).map { element ->
            latestUpdatesFromElement(element)
        }

        val lastPage = document.select(latestUpdatesNextPageSelector()).last()
        val activePage = document.select(popularMangaActivePageSelector()).last()
        val hasNextPage = lastPage != activePage

        return MangasPage(mangas, hasNextPage)
    }

    override fun latestUpdatesNextPageSelector(): String = "div.pagination > a"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request
    {
        var url = HttpUrl.parse("$baseUrl")!!.newBuilder().addQueryParameter("q", query)
        (if (filters.isEmpty()) getFilterList() else filters).forEach { filter ->
            when (filter)
            {
//                is Type -> url.addQueryParameter("type", if (filter.state == 0) "-1" else type.indexOf(filter.state.toString()).toString())
                is TextField -> {
                    if (filter.key == "au")
                        url.addQueryParameter("au", filter.state)
                }

                is StatusList -> filter.state.forEachIndexed { index, status ->
                    if (status.state)
                    {
                        when (status.name)
                        {
                            "Chưa hoàn thành" -> url.addQueryParameter("st[]", "1")
                            "Hoàn thành" -> url.addQueryParameter("st[]", "2")
                        }
                    }
                }

                is RatingList -> filter.state.forEachIndexed { index, status ->
                    if (status.state)
                    {
                        when (status.name)
                        {
                            "5 sao" -> url.addQueryParameter("star[]", "5")
                            "4 sao" -> url.addQueryParameter("star[]", "4")
                            "3 sao" -> url.addQueryParameter("star[]", "3")
                            "2 sao" -> url.addQueryParameter("star[]", "2")
                            "1 sao" -> url.addQueryParameter("star[]", "1")
                        }
                    }
                }

                is OrderBy -> {
                    when (filter.state?.index)
                    {
                        0 -> url = url.addPathSegment("tim-kiem.tall.html") //HttpUrl.parse("$baseUrl/tim-kiem.tall.html?").newBuilder()
                        1 -> url = url.addPathSegment("tim-kiem.txemnhieu.html")
                        2 -> url = url.addPathSegment("tim-kiem.txemnhieu.html")
                        3 -> url = url.addPathSegment("tim-kiem.tmoinhat.html")
                    }
                }

                is GenreList -> filter.state.forEachIndexed { index, genre ->
                    if (genre.state)
                        url.addQueryParameter("tl[]", getGenreId(genre.name).toString())
                }
            }
        }
//        url.addQueryParameter("submit", "Tìm+kiếm")
        url.addQueryParameter("p", page.toString())
        return GET(url.toString(), headers)
    }

    override fun searchMangaSelector() = "div.searchlist-items"

    override fun searchMangaFromElement(element: Element): SManga {
        return searchPopularMangaFromElement(element)
    }

    fun searchPopularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        element.select("a").last().let {
            manga.setUrlWithoutDomain(it.attr("href"))
            manga.title = it.text()
        }
        return manga
    }

    override fun searchMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val mangas = document.select(searchMangaSelector()).map { element ->
            searchMangaFromElement(element)
        }

        val lastPage = document.select(searchMangaNextPageSelector()).last()
        val activePage = document.select(popularMangaActivePageSelector()).last()
        val hasNextPage = lastPage != activePage

        return MangasPage(mangas, hasNextPage)
    }

    override fun searchMangaNextPageSelector() = "div.pagination > a"

    override fun mangaDetailsParse(document: Document): SManga
    {
        val manga = SManga.create()

        val infoElement = document.select("div.media-body").first()
        val descriptionUpdate = infoElement.select("p.description-update").first()

        manga.author = descriptionUpdate.select("span").get(3)?.nextSibling().toString()
        val spanGen = descriptionUpdate.select("span").get(2)
        var genre = spanGen?.nextElementSibling()
        var genreString = ""
        while (genre != null && genre.childNodeSize() > 0)
        {
            genreString = genreString.plus(genre.childNode(0).toString()) + ", "
            genre = genre.nextElementSibling()
        }

        if (genreString.length > 2)
        {
            genreString = genreString.dropLast(2)
        }

        manga.genre = genreString
        manga.status = descriptionUpdate?.text().orEmpty().let { parseStatus(it) }

        val elementContent = document.select("div.manga-content").first()

        // Check if element is Search Item or normal list item
        var content = elementContent.select("div").last()
        if (content == null)
        {
            content = elementContent.select("p").last()
        }

        manga.description = content.text().trim()
        manga.thumbnail_url = document.select("div.cover-detail img").first()?.attr("src")
        return manga
    }

    fun parseStatus(status: String) = when {
        status.contains("Chưa hoàn thành") -> SManga.ONGOING
        status.contains("Hoàn thành") -> SManga.COMPLETED
        else -> SManga.UNKNOWN
    }

    override fun chapterListSelector() = "div.content > p"

    override fun chapterFromElement(element: Element): SChapter {
        val urlElement = element.select("a").first()
        val chapter = SChapter.create()
        chapter.setUrlWithoutDomain(urlElement.attr("href"))
        chapter.name = urlElement.attr("title")
        chapter.date_upload = urlElement.select("span").first()?.text()?.let {
//            parseChapterDate(it)
            SimpleDateFormat("dd/MM/yyyy").parse(it).time
        } ?: 0
        return chapter
    }

    private fun parseChapterDate(date: String): Long {
        val dateWords: List<String> = date.split(" ")
        if (dateWords.size == 3) {
            val timeAgo = Integer.parseInt(dateWords[0])
            val dates: Calendar = Calendar.getInstance()
            if (dateWords[1].contains("minute")) {
                dates.add(Calendar.MINUTE, -timeAgo)
            } else if (dateWords[1].contains("hour")) {
                dates.add(Calendar.HOUR_OF_DAY, -timeAgo)
            } else if (dateWords[1].contains("day")) {
                dates.add(Calendar.DAY_OF_YEAR, -timeAgo)
            } else if (dateWords[1].contains("week")) {
                dates.add(Calendar.WEEK_OF_YEAR, -timeAgo)
            } else if (dateWords[1].contains("month")) {
                dates.add(Calendar.MONTH, -timeAgo)
            } else if (dateWords[1].contains("year")) {
                dates.add(Calendar.YEAR, -timeAgo)
            }
            return dates.timeInMillis
        }
        return 0L
    }

    override fun pageListRequest(chapter: SChapter) = GET(baseUrl + chapter.url, headers)

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        var i = 0
        document.select("div.each-page > img").forEach {
            pages.add(Page(i++, "", it.attr("src")))
        }

        if (pages.size == 0)
        {
            document.select("div.OtherText > p > img").forEach {
                pages.add(Page(i++, "", it.attr("src")))
            }
        }

        return pages
    }

    override fun imageUrlRequest(page: Page) = GET(page.url)

    override fun imageUrlParse(document: Document) = ""

    var type = arrayOf("Khác", "Manga", "Manhwa", "Manhua", "Tất cả")
    var status = arrayOf("Chưa hoàn thành", "Hoàn thành")

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
            Status("Hoàn thành", 2),
            Status("Chưa hoàn thành", 1)
    )

    private fun getRatingList() = listOf(
            Rate("5 sao", 5),
            Rate("4 sao", 4),
            Rate("3 sao", 3),
            Rate("2 sao", 2),
            Rate("1 sao", 1)
    )

    private fun getGenreList() = listOf(
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

    private fun getGenreId(genre: String) : Int
    {
        when(genre)
        {
            "4 koma" -> return 62
            "Action" -> return 1
            "Adventure" -> return 3
            "Award winning" -> return 4
            "Comedy" -> return 5
            "Comic" -> return 55
            "Cooking" -> return 6
            "Cổ đại" -> return 64
            "Demons" -> return 7
            "Doujinshi" -> return 8
            "Drama" -> return 9
            "Ecchi" -> return 10
            "Fantasy" -> return 11
            "Gay cấn" -> return 69
            "Gender bender" -> return 12
            "Giống game" -> return 50
            "Hay Nhức Nhói" -> return 61
            "Historical" -> return 14
            "Horror" -> return 15
            "Huyền Huyễn" -> return 56
            "Magic" -> return 16
            "Manga" -> return 58
            "Manhua" -> return 48
            "Manhwa" -> return 49
            "Martial Arts" -> return 17
            "Mature" -> return 18
            "Mecha" -> return 19
            "Medical" -> return 20
            "Military" -> return 21
            "Music" -> return 22
            "Mystery" -> return 23
            "One shot" -> return 24
            "Oneshot" -> return 54
            "Psychological" -> return 25
            "Romance" -> return 26
            "School Life" -> return 27
            "Sci-Fi" -> return 28
            "Seinen" -> return 29
            "Shoujo" -> return 30
            "Shoujo Ai" -> return 31
            "Shounen" -> return 32
            "Shounen Ai" -> return 33
            "Slice of Life" -> return 34
            "Smut" -> return 35
            "Sports" -> return 36
            "Staff pick" -> return 37
            "Super power" -> return 38
            "Supernatural" -> return 39
            "Tragedy" -> return 40
            "Trap (Crossdressing)" -> return 57
            "Trinh Thám" -> return 65
            "VNComic" -> return 52
            "Vampire" -> return 41
            "Webtoon" -> return 42
            "Xuyên Không" -> return 59
            "Yaoi" -> return 43
            "Yuri" -> return 44
            "boy love" -> return 63
            "kinh dị" -> return 68
            "Đam Mỹ" -> return 60
        }

        return 0
    }
}