package me.blvckbytes.authus

import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import me.blvckbytes.authus.application.dto.PermissionDTO
import me.blvckbytes.authus.domain.model.util.SortDirection
import me.blvckbytes.authus.util.ITBase
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.Comparator
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1

class PermissionsIT : ITBase() {

    @Test
    fun `create a permission and fetch it`() {
        val data = createPermission()

        makeRequest()
            .get("/permissions/${data.id}")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .spec(permissionSpec(data))
    }

    @Test
    fun `create a permission and delete it`() {
        val data = createPermission()

        makeRequest()
            .delete("/permissions/${data.id}")
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value())

        makeRequest()
            .delete("/permissions/${data.id}")
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `create a permission and change it`() {
        val data = createPermission()
        val changed = PermissionDTO(data.id, generateString(), generateString())

        makeRequest()
            .body(changed)
            .put("/permissions/${data.id}")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .spec(permissionSpec(changed))

        makeRequest()
            .get("/permissions/${data.id}")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .spec(permissionSpec(changed))
    }

    @Test
    fun `provoke a node collision by change`() {
        val data = createPermission()
        val data2 = createPermission()

        makeRequest()
            .body(data)
            .put("/permissions/${data2.id}")
            .then().log().all()
            .statusCode(HttpStatus.CONFLICT.value())
    }

    @Test
    fun `provoke a node collision by creation`() {
        val data = createPermission()

        makeRequest()
            .body(data)
            .post("/permissions")
            .then().log().all()
            .statusCode(HttpStatus.CONFLICT.value())
    }

    @Test
    fun `list multiple permissions`() {
        val pageSize = 10
        val pages = 10

        /*val data = (1..pageSize * pages)
            .map { createPermission() }
            .sortedWith(compareBy(PermissionDTO::node))
            .sortedWith(
                // Sorting +node,-description
                compareBy<PermissionDTO>{ it.node }.thenByDescending { it.description }
            )
         */

        val values = mutableMapOf<String, MutableList<String?>>()
        (1..100).map { createPermission() }.forEach {
            values["id"] = values["id"]?.plus(it.id?.toString())?.toMutableList() ?: mutableListOf()
            values["description"] = values["description"]?.plus(it.description)?.toMutableList() ?: mutableListOf()
            values["node"] = values["node"]?.plus(it.node)?.toMutableList() ?: mutableListOf()
        }

        validatePaginator("/permissions", values)

        /*
        for (page in (0 until pages)) {
            val pageContent = data.subList(page * pageSize, page * pageSize + pageSize)
            makeRequest()
                .queryParam("limit", pageSize)
                .queryParam("offset", page * pageSize)
                .queryParam("sort_by", "+node", "-description")
                .get("/permissions")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("items.id", hasItems(*pageContent.map { it.id.toString() }.toTypedArray()))
                .body("items.node", hasItems(*pageContent.map { it.node.toString() }.toTypedArray()))
                .body("items.description", hasItems(*pageContent.map { it.description.toString() }.toTypedArray()))
                .body("page_cursor.limit", equalTo(pageSize))
                .body("page_cursor.offset", equalTo(page * pageSize))
                .body("page_cursor.responded_items", lessThanOrEqualTo(pageSize))
                .body("page_cursor.total_items", equalTo(data.size))
                .body("page_cursor.sort_by", equalTo("+node,-description"))
        }
         */
    }

    private fun validatePaginator(path: String, data: Map<String, List<String?>>) {

    }

    private fun createPermission(): PermissionDTO {
        val data = PermissionDTO(null, generateString(), generateString())

        val res = makeRequest()
            .body(data)
            .`when`().post("/permissions")
            .then()
            .statusCode(HttpStatus.CREATED.value())

        data.id = UUID.fromString(res
            .extract()
            .path("id"))

        res.spec(permissionSpec(data))
        return data
    }

    private fun permissionSpec(data: PermissionDTO): ResponseSpecification {
        return ResponseSpecBuilder()
            .expectBody("id", equalTo(data.id.toString()))
            .expectBody("node", equalTo(data.node))
            .expectBody("description", equalTo(data.description))
            .build()
    }
}