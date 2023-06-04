package eu.kanade.tachiyomi.ui.library.anime

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import eu.kanade.domain.base.BasePreferences
import eu.kanade.domain.category.anime.interactor.SetDisplayModeForAnimeCategory
import eu.kanade.domain.category.anime.interactor.SetSortModeForAnimeCategory
import eu.kanade.domain.library.service.LibraryPreferences
import eu.kanade.tachiyomi.data.track.TrackManager
import eu.kanade.tachiyomi.util.preference.toggle
import eu.kanade.tachiyomi.widget.TriState
import tachiyomi.core.preference.Preference
import tachiyomi.core.preference.getAndSet
import tachiyomi.core.util.lang.launchIO
import tachiyomi.domain.category.anime.interactor.GetAnimeCategories
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.anime.model.AnimeLibrarySort
import tachiyomi.domain.library.model.LibraryDisplayMode
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class AnimeLibrarySettingsScreenModel(
    val preferences: BasePreferences = Injekt.get(),
    val libraryPreferences: LibraryPreferences = Injekt.get(),
    private val getCategories: GetAnimeCategories = Injekt.get(),
    private val setDisplayModeForCategory: SetDisplayModeForAnimeCategory = Injekt.get(),
    private val setSortModeForCategory: SetSortModeForAnimeCategory = Injekt.get(),
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
        toggleFilter { libraryPreferences.filterTrackedAnime(id) }
    }

    fun setDisplayMode(category: Category, mode: LibraryDisplayMode) {
        coroutineScope.launchIO {
            setDisplayModeForCategory.await(category, mode)
        }
    }

    fun setSort(category: Category, mode: AnimeLibrarySort.Type, direction: AnimeLibrarySort.Direction) {
        coroutineScope.launchIO {
            setSortModeForCategory.await(category, mode, direction)
        }
    }
}