
package com.jeantituana2024.tesis.models

import com.google.gson.annotations.SerializedName

//MODELS GENERIC
data class RegisterResponse (
    val success: String,
    val user: UserRegister,
    val verificationToken: String
)

data class GenericResponse(
    val success: String
)

//MODELS AUTH
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)
data class CreateUser(
    val name: String,
    val email: String,
    val password: String
)
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: String,
    val user: UserLogin,
    val token:String
)

data class ResetPasswordRequest(
    val email: String
)

//MODELS PROFILE
data class EditProfileWithImageRequest(
    val name: String,
    val image: String?
)

data class EditProfileRequest(
    val name: String,
)

data class ProfileResponse(
    val success: String,
    val user: UserModel
)

data class EditProfileResponse(
    val success: String,
    val user: UserLogin
)

//MODELS ERROR
data class ErrorDetail(
    val validation: String?,
    val code: String,
    val message: String,
    val path: List<String>,
    val minimum: Int? = null,
    val type: String? = null,
    val inclusive: Boolean? = null,
    val exact: Boolean? = null
)

data class ErrorResponse(
    val error: String,
    val details: List<ErrorDetail>
)

data class SingleErrorResponse(
    val error: String
)

//MODELS USER
data class UserRegister (
    val name:String,
    val email: String
)
data class EditUserRequest(
    val name: String,
    val role: String
)

data class EditUserWithImageRequest(
    val name: String,
    val role: String,
    val image: String?
)

data class EditUserResponse(
    val success: String,
    val user: UserModel
)
data class UserLogin(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val image: String? // Puede ser null
)
data class UsersResponse(
    val success: String,
    val user: Map<String, UserModel>
)
data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    var status: Boolean,
    val role: String,
    val emailVerified: String?,
    val image: String?,
    val createdAt: String,
    val updatedAt: String
)

