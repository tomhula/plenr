package cz.tomashula.plenr.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

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
