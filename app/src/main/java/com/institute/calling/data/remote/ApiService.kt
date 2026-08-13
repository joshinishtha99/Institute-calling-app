package com.institute.calling.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("structure")
    suspend fun getStructure(): List<CityDto>

    @GET("owners")
    suspend fun getOwners(): List<OwnerDto>

    @POST("cities")
    suspend fun addCity(@Body body: NameRequest): CityDto

    @POST("cities/{cityId}/branches")
    suspend fun addBranch(@Path("cityId") cityId: String, @Body body: NameRequest): BranchDto

    @POST("branches/{branchId}/callers")
    suspend fun addCaller(@Path("branchId") branchId: String, @Body body: CreateCallerRequest): CallerDto

    @POST("calls")
    suspend fun logCall(@Body body: LogCallRequest): CallCreatedDto

    @GET("branches/{branchId}/summary")
    suspend fun getBranchSummary(
        @Path("branchId") branchId: String,
        @Query("date") date: String? = null,
    ): BranchSummaryDto

    @GET("branches/{branchId}/staff")
    suspend fun getBranchStaff(@Path("branchId") branchId: String): BranchStaffDto

    @PATCH("callers/{callerId}")
    suspend fun updateCaller(
        @Path("callerId") callerId: String,
        @Body body: UpdateCallerRequest,
    ): StaffMemberDto

    @GET("calls")
    suspend fun getCalls(
        @Query("date") date: String,
        @Query("branchId") branchId: String? = null,
        @Query("callerId") callerId: String? = null,
    ): List<CallDto>
}
