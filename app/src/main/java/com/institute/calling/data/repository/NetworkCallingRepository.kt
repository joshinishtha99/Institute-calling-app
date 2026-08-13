package com.institute.calling.data.repository

import com.institute.calling.data.remote.ApiService
import com.institute.calling.data.remote.CityDto
import com.institute.calling.data.remote.CreateCallerRequest
import com.institute.calling.data.remote.LogCallRequest
import com.institute.calling.data.remote.LoginRequest
import com.institute.calling.data.remote.NameRequest
import com.institute.calling.data.remote.TokenStore
import com.institute.calling.data.remote.UpdateCallerRequest
import com.institute.calling.domain.model.AuthUser
import com.institute.calling.domain.model.Branch
import com.institute.calling.domain.model.BranchStaff
import com.institute.calling.domain.model.BranchSummary
import com.institute.calling.domain.model.CallLogEntry
import com.institute.calling.domain.model.CallRecord
import com.institute.calling.domain.model.Caller
import com.institute.calling.domain.model.City
import com.institute.calling.domain.model.Owner
import com.institute.calling.domain.model.Role
import com.institute.calling.domain.model.StaffCount
import com.institute.calling.domain.model.StaffMember
import com.institute.calling.domain.repository.CallingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the backend over HTTP. Online-only: every call hits the network; there
 * is no local cache yet (that's the offline step). Structure is exposed as a Flow
 * that [refreshStructure] refills from the server.
 */
@Singleton
class NetworkCallingRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : CallingRepository {

    private val cities = MutableStateFlow<List<City>>(emptyList())

    override fun observeCities(): Flow<List<City>> = cities.asStateFlow()

    override suspend fun refreshStructure() {
        cities.value = api.getStructure().map { it.toDomain() }
    }

    override suspend fun getOwners(): List<Owner> =
        api.getOwners().map { Owner(id = it.id, name = it.name) }

    override suspend fun addCity(name: String) {
        api.addCity(NameRequest(name))
        refreshStructure()
    }

    override suspend fun addBranch(cityId: String, name: String) {
        api.addBranch(cityId, NameRequest(name))
        refreshStructure()
    }

    override suspend fun addCaller(branchId: String, name: String, pin: String) {
        api.addCaller(branchId, CreateCallerRequest(name, pin))
        refreshStructure()
    }

    override suspend fun authenticateOwner(ownerId: String, pin: String): AuthUser? =
        loginOrNull(ownerId, pin)

    override suspend fun authenticateCaller(callerId: String, pin: String): AuthUser? =
        loginOrNull(callerId, pin)

    override suspend fun logCall(record: CallRecord) {
        api.logCall(
            LogCallRequest(
                phoneNumber = record.phoneNumber,
                startTime = iso(record.startTimeMillis),
                endTime = iso(record.endTimeMillis),
                disposition = record.disposition,
                notes = record.notes,
            ),
        )
    }

    override suspend fun clearSession() {
        tokenStore.clear()
    }

    override suspend fun getBranchSummary(branchId: String, date: String?): BranchSummary {
        val dto = api.getBranchSummary(branchId, date)
        return BranchSummary(
            branchId = dto.branchId,
            branchName = dto.branchName,
            cityName = dto.cityName,
            total = dto.total,
            staff = dto.staff.map { StaffCount(id = it.id, name = it.name, calls = it.calls) },
        )
    }

    override suspend fun getBranchStaff(branchId: String): BranchStaff {
        val dto = api.getBranchStaff(branchId)
        return BranchStaff(
            branchId = dto.branchId,
            branchName = dto.branchName,
            staff = dto.staff.map { StaffMember(id = it.id, name = it.name, isActive = it.isActive) },
        )
    }

    override suspend fun updateCaller(callerId: String, name: String?, pin: String?, isActive: Boolean?) {
        api.updateCaller(callerId, UpdateCallerRequest(name = name, pin = pin, isActive = isActive))
        refreshStructure()
    }

    override suspend fun getCalls(date: String, branchId: String?, callerId: String?): List<CallLogEntry> {
        return api.getCalls(date, branchId, callerId).map { c ->
            CallLogEntry(
                id = c.id,
                callerName = c.caller.name,
                branchId = c.branch.id,
                branchName = c.branch.name,
                phoneNumber = c.phoneNumber,
                startMillis = parseIsoMillis(c.startTime),
                endMillis = parseIsoMillis(c.endTime),
                durationSeconds = c.durationSeconds,
                disposition = c.disposition,
                notes = c.notes,
            )
        }
    }

    private fun parseIsoMillis(iso: String): Long = try {
        isoParser.parse(iso)?.time ?: isoParserNoMs.parse(iso)?.time ?: 0L
    } catch (e: Exception) {
        try { isoParserNoMs.parse(iso)?.time ?: 0L } catch (e2: Exception) { 0L }
    }

    /** Returns the user on success, null on invalid credentials (HTTP 401). Other errors propagate. */
    private suspend fun loginOrNull(userId: String, pin: String): AuthUser? {
        return try {
            val response = api.login(LoginRequest(userId, pin))
            tokenStore.set(response.accessToken)
            val u = response.user
            AuthUser(
                id = u.id,
                name = u.name,
                role = Role.valueOf(u.role),
                branchId = u.branchId,
                branchLabel = u.branchLabel,
            )
        } catch (e: HttpException) {
            if (e.code() == 401) null else throw e
        }
    }

    private fun CityDto.toDomain(): City = City(
        id = id,
        name = name,
        branches = branches.map { b ->
            Branch(
                id = b.id,
                cityId = this.id,
                cityName = this.name,
                name = b.name,
                callers = b.callers.map { c -> Caller(id = c.id, branchId = b.id, name = c.name) },
            )
        },
    )

    private fun iso(millis: Long): String = isoFormat.format(Date(millis))

    private companion object {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val isoParserNoMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}