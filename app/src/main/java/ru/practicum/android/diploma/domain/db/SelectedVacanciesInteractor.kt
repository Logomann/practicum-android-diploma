package ru.practicum.android.diploma.domain.db

import kotlinx.coroutines.flow.Flow

interface SelectedVacanciesInteractor {
    suspend fun getVacancy(vacancyId: Int): Vacancy

    suspend fun addVacancy(vacancy: Vacancy)

    suspend fun deleteVacancy(vacancyId: Int)

    suspend fun hasLike(vacancyId: Int): Boolean

    fun listVacancies(): Flow<List<Vacancy>>
}
