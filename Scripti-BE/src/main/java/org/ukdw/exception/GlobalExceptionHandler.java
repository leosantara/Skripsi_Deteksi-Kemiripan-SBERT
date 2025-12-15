/**
 * Author: dendy
 * Date:01/10/2024
 * Time:13:23
 * Description: Spring 3.2 brings support for a global @ExceptionHandler with the @ControllerAdvice annotation.
 * This enables a mechanism that breaks away from the older MVC model and makes use of ResponseEntity
 * along with the type safety and flexibility of @ExceptionHandler:
 * The@ControllerAdvice annotation allows us to consolidate our multiple, scattered @ExceptionHandlers from before into
 * a single, global error handling component.
 * <p>
 * The actual mechanism is extremely simple but also very flexible:
 * <p>
 * It gives us full control over the body of the response as well as the status code.
 * It provides mapping of several exceptions to the same method, to be handled together.
 * It makes good use of the newer RESTful ResponseEntity response.
 * One thing to keep in mind here is to match the exceptions declared with @ExceptionHandler to the exception used as the argument of the method.
 * <p>
 * If these don’t match, the compiler will not complain — no reason it should — and Spring will not complain either.
 * <p>
 * However, when the exception is actually thrown at runtime,  the exception resolving mechanism will fail with.
 * https://www.baeldung.com/exception-handling-for-rest-with-spring
 */

package org.ukdw.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.ukdw.dto.response.ResponseWrapper;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Handle all 403 Forbidden errors
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                AccessDeniedException.class.getSimpleName(),
                ex.getMessage()
        );
        ResponseWrapper<ErrorResponse> response = new ResponseWrapper<>(HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN", errorResponse);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseEntity<ResponseWrapper<ErrorResponse> > handleAuthenticationException(AuthenticationException ex) {

        ErrorResponse errorResponse = new ErrorResponse(
                AuthenticationException.class.getSimpleName(),
                ex.getMessage()
        );
        ResponseWrapper<ErrorResponse> response = new ResponseWrapper<>(HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED", errorResponse);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseBody
    public ResponseEntity<ResponseWrapper<ErrorResponse>> handleBadRequestException(BadRequestException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                BadRequestException.class.getSimpleName(),
                ex.getMessage()
        );
        ResponseWrapper<ErrorResponse> response = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ScNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNotFoundException(ScNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ScNotFoundException.class.getSimpleName(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    //https://www.baeldung.com/spring-boot-bean-validation
    /*By overriding this method, you are customizing the default Spring behavior for validation errors
    (@Valid or @Validated annotations).*/
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");
        ErrorResponse errorResponse = new ErrorResponse(
                MethodArgumentNotValidException.class.getSimpleName(),
                errorMessage
        );
        ResponseWrapper<ErrorResponse> response = new ResponseWrapper<>(HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST", errorResponse);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
