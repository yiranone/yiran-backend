package one.yiran.dashboard.manage.validator;

import lombok.Data;
import one.yiran.dashboard.common.annotation.Option;
import org.hibernate.validator.constraints.Length;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OptionValidatorTest {
    public static void main(String[] args) {
        TestModel testModel = new TestModel();
        testModel.setStatus("1212");

        Set<ConstraintViolation<TestModel>> validRes = Validation
                .buildDefaultValidatorFactory()
                .getValidator()
                .validate(testModel);
        List<String> res = validRes
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        System.out.println(">>>>>>>" + res);
    }
}

@Data
class TestModel {

    @Length(max = 100)
    @Option(value = {"0","1"},message = "状态只能是0，1。 0=正常,1=停用")
    private String status;
}
