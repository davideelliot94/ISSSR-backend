package com.isssr.ticketing_system.validator;

import com.isssr.ticketing_system.entity.Sprint;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class SprintValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Sprint.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        
    }

    //TODO definire un validator per sprint
   /* @Override
    public void validate(Object o, Errors errors) {
        Sprint sprint = (Sprint) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "", "title.required", "Ticket title required");

    }*/
}
