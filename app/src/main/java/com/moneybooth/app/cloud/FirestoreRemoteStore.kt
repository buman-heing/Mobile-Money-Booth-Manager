package com.moneybooth.app.cloud

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneybooth.app.core.sync.JoinCode
import com.moneybooth.app.core.sync.PushResult
import com.moneybooth.app.core.sync.RemoteRecord
import com.moneybooth.app.core.sync.RemoteStore
import com.moneybooth.app.core.sync.SyncEntityType
import kotlinx.coroutines.tasks.await

/**
 * Cloud layout:
 *   businesses/{businessUid}                       the business itself (+ joinCode)
 *   businesses/{businessUid}/{collection}/{uid}    everything else
 *   joinCodes/{code} -> { businessUid }            lets an owner phone find a business by code
 *
 * Every write is a merge keyed by uid, so re-sending the same record is harmless.
 */
class FirestoreRemoteStore(
    private val firestore: FirebaseFirestore,
    private val auth: CloudAuth,
) : RemoteStore {
    override val isConfigured: Boolean = true

    override suspend fun push(records: List<RemoteRecord>): PushResult {
        return try {
            auth.ensureSignedIn()
            records.chunked(MAX_OPS_PER_BATCH).forEach { chunk ->
                val batch = firestore.batch()
                for (record in chunk) {
                    val data = HashMap<String, Any?>(record.fields).apply {
                        put("uid", record.uid)
                        put("businessUid", record.businessUid)
                        put("syncedAt", FieldValue.serverTimestamp())
                    }
                    if (record.type == SyncEntityType.BUSINESS) {
                        val code = JoinCode.forBusiness(record.uid)
                        data["joinCode"] = code
                        batch.set(firestore.collection("businesses").document(record.uid), data, SetOptions.merge())
                        batch.set(
                            firestore.collection("joinCodes").document(code),
                            mapOf("businessUid" to record.uid),
                            SetOptions.merge(),
                        )
                    } else {
                        batch.set(
                            firestore.collection("businesses").document(record.businessUid)
                                .collection(record.type.collection).document(record.uid),
                            data,
                            SetOptions.merge(),
                        )
                    }
                }
                batch.commit().await()
            }
            PushResult.Success
        } catch (e: Exception) {
            PushResult.Failure(e.message ?: e.javaClass.simpleName)
        }
    }

    /** "This phone is alive": lets the owner tell a quiet day from a dead employee phone. */
    suspend fun heartbeat(businessUid: String, deviceId: String, fields: Map<String, Any?>) {
        runCatching {
            auth.ensureSignedIn()
            firestore.collection("businesses").document(businessUid).collection("devices").document(deviceId)
                .set(fields + ("lastSeenAt" to FieldValue.serverTimestamp()), SetOptions.merge()).await()
        }
    }

    suspend fun lookupJoinCode(code: String): String? {
        auth.ensureSignedIn()
        val snap = firestore.collection("joinCodes").document(code).get().await()
        return snap.getString("businessUid")
    }

    companion object {
        // Firestore caps a batch at 500 operations; BUSINESS records use two.
        private const val MAX_OPS_PER_BATCH = 200
    }
}

/** Anonymous sign-in for now; phone-number sign-in replaces this when member roles arrive. */
class CloudAuth(private val firebaseAuth: FirebaseAuth) {
    suspend fun ensureSignedIn(): String {
        firebaseAuth.currentUser?.let { return it.uid }
        val result = firebaseAuth.signInAnonymously().await()
        return result.user?.uid ?: error("Sign-in returned no user")
    }
}
