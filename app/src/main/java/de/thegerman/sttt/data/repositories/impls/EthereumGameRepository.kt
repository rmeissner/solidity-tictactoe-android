package de.thegerman.sttt.data.repositories.impls

import de.thegerman.sttt.data.db.GameDao
import de.thegerman.sttt.data.repositories.GameRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EthereumGameRepository @Inject constructor(
        private val gameDao: GameDao
) : GameRepository {
}