package me.blvckbytes.authus.infrastructure.repo

import me.blvckbytes.authus.domain.exception.*
import me.blvckbytes.authus.domain.model.util.FilterConnection
import me.blvckbytes.authus.domain.model.util.FilterOperation
import me.blvckbytes.authus.domain.model.util.FilterRequest
import me.blvckbytes.authus.domain.model.util.PageCursorModel
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntity
import me.blvckbytes.authus.infrastructure.entity.base.BaseUUIDEntityClass
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.DateColumnType
import org.joda.time.DateTime
import java.util.*

/**
 * Base of every repository, handles reoccurring tasks like exception-handling
 * and the application of page-cursor to list-requests
 */
abstract class ARepoBase<Model: Any>(
    private val modelName: String,
    protected val table: UUIDTable
) {

    ////////////////////////////////////////////
    //          Exception-Helpers             //
    ////////////////////////////////////////////

    /**
     * Generate a collision-exception based on a field and it's value using
     * the repo's model-name
     */
    protected fun collision(field: String, value: String): ModelCollisionException {
        return ModelCollisionException(modelName, field, value)
    }

    /**
     * Generate a in-use-exception based on the models ID using the repo's model-name
     */
    protected fun inUse(id: UUID): ModelInUseException {
        return ModelInUseException(modelName, id.toString())
    }

    /**
     * Generate a in-use-exception based on an artificial key
     * using the repo's model-name
     */
    protected fun inUse(id: String): ModelInUseException {
        return ModelInUseException(modelName, id)
    }

    /**
     * Generate a not-found-exception based on an artificial key
     * using the repo's model-name
     */
    protected fun notFound(id: String): ModelNotFoundException {
        return ModelNotFoundException(modelName, id)
    }

    /**
     * Generate a not-found-exception based on the models ID
     * using the repo's model-name
     */
    protected fun notFound(id: UUID): ModelNotFoundException {
        return ModelNotFoundException(modelName, id.toString())
    }

    ////////////////////////////////////////////
    //              Pagination                //
    ////////////////////////////////////////////

    /**
     * Apply a page cursor to retrieve a list containing the resulting items, after
     * all available options have been applied
     * @param fullQuery Query containing all tables (and thus fields) that support filtering
     * @param cursor: Page-cursor request to apply
     * @param additionalOp Additional operations to be applied with AND-junction
     * @param entity Entity that will be wrapping individual result-rows
     * @param resultMapper Function to map resulting entities to desired output format
     * @return List of resulting items after mapping, applied page-cursor
     */
    protected fun<R: BaseUUIDEntity, PC: PageCursorModel?, X> applyCursor(
        fullQuery: Query,
        cursor: PC,
        additionalOp: Op<Boolean>? = null,
        entity: BaseUUIDEntityClass<R>,
        resultMapper: (input: R) -> X
    ): Pair<List<X>, PC> {
        // These columns are only allowed to exist within the resulting entity's table
        val entityOnlyCols = arrayOf("id", "createdAt", "updatedAt").map { it.lowercase() }

        // Get all cols from all available tables into one big list (names
        // should always be unique to allow automation!)
        val cols = fullQuery.targets
            .map { table -> table.columns }
            .fold(listOf<Column<*>>()) { acc, curr -> acc.plus(curr) }
            // filter out all entity-only cols that are not from the entity
            .filter { !entityOnlyCols.contains(it.name.lowercase()) || (it.table == entity.table) }

        // Apply filter request from cursor
        var resultingOp = translateFiltersToOp(cols, cursor?.filterBy)

        // Append additional operation from the outside
        if (additionalOp != null)
            resultingOp = resultingOp?.and(additionalOp) ?: additionalOp

        // Return result from resulting OP or just input query if there are no filters and ops
        var resultingQuery = if (resultingOp != null) fullQuery.andWhere { resultingOp } else fullQuery

        // Set total items count before applying limit and offset
        cursor?.totalItems = resultingQuery.count()

        // Apply limit, offset and sort-direction
        if (cursor != null)
            resultingQuery = resultingQuery
                .limit(cursor.limit, cursor.offset)
                .orderBy(*parseCorrespondingColumns(cursor, cols))

        // Now count items that will be responded
        cursor?.respondedItems = resultingQuery.count().toInt()
        return Pair(resultingQuery.map { resultMapper(entity.wrapRow(it)) }, cursor)
    }

    /**
     * Map a internal filter operation to the matching exposed expression operation
     * @param filter Filter to be applied
     * @param column Column that needs to be filtered by
     * @param value Value to pass into expression operation
     */
    private fun<T : Comparable<T>> mapFilterToOperation(filter: FilterOperation, column: ExpressionWithColumnType<in T>, colName: String, value: T?): Op<Boolean> {
        // Only equals and not equals support nullables
        if (value == null && !arrayOf(FilterOperation.EQ, FilterOperation.NEQ).contains(filter))
            throw InvalidFilterException(FilterExceptionReason.NOT_NULLABLE, colName)

        return Op.build { when(filter) {
            FilterOperation.LT -> column.less(value!!)
            FilterOperation.LTE -> column.lessEq(value!!)
            FilterOperation.GT -> column.greater(value!!)
            FilterOperation.GTE -> column.greaterEq(value!!)
            FilterOperation.EQ -> if (value == null) column.isNull() else column.eq(value)
            FilterOperation.NEQ -> if (value == null) column.isNotNull() else column.neq(value)
            FilterOperation.CNT -> { column.castTo<String>(VarCharColumnType()).like("%${value as String}%") }
        } }
    }

    /**
     * Create an operation based on the target column to filter on, and which operation to perform
     * @param column Column to filter on
     * @param operation Operation to apply
     * @param value Value to pass into operation
     */
    private fun createColumnOperation(column: Column<*>, operation: FilterOperation, value: String): Op<Boolean> {
        return when (column.columnType) {
            // String(able) columns
            is VarCharColumnType, is TextColumnType, is CharacterColumnType, is BooleanColumnType -> {
                // Strings only support equals, !equals and contains
                if (arrayOf(FilterOperation.EQ, FilterOperation.NEQ, FilterOperation.CNT).contains(operation))
                    mapFilterToOperation(operation, column.castTo(VarCharColumnType()), column.name, value.ifBlank { null })
                else throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_OPERATION, column.name)
            }

            // UUIDs or entity-IDs
            is UUIDColumnType, is EntityIDColumnType<*> -> {
                val reqId = try {
                    if (value.isBlank()) null else UUID.fromString(value)
                } catch (ex: IllegalArgumentException) {
                    // Unparsable UUID provided
                    throw InvalidFilterException(FilterExceptionReason.NOT_PARSABLE, "(UUID) $value")
                }

                // UUIDs only support equals and !equals, contains would make very little sense
                if (arrayOf(FilterOperation.EQ, FilterOperation.NEQ).contains(operation))
                    mapFilterToOperation(operation, column.castTo(UUIDColumnType()), column.name, reqId)
                else throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_OPERATION, column.name)
            }

            // DateTime columns
            is DateColumnType -> {
                val reqDate = try {
                    if (value.isBlank()) null else DateTime.parse(value)
                } catch (ex: IllegalArgumentException) {
                    // Unparsable date provided
                    throw InvalidFilterException(FilterExceptionReason.NOT_PARSABLE, "(DateTime) $value")
                }

                // DateTime supports equals, !equals, less than + greater than (past/future comparison)
                if (arrayOf(
                    FilterOperation.EQ, FilterOperation.NEQ, FilterOperation.LTE,
                    FilterOperation.LT, FilterOperation.GT, FilterOperation.GTE
                ).contains(operation))
                    mapFilterToOperation(operation, column.castTo(DateColumnType(true)), column.name, reqDate)
                else throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_OPERATION, column.name)
            }

            // All kinds of numbers
            is AutoIncColumnType, is ByteColumnType, is ShortColumnType, is IntegerColumnType,
            is LongColumnType, is FloatColumnType, is DoubleColumnType, is DecimalColumnType -> {
                val reqNumber = if (value.isBlank()) null else value.toDoubleOrNull() ?: throw InvalidFilterException(FilterExceptionReason.NOT_PARSABLE, "(Number) $value")

                // Numbers support equals, !equals, less than + greater than (bigger/smaller comparison)
                if (arrayOf(
                    FilterOperation.EQ, FilterOperation.NEQ, FilterOperation.LTE, FilterOperation.LT,
                    FilterOperation.GT, FilterOperation.GTE
                ).contains(operation))
                    mapFilterToOperation(operation, column.castTo(DoubleColumnType()), column.name, reqNumber)
                else throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_OPERATION, column.name)
            }

            // Unsupported column type for filtering
            else -> throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_FIELD, column.name)
        }
    }

    /**
     * Translate a filter-request to an exposed-operation, filtering on all requested
     * columns and making sure only plausible actions are allowed
     * @param cols List of all available columns, can stem from multiple tables
     * @param fReq Filter request to translate
     * @return Resulting operation, null if no request has been issued
     */
    private fun translateFiltersToOp(cols: List<Column<*>>, fReq: FilterRequest?): Op<Boolean>? {
        if (fReq == null) return null
        var resultingOp: Op<Boolean>? = null

        // Create explicit connections for those assumed implicitly (missing = AND)
        val connections = fReq.operators.keys
            .filter { !fReq.connections.containsKey(it) }
            .map { Pair(it, FilterConnection.AND) }
            .plus(fReq.connections.map { Pair(it.key, it.value) })

        // Handle each connection individual (every field)
        connections.forEach {
            val field = it.first
            val operators = fReq.operators[field]
            val connection = it.second

            // Now apply the operators using this connection
            var connectedOp: Op<Boolean>? = null
            operators?.forEach { op ->

                // Find target column by name
                val tarCol = cols.firstOrNull { col ->
                    col.name.equals(field, ignoreCase = true)
                } ?: throw InvalidFilterException(FilterExceptionReason.UNSUPPORTED_FIELD, field)

                // Create operation from filter on specific column
                val currOp = createColumnOperation(tarCol, op.first, op.second)

                // Append query to connected operation using desired junction
                connectedOp = if (connectedOp == null) currOp else {
                    when (connection) {
                        FilterConnection.AND -> connectedOp!!.and(currOp)
                        FilterConnection.OR -> connectedOp!!.or(currOp)
                    }
                }
            }

            // If there is an OP which contains all connected field operations, append
            // it with grouping-connector to result
            if (connectedOp != null)
                resultingOp = if (resultingOp == null) connectedOp else {
                    when (fReq.groupingConnection) {
                        FilterConnection.AND -> resultingOp!!.and(connectedOp!!)
                        FilterConnection.OR -> resultingOp!!.or(connectedOp!!)
                    }
                }
        }

        return resultingOp
    }

    /**
     * Parses a page-cursor's sort request into pairs of column to sort-order
     * @param cursor Cursor to retrieve sort-request from
     * @param columns Available columns to sort by
     * @return Array of individual columns corresponding to sort-orders
     */
    private fun parseCorrespondingColumns(cursor: PageCursorModel?, columns: List<Column<*>>): Array<Pair<Column<*>, SortOrder>> {
        val result = mutableListOf<Pair<Column<*>, SortOrder>>()
        cursor?.sortBy?.forEach { req ->
            // Find target column by name
            val col = columns.firstOrNull { col ->
                col.name.equals(req.key, ignoreCase = true)
            } ?: throw IllegalSortFieldException(req.key)

            // Map column to sort-order
            result.add(Pair(col, SortOrder.valueOf(req.value.toString())))
        }

        // Return as array to allow vararg expansion
        return result.toTypedArray()
    }

    ////////////////////////////////////////////
    //               Helpers                  //
    ////////////////////////////////////////////

    fun strColEq(col: Column<*>, value: String): Op<Boolean> {
        return Op.build { col.castTo<String>(VarCharColumnType()).lowerCase() eq value.lowercase() }
    }

    fun strColNeq(col: Column<*>, value: String): Op<Boolean> {
        return Op.build { col.castTo<String>(VarCharColumnType()).lowerCase() neq value.lowercase() }
    }
}