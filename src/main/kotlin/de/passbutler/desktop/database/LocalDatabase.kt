package de.passbutler.desktop.database

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import de.passbutler.common.database.LOCAL_DATABASE_SQL_FOREIGN_KEYS_ENABLE
import de.passbutler.common.database.LocalRepository
import de.passbutler.common.database.PassButlerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun createLocalRepository(databasePath: String): LocalRepository {
    return withContext(Dispatchers.IO) {
        val driver = JdbcSqliteDriver("jdbc:sqlite:$databasePath")

        PassButlerDatabase.Schema.create(driver)
        driver.execute(null, LOCAL_DATABASE_SQL_FOREIGN_KEYS_ENABLE, 0)

        val localDatabase = PassButlerDatabase(driver)
        LocalRepository(localDatabase, driver)
    }
}