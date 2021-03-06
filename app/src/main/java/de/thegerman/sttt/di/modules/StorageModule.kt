package de.thegerman.sttt.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import de.thegerman.sttt.data.db.AppDb
import pm.gnosis.heimdall.common.di.ApplicationContext
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun providesDb(@ApplicationContext context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, AppDb.DB_NAME).build()

    @Provides
    @Singleton
    fun providesGameDao(db: AppDb) = db.gameDao()
}
