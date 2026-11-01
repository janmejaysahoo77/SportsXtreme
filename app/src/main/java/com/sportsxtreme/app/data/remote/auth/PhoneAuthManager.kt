package com.sportsxtreme.app.data.remote.auth

import com.sportsxtreme.app.domain.model.PhoneAuthSession

interface PhoneAuthManager {
    suspend fun sendOtp(phoneNumber: String): Result<PhoneAuthSession>
    suspend fun resendOtp(session: PhoneAuthSession): Result<PhoneAuthSession>
    suspend fun verifyOtp(verificationId: String, otpCode: String): Result<Boolean>
}
