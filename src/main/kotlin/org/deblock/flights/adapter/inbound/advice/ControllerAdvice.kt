package org.deblock.flights.adapter.inbound.advice

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerAdvice {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val cause = ex.cause
        return when (cause) {
            is InvalidFormatException -> invalidFormat(cause)
            is MissingKotlinParameterException -> missingParameter(cause)
            is ValueInstantiationException -> valueInstantiation(cause)
            else -> ResponseEntity.badRequest().body(ErrorResponse.invalidRequestBody())
        }
    }

    @ExceptionHandler(ConstraintViolationException::class, MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: Exception): ResponseEntity<ErrorResponse> {
        val details =
            when (ex) {
                is ConstraintViolationException -> ex.constraintViolations.map { it.message }
                is MethodArgumentNotValidException -> ex.bindingResult.fieldErrors.mapNotNull { it.defaultMessage }
                else -> emptyList()
            }
        return ResponseEntity.badRequest().body(ErrorResponse.invalidRequestBody(details))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error while handling request", ex)
        return ResponseEntity.internalServerError().body(ErrorResponse.internalServerError())
    }

    private fun valueInstantiation(ex: ValueInstantiationException): ResponseEntity<ErrorResponse> {
        val details = if (ex.cause is IllegalArgumentException && ex.cause?.message != null) {
            listOf(ex.cause!!.message!!)
        } else emptyList()
        return ResponseEntity.badRequest().body(ErrorResponse.invalidRequestBody(details))
    }

    private fun missingParameter(ex: MissingKotlinParameterException): ResponseEntity<ErrorResponse> {
        val fieldName = ex.path.firstOrNull()?.fieldName ?: "unknown"
        return ResponseEntity.badRequest().body(
            ErrorResponse.invalidRequestBody("Missing request body parameter: $fieldName"),
        )
    }

    private fun invalidFormat(ex: InvalidFormatException): ResponseEntity<ErrorResponse> {
        val fieldName = ex.path.firstOrNull()?.fieldName ?: "unknown"
        return ResponseEntity.badRequest().body(
            ErrorResponse.invalidRequestBody("Invalid format for parameter: $fieldName"),
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)
    }
}

data class ErrorResponse(
    val error: String,
    val details: List<String> = emptyList(),
) {
    companion object {
        fun invalidRequestBody(errorDetails: List<String> = emptyList()): ErrorResponse =
            ErrorResponse("Invalid request body", errorDetails)

        fun invalidRequestBody(singleDetail: String): ErrorResponse =
            invalidRequestBody(listOf(singleDetail))

        fun internalServerError(): ErrorResponse =
            ErrorResponse("Internal server error")
    }
}
