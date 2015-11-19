package com.webservice.peglefiles;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseController {

    private static final Logger _logger = Logger.getLogger(BaseController.class.getName());

    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    protected BaseResponse handle404Exceptions(Exception ex) {
        String message = ex.getMessage();
        if(message == null && ex.getCause() != null) {
            message = ex.getCause().getMessage();
            if(message == null) {
                StringWriter strWriter = new StringWriter();
                PrintWriter writer = new PrintWriter(strWriter);
                ex.getCause().printStackTrace(writer);
                message = strWriter.toString();
            }
        }
        if(message == null) {
            StringWriter strWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(strWriter);
            ex.printStackTrace(writer);
            message = strWriter.toString();
        }
        _logger.log(Level.SEVERE, "Blad kontrolera !", ex);
        return BaseResponse.error(message);
    }
}
