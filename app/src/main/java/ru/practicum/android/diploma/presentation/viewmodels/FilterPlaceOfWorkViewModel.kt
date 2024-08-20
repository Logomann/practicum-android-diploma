package ru.practicum.android.diploma.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.practicum.android.diploma.domain.FilterInteractor
import ru.practicum.android.diploma.domain.models.Country
import ru.practicum.android.diploma.domain.models.Region
import ru.practicum.android.diploma.domain.models.SaveFiltersSharedPrefs
import ru.practicum.android.diploma.ui.state.PlaceOfWorkScreenState
import ru.practicum.android.diploma.util.ResponseData

class FilterPlaceOfWorkViewModel(
    private val filterInteractor: FilterInteractor
) : ViewModel() {

    private val screenStateLiveData = MutableLiveData<PlaceOfWorkScreenState>()
    private val _filtersSave = MutableLiveData<SaveFiltersSharedPrefs>()
    private var country = Country("", "")
    private var region = Region("", "", null)
    val sharedPrefs: LiveData<SaveFiltersSharedPrefs>
        get() = _filtersSave

    fun render(): LiveData<PlaceOfWorkScreenState> {
        return screenStateLiveData
    }

    fun setCountry(country: Country) {
        this.country = country
    }

    fun setRegion(region: Region) {
        this.region = region
    }

    fun getCountry(): Country {
        return country
    }

    fun getRegion(): Region {
        return region
    }

    fun getCountryName(region: Region, isSaving: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            filterInteractor
                .getCountries()
                .collect { response ->
                    when (response) {
                        is ResponseData.Data -> {
                            response.value.forEach {
                                if (it.id == region.parentId.toString()) {
                                    setData(isSaving, it, region)
                                }
                            }
                        }

                        is ResponseData.Error -> {}
                    }
                }
        }
    }

    /*fun saveSharedPrefs(country: Country?, region: Region?) {
        viewModelScope.launch {
            filterInteractor.writeSharedPrefs(
                SaveFiltersSharedPrefs(
                    industries = null,
                    country = country,
                    region = region,
                    currency = null,
                    noCurrency = false
                )
            )
        }
    }*/

    fun saveFields(country: Country, region: Region) {
        if (country.name.isEmpty()) {
            getCountryName(region, true)
        } else {
            setState(PlaceOfWorkScreenState.Saved(country, region))
        }
    }

    fun getFilterSetting() {
        viewModelScope.launch {
            val filters = filterInteractor.readSharedPrefs()
            if (filters != null && filters.country?.name?.isNotEmpty() == true) {
                setState(PlaceOfWorkScreenState.Loaded(filters))
            }
        }
    }

    private fun setData(isSaving: Boolean, country: Country, region: Region) {
        if (isSaving) {
            setState(PlaceOfWorkScreenState.Saved(country, region))
        } else {
            setState(PlaceOfWorkScreenState.CountryName(country))
        }
    }

    fun setCountryName(country: Country) {
        if (country.name.isNotEmpty()) {
            setState(PlaceOfWorkScreenState.CountryName(country))
        } else {
            setState(PlaceOfWorkScreenState.NoCountryName)
        }
    }

    fun setRegionName(region: Region) {
        if (region.name.isNotEmpty()) {
            setState(PlaceOfWorkScreenState.RegionName(region))
        } else {
            setState(PlaceOfWorkScreenState.NoRegionName)
        }
    }

    private fun setState(state: PlaceOfWorkScreenState) {
        screenStateLiveData.postValue(state)
    }

    fun readSharedPrefs() {
        viewModelScope.launch {
            _filtersSave.postValue(filterInteractor.readSharedPrefs())
        }
    }
}
