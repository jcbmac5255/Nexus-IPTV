package com.nexus.iptv.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.nexus.iptv.BuildConfig
import com.nexus.iptv.data.local.NexusDatabase
import com.nexus.iptv.data.local.dao.*
import com.nexus.iptv.data.local.dao.ChannelPreferenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DEBUG_SLOW_QUERY_THRESHOLD_MS = 100L

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexusDatabase =
        Room.databaseBuilder(
            context,
            NexusDatabase::class.java,
            "streamvault.db"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .openHelperFactory(
                if (BuildConfig.DEBUG) {
                    SlowQueryLoggingOpenHelperFactory(
                        delegate = FrameworkSQLiteOpenHelperFactory(),
                        slowQueryThresholdMs = DEBUG_SLOW_QUERY_THRESHOLD_MS
                    )
                } else {
                    FrameworkSQLiteOpenHelperFactory()
                }
            )
            .addMigrations(
                NexusDatabase.MIGRATION_1_2,
                NexusDatabase.MIGRATION_2_3,
                NexusDatabase.MIGRATION_3_4,
                NexusDatabase.MIGRATION_4_5,
                NexusDatabase.MIGRATION_5_6,
                NexusDatabase.MIGRATION_6_7,
                NexusDatabase.MIGRATION_7_8,
                NexusDatabase.MIGRATION_8_9,
                NexusDatabase.MIGRATION_9_10,
                NexusDatabase.MIGRATION_10_11,
                NexusDatabase.MIGRATION_11_12,
                NexusDatabase.MIGRATION_12_13,
                NexusDatabase.MIGRATION_13_14,
                NexusDatabase.MIGRATION_14_15,
                NexusDatabase.MIGRATION_15_16,
                NexusDatabase.MIGRATION_16_17,
                NexusDatabase.MIGRATION_17_18,
                NexusDatabase.MIGRATION_18_19,
                NexusDatabase.MIGRATION_19_20,
                NexusDatabase.MIGRATION_20_21,
                NexusDatabase.MIGRATION_21_22,
                NexusDatabase.MIGRATION_22_23,
                NexusDatabase.MIGRATION_23_24,
                NexusDatabase.MIGRATION_24_25,
                NexusDatabase.MIGRATION_25_26,
                NexusDatabase.MIGRATION_26_27,
                NexusDatabase.MIGRATION_27_28,
                NexusDatabase.MIGRATION_28_29,
                NexusDatabase.MIGRATION_29_30,
                NexusDatabase.MIGRATION_30_31,
                NexusDatabase.MIGRATION_31_32,
                NexusDatabase.MIGRATION_32_33,
                NexusDatabase.MIGRATION_33_34,
                NexusDatabase.MIGRATION_34_35,
                NexusDatabase.MIGRATION_35_36,
                NexusDatabase.MIGRATION_36_37,
                NexusDatabase.MIGRATION_37_38,
                NexusDatabase.MIGRATION_38_39,
                NexusDatabase.MIGRATION_39_40,
                NexusDatabase.MIGRATION_40_41,
                NexusDatabase.MIGRATION_41_42,
                NexusDatabase.MIGRATION_42_43,
                NexusDatabase.MIGRATION_43_44,
                NexusDatabase.MIGRATION_44_45,
                NexusDatabase.MIGRATION_45_46,
                NexusDatabase.MIGRATION_46_47,
                NexusDatabase.MIGRATION_47_48,
                NexusDatabase.MIGRATION_48_49,
                NexusDatabase.MIGRATION_49_50,
                NexusDatabase.MIGRATION_50_51,
                NexusDatabase.MIGRATION_51_52
            )
            // NOTE: fallbackToDestructiveMigration() intentionally removed.
            // All future schema changes MUST add a corresponding Migration in NexusDatabase.
            .build()

    @Provides fun provideProviderDao(db: NexusDatabase): ProviderDao = db.providerDao()
    @Provides fun provideChannelDao(db: NexusDatabase): ChannelDao = db.channelDao()
    @Provides fun provideChannelPreferenceDao(db: NexusDatabase): ChannelPreferenceDao = db.channelPreferenceDao()
    @Provides fun provideMovieDao(db: NexusDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: NexusDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: NexusDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideCategoryDao(db: NexusDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideCatalogSyncDao(db: NexusDatabase): CatalogSyncDao = db.catalogSyncDao()
    @Provides fun provideProgramDao(db: NexusDatabase): ProgramDao = db.programDao()
    @Provides fun provideFavoriteDao(db: NexusDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideVirtualGroupDao(db: NexusDatabase): VirtualGroupDao = db.virtualGroupDao()
    @Provides fun providePlaybackHistoryDao(db: NexusDatabase): PlaybackHistoryDao = db.playbackHistoryDao()
    @Provides fun provideTmdbIdentityDao(db: NexusDatabase): TmdbIdentityDao = db.tmdbIdentityDao()
    @Provides fun provideSearchHistoryDao(db: NexusDatabase): SearchHistoryDao = db.searchHistoryDao()
    @Provides fun provideSearchDao(db: NexusDatabase): SearchDao = db.searchDao()
    @Provides fun provideSyncMetadataDao(db: NexusDatabase): SyncMetadataDao = db.syncMetadataDao()
    @Provides fun provideMovieCategoryHydrationDao(db: NexusDatabase): MovieCategoryHydrationDao = db.movieCategoryHydrationDao()
    @Provides fun provideSeriesCategoryHydrationDao(db: NexusDatabase): SeriesCategoryHydrationDao = db.seriesCategoryHydrationDao()
    @Provides fun provideEpgSourceDao(db: NexusDatabase): EpgSourceDao = db.epgSourceDao()
    @Provides fun provideProviderEpgSourceDao(db: NexusDatabase): ProviderEpgSourceDao = db.providerEpgSourceDao()
    @Provides fun provideEpgChannelDao(db: NexusDatabase): EpgChannelDao = db.epgChannelDao()
    @Provides fun provideEpgProgrammeDao(db: NexusDatabase): EpgProgrammeDao = db.epgProgrammeDao()
    @Provides fun provideChannelEpgMappingDao(db: NexusDatabase): ChannelEpgMappingDao = db.channelEpgMappingDao()
    @Provides fun provideCombinedM3uProfileDao(db: NexusDatabase): CombinedM3uProfileDao = db.combinedM3uProfileDao()
    @Provides fun provideCombinedM3uProfileMemberDao(db: NexusDatabase): CombinedM3uProfileMemberDao = db.combinedM3uProfileMemberDao()
    @Provides fun provideRecordingScheduleDao(db: NexusDatabase): RecordingScheduleDao = db.recordingScheduleDao()
    @Provides fun provideRecordingRunDao(db: NexusDatabase): RecordingRunDao = db.recordingRunDao()
    @Provides fun provideProgramReminderDao(db: NexusDatabase): ProgramReminderDao = db.programReminderDao()
    @Provides fun provideRecordingStorageDao(db: NexusDatabase): RecordingStorageDao = db.recordingStorageDao()
    @Provides fun providePlaybackCompatibilityDao(db: NexusDatabase): PlaybackCompatibilityDao = db.playbackCompatibilityDao()
    @Provides fun provideXtreamContentIndexDao(db: NexusDatabase): XtreamContentIndexDao = db.xtreamContentIndexDao()
    @Provides fun provideXtreamIndexJobDao(db: NexusDatabase): XtreamIndexJobDao = db.xtreamIndexJobDao()
    @Provides fun provideXtreamLiveOnboardingDao(db: NexusDatabase): XtreamLiveOnboardingDao = db.xtreamLiveOnboardingDao()
}
