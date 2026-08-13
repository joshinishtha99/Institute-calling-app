package com.institute.calling.domain.repository

import com.institute.calling.domain.model.AuthUser
import com.institute.calling.domain.model.BranchStaff
import com.institute.calling.domain.model.BranchSummary
import com.institute.calling.domain.model.CallLogEntry
import com.institute.calling.domain.model.CallRecord
import com.institute.calling.domain.model.City
import com.institute.calling.domain.model.Owner
import com.institute.calling.domain.model.Role
import kotlinx.coroutines.flow.Flow

/**
 * The single boundary the UI/domain talks to. The in-memory implementation used
 * now can be swapped for a Room-backed, Retrofit-syncing implementation without
 * any change above this interface.
 */
interface CallingRepository {

    /** Observable network structure (City -> Branch -> Caller). */
    fun observeCities(): Flow<List<City>>

    /** Fetch the latest structure from the source and update [observeCities]. */
    suspend fun refreshStructure()

    /** Owner accounts. */
    suspend fun getOwners(): List<Owner>

    // --- Owner management of the hierarchy ---
    suspend fun addCity(name: String)
    suspend fun addBranch(cityId: String, name: String)
    suspend fun addCaller(branchId: String, name: String, pin: String)

    /**
     * Authenticate a selected account with a PIN.
     *
     * NOTE (v1 prototype): this accepts any 4-digit PIN and simply resolves the
     * selected account into an [AuthUser]. The real implementation will verify
     * credentials against the backend and return a session/JWT.
     */
    suspend fun authenticateOwner(ownerId: String, pin: String): AuthUser?
    suspend fun authenticateCaller(callerId: String, pin: String): AuthUser?

    /** Persist a completed call. */
    suspend fun logCall(record: CallRecord)

    /** A branch's total + per-caller counts for a day (null date = today). */
    suspend fun getBranchSummary(branchId: String, date: String? = null): BranchSummary

    /** All callers in a branch (incl. deactivated) for management. */
    suspend fun getBranchStaff(branchId: String): BranchStaff

    /** Rename, reset PIN, and/or (de)activate a caller. Nulls are left unchanged. */
    suspend fun updateCaller(callerId: String, name: String?, pin: String?, isActive: Boolean?)

    /** Calls on a given day (YYYY-MM-DD), optionally filtered by branch/caller. */
    suspend fun getCalls(date: String, branchId: String? = null, callerId: String? = null): List<CallLogEntry>

    /** Clear any stored session/token (on logout). */
    suspend fun clearSession()
}
