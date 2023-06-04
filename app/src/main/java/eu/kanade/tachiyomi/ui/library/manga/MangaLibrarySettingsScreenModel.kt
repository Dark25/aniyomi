package eu.kanade.tachiyomi.ui.library.manga

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import eu.kanade.domain.base.BasePreferences
import eu.kanade.domain.category.manga.interactor.SetDisplayModeForMangaCategory
import eu.kanade.domain.category.manga.interactor.SetSortModeForMangaCategory
import eu.kanade.domain.library.service.LibraryPreferences
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.util.preference.toggle
import eu.kanade.tachiyomi.widget.TriState
import tachiyomi.core.preference.Preference
import tachiyomi.core.preference.getAndSet
import tachiyomi.core.util.lang.launchIO
import tachiyomi.domain.category.manga.interactor.GetMangaCategories
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.manga.model.MangaLibrarySort
import tachiyomi.domain.library.model.LibraryDisplayMode
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MangaLibrarySettingsScreenModel(
    val preferences: BasePreferences = Injekt.get(),
    val libraryPreferences: LibraryPreferences = Injekt.get(),
    private val getCategories: GetMangaCategories = Injekt.get(),
    private val setDisplayModeForCategory: SetDisplayModeForMangaCategory = Injekt.get(),
    private val setSortModeForCategory: SetSortModeForMangaCategory = Injekt.get(),
    trackManager: TrackManager = Injekt.get(),
) : ScreenModel {

    val trackServices = trackManager.services.filter { service -> service.isLogged }

    fun togglePreference(preference: (LibraryPreferences) -> Preference<Boolean>) {
        preference(libraryPreferences).toggle()
    }

    fun toggleFilter(preference: (LibraryPreferences) -> Preference<Int>) {
        preference(libraryPreferences).getAndSet {
            TriState.valueOf(it).next().value
        }
    }

    fun toggleTracker(id: Int) {
        toggleFilter { libraryPreferences.filterTrackedManga(id) }
    }

    fun setDisplayMode(category: Category, mode: LibraryDisplayMode) {
        coroutineScope.launchIO {
            setDisplayModeForCategory.await(category, mode)
        }
    }

    fun setSort(category: Category, mode: MangaLibrarySort.Type, direction: MangaLibrarySort.Direction) {
        coroutineScope.launchIO {
            setSortModeForCategory.await(category, mode, direction)
        }
    }
}