//MODELS PLAN
data class Plan(
    val id: Int,
    val name: String,
    val description: String,
    val price: String,
    val duration: Int,
    val status: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class PlanCreateRequest(
    val name: String,
    val description: String,
    val price: Double,
    val duration: Int
)

data class PlansResponse(
    val success: String,
    val plan: Map<String, Plan>
)

data class PlanResponse(
    val success: String,
    val plan: Plan
)

//MODELS MEMBER
data class Member(
    val id: Int,
    val identification: String,
    val name: String,
    val lastname: String,
    val email: String,
    val phone: String,
    @SerializedName("emergency_phone")
    val emergencyPhone: String,
    @SerializedName("born_date")
    val bornDate: String,
    val direction: String,
    @SerializedName("inscription_date")
    val inscriptionDate: String,
    var status: Boolean,
    val gender: String,
    val nacionality: String,
    val planId: Int,
    val plan: Plan,
    val createdAt: String,
    val updatedAt: String
)

data class MemberRequest(
    val identification: String,
    val name: String,
    val lastname: String,
    val email: String,
    val phone: String,
    val emergency_phone: String,
    val born_date: String,
    val direction: String,
    val gender: String,
    val nacionality: String,
    val planId: Int
)
data class MemberUpdateRequest(
    val identification: String,
    val name: String,
    val lastname: String,
    val phone: String,
    val emergency_phone: String,
    val born_date: String,
    val direction: String,
    val gender: String,
    val nacionality: String,
    val planId: Int
)
data class MemberResponse(
    val success: String,
    val member: Member
)
data class MembersResponse(
    val success: String,
    val members: List<Member>
)

//MODELS ATTENDANCE

data class AttendancesResponse(
    val success: String,
    val attendance: Map<String, Attendance>
)

data class AttendancesCoachResponse(
    val success: String,
    val attendance: List<AttendanceCoach>
)

data class Attendance(
    val id: Int,
    val date: String,
    val status: Boolean,
    val createdAt: String,
    val memberId: Int,
    val updatedAt: String
)

data class AttendanceCoach(
    val id: Int,
    val date: String,
    val status: Boolean,
    val createdAt: String,
    val memberId: Int,
    val updatedAt: String,
    val Member: Member
)

data class AttendanceRequest(
    val date: String
)

data class AttendanceResponse(
    val success: String,
    val attendance: Attendance
)

//MODELS PAY

data class PaymentsResponse(
    val success: String,
    val pay: List<Payment>
)

data class PaymentsResponseUser(
    val success: String,
    val pay: Map<String, Payment>
)

data class Payment(
    val id: Int,
    val date: String,
    var status: Boolean,
    val payment_type: String,
    val createdAt: String,
    val memberId: Int,
    val updatedAt: String,
    val Member: Member
)

data class PaymentRequest(
    val date: String,
    val payment_type: String
)

// Modelo para la respuesta de creación de pago
data class PaymentResponse(
    val success: String,
    val attendance: Payment
)

data class GetPaymentResponse(
    val success: String,
    val pay: Payment
)

data class DeletePaymentResponse(
    val success: String,
    val plan: Payment
)

//KPIs

//data class EarningsResponse(
//    val success: String,
//    val earnings_by_plan: Map<String, EarningByPlan>
//)
//
//data class EarningByPlan(
//    val id: Int,
//    val plan_name: String,
//    val earnings: String
//)

data class AnnualAttendanceResponse(
    val success: String,
    val annual_attendance: Map<String, AnnualAttendance>
)

data class AnnualAttendance(
    val id: Int,
    val year: Int,
    val annual_attendances: Int
)

data class AttendancesByGenderResponse(
    val success: String,
    val attendaces_by_gender: Map<String, GenderAttendance>
)

data class GenderAttendance(
    val id: Int,
    val gender: String,
    val total_attendances: Int
)

data class DailyAttendancesResponse(
    val success: String,
    val daily_attendance: Map<String, DailyAttendance>
)

data class DailyAttendance(
    val id: Int,
    val day: String, // Formato ISO 8601
    val daily_attendances: Int
)

data class MonthlyAttendancesResponse(
    val success: String,
    val monthly_attendance: Map<String, MonthlyAttendance>
)

data class MonthlyAttendance(
    val id: Int,
    val year: Int,
    val month: Int,
    val monthly_attendances: Int
)

data class AnnualEarningsResponse(
    val success: String,
    val annual_earning: Map<String, AnnualEarning>
)

data class AnnualEarning(
    val id: Int,
    val year: Int,
    val annual_earnings: String
)


data class EarningsByPlanResponse(
    val success: String,
    val earnings_by_plan: Map<String, EarningsByPlan>
)

data class EarningsByPlan(
    val id: Int,
    val plan_name: String,
    val earnings: String
)

data class MonthlyEarningsResponse(
    val success: String,
    val monthly_earning: Map<String, MonthlyEarning>
)

data class MonthlyEarning(
    val id: Int,
    val year: Int,
    val month: Int,
    val monthly_earnings: String
)

data class TotalMembersResponse(
    val total_membership: Map<String, TotalMembership>
)
data class TotalMembership(
    val id: Int, val total_memberships: Int
)

data class ActiveMembersResponse(
    val active_membership: Map<String, ActiveMembership>
)
data class ActiveMembership(
    val id: Int,
    val active_memberships: Int
)

data class InactiveMembersResponse(
    val inactive_membership: Map<String, InactiveMembership>
)
data class InactiveMembership(
    val id: Int,
    val inactive_memberships: Int
)

data class AttendanceDataResponse(
    val success: String,
    val total_attendance: Map<String, AttendanceDataBody>
)

data class AttendanceDataBody(
    val id: Int,
    val total_attendances: Int
)

data class EarningsResponse(
    val success: String,
    val total_earning: Map<String, EarningsData>
)

data class EarningsData(
    val id: Int,
    val total_earnings: String
)

data class PaysInfoResponse(
    val success: String,
    val pay: Map<String, PayInfo>
)

data class PayInfo(
    val id: Int,
    val member_id: Int,
    val member_identification: String,
    val member_name: String,
    val member_lastname: String,
    val member_email: String,
    val member_phone: String,
    val plan_id: Int,
    val plan_name: String,
    val plan_price: String,
    val plan_duration: Int,
    val first_payment_date: String,
    val last_payment_date: String,
    val next_payment_date: String,
    val days_remaining: Int
)

//MODEL CHECK
data class CheckSessionResponse(
    val success: String?,
    val user: UserModel?,
    val token: String?,
    val error: String?
)