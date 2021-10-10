package me.blvckbytes.authus.rest.config

import me.blvckbytes.authus.domain.exception.FilterExceptionReason
import me.blvckbytes.authus.domain.exception.InvalidFilterException
import me.blvckbytes.authus.domain.exception.InvalidSortRequestException
import me.blvckbytes.authus.domain.model.util.*
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import kotlin.math.max
import kotlin.math.min

object PageCursorParamResolver : HandlerMethodArgumentResolver {

    // Constants used for processing
    private const val filterParamPrefix = "filter_by_"
    private val filterRequestRegex = Regex("^[A-Za-z_]+\\[[A-Za-z]+]$")

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        // Has the corresponding annotation applied
        return parameter.hasParameterAnnotation(PageCursorParam::class.java) &&
        // Is of result wrapper type
              parameter.parameterType == PageCursorModel::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): PageCursorModel {
        val sortRequest = parseSortRequest(webRequest)
        val filterRequest = parseFilterRequest(webRequest)

        // Lower bound: 1, upper bound: 100, fallback: 5
        val limit = min(100, max(1,
            webRequest.parameterMap["limit"]?.firstOrNull()?.toIntOrNull() ?: 5
        ))

        // Lower bound: 0, no upper bound, fallback: 0
        val offset = max(0,
            webRequest.parameterMap["offset"]?.firstOrNull()?.toLongOrNull() ?: 0
        )

        // Create final page-cursor from all collected data
        return PageCursorModel(limit, offset, sortRequest, filterRequest, 0, 0)
    }

    private fun parseSortRequest(webRequest: NativeWebRequest): Map<String, SortDirection> {
        val requests = mutableMapOf<String, SortDirection>()

        // Iterate parameter-array
        webRequest.parameterMap["sort_by"]?.forEach {
            var dir: SortDirection? = null

            // Map symbol-prefixes to sort-directions
            if (it.first() == '+') dir = SortDirection.ASC
            if (it.first() == '-') dir = SortDirection.DESC

            // Append request, throw exception if prefix was unknown
            requests[it.substring(1)] = dir ?: throw InvalidSortRequestException(it)
        }
        return requests
    }

    private fun parseFilterConnection(value: String): FilterConnection {
        return FilterConnection.values().firstOrNull {
            fConn -> fConn.toString().equals(value, ignoreCase = true)
        } ?: throw InvalidFilterException(FilterExceptionReason.INVALID_CONNECTION, value)
    }

    private fun parseFilterRequest(webRequest: NativeWebRequest): FilterRequest {
        // Collecting lists
        val connections = mutableMapOf<String, FilterConnection>()
        val operators = mutableMapOf<String, MutableList<Pair<FilterOperation, String>>>()
        var groupingConnection = FilterConnection.AND

        // Find all request params that start with the filter prefix
        val params = webRequest.parameterMap.filter {
            it.key.startsWith(filterParamPrefix.substring(0, filterParamPrefix.length - 1))
        }

        params.forEach {

            // Specifies global connection (no field name)
            if (it.key.substring(filterParamPrefix.length - 1).startsWith("[")) {

                // Global operation only supports CONN
                if (!it.key.substring(filterParamPrefix.length, it.key.length - 1).equals("conn", ignoreCase = true))
                    throw InvalidFilterException(FilterExceptionReason.INVALID_OPERATION, "${it.key}=${it.value[0]}")

                // Set grouping connection and skip all other processing
                groupingConnection = parseFilterConnection(it.value[0])
                return@forEach
            }

            // Remove prefix from model key name
            val keyName = it.key.substring(filterParamPrefix.length)

            // Same key values are collected together, handle separately
            it.value.forEach { value ->

                // Unparsable format encountered
                if (!filterRequestRegex.matches(keyName))
                    throw InvalidFilterException(FilterExceptionReason.INVALID_FORMAT, it.key)

                // Parse key and operation from
                val modelKey = keyName.substring(0, keyName.indexOf("[")).lowercase()
                val operation = keyName.substring(keyName.indexOf("[") + 1, keyName.length - 1)

                // Operation is based on filter-connection
                if (operation.equals("conn", ignoreCase = true))
                    connections[modelKey] = parseFilterConnection(value)

                // Operation is based on actual operation
                else {
                    // Try parse filter-operation
                    val fOp = FilterOperation.values().firstOrNull {
                            fConn -> fConn.toString().equals(operation, ignoreCase = true)
                    } ?: throw InvalidFilterException(FilterExceptionReason.INVALID_OPERATION, operation)

                    // Create empty list if key is yet unknown to map
                    if (!operators.containsKey(modelKey))
                        operators[modelKey] = mutableListOf()

                    // Append operation
                    operators[modelKey]?.add(Pair(fOp, value))
                }
            }
        }

        return FilterRequest(operators, connections, groupingConnection)
    }

    fun reconstructFilterByRequest(filterRequest: FilterRequest): String {
        var filterBy = ""

        // Append grouping connector
        filterBy += "${filterParamPrefix.substring(0, filterParamPrefix.length - 1)}[conn]=${filterRequest.groupingConnection},"

        // Append operators
        filterRequest.operators.forEach {
            it.value.forEach { op ->
                filterBy += "$filterParamPrefix${it.key}[${op.first}]=${op.second},"
            }
        }

        // Append connections
        filterRequest.connections.forEach {
            filterBy += "$filterParamPrefix${it.key}[conn]=${it.value},"
        }

        // Remove trailing comma
        return if (filterBy.isNotEmpty()) filterBy.substring(0, filterBy.length - 1) else filterBy
    }

    fun reconstructSortRequest(sortRequest: Map<String, SortDirection>): String {
        var sortBy = ""

        // Append sorted keys
        sortRequest.keys.forEachIndexed { index, field ->
            val dir = when(sortRequest[field]) {
                SortDirection.ASC -> "+"
                SortDirection.DESC -> "-"
                else -> "?"
            }
            sortBy += dir + field + (if (index == sortRequest.size - 1 ) "" else ",")
        }
        return sortBy
    }
}