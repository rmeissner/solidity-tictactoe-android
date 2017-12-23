package de.thegerman.sttt.ui.overview

import de.thegerman.sttt.data.repositories.GameRepository
import javax.inject.Inject

class OverviewViewModel @Inject constructor(
        private val gameRepository: GameRepository
) : OverviewContract()