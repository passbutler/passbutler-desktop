package de.passbutler.desktop.database

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import de.passbutler.common.database.LOCAL_DATABASE_SQL_FOREIGN_KEYS_ENABLE
import de.passbutler.common.database.LocalRepository
import de.passbutler.common.database.PassButlerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Throws(Exception::class)
suspend fun createLocalRepository(databasePath: File, mode: DatabaseInitializationMode): LocalRepository {
    return withContext(Dispatchers.IO) {
        if (mode is DatabaseInitializationMode.Open) {
            require(databasePath.exists()) { "The given database file does not exists!" }
        }

        if (mode is DatabaseInitializationMode.Create) {
            require(databasePath.canWrite()) { "The given database file is not writable!" }
        }

        val databasePathString = databasePath.absolutePath
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePathString")

        // Create database schema for new files
        if (mode is DatabaseInitializationMode.Create) {
            PassButlerDatabase.Schema.create(driver)
        }

        driver.execute(null, LOCAL_DATABASE_SQL_FOREIGN_KEYS_ENABLE, 0)

        val localDatabase = PassButlerDatabase(driver)
        LocalRepository(localDatabase, driver)
    }
}

sealed class DatabaseInitializationMode {
    object Create : DatabaseInitializationMode()
    object Open : DatabaseInitializationMode()
}
