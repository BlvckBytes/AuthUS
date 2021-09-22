package me.blvckbytes.authus.application.dto

import me.blvckbytes.authus.application.config.PageCursorParamResolver
import me.blvckbytes.authus.domain.model.util.PageCursorModel

class PageCursorDTO(
    var limit: Int,
    var offset: Long,
    var sort_by: String,
    var filter_by: String,
    var total_items: Long,
    var responded_items: Int
)

fun PageCursorModel.toDTO(): PageCursorDTO {
    return PageCursorDTO(
        limit, offset, PageCursorParamResolver.reconstructSortRequest(sortBy),
        PageCursorParamResolver.reconstructFilterByRequest(filterBy),
        totalItems, respondedItems
    )
}