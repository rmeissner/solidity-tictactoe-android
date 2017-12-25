package de.thegerman.sttt.ui.account.unlock

import de.thegerman.sttt.utils.Result
import de.thegerman.sttt.utils.mapToResult
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import pm.gnosis.heimdall.security.EncryptionManager
import javax.inject.Inject

class UnlockViewModel @Inject constructor(
        private var encryptionManager: EncryptionManager
) : UnlockContract() {
    override fun loadState(): Single<State> =
            encryptionManager.initialized().flatMap {
                if (it) encryptionManager.unlocked()
                        .map {
                            if (it) State.UNLOCKED
                            else State.LOCKED
                        }
                else Single.just(State.CLEAN)
            }

    override fun unlockTransformer(): ObservableTransformer<Pair<String, String>, Result<State>> =
            ObservableTransformer {
                it.switchMapSingle { (password, repeat) ->
                    loadState().flatMap {
                        when (it) {
                            State.UNLOCKED -> Single.just(true)
                            State.LOCKED -> encryptionManager.unlockWithPassword(password.trim().toByteArray())
                            State.CLEAN -> {
                                if (password.isBlank()) throw IllegalArgumentException()
                                if (password.trim() != repeat.trim()) throw IllegalArgumentException()
                                encryptionManager.setupPassword(password.trim().toByteArray())
                            }
                        }
                    }
                            .map {
                                if (it) State.UNLOCKED
                                else throw IllegalArgumentException()
                            }
                            .mapToResult()
                }
            }
}