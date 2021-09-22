package me.blvckbytes.authus.infrastructure

import me.blvckbytes.authus.infrastructure.table.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class DatabaseConnection(
    @Autowired ds: DataSource
) {

    companion object {
        val tables = arrayOf(
            //////////////////////////////
            //          Accounts        //
            //////////////////////////////
            UserAccounts,

            //////////////////////////////
            //        Permissions       //
            //////////////////////////////
            GroupsPermissions, Groups, Permissions,
            UsersGroups, UsersPermissions, GroupsInheritances,

            //////////////////////////////
            //       Token/Session      //
            //////////////////////////////
            InvalidatedTokens
        )

        fun clearAllTables() {
            transaction {
                tables.forEach { it.deleteAll() }
            }
        }
    }

    private val logger: Logger? = LoggerFactory.getLogger(javaClass)

    init {
        Database.connect(ds)
        logger?.info("Connected to the database!")

        transaction {
            SchemaUtils.create(*tables)
            logger?.info("Created the schema!")
        }
    }
}