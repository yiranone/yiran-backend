package one.yiran.dashboard.common.validator;

import one.yiran.dashboard.common.annotation.Option;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

@Component
public class OptionValidator implements ConstraintValidator<Option,Object> {

    private String[] options;

    @Override
    public void initialize(Option constraintAnnotation) {
        this.options = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(options == null || options.length == 0)
            return true;
        if(value == null)
            return true;
        return Arrays.asList(options).contains(value.toString());
    }

}
