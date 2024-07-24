package com.jeantituana2024.tesis.api

import com.jeantituana2024.tesis.models.ActiveMembersResponse
import com.jeantituana2024.tesis.models.AnnualAttendanceResponse
import com.jeantituana2024.tesis.models.AnnualEarningsResponse
import com.jeantituana2024.tesis.models.AttendanceDataResponse
import com.jeantituana2024.tesis.models.AttendanceRequest
import com.jeantituana2024.tesis.models.AttendanceResponse
import com.jeantituana2024.tesis.models.AttendancesByGenderResponse
import com.jeantituana2024.tesis.models.AttendancesCoachResponse
import com.jeantituana2024.tesis.models.AttendancesResponse
import com.jeantituana2024.tesis.models.CheckSessionResponse
import com.jeantituana2024.tesis.models.CreateUser
import com.jeantituana2024.tesis.models.DailyAttendancesResponse
import com.jeantituana2024.tesis.models.EarningsByPlanResponse
import com.jeantituana2024.tesis.models.EarningsResponse
import com.jeantituana2024.tesis.models.EditProfileRequest
import com.jeantituana2024.tesis.models.EditProfileResponse
import com.jeantituana2024.tesis.models.EditProfileWithImageRequest
import com.jeantituana2024.tesis.models.EditUserRequest
import com.jeantituana2024.tesis.models.EditUserResponse
import com.jeantituana2024.tesis.models.EditUserWithImageRequest
import com.jeantituana2024.tesis.models.LoginResponse
import com.jeantituana2024.tesis.models.GenericResponse
import com.jeantituana2024.tesis.models.InactiveMembersResponse
import com.jeantituana2024.tesis.models.RegisterRequest
import com.jeantituana2024.tesis.models.LoginRequest
import com.jeantituana2024.tesis.models.MemberRequest
import com.jeantituana2024.tesis.models.MemberResponse
import com.jeantituana2024.tesis.models.MemberUpdateRequest
import com.jeantituana2024.tesis.models.MembersResponse
import com.jeantituana2024.tesis.models.MonthlyAttendancesResponse
import com.jeantituana2024.tesis.models.MonthlyEarningsResponse
import com.jeantituana2024.tesis.models.PaymentRequest
import com.jeantituana2024.tesis.models.PaymentResponse
import com.jeantituana2024.tesis.models.PaymentsResponse
import com.jeantituana2024.tesis.models.PaymentsResponseUser
import com.jeantituana2024.tesis.models.PaysInfoResponse
import com.jeantituana2024.tesis.models.PlanCreateRequest
import com.jeantituana2024.tesis.models.PlanResponse
import com.jeantituana2024.tesis.models.PlansResponse
import com.jeantituana2024.tesis.models.ProfileResponse
import com.jeantituana2024.tesis.models.RegisterResponse
import com.jeantituana2024.tesis.models.ResetPasswordRequest
import com.jeantituana2024.tesis.models.TotalMembersResponse
import com.jeantituana2024.tesis.models.UserRequest
import com.jeantituana2024.tesis.models.UserResponse
import com.jeantituana2024.tesis.models.UsersResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface Api {

    //AUTHENTICATION
    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ):Call<RegisterResponse>

    @POST("login")
    fun login(
        @Body request:LoginRequest
    ):Call<LoginResponse>

    @POST("reset_password")
    fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Call<GenericResponse>

    @GET("view_profile")
    fun viewProfile(
        @Header("Authorization") token: String
    ): Call<ProfileResponse>

    @PUT("edit_profile")
    fun editProfile(
        @Header("Authorization") token: String,
        @Body response: EditProfileRequest
    ): Call<EditProfileResponse>

    @PUT("edit_profile")
    fun editProfileWithImage(
        @Header("Authorization") token: String,
        @Body response: EditProfileWithImageRequest
    ): Call<EditProfileResponse>

    @POST("logout")
    fun logout(
        @Header("Authorization") token: String
    ): Call<GenericResponse>

    //CHECK TOKEN
    @GET("check")
    fun checkSession(
        @Header("Authorization") token: String
    ): Call<CheckSessionResponse>

    //USERS

    @GET("users")
    fun getUsers(
        @Header("Authorization") token: String
    ): Call<UsersResponse>

    @POST("users")
    fun createUser(
        @Header("Authorization") token: String,
        @Body user: UserRequest
    ): Call<UserResponse>

    @PUT("users/{id}")
    fun editUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body user: EditUserRequest
    ): Call<EditUserResponse>

    @PUT("users/{id}")
    fun editUserWithImage(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body response: EditUserWithImageRequest
    ): Call<EditUserResponse>

    @GET("users/{id}")
    fun viewUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Call<EditUserResponse>



    @DELETE("users/{id}")
    fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): Call<EditUserResponse>

    //PLANS
    @GET("plans")
    fun getPlans(
        @Header("Authorization") token: String
    ): Call<PlansResponse>

    @POST("plans")
    fun createPlan(
        @Header("Authorization") token: String,
        @Body plan: PlanCreateRequest
    ): Call<PlanResponse>

    @PUT("plans/{id}")
    fun updatePlan(
        @Header("Authorization") token: String,
        @Path("id") planId: String,
        @Body planUpdateRequest: PlanCreateRequest
    ): Call<PlanResponse>

    @GET("plans/{id}")
    fun viewPlan(
        @Header("Authorization") token: String,
        @Path("id") planId: String,
    ): Call<PlanResponse>

    @DELETE("plans/{id}")
    fun deletePlan(
        @Header("Authorization") authorizationHeader: String,
        @Path("id") planId: String
    ): Call<PlanResponse>

    //MEMBERS
    @GET("members")
    fun getMembers(
        @Header("Authorization") token: String
    ): Call<MembersResponse>

    @POST("members")
    fun createMember(
        @Header("Authorization") token: String,
        @Body memberRequest: MemberRequest
    ): Call<MemberResponse>

    @GET("members/{id}")
    fun viewMember(
        @Header("Authorization") token: String,
        @Path("id") memberId: String
    ): Call<MemberResponse>

    @PUT("members/{id}")
    fun updateMember(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body memberUpdateRequest: MemberUpdateRequest
    ): Call<MemberResponse>

    @DELETE("members/{id}")
    fun deleteMember(
        @Header("Authorization") token: String,
        @Path("id") memberId: String
    ): Call<MemberResponse>

    //ASISTENCIAS

    @GET("members/{id}/attendances")
    fun getMemberAttendances(
        @Header("Authorization") token: String,
        @Path("id") memberId: String
    ): Call<AttendancesResponse>

    @POST("members/{id}/attendances")
    fun createAttendance(
        @Header("Authorization") token: String,
        @Path("id") memberId: String,
        @Body attendanceData: AttendanceRequest
    ): Call<AttendanceResponse>

    @DELETE("members/{memberId}/attendances/{attendanceId}")
    fun deleteAttendance(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: String,
        @Path("attendanceId") attendanceId: String
    ): Call<AttendanceResponse>

    @GET("attendances")
    fun getAttendancesCoach(
        @Header("Authorization") token: String
    ): Call<AttendancesCoachResponse>

    //PAYS

    @GET("pays")
    fun getPayments(
        @Header("Authorization") token: String
    ): Call<PaymentsResponse>

    @POST("members/{id}/pays")
    fun createPayment(
        @Header("Authorization") token: String,
        @Path("id") memberId: String,
        @Body paymentRequest: PaymentRequest
    ): Call<PaymentResponse>

    @GET("members/{memberId}/pays/{payId}")
    fun getPayment(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: String,
        @Path("payId") payId: String,
    ): Call<PaymentResponse>

    @PUT("members/{memberId}/pays/{payId}")
    fun updatePayment(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: String,
        @Path("payId") payId: String,
        @Body paymentUpdateRequest: PaymentRequest
    ): Call<PaymentResponse>

    @DELETE("members/{memberId}/pays/{payId}")
    fun deletePayment(
        @Header("Authorization") token: String,
        @Path("memberId") memberId: String,
        @Path("payId") payId: String,
    ): Call<PaymentResponse>

    //KPIs

    @GET("annual_attendances")
    fun getAnnualAttendances(
        @Header("Authorization") token: String
    ): Call<AnnualAttendanceResponse>

