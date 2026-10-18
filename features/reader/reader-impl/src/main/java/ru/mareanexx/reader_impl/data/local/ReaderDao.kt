package ru.mareanexx.reader_impl.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.mareanexx.reader_impl.data.local.entity.*

@Dao
internal interface ReaderDao {
    @Query("SELECT * FROM reader_transfers WHERE userId = :userId ORDER BY completedAt DESC")
    fun transfers(userId: String): Flow<List<TransferEntity>>
    @Query("SELECT * FROM reader_transfers WHERE userId = :userId")
    suspend fun allTransfers(userId: String): List<TransferEntity>
    @Query("SELECT * FROM reader_transfers WHERE userId = :userId AND bookId = :bookId")
    suspend fun transfer(userId: String, bookId: String): TransferEntity?
    @Upsert suspend fun putTransfer(value: TransferEntity)
    @Query("DELETE FROM reader_transfers WHERE userId = :userId AND bookId = :bookId")
    suspend fun deleteTransfer(userId: String, bookId: String)
    @Query("SELECT * FROM reader_positions WHERE userId = :userId AND bookId = :bookId")
    suspend fun position(userId: String, bookId: String): PositionEntity?
    @Query("SELECT * FROM reader_positions WHERE userId = :userId AND bookId = :bookId")
    fun observePosition(userId: String, bookId: String): Flow<PositionEntity?>
    @Upsert suspend fun putPosition(value: PositionEntity)
    @Query("UPDATE reader_positions SET syncedRevision = :revision WHERE userId = :userId AND bookId = :bookId AND revision = :revision")
    suspend fun acknowledgePosition(userId: String, bookId: String, revision: Long)
    @Query("SELECT * FROM reader_positions WHERE userId = :userId AND revision > syncedRevision")
    suspend fun dirtyPositions(userId: String): List<PositionEntity>
    @Query("SELECT * FROM reader_annotations WHERE userId = :userId AND bookId = :bookId")
    fun annotations(userId: String, bookId: String): Flow<List<AnnotationEntity>>
    @Query("SELECT * FROM reader_annotations WHERE userId = :userId AND id = :id")
    suspend fun annotation(userId: String, id: String): AnnotationEntity?
    @Query("SELECT * FROM reader_annotations WHERE userId = :userId AND revision > syncedRevision")
    suspend fun dirtyAnnotations(userId: String): List<AnnotationEntity>
    @Upsert suspend fun putAnnotation(value: AnnotationEntity)
    @Query("UPDATE reader_annotations SET syncedRevision = :revision WHERE userId = :userId AND id = :id AND revision = :revision")
    suspend fun acknowledgeAnnotation(userId: String, id: String, revision: Long)
    @Query("SELECT * FROM reader_settings WHERE userId = :userId")
    fun settings(userId: String): Flow<SettingsEntity?>
    @Upsert suspend fun putSettings(value: SettingsEntity)
}
