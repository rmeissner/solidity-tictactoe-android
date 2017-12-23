package de.thegerman.sttt.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import de.thegerman.sttt.data.db.AppDb
import de.thegerman.sttt.di.annotations.ApplicationContext
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun providesDb(@ApplicationContext context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, AppDb.DB_NAME)
                    .build()
}