//    @GET("views/earnings/earnings_by_plan")
//    fun getEarningsByPlan(
//        @Header("Authorization") token: String
//    ): Call<EarningsResponse>

    @GET("attendances_by_gender")
    fun getAttendancesByGender(
        @Header("Authorization") token: String
    ): Call<AttendancesByGenderResponse>

    @GET("daily_attendances")
    fun getDailyAttendances(
        @Header("Authorization") token: String
    ): Call<DailyAttendancesResponse>

    @GET("monthly_attendances")
    fun getMonthlyAttendances(
        @Header("Authorization") token: String
    ): Call<MonthlyAttendancesResponse>

    @GET("annual_earnings")
    fun getAnnualEarnings(
        @Header("Authorization") token: String
    ): Call<AnnualEarningsResponse>

    @GET("earnings_by_plan")
    fun getEarningsByPlan(
        @Header("Authorization") token: String
    ): Call<EarningsByPlanResponse>

    @GET("monthly_earnings")
    fun getMonthlyEarnings(
        @Header("Authorization") token: String
    ): Call<MonthlyEarningsResponse>

    @GET("total_members")
    fun getTotalMembers(
        @Header("Authorization") token: String
    ): Call<TotalMembersResponse>

    @GET("active_members")
    fun getActiveMembers(
        @Header("Authorization") token: String
    ): Call<ActiveMembersResponse>

    @GET("inactive_members")
    fun getInactiveMembers(
        @Header("Authorization") token: String
    ): Call<InactiveMembersResponse>

    //NUMBERS
    @GET("total_members")
    suspend fun getTotalMembersNumber(
        @Header("Authorization") token: String
    ): TotalMembersResponse

    @GET("active_members")
    suspend fun getActiveMembersNumber(
        @Header("Authorization") token: String
    ): ActiveMembersResponse

    @GET("inactive_members")
    suspend fun getInactiveMembersNumber(
        @Header("Authorization") token: String
    ): InactiveMembersResponse
    @GET("total_attendances")
    suspend fun getTotalAttendances(
        @Header("Authorization") token: String
    ): AttendanceDataResponse

    @GET("total_earnings")
    suspend fun getTotalEarnings(
        @Header("Authorization") token: String
    ): EarningsResponse

    //Users
    @GET("members")
    fun getMemberUser(
        @Header("Authorization") token: String
    ): Call<MemberResponse>
    @GET("attendances")
    fun getAttendancesUser(
        @Header("Authorization") token: String
    ): Call<AttendancesResponse>

    @GET("pays")
    fun getPaymentsUser(
        @Header("Authorization") token: String
    ): Call<PaymentsResponseUser>

    @GET("pays_info")
    fun getPaysInfo(
        @Header("Authorization") token: String
    ): Call<PaysInfoResponse>

}