package com.example.sportsxtreme.data.remote.auth

import com.example.sportsxtreme.domain.model.PhoneAuthSession

interface PhoneAuthManager {
    suspend fun sendOtp(phoneNumber: String): Result<PhoneAuthSession>
    suspend fun resendOtp(session: PhoneAuthSession): Result<PhoneAuthSession>
    suspend fun verifyOtp(verificationId: String, otpCode: String): Result<Unit>
}
