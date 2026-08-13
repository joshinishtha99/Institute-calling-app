package com.institute.calling.data.remote

import com.institute.calling.domain.model.Disposition

/**
 * Wire models for the backend API. Kept separate from domain models; mapped in
 * NetworkCallingRepository. Field names match the JSON exactly.
 */

data class LoginRequest(val userId: String, val pin: String)

data class LoginResponse(val accessToken: String, val user: AuthUserDto)

data class AuthUserDto(
    val id: String,
    val name: String,
    val role: String,
    val branchId: String?,
    val branchLabel: String?,
)

data class CityDto(val id: String, val name: String, val branches: List<BranchDto>)

data class BranchDto(val id: String, val name: String, val callers: List<CallerDto>)

data class CallerDto(val id: String, val name: String)

data class OwnerDto(val id: String, val name: String)

data class NameRequest(val name: String)

data class CreateCallerRequest(val name: String, val pin: String)

data class LogCallRequest(
    val phoneNumber: String,
    val startTime: String,   // ISO 8601
    val endTime: String,     // ISO 8601
    val disposition: Disposition,
    val notes: String,
)

/** Minimal shape of the created call the API returns; extra fields are ignored. */
data class CallCreatedDto(val id: String)

data class BranchSummaryDto(
    val branchId: String,
    val branchName: String,
    val cityName: String,
    val total: Int,
    val staff: List<StaffCountDto>,
)

data class StaffCountDto(val id: String, val name: String, val calls: Int)

data class StaffMemberDto(val id: String, val name: String, val isActive: Boolean)

data class BranchStaffDto(val branchId: String, val branchName: String, val staff: List<StaffMemberDto>)

data class UpdateCallerRequest(
    val name: String? = null,
    val pin: String? = null,
    val isActive: Boolean? = null,
)

data class NamedRefDto(val id: String, val name: String)

data class CallDto(
    val id: String,
    val caller: NamedRefDto,
    val branch: NamedRefDto,
    val phoneNumber: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val disposition: String,
    val notes: String,
    val recordingUrl: String?,
)
