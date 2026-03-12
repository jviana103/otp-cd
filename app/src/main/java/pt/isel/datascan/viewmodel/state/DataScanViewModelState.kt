package pt.isel.datascan.viewmodel.state


import pt.isel.datascan.domain.ScanReading
import kotlin.time.Duration.Companion.seconds

const val DEFAULT_TIMEOUT = 15 * 60
const val DEFAULT_SUBJ_RATING = 3

const val DEFAULT_INTERVAL = 30

data class DataScanUiState(
    val isRiding: Boolean = false,
    val isAwaitingInitialRating: Boolean = false,
    val secondsRemaining: Int = DEFAULT_TIMEOUT,
    val tripId: String? = null,
    val currentSubjectiveRating: Int = DEFAULT_SUBJ_RATING,
    val lastRead: ScanReading? = null
)
