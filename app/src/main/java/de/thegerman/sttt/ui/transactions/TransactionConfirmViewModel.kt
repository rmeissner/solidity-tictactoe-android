package de.thegerman.sttt.ui.transactions

import de.thegerman.sttt.data.repositories.GameRepository
import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import io.reactivex.ObservableTransformer
import pm.gnosis.models.Wei
import java.math.BigInteger
import javax.inject.Inject

class TransactionConfirmViewModel @Inject constructor(
        private val gameRepository: GameRepository
) : TransactionConfirmationContract() {
    override fun estimateTransformer() = ObservableTransformer<Pair<BigInteger, Action>, Result<Wei>> {
        it.switchMap { (gameId, action) ->
            when (action) {
                is Action.Join -> gameRepository.estimateJoinGame(gameId)
                is Action.MakeMove -> gameRepository.estimateMakeMoveGame(gameId, action.field)
                is Action.Cancel -> gameRepository.estimateCancelGame(gameId)
                is Action.Kick -> gameRepository.estimateKickPlayer(gameId)
            }
                    .mapToResult()
        }
    }

    override fun confirmTransformer() = ObservableTransformer<Pair<BigInteger, Action>, Result<String>> {
        it.switchMap { (gameId, action) ->
            when (action) {
                is Action.Join -> gameRepository.joinGame(gameId)
                is Action.MakeMove -> gameRepository.makeMove(gameId, action.field)
                is Action.Cancel ->  gameRepository.cancelGame(gameId)
                is Action.Kick ->  gameRepository.kickPlayer(gameId)
            }
                    .mapToResult()
        }
    }
}