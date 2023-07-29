package com.senderman.jlogrep.controller;

import com.senderman.jlogrep.exception.BadRequestException;
import com.senderman.jlogrep.exception.NoSuchTaskException;
import com.senderman.jlogrep.model.response.Message;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ErrorController {

    private final static Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @Error(global = true)
    public HttpResponse<Message> badRequest(BadRequestException e) {
        logger.error(e.getMessage(), e);
        return HttpResponse.badRequest(new Message(HttpStatus.BAD_REQUEST.getCode(), e.getMessage()));
    }

    @Error(global = true)
    public HttpResponse<Message> serverError(Throwable t) {
        logger.error(t.getMessage(), t);
        return HttpResponse.serverError(new Message(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), t.getMessage()));
    }

    @Error(global = true)
    public HttpResponse<Message> noSuchTask(NoSuchTaskException e) {
        logger.warn(e.getMessage());
        return HttpResponse.notFound(new Message(HttpStatus.NOT_FOUND.getCode(), e.getMessage()));
    }

    @Error(global = true)
    public HttpResponse<Message> unsatisfiedRequest(UnsatisfiedRouteException e) {
        logger.error(e.getMessage(), e);
        return HttpResponse.badRequest(new Message(HttpStatus.BAD_REQUEST.getCode(), e.getMessage()));
    }

}
