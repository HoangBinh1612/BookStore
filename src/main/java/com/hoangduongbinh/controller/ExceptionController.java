package com.hoangduongbinh.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller xử lý lỗi HTTP: 404, 403, 500.
 */
@Controller
public class ExceptionController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            }
        }

        // Ghi lại lỗi thực sự để hiển thị trên màn hình
        StringBuilder errorDetail = new StringBuilder();
        if (exception != null) {
            errorDetail.append("Exception: ").append(exception.toString()).append("\n");
            if (exception instanceof Throwable) {
                Throwable t = (Throwable) exception;
                if (t.getCause() != null) {
                    errorDetail.append("Cause: ").append(t.getCause().toString()).append("\n");
                }
            }
        }
        if (message != null) {
            errorDetail.append("Message: ").append(message.toString());
        }
        model.addAttribute("errorDetail", errorDetail.toString());

        return "error/500";
    }
}
