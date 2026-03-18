package pt.isel.datascan.viewmodel.state


import pt.isel.datascan.domain.ScanReading


var DEFAULT_TIMEOUT = 15 * 60
var DEFAULT_INTERVAL = 30
var IS_TEST_TRIP = false

const val DEFAULT_SUBJ_RATING = 3

data class DataScanUiState(
    val isRiding: Boolean = false,
    val isAwaitingInitialRating: Boolean = false,
    val secondsRemaining: Int = DEFAULT_TIMEOUT,
    val tripId: String? = null,
    val currentSubjectiveRating: Int = DEFAULT_SUBJ_RATING,
    val lastRead: ScanReading? = null
)
