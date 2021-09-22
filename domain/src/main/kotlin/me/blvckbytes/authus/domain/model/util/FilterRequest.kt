package me.blvckbytes.authus.domain.model.util

class FilterRequest(
    val operators: Map<String, List<Pair<FilterOperation, String>>>,
    val connections: Map<String, FilterConnection>,
    val groupingConnection: FilterConnection
)