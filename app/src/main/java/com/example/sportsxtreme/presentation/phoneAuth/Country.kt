package com.example.sportsxtreme.presentation.phoneAuth

data class Country(
    val code: String,
    val name: String,
    val dialCode: String,
    val flagEmoji: String,
    val digitCount: Int,
    val placeholder: String
) {
    companion object {
        val ALL: List<Country> = listOf(
            Country("IN", "India", "+91", "🇮🇳", 10, "98765 43210"),
            Country("US", "United States", "+1", "🇺🇸", 10, "(555) 000-0000"),
            Country("GB", "United Kingdom", "+44", "🇬🇧", 10, "7911 123456"),
            Country("AU", "Australia", "+61", "🇦🇺", 9, "412 345 678"),
            Country("CA", "Canada", "+1", "🇨🇦", 10, "(555) 000-0000"),
            Country("AE", "United Arab Emirates", "+971", "🇦🇪", 9, "50 123 4567"),
            Country("SA", "Saudi Arabia", "+966", "🇸🇦", 9, "50 123 4567"),
            Country("SG", "Singapore", "+65", "🇸🇬", 8, "8123 4567"),
            Country("NZ", "New Zealand", "+64", "🇳🇿", 9, "21 123 4567"),
            Country("ZA", "South Africa", "+27", "🇿🇦", 9, "71 123 4567"),
            Country("PK", "Pakistan", "+92", "🇵🇰", 10, "300 1234567"),
            Country("BD", "Bangladesh", "+880", "🇧🇩", 10, "1712 345678"),
            Country("LK", "Sri Lanka", "+94", "🇱🇰", 9, "71 234 5678"),
            Country("NP", "Nepal", "+977", "🇳🇵", 10, "98 12345678"),
            Country("DE", "Germany", "+49", "🇩🇪", 10, "151 12345678"),
            Country("FR", "France", "+33", "🇫🇷", 9, "6 12 34 56 78"),
            Country("BR", "Brazil", "+55", "🇧🇷", 11, "11 91234-5678"),
            Country("JP", "Japan", "+81", "🇯🇵", 10, "90 1234 5678"),
            Country("KE", "Kenya", "+254", "🇰🇪", 9, "712 345678"),
            Country("NG", "Nigeria", "+234", "🇳🇬", 10, "802 123 4567"),
            Country("QA", "Qatar", "+974", "🇶🇦", 8, "3312 3456"),
            Country("KW", "Kuwait", "+965", "🇰🇼", 8, "5123 4567"),
            Country("OM", "Oman", "+968", "🇴🇲", 8, "9123 4567"),
            Country("BH", "Bahrain", "+973", "🇧🇭", 8, "3612 3456"),
            Country("ID", "Indonesia", "+62", "🇮🇩", 11, "812 3456 7890"),
            Country("MY", "Malaysia", "+60", "🇲🇾", 10, "12 345 6789"),
            Country("PH", "Philippines", "+63", "🇵🇭", 10, "917 123 4567"),
            Country("IT", "Italy", "+39", "🇮🇹", 10, "312 345 6789"),
            Country("ES", "Spain", "+34", "🇪🇸", 9, "612 345 678"),
            Country("MX", "Mexico", "+52", "🇲🇽", 10, "55 1234 5678"),
            Country("NL", "Netherlands", "+31", "🇳🇱", 9, "6 12345678"),
            Country("IE", "Ireland", "+353", "🇮🇪", 9, "85 123 4567"),
            Country("SE", "Sweden", "+46", "🇸🇪", 9, "70 123 45 67"),
            Country("NO", "Norway", "+47", "🇳🇴", 8, "412 34 567"),
            Country("DK", "Denmark", "+45", "🇩🇰", 8, "20 12 34 56"),
            Country("CH", "Switzerland", "+41", "🇨🇭", 9, "78 123 45 67"),
            Country("EG", "Egypt", "+20", "🇪🇬", 10, "10 1234 5678"),
            Country("TR", "Turkey", "+90", "🇹🇷", 10, "532 123 4567"),
            Country("AR", "Argentina", "+54", "🇦🇷", 10, "11 1234 5678"),
            Country("CO", "Colombia", "+57", "🇨🇴", 10, "300 123 4567"),
            Country("CL", "Chile", "+56", "🇨🇱", 9, "9 1234 5678"),
            Country("VN", "Vietnam", "+84", "🇻🇳", 9, "91 234 5678"),
            Country("TH", "Thailand", "+66", "🇹🇭", 9, "81 234 5678"),
            Country("KR", "South Korea", "+82", "🇰🇷", 10, "10 1234 5678"),
            Country("HK", "Hong Kong", "+852", "🇭🇰", 8, "9123 4567")
        )

        val DEFAULT: Country = ALL.first { it.code == "IN" }
    }
}
