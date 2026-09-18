package com.moneybooth.app.core.sync

import com.moneybooth.app.core.data.database.dao.SyncOutboxDao
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncEngineTest {

    private class FakeOutbox(entries: List<SyncOutboxEntity>) : SyncOutboxDao {
        val rows = entries.toMutableList()
        override suspend fun enqueue(entry: SyncOutboxEntity): Long { rows += entry; return entry.id }
        override suspend fun nextBatch(limit: Int) = rows.sortedWith(compareBy({ it.createdAt }, { it.id })).take(limit)
        override suspend fun delete(ids: List<Long>) { rows.removeAll { it.id in ids } }
        override suspend fun markFailed(ids: List<Long>, error: String?) {
            rows.replaceAll { if (it.id in ids) it.copy(attempts = it.attempts + 1, lastError = error) else it }
        }
        override fun observePendingCount(): Flow<Int> = flowOf(rows.size)
        override suspend fun pendingCount(): Int = rows.size
    }

    private class RecordingStore(private val results: ArrayDeque<PushResult>) : RemoteStore {
        override val isConfigured = true
        val pushed = mutableListOf<List<RemoteRecord>>()
        override suspend fun push(records: List<RemoteRecord>): PushResult {
            pushed += records
            return results.removeFirstOrNull() ?: PushResult.Success
        }
    }

    private fun entry(id: Long, uid: String = "u$id") =
        SyncOutboxEntity(id = id, entityType = SyncEntityType.TRANSACTION, entityUid = uid, createdAt = id)

    private fun record(e: SyncOutboxEntity) = RemoteRecord(e.entityType, e.entityUid, "biz", mapOf("n" to e.id))

    @Test
    fun `uploads everything in order and clears the queue`() = runBlocking {
        val outbox = FakeOutbox(listOf(entry(3), entry(1), entry(2)))
        val store = RecordingStore(ArrayDeque())
        val engine = SyncEngine(outbox, store, batchSize = 2) { record(it) }

        val result = engine.runOnce()

        assertEquals(SyncRunResult.Uploaded(3), result)
        assertEquals(listOf("u1", "u2", "u3"), store.pushed.flatten().map { it.uid })
        assertTrue(outbox.rows.isEmpty())
    }

    @Test
    fun `a failed upload keeps rows queued with the error recorded`() = runBlocking {
        val outbox = FakeOutbox(listOf(entry(1), entry(2)))
        val store = RecordingStore(ArrayDeque(listOf(PushResult.Failure("no signal"))))
        val engine = SyncEngine(outbox, store) { record(it) }

        val result = engine.runOnce()

        assertEquals(SyncRunResult.Failed("no signal", remaining = 2), result)
        assertEquals(2, outbox.rows.size)
        assertTrue(outbox.rows.all { it.attempts == 1 && it.lastError == "no signal" })
    }

    @Test
    fun `rows whose local record vanished are dropped without an upload`() = runBlocking {
        val outbox = FakeOutbox(listOf(entry(1), entry(2)))
        val store = RecordingStore(ArrayDeque())
        val engine = SyncEngine(outbox, store) { if (it.id == 1L) null else record(it) }

        val result = engine.runOnce()

        assertEquals(SyncRunResult.Uploaded(1), result)
        assertEquals(listOf("u2"), store.pushed.flatten().map { it.uid })
        assertTrue(outbox.rows.isEmpty())
    }

    @Test
    fun `an unconfigured cloud leaves the queue untouched`() = runBlocking {
        val outbox = FakeOutbox(listOf(entry(1)))
        val engine = SyncEngine(outbox, UnconfiguredRemoteStore) { record(it) }

        assertEquals(SyncRunResult.CloudNotConfigured, engine.runOnce())
        assertEquals(1, outbox.rows.size)
    }
}
