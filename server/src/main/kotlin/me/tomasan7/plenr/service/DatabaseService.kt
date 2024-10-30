package me.tomasan7.plenr.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class DatabaseService(
    private val database: Database,
    private vararg val tables: Table,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
)
{
    protected suspend fun <T> query(query: Transaction.() -> T) = withContext(coroutineDispatcher) {
        transaction(database) {
            query()
        }
    }

    suspend fun createIfNotExists() = query {
        SchemaUtils.create(*tables)
    }
}