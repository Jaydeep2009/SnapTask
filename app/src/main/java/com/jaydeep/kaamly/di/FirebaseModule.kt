package com.jaydeep.kaamly.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.content.Context
import com.jaydeep.kaamly.data.repository.AIRepository
import com.jaydeep.kaamly.data.repository.AIRepositoryImpl
import com.jaydeep.kaamly.data.repository.AuthRepository
import com.jaydeep.kaamly.data.repository.AuthRepositoryImpl
import com.jaydeep.kaamly.data.repository.BidRepository
import com.jaydeep.kaamly.data.repository.BidRepositoryImpl
import com.jaydeep.kaamly.data.repository.LocationRepository
import com.jaydeep.kaamly.data.repository.LocationRepositoryImpl
import com.jaydeep.kaamly.data.repository.NotificationRepository
import com.jaydeep.kaamly.data.repository.NotificationRepositoryImpl
import com.jaydeep.kaamly.data.repository.ReviewRepository
import com.jaydeep.kaamly.data.repository.ReviewRepositoryImpl
import com.jaydeep.kaamly.data.repository.TaskRepository
import com.jaydeep.kaamly.data.repository.TaskRepositoryImpl
import com.jaydeep.kaamly.data.repository.UserRepository
import com.jaydeep.kaamly.data.repository.UserRepositoryImpl
import com.jaydeep.kaamly.data.repository.WorkerRepository
import com.jaydeep.kaamly.data.repository.WorkerRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Firebase dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): UserRepository {
        return UserRepositoryImpl(firestore, storage)
    }
    
    @Provides
    @Singleton
    fun provideWorkerRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): WorkerRepository {
        return WorkerRepositoryImpl(firestore, storage)
    }
    
    @Provides
    @Singleton
    fun provideTaskRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): TaskRepository {
        return TaskRepositoryImpl(firestore, storage)
    }
    
    @Provides
    @Singleton
    fun provideAIRepository(): AIRepository {
        return AIRepositoryImpl()
    }
    
    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository {
        return LocationRepositoryImpl(context)
    }
    
    @Provides
    @Singleton
    fun provideBidRepository(
        firestore: FirebaseFirestore
    ): BidRepository {
        return BidRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun providePaymentRepository(
        firestore: FirebaseFirestore
    ): com.jaydeep.kaamly.data.repository.PaymentRepository {
        return com.jaydeep.kaamly.data.repository.PaymentRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun provideReviewRepository(
        firestore: FirebaseFirestore
    ): ReviewRepository {
        return ReviewRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }
    
    @Provides
    @Singleton
    fun provideWalletRepository(
        firestore: FirebaseFirestore
    ): com.jaydeep.kaamly.data.repository.WalletRepository {
        return com.jaydeep.kaamly.data.repository.WalletRepositoryImpl(firestore)
    }
}
