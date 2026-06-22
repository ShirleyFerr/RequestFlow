package com.requestflow.exception;

import com.requestflow.dto.error.ErrorResponseDTO;
import com.requestflow.dto.error.FieldErrorDTO;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ConstraintViolationException validationException) {
            return buildValidationResponse(validationException);
        }
        if (exception instanceof BusinessException) {
            return buildResponse(Response.Status.BAD_REQUEST, exception.getMessage(), List.of());
        }
        if (exception instanceof UnauthorizedException) {
            return buildResponse(Response.Status.UNAUTHORIZED, exception.getMessage(), List.of());
        }
        if (exception instanceof ForbiddenException) {
            return buildResponse(Response.Status.FORBIDDEN, exception.getMessage(), List.of());
        }
        if (exception instanceof NotFoundException) {
            return buildResponse(Response.Status.NOT_FOUND, exception.getMessage(), List.of());
        }
        if (exception instanceof ConflictException) {
            return buildResponse(Response.Status.CONFLICT, exception.getMessage(), List.of());
        }
        if (exception instanceof BadRequestException) {
            return buildResponse(Response.Status.BAD_REQUEST, safeMessage(exception, "Requisicao invalida"), List.of());
        }
        if (exception instanceof WebApplicationException webException) {
            return buildWebApplicationResponse(webException);
        }

        LOG.error("Unhandled API exception", exception);

        return buildResponse(
                Response.Status.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado. Tente novamente ou contate o suporte.",
                List.of()
        );
    }

    private Response buildValidationResponse(ConstraintViolationException exception) {
        List<FieldErrorDTO> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new FieldErrorDTO(
                        extractFieldName(violation.getPropertyPath().toString()),
                        violation.getMessage()
                ))
                .toList();

        return buildResponse(Response.Status.BAD_REQUEST, "Dados invalidos", fieldErrors);
    }

    private Response buildWebApplicationResponse(WebApplicationException exception) {
        Response.Status status = Response.Status.fromStatusCode(exception.getResponse().getStatus());
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return buildResponse(status, safeMessage(exception, status.getReasonPhrase()), List.of());
    }

    private Response buildResponse(Response.Status status, String message, List<FieldErrorDTO> fieldErrors) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.getStatusCode(),
                status.getReasonPhrase(),
                message,
                currentPath(),
                fieldErrors
        );

        return Response.status(status)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .entity(error)
                .build();
    }

    private String safeMessage(Throwable exception, String fallback) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallback;
        }
        return exception.getMessage();
    }

    private String currentPath() {
        if (uriInfo == null || uriInfo.getPath() == null) {
            return "";
        }
        return "/" + uriInfo.getPath();
    }

    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "";
        }
        int lastDot = propertyPath.lastIndexOf('.');
        if (lastDot < 0 || lastDot == propertyPath.length() - 1) {
            return propertyPath;
        }
        return propertyPath.substring(lastDot + 1);
    }
}
