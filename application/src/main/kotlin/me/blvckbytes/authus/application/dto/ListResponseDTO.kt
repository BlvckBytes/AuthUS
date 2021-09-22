package me.blvckbytes.authus.application.dto

import com.fasterxml.jackson.annotation.JsonInclude

class ListResponseDTO<T>(
    val items: List<T>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val page_cursor: PageCursorDTO? = null
)