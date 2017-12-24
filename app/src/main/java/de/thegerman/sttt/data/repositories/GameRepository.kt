package de.thegerman.sttt.data.repositories

import de.thegerman.sttt.data.models.GameInfo
import io.reactivex.Observable
import java.math.BigInteger

interface GameRepository {
    fun loadGameDetails(gameId: BigInteger): Observable<GameInfo>
}