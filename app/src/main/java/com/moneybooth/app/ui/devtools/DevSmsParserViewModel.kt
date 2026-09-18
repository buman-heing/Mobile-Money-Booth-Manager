package com.moneybooth.app.ui.devtools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.domain.transactions.ParseConfidence
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.ParserOutcome
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.core.domain.transactions.ValidationResult
import com.moneybooth.app.sms.common.IngestionResult
import com.moneybooth.app.sms.common.SmsIngestionPipeline
import com.moneybooth.app.sms.providers.ProviderRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ParsePreview(
    val providerName: String,
    val transactionType: TransactionType,
    val status: TransactionStatus,
    val direction: TransactionDirection,
    val amountMinor: Long?,
    val transactionId: String?,
    val balanceAfterMinor: Long?,
    val commissionMinor: Long?,
    val party: String?,
    val confidence: ParseConfidence,
    val validation: ValidationResult,
)

sealed interface DevSmsUiState {
    data object Idle : DevSmsUiState
    data class NoMatch(val reason: String) : DevSmsUiState
    data class Previewed(val input: RawSmsInput, val result: ParsedTransactionResult, val preview: ParsePreview) : DevSmsUiState
    data class Accepted(val outcome: IngestionResult) : DevSmsUiState
}

class DevSmsParserViewModel(
    private val providerRegistry: ProviderRegistry,
    private val pipeline: SmsIngestionPipeline,
) : ViewModel() {

    private val _state = MutableStateFlow<DevSmsUiState>(DevSmsUiState.Idle)
    val state: StateFlow<DevSmsUiState> = _state

    fun parse(sender: String, body: String) {
        if (body.isBlank()) return
        val input = RawSmsInput(
            sender = sender.ifBlank { "UNKNOWN" },
            body = body,
            receivedTimestamp = System.currentTimeMillis(),
        )
        val provider = providerRegistry.detect(input)
        if (provider == null) {
            _state.value = DevSmsUiState.NoMatch("No provider recognized this message format.")
            return
        }
        when (val outcome = provider.parser.parse(input)) {
            is ParserOutcome.NoMatch -> {
                _state.value = DevSmsUiState.NoMatch("Message format not recognized by the ${provider.providerName} parser.")
            }
            is ParserOutcome.Matched -> {
                val validation = provider.validate(outcome.result)
                val preview = ParsePreview(
                    providerName = provider.providerName,
                    transactionType = outcome.result.transactionType,
                    status = outcome.result.status,
                    direction = outcome.result.direction,
                    amountMinor = outcome.result.amountMinor,
                    transactionId = outcome.result.externalTransactionId,
                    balanceAfterMinor = outcome.result.balanceAfterMinor,
                    commissionMinor = outcome.result.commissionMinor,
                    party = outcome.result.senderName ?: outcome.result.recipientName ?: outcome.result.merchantName,
                    confidence = outcome.result.confidence,
                    validation = validation,
                )
                _state.value = DevSmsUiState.Previewed(input, outcome.result, preview)
            }
        }
    }

    fun accept() {
        val current = _state.value
        if (current !is DevSmsUiState.Previewed) return
        viewModelScope.launch {
            val outcome = pipeline.ingest(current.input, TransactionSource.SMS_DEV_PASTE)
            _state.value = DevSmsUiState.Accepted(outcome)
        }
    }

    fun reject() {
        _state.value = DevSmsUiState.Idle
    }

    fun reset() {
        _state.value = DevSmsUiState.Idle
    }
}
