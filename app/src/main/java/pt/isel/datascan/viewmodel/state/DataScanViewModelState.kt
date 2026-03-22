package pt.isel.datascan.viewmodel.state


import pt.isel.datascan.domain.ScanReading


const val DEFAULT_TIMEOUT = 15 * 60
const val DEFAULT_INTERVAL = 30
const val IS_TEST_TRIP = false

const val NOTIFICATION_REMINDER_INTERVAL = 3 * 60

const val DEFAULT_SUBJ_RATING = 3


data class DataScanUiState(
    val isRiding: Boolean = false,
    val isAwaitingInitialRating: Boolean = false,
    val secondsRemaining: Int = DEFAULT_TIMEOUT,
    val tripId: String? = null,
    val currentSubjectiveRating: Int = DEFAULT_SUBJ_RATING,
    val lastRead: ScanReading? = null
)
