package com.isssr.ticketing_system.validator;

import com.isssr.ticketing_system.entity.ScrumTeam;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ScrumTeamValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return ScrumTeam.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        ScrumTeam scrumTeam = (ScrumTeam) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.required", "Scrum Team name required");
        if (scrumTeam.getName() != null && (scrumTeam.getName().length() < 1))
            errors.rejectValue("name", "Insert a team name bigger than 1 char");
    }

}
