package com.example.vinilosmobile

import com.google.gson.JsonParser
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/** Stable HTTP fixtures exercise Retrofit, DTO mapping, repositories and UI together. */
class FixtureApiInterceptor : Interceptor {
    val responseCodes = ConcurrentHashMap<String, Int>()
    val requestedPaths: MutableList<String> = Collections.synchronizedList(mutableListOf())
    @Volatile var emptyAlbums = false
    @Volatile var emptyArtists = false
    @Volatile var albumWithoutTracks = false

    /** JSON body of the last POST /albums, as the app sent it. */
    @Volatile var sentAlbum: String? = null
    @Volatile private var createdAlbum: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        // GET keeps the bare path so existing tests and response codes still match.
        val key = if (request.method == "GET") path else "${request.method} $path"
        requestedPaths.add(key)
        val code = responseCodes[key] ?: when (key) {
            "/albums", "/albums/100", "/musicians", "/bands", "/musicians/100",
            "/bands/101", "/performerprizes", "POST /albums" -> 200
            "/albums/$CREATED_ALBUM_ID" -> if (createdAlbum != null) 200 else 404
            else -> 404
        }
        val body = if (code == 200) when (key) {
            "/albums" -> albums()
            "POST /albums" -> createAlbum(request)
            "/albums/$CREATED_ALBUM_ID" -> createdAlbumDetail()
            "/albums/100" -> if (albumWithoutTracks) ALBUM_WITHOUT_TRACKS else ALBUM_DETAIL
            "/musicians" -> if (emptyArtists) "[]" else MUSICIANS
            "/bands" -> if (emptyArtists) "[]" else BANDS
            "/musicians/100" -> MUSICIAN_DETAIL
            "/bands/101" -> BAND_DETAIL
            "/performerprizes" -> PRIZES
            else -> "{}"
        } else "{}"
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Fixture error")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    // Like BackVynils, the new album is not added at the start of GET /albums.
    private fun albums(): String {
        if (emptyAlbums) return "[]"
        val created = createdAlbum ?: return ALBUMS
        return ALBUMS.trimEnd().removeSuffix("]") + ",\n" + created + "]"
    }

    private fun createAlbum(request: Request): String {
        val sent = Buffer().also { request.body?.writeTo(it) }.readUtf8()
        sentAlbum = sent
        val created = JsonParser.parseString(sent).asJsonObject.apply { addProperty("id", CREATED_ALBUM_ID) }
        return created.toString().also { createdAlbum = it }
    }

    private fun createdAlbumDetail(): String =
        JsonParser.parseString(createdAlbum).asJsonObject.apply {
            add("tracks", JsonParser.parseString("[]"))
            add("comments", JsonParser.parseString("[]"))
            add("performers", JsonParser.parseString("[]"))
        }.toString()

    companion object {
        const val CREATED_ALBUM_ID = 102L

        private val ALBUMS = """[
          {"id":100,"name":"Buscando América","genre":"Salsa","releaseDate":"1984-08-01",
           "recordLabel":"Elektra","description":"Álbum de Rubén Blades",
           "performers":[{"id":100,"name":"Rubén Blades"}]},
          {"id":101,"name":"A Night at the Opera","genre":"Rock","releaseDate":"1975-11-21",
           "recordLabel":"EMI","description":"Álbum de Queen",
           "performers":[{"id":101,"name":"Queen"}]}
        ]"""
        private val ALBUM_DETAIL = """{
          "id":100,"name":"Buscando América","genre":"Salsa","releaseDate":"1984-08-01",
          "recordLabel":"Elektra","description":"Álbum de Rubén Blades",
          "performers":[{"id":100,"name":"Rubén Blades"}],
          "tracks":[{"id":500,"name":"Decisiones","duration":"5:05"},
                    {"id":501,"name":"Desapariciones","duration":"6:29"}],
          "comments":[{"id":700,"description":"Gran álbum","rating":5}]
        }"""
        private val ALBUM_WITHOUT_TRACKS = """{
          "id":100,"name":"Buscando América","genre":"Salsa","releaseDate":"1984-08-01",
          "recordLabel":"Elektra","performers":[{"id":100,"name":"Rubén Blades"}],
          "tracks":[],"comments":[]
        }"""
        private val MUSICIANS = """[
          {"id":100,"name":"Rubén Blades","birthDate":"1948-07-16",
           "albums":[{"id":100,"name":"Buscando América","releaseDate":"1984-08-01"}]}
        ]"""
        private val BANDS = """[
          {"id":101,"name":"Queen","creationDate":"1970-01-01",
           "albums":[{"id":101,"name":"A Night at the Opera","releaseDate":"1975-11-21"}]}
        ]"""
        private val MUSICIAN_DETAIL = """{
          "id":100,"name":"Rubén Blades","birthDate":"1948-07-16",
          "description":"Cantante y compositor panameño.",
          "albums":[{"id":100,"name":"Buscando América","releaseDate":"1984-08-01"}],
          "performerPrizes":[{"id":800,"premiationDate":"1985-01-01"}]
        }"""
        private val BAND_DETAIL = """{
          "id":101,"name":"Queen","creationDate":"1970-01-01",
          "description":"Banda británica de rock.",
          "albums":[{"id":101,"name":"A Night at the Opera","releaseDate":"1975-11-21"}],
          "musicians":[{"id":102,"name":"Freddie Mercury"}],
          "performerPrizes":[]
        }"""
        private val PRIZES = """[{"id":800,"prize":{"id":900,"name":"Grammy Award"}}]"""
    }
}
