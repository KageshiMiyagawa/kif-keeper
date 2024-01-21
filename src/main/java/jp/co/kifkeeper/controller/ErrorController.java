package jp.co.kifkeeper.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
    	ex.printStackTrace();
		//        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}