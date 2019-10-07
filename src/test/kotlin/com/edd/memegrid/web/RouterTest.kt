package com.edd.memegrid.web

import com.edd.memegrid.memes.Meme
import com.edd.memegrid.memes.MemeManager
import com.edd.memegrid.util.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.*
import spark.Spark
import java.lang.Exception

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouterTest {
    companion object {
        const val PORT = 4444

        val mapper = jacksonObjectMapper()

        val mockImageValidator = mockk<ImageValidator>()
        val mockMemeManager = mockk<MemeManager>()
        val mockTemplateReader = mockk<TemplateReader>()

        @BeforeAll
        @JvmStatic
        fun startServer() {
            Spark.port(PORT)
            RestAssured.port = PORT

            every { mockTemplateReader.read(any(), any()) } returns "index"

            val router = Router(
                    imageValidator = mockImageValidator,
                    memeManager = mockMemeManager,
                    reader = mockTemplateReader,
                    domain = "domain")

            router.start()
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            Spark.stop()
        }
    }

    @BeforeEach
    fun beforeEach() {
        clearMocks(mockImageValidator, mockMemeManager, mockTemplateReader)
    }
    @Test
    fun `should return 200 given get request with HTML content type`() {
        Given {
            accept(ContentType.HTML)
        } When {
            get("/")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `should return 415 given get request with non HTML content type`() {
        every { mockTemplateReader.read(any(), any()) } returns "index"

        Given {
            accept(ContentType.JSON)
        } When {
            get("/")
        } Then {
            statusCode(STATUS_CODE_UNSUPPORTED_MEDIA_TYPE)
        }
    }

    @Test
    fun `should return 200 and memes given get request`() {

        val memes = listOf(Meme(1, "Good meme", "/meme/url"),
                Meme(2, "Better meme", "/better/meme/url"))

        every { mockMemeManager.getMemes() } returns memes

        val resultJsonNode = Given {
            accept(ContentType.JSON)
        } When {
            get("/api/memes")
        } Then {
            statusCode(200)
        } Extract {
            `as`(JsonNode::class.java)
        }

        assertThat(resultJsonNode.toMemeList(), equalTo(memes))
    }

    @Test
    fun `should return 404 given get request for non existent meme`() {
        val invalidMemeId: Long = 1
        every { mockMemeManager.getMeme(invalidMemeId) } returns null

        given()
                .accept(ContentType.JSON)
        .get("/api/memes/{id}", invalidMemeId)
        .then()
                .statusCode(STATUS_CODE_NOT_FOUND)
    }

    @Test
    fun `should return 200 and meme in payload given get request for existing meme`() {
        val validMemeId: Long = 1
        val meme = Meme(validMemeId, "Good meme", "/meme/url")
        every { mockMemeManager.getMeme(validMemeId) } returns meme

        val resultMeme = Given {
            accept(ContentType.JSON)
        } When {
            get("/api/memes/{id}", validMemeId)
        } Then {
            statusCode(200)
        } Extract {
            `as`(Meme::class.java)
        }

        assertThat(resultMeme, equalTo(meme))
    }

    @Test
    fun `should return 400 given post request with invalid url`() {
        val meme = Meme(1, "Good meme", "invalidUrl")
        every { mockImageValidator.isValid(any()) } returns false

        Given {
            accept(ContentType.JSON)
            body(meme)
        } When {
            post("/api/memes")
        } Then {
            statusCode(STATUS_CODE_BAD_REQUEST)
        }
    }

    @Test
    fun `should return 400 given post request with meme that already exists`() {
        val meme = Meme(1, "Good meme", "url")
        every { mockImageValidator.isValid(any()) } returns true
        every { mockMemeManager.getMeme(any<String>()) } returns meme

        Given {
            accept(ContentType.JSON)
            body(meme)
        } When {
            post("/api/memes")
        } Then {
            statusCode(STATUS_CODE_BAD_REQUEST)
        }
    }

    @Test
    fun `should return 201 and saved meme given post request with valid new meme`() {
        val meme = Meme(1, "Good meme", "url")
        every { mockImageValidator.isValid(any()) } returns true
        every { mockMemeManager.getMeme(any<String>()) } returns null
        every { mockMemeManager.saveMeme(any(), any()) } returns meme

        val resultMeme = Given {
            accept(ContentType.JSON)
            body(meme)
        } When {
            post("/api/memes")
        } Then {
            statusCode(STATUS_CODE_CREATED)
        } Extract {
            `as`(Meme::class.java)
        }

        assertThat(resultMeme, equalTo(meme))
    }

    @Test
    fun `should return 200 and deleted meme given delete request for existing meme`() {
        val memeId: Long = 1
        val meme = Meme(memeId, "Good meme", "url")
        every { mockMemeManager.getMeme(memeId) } returns meme
        every { mockMemeManager.deleteMeme(meme) } returns 1

        val resultMeme = given()
                    .accept(ContentType.JSON)
                .delete("/api/memes/{id}", memeId)
                .then()
                    .statusCode(200)
                .extract()
                    .`as`(Meme::class.java)

        assertThat(resultMeme, equalTo(meme))
    }

    @Test
    fun `should return 200 and memes given get request for page`() {

        val memesOnPage = listOf(Meme(1, "Good meme", "/meme/url"),
                Meme(2, "Better meme", "/better/meme/url"))

        every { mockMemeManager.getMemes(any()) } returns memesOnPage

        val page: Long = 1
        val resultJsonNode = given()
            .accept(ContentType.JSON)
        .get("/api/memes/page/{page}", page)
        .then()
            .statusCode(200)
        .extract()
                .`as`(JsonNode::class.java)

        assertThat(resultJsonNode.toMemeList(), equalTo(memesOnPage))
    }

    @Test
    fun `should return 404 with json body given get request that accepts json to invalid path`() {
        Given {
            accept(ContentType.JSON)
        } When {
            get("/invalid")
        } Then {
            statusCode(STATUS_CODE_NOT_FOUND)
            body(containsString("Path does not exist"))
        }
    }

    @Test
    fun `should return 404 with html body given get request that accepts html to invalid path`() {
        val htmlResponseBody = "Not found HTML"
        every { mockTemplateReader.read(any(), any()) } returns htmlResponseBody

        Given {
            accept(ContentType.HTML)
        } When {
            get("/invalid")
        } Then {
            statusCode(STATUS_CODE_NOT_FOUND)
            body(containsString(htmlResponseBody))
        }
    }

    @Test
    fun `should return 500 when saving a new meme fails`() {
        val meme = Meme(1, "Good meme", "url")
        every { mockImageValidator.isValid(any()) } returns true
        every { mockMemeManager.getMeme(any<String>()) } returns null
        every { mockMemeManager.saveMeme(any(), any()) } throws Exception()

        Given {
            accept(ContentType.JSON)
            body(meme)
        } When {
            post("/api/memes")
        } Then {
            statusCode(STATUS_CODE_SERVER_ERROR)
        }
    }

    private fun JsonNode.toMemeList() = mapper.readValue<List<Meme>>(this.toString())
}
