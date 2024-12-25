package com.hasib.samsungalbumshowcase.di

import com.hasib.samsungalbumshowcase.data.repositories.AlbumRepositoryImpl
import com.hasib.samsungalbumshowcase.data.repositories.PhotoRepositoryImpl
import com.hasib.samsungalbumshowcase.data.repositories.UserRepositoryImpl
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun provideAlbumRepository(albumRepositoryImpl: AlbumRepositoryImpl): AlbumRepository

    @Singleton
    @Binds
    fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    fun providePhotoRepository(photoRepositoryImpl: PhotoRepositoryImpl): PhotoRepository
}