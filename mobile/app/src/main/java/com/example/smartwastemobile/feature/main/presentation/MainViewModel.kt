package com.example.smartwastemobile.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartwastemobile.feature.points.data.PointsRepository
import com.example.smartwastemobile.feature.points.data.model.PointsBalanceDto
import com.example.smartwastemobile.feature.points.data.model.PointsHistoryItemDto
import com.example.smartwastemobile.feature.redemptions.data.RedemptionsRepository
import com.example.smartwastemobile.feature.redemptions.data.model.RedemptionDto
import com.example.smartwastemobile.feature.rewards.data.RewardsRepository
import com.example.smartwastemobile.feature.rewards.data.model.RewardDto
import com.example.smartwastemobile.feature.scan.data.ScanRepository
import com.example.smartwastemobile.feature.scan.data.model.ScanResultDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoadingRewards: Boolean = false,
    val rewards: List<RewardDto> = emptyList(),
    val rewardsErrorMessage: String? = null,
    val isLoadingPoints: Boolean = false,
    val pointsBalance: PointsBalanceDto? = null,
    val pointsHistory: List<PointsHistoryItemDto> = emptyList(),
    val pointsErrorMessage: String? = null,
    val isLoadingRedemptions: Boolean = false,
    val redemptions: List<RedemptionDto> = emptyList(),
    val redemptionsErrorMessage: String? = null,
    val isSubmittingRedemption: Boolean = false,
    val redeemingRewardId: Int? = null,
    val redemptionFeedbackMessage: String? = null,
    val recentlyRedeemedRewardId: Int? = null,
    val isSubmittingScan: Boolean = false,
    val scanErrorMessage: String? = null,
    val lastScanResult: ScanResultDto? = null
)

class MainViewModel(
    private val rewardsRepository: RewardsRepository,
    private val pointsRepository: PointsRepository,
    private val scanRepository: ScanRepository,
    private val redemptionsRepository: RedemptionsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun refreshRewards() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingRewards = true, rewardsErrorMessage = null)
            }

            try {
                val rewards = rewardsRepository.getRewards()
                _uiState.update {
                    it.copy(
                        isLoadingRewards = false,
                        rewards = rewards,
                        rewardsErrorMessage = null
                    )
                }
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoadingRewards = false,
                        rewardsErrorMessage = exception.message
                    )
                }
            }
        }
    }

    fun refreshPointsData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingPoints = true, pointsErrorMessage = null)
            }

            try {
                val balance = pointsRepository.getPointsBalance()
                val history = pointsRepository.getPointsHistory(limit = 10)
                _uiState.update {
                    it.copy(
                        isLoadingPoints = false,
                        pointsBalance = balance,
                        pointsHistory = history,
                        pointsErrorMessage = null
                    )
                }
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoadingPoints = false,
                        pointsErrorMessage = exception.message
                    )
                }
            }
        }
    }

    fun refreshAuthenticatedData() {
        refreshRewards()
        refreshPointsData()
        refreshRedemptions()
    }

    fun refreshRedemptions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingRedemptions = true, redemptionsErrorMessage = null)
            }

            try {
                val redemptions = redemptionsRepository.getMyRedemptions(limit = 10)
                _uiState.update {
                    it.copy(
                        isLoadingRedemptions = false,
                        redemptions = redemptions,
                        redemptionsErrorMessage = null
                    )
                }
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isLoadingRedemptions = false,
                        redemptionsErrorMessage = exception.message
                    )
                }
            }
        }
    }

    fun redeemReward(rewardId: Int) {
        viewModelScope.launch {
            val startedAtMs = System.currentTimeMillis()

            _uiState.update {
                it.copy(
                    isSubmittingRedemption = true,
                    redeemingRewardId = rewardId,
                    redemptionFeedbackMessage = null,
                    redemptionsErrorMessage = null,
                    recentlyRedeemedRewardId = null
                )
            }

            try {
                val response = redemptionsRepository.redeemReward(rewardId)
                val elapsedMs = System.currentTimeMillis() - startedAtMs
                val remainingMinimumLoadingMs = (1_000L - elapsedMs).coerceAtLeast(0L)
                if (remainingMinimumLoadingMs > 0L) {
                    delay(remainingMinimumLoadingMs)
                }
                _uiState.update {
                    it.copy(
                        isSubmittingRedemption = false,
                        redeemingRewardId = null,
                        redemptionFeedbackMessage = response.message,
                        recentlyRedeemedRewardId = rewardId,
                        pointsBalance = it.pointsBalance?.copy(
                            currentPointsBalance = response.currentPointsBalance
                        ) ?: it.pointsBalance
                    )
                }

                refreshPointsData()
                refreshRedemptions()

                delay(1400)
                _uiState.update { state ->
                    if (state.recentlyRedeemedRewardId == rewardId && !state.isSubmittingRedemption) {
                        state.copy(
                            recentlyRedeemedRewardId = null,
                            redemptionFeedbackMessage = null
                        )
                    } else {
                        state
                    }
                }
            } catch (exception: IllegalStateException) {
                val elapsedMs = System.currentTimeMillis() - startedAtMs
                val remainingMinimumLoadingMs = (1_000L - elapsedMs).coerceAtLeast(0L)
                if (remainingMinimumLoadingMs > 0L) {
                    delay(remainingMinimumLoadingMs)
                }
                _uiState.update {
                    it.copy(
                        isSubmittingRedemption = false,
                        redeemingRewardId = null,
                        redemptionFeedbackMessage = null,
                        recentlyRedeemedRewardId = null,
                        redemptionsErrorMessage = exception.message
                    )
                }
            }
        }
    }

    fun submitScan(qrRaw: String) {
        if (qrRaw.isBlank()) {
            _uiState.update {
                it.copy(scanErrorMessage = "Scanned QR data was empty.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingScan = true,
                    scanErrorMessage = null,
                    lastScanResult = null
                )
            }

            try {
                val response = scanRepository.claimScan(qrRaw)
                _uiState.update {
                    it.copy(
                        isSubmittingScan = false,
                        scanErrorMessage = if (response.scan.isValid) null else response.scan.invalidReason,
                        lastScanResult = response.scan,
                        pointsBalance = it.pointsBalance?.copy(
                            currentPointsBalance = response.currentPointsBalance
                        ) ?: it.pointsBalance
                    )
                }

                refreshPointsData()
            } catch (exception: IllegalStateException) {
                _uiState.update {
                    it.copy(
                        isSubmittingScan = false,
                        scanErrorMessage = exception.message,
                        lastScanResult = null
                    )
                }
            }
        }
    }

    fun markScanCancelled() {
        _uiState.update {
            it.copy(
                scanErrorMessage = null,
                lastScanResult = null
            )
        }
    }

    fun setScanError(message: String) {
        _uiState.update {
            it.copy(
                scanErrorMessage = message,
                lastScanResult = null
            )
        }
    }

    fun clearScanFeedback() {
        _uiState.update {
            it.copy(
                scanErrorMessage = null,
                lastScanResult = null
            )
        }
    }

    fun clearRedemptionFeedback() {
        _uiState.update {
            it.copy(
                redemptionFeedbackMessage = null,
                redemptionsErrorMessage = null,
                recentlyRedeemedRewardId = null
            )
        }
    }
}

class MainViewModelFactory(
    private val rewardsRepository: RewardsRepository,
    private val pointsRepository: PointsRepository,
    private val scanRepository: ScanRepository,
    private val redemptionsRepository: RedemptionsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                rewardsRepository = rewardsRepository,
                pointsRepository = pointsRepository,
                scanRepository = scanRepository,
                redemptionsRepository = redemptionsRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
