package de.menkalian.aquila.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ElementNotFoundException : RuntimeException("Element was not found")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class IllegalInputException : IllegalArgumentException("Input validation failed")

@ResponseStatus(HttpStatus.FORBIDDEN)
class InsufficientUserRightsException : RuntimeException("The authenticated user may not perform this action")

@ResponseStatus(HttpStatus.CONFLICT)
class RedundantDataException : RuntimeException("The created data already exists")

