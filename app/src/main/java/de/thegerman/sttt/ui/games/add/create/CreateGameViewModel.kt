package de.thegerman.sttt.ui.games.add.create

import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import io.reactivex.ObservableTransformer
import pm.gnosis.models.Wei
import javax.inject.Inject

class CreateGameViewModel @Inject constructor(
        private val gameRepository: GameRepository
) : CreateGameContract() {
    override fun estimateTransformer() = ObservableTransformer<Unit, Result<Wei>> {
        it.switchMap {
            gameRepository.estimateCreateGame().mapToResult()
        }
    }

    override fun createTransformer() = ObservableTransformer<Unit, Result<String>> {
        it.switchMap {
            gameRepository.createGame().mapToResult()
        }
    }
}