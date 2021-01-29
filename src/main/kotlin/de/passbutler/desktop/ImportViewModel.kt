package de.passbutler.desktop

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import de.passbutler.common.ItemEditingViewModel
import de.passbutler.common.LoggedInUserViewModelUninitializedException
import de.passbutler.common.UserManagerUninitializedException
import de.passbutler.common.base.Failure
import de.passbutler.common.base.Result
import de.passbutler.common.base.Success
import de.passbutler.common.database.models.ItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.kotlin.Logger
import tornadofx.ViewModel
import java.io.File

class ImportViewModel : ViewModel(), UserViewModelUsingViewModel {

    override val userViewModelProvidingViewModel by injectUserViewModelProvidingViewModel()

    suspend fun importKeePass2X(selectedFile: File): Result<Int> {
        return import(selectedFile, KeePassX2ImportProvider())
    }

    suspend fun importKeePass2(selectedFile: File): Result<Int> {
        return import(selectedFile, KeePass2ImportProvider())
    }

    private suspend fun import(selectedFile: File, importProvider: ImportProvider): Result<Int> {
        Logger.debug("Import file '$selectedFile' with import provider '${importProvider.javaClass.simpleName}'")

        val loggedInUserViewModel = loggedInUserViewModel ?: throw LoggedInUserViewModelUninitializedException
        val userManager = userViewModelProvidingViewModel.userManager ?: throw UserManagerUninitializedException
        val localRepository = userManager.localRepository

        return when (val itemDataListResult = importProvider.import(selectedFile)) {
            is Success -> {
                val itemModels = itemDataListResult.result

                itemModels.forEach { itemModel ->
                    val itemEditingViewModel = ItemEditingViewModel(itemModel, loggedInUserViewModel, localRepository)
                    itemEditingViewModel.save()
                }

                Success(itemModels.size)
            }
            is Failure -> {
                Logger.debug("The file could not be imported with provider!")
                Failure(itemDataListResult.throwable)
            }
        }
    }
}

object ImportFileEmptyException : IllegalStateException("The file to import seems to be empty!")

private interface ImportProvider {
    suspend fun import(file: File): Result<List<ItemEditingViewModel.ItemModel.Imported>>
}

private class KeePassX2ImportProvider : ImportProvider {
    override suspend fun import(file: File): Result<List<ItemEditingViewModel.ItemModel.Imported>> {
        return withContext(Dispatchers.IO) {
            try {
                val csvReadResult = csvReader().readAll(file)

                if (csvReadResult.size < 2) {
                    throw ImportFileEmptyException
                }

                // The header seems not be recognized
                val csvReadLinesWithoutHeader = csvReadResult.drop(1)

                val itemModels = csvReadLinesWithoutHeader.map { csvLine ->
                    val itemGroup = csvLine.getOrNull(0) ?: ""
                    val itemTitle = csvLine.getOrNull(1) ?: ""
                    val itemUsername = csvLine.getOrNull(2) ?: ""
                    val itemPassword = csvLine.getOrNull(3) ?: ""
                    val itemUrl = csvLine.getOrNull(4) ?: ""
                    val itemNotes = csvLine.getOrNull(5) ?: ""

                    val itemData = ItemData(itemTitle, itemUsername, itemPassword, itemUrl, itemNotes, emptyList())
                    val itemWasDeleted = itemGroup.contains("Recycle Bin")

                    ItemEditingViewModel.ItemModel.Imported(itemData, itemWasDeleted)
                }

                Success(itemModels)
            } catch (exception: Exception) {
                Failure(exception)
            }
        }
    }
}

private class KeePass2ImportProvider : ImportProvider {
    override suspend fun import(file: File): Result<List<ItemEditingViewModel.ItemModel.Imported>> {
        return withContext(Dispatchers.IO) {
            try {
                val csvReadResult = csvReader().readAll(file)

                if (csvReadResult.size < 2) {
                    throw ImportFileEmptyException
                }

                // The header seems not be recognized
                val csvReadLinesWithoutHeader = csvReadResult.drop(1)

                val itemModels = csvReadLinesWithoutHeader.map { csvLine ->
                    val itemTitle = csvLine.getOrNull(0) ?: ""
                    val itemUsername = csvLine.getOrNull(1) ?: ""
                    val itemPassword = csvLine.getOrNull(2) ?: ""
                    val itemUrl = csvLine.getOrNull(3) ?: ""
                    val itemNotes = csvLine.getOrNull(4) ?: ""

                    val itemData = ItemData(itemTitle, itemUsername, itemPassword, itemUrl, itemNotes, emptyList())
                    val itemWasDeleted = false

                    ItemEditingViewModel.ItemModel.Imported(itemData, itemWasDeleted)
                }

                Success(itemModels)
            } catch (exception: Exception) {
                Failure(exception)
            }
        }
    }
}
