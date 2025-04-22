package cz.tomashula.plenr.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.withSuspendTransaction

abstract class DatabaseService(
    private val database: Database,
    private vararg val tables: Table,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
)
{
    open suspend fun createIfNotExists() = dbQuery {
        SchemaUtils.create(*tables)
    }

    protected suspend fun <T> dbQuery(query: suspend Transaction.() -> T) =
        newSuspendedTransaction(coroutineDispatcher, database, statement = query)
}
