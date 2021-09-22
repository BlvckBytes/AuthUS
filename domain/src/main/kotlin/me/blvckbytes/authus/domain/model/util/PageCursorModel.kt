package me.blvckbytes.authus.domain.model.util

class PageCursorModel(
    // How many results to show at max
    var limit: Int,

    // How many records to skip, counting from the start
    var offset: Long,

    // Sorting request, mapping fields to their sort-direction
    var sortBy: Map<String, SortDirection>,

    // Filter request, operations based on fields
    var filterBy: FilterRequest,

    // How many total items existing in storage
    var totalItems: Long,

    // How many items have been responded
    var respondedItems: Int
)