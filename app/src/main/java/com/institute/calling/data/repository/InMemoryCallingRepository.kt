package com.institute.calling.data.repository

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
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A self-contained, in-memory stand-in for the real backend. It holds the
 * City -> Branch -> Caller structure and the list of logged calls in memory so
 * the whole login + caller flow runs with no server. Replace with a Room +
 * Retrofit implementation behind the same [CallingRepository] interface.
 *
 * All data is lost when the process dies — this is intentional for the prototype.
 */
@Singleton
class InMemoryCallingRepository @Inject constructor() : CallingRepository {

    private val cities = MutableStateFlow(seedCities())
    private val owners = listOf(
        Owner(id = "owner-neha", name = "Neha"),
        Owner(id = "owner-deepa", name = "Deepa"),
    )
    private val callLog = mutableListOf<CallRecord>()

    override fun observeCities(): Flow<List<City>> = cities.asStateFlow()

    override suspend fun refreshStructure() {
        // No-op: the in-memory data is already present.
    }

    override suspend fun clearSession() {
        // No-op for in-memory.
    }

    override suspend fun getBranchSummary(branchId: String, date: String?): BranchSummary {
        for (city in cities.value) {
            val branch = city.branches.firstOrNull { it.id == branchId }
            if (branch != null) {
                return BranchSummary(
                    branchId = branch.id,
                    branchName = branch.name,
                    cityName = city.name,
                    total = branch.callsToday,
                    staff = branch.callers.map { StaffCount(it.id, it.name, 0) },
                )
            }
        }
        return BranchSummary(branchId, "", "", 0, emptyList())
    }

    override suspend fun getBranchStaff(branchId: String): BranchStaff {
        for (city in cities.value) {
            val branch = city.branches.firstOrNull { it.id == branchId }
            if (branch != null) {
                return BranchStaff(
                    branchId = branch.id,
                    branchName = branch.name,
                    staff = branch.callers.map { StaffMember(it.id, it.name, true) },
                )
            }
        }
        return BranchStaff(branchId, "", emptyList())
    }

    override suspend fun updateCaller(callerId: String, name: String?, pin: String?, isActive: Boolean?) {
        if (name == null) return
        cities.update { list ->
            list.map { city ->
                city.copy(
                    branches = city.branches.map { branch ->
                        branch.copy(callers = branch.callers.map { c -> if (c.id == callerId) c.copy(name = name) else c })
                    },
                )
            }
        }
    }

    override suspend fun getCalls(date: String, branchId: String?, callerId: String?): List<CallLogEntry> = emptyList()

    override suspend fun getOwners(): List<Owner> = owners

    override suspend fun addCity(name: String) {
        val cityId = "city-${UUID.randomUUID()}"
        cities.update { it + City(id = cityId, name = name.trim(), branches = emptyList()) }
    }

    override suspend fun addBranch(cityId: String, name: String) {
        cities.update { list ->
            list.map { city ->
                if (city.id != cityId) city
                else city.copy(
                    branches = city.branches + Branch(
                        id = "branch-${UUID.randomUUID()}",
                        cityId = city.id,
                        cityName = city.name,
                        name = name.trim(),
                        callers = emptyList(),
                    ),
                )
            }
        }
    }

    override suspend fun addCaller(branchId: String, name: String, pin: String) {
        cities.update { list ->
            list.map { city ->
                city.copy(
                    branches = city.branches.map { branch ->
                        if (branch.id != branchId) branch
                        else branch.copy(
                            callers = branch.callers + Caller(
                                id = "caller-${UUID.randomUUID()}",
                                branchId = branch.id,
                                name = name.trim(),
                            ),
                        )
                    },
                )
            }
        }
    }

    override suspend fun authenticateOwner(ownerId: String, pin: String): AuthUser? {
        if (!pin.isValidPin()) return null
        val owner = owners.firstOrNull { it.id == ownerId } ?: return null
        return AuthUser(id = owner.id, name = owner.name, role = Role.OWNER)
    }

    override suspend fun authenticateCaller(callerId: String, pin: String): AuthUser? {
        if (!pin.isValidPin()) return null
        for (city in cities.value) {
            for (branch in city.branches) {
                val caller = branch.callers.firstOrNull { it.id == callerId }
                if (caller != null) {
                    return AuthUser(
                        id = caller.id,
                        name = caller.name,
                        role = Role.EMPLOYEE,
                        branchId = branch.id,
                        branchLabel = "${branch.name} · ${city.name}",
                    )
                }
            }
        }
        return null
    }

    override suspend fun logCall(record: CallRecord) {
        callLog.add(record)
        // Reflect the new call in the branch's "today" counter for the owner view.
        cities.update { list ->
            list.map { city ->
                city.copy(
                    branches = city.branches.map { branch ->
                        if (branch.id == record.branchId) branch.copy(callsToday = branch.callsToday + 1)
                        else branch
                    },
                )
            }
        }
    }

    private fun String.isValidPin(): Boolean = length == 4 && all { it.isDigit() }

    private fun seedCities(): List<City> {
        val kalyan = "city-kalyan"
        val dombivli = "city-dombivli"
        val thane = "city-thane"
        val rambaug = "branch-rambaug"
        val station = "branch-station"
        val midc = "branch-midc"
        val naupada = "branch-naupada"
        return listOf(
            City(
                id = kalyan, name = "Kalyan",
                branches = listOf(
                    Branch(
                        id = rambaug, cityId = kalyan, cityName = "Kalyan", name = "Rambaug branch",
                        callsToday = 28,
                        callers = listOf(
                            Caller("caller-priya", rambaug, "Priya S."),
                            Caller("caller-rahul", rambaug, "Rahul K."),
                            Caller("caller-sneha", rambaug, "Sneha P."),
                            Caller("caller-amit", rambaug, "Amit D."),
                        ),
                    ),
                    Branch(
                        id = station, cityId = kalyan, cityName = "Kalyan", name = "Station Road branch",
                        callsToday = 22,
                        callers = listOf(
                            Caller("caller-kiran", station, "Kiran M."),
                            Caller("caller-pooja", station, "Pooja R."),
                        ),
                    ),
                ),
            ),
            City(
                id = dombivli, name = "Dombivli",
                branches = listOf(
                    Branch(
                        id = midc, cityId = dombivli, cityName = "Dombivli", name = "MIDC branch",
                        callsToday = 19,
                        callers = listOf(Caller("caller-nikhil", midc, "Nikhil J.")),
                    ),
                ),
            ),
            City(
                id = thane, name = "Thane",
                branches = listOf(
                    Branch(
                        id = naupada, cityId = thane, cityName = "Thane", name = "Naupada branch",
                        callsToday = 12,
                        callers = listOf(Caller("caller-riya", naupada, "Riya K.")),
                    ),
                ),
            ),
        )
    }
}
