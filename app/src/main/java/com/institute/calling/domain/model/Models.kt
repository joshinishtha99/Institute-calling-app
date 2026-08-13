package com.institute.calling.domain.model

/**
 * Domain models for the calling workflow.
 *
 * Organisation hierarchy: City -> Branch -> Caller.
 * Owners sit outside the branch hierarchy and manage it.
 *
 * These are pure Kotlin types with no Android or framework dependencies, so the
 * same models are reused when the in-memory repository is later replaced by a
 * real Room + Retrofit data layer.
 */

enum class Role { OWNER, EMPLOYEE }

data class City(
    val id: String,
    val name: String,
    val branches: List<Branch>,
)

data class Branch(
    val id: String,
    val cityId: String,
    val cityName: String,
    val name: String,
    val callers: List<Caller>,
    val callsToday: Int = 0,
)

data class Caller(
    val id: String,
    val branchId: String,
    val name: String,
) {
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

data class Owner(
    val id: String,
    val name: String,
) {
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

/** The six call outcomes the caller taps after a call ends. */
enum class Disposition(val label: String) {
    INTERESTED("Interested"),
    FOLLOW_UP("Follow Up"),
    NOT_INTERESTED("Not Interested"),
    BUSY("Busy"),
    WRONG_NUMBER("Wrong Number"),
    SWITCHED_OFF("Switched Off");

    /** Whether this outcome counts as a connected call (drives connection rate later). */
    val isConnected: Boolean
        get() = this == INTERESTED || this == FOLLOW_UP || this == NOT_INTERESTED
}

/**
 * A completed call. In v1 there is no audio; [recordingUrl] stays null but exists
 * on the model so recording can be switched on later with no schema change.
 */
data class CallRecord(
    val callerId: String,
    val branchId: String,
    val phoneNumber: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val disposition: Disposition,
    val notes: String,
    val recordingUrl: String? = null,
) {
    val durationSeconds: Long
        get() = ((endTimeMillis - startTimeMillis) / 1000).coerceAtLeast(0)
}

/** The signed-in user. For a caller, [branchId]/[branchLabel] identify their branch. */
data class AuthUser(
    val id: String,
    val name: String,
    val role: Role,
    val branchId: String? = null,
    val branchLabel: String? = null,
) {
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

/** A branch's call totals for a day, with the per-caller breakdown. */
data class BranchSummary(
    val branchId: String,
    val branchName: String,
    val cityName: String,
    val total: Int,
    val staff: List<StaffCount>,
)

data class StaffCount(
    val id: String,
    val name: String,
    val calls: Int,
) {
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

/** A staff member for management (includes active/deactivated state). */
data class StaffMember(
    val id: String,
    val name: String,
    val isActive: Boolean,
) {
    val initial: String get() = name.trim().firstOrNull()?.uppercase() ?: "?"
}

data class BranchStaff(
    val branchId: String,
    val branchName: String,
    val staff: List<StaffMember>,
)

/** One call in the owner's date-by-date review. Times are epoch millis (0 if unparseable). */
data class CallLogEntry(
    val id: String,
    val callerName: String,
    val branchId: String,
    val branchName: String,
    val phoneNumber: String,
    val startMillis: Long,
    val endMillis: Long,
    val durationSeconds: Int,
    val disposition: String,
    val notes: String,
)