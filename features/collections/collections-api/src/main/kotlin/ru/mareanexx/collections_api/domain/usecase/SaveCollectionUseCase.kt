package ru.mareanexx.collections_api.domain.usecase

import ru.mareanexx.collections_api.domain.model.*
import ru.mareanexx.collections_api.domain.repository.CollectionsRepository
import ru.mareanexx.collections_api.domain.validation.CollectionRules

class SaveCollectionUseCase(
    private val repository: CollectionsRepository,
) {
    suspend operator fun invoke(id: String?, form: CollectionForm): CollectionsResult<BookCollection> {
        CollectionRules.validate(form.name)?.let { return CollectionsResult.Failure(it) }
        val normalized = form.copy(name = CollectionRules.normalize(form.name))
        return if (id == null) repository.create(normalized) else repository.update(id, normalized)
    }
}
