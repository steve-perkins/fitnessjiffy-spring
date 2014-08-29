package net.steveperkins.fitnessjiffy.controller;

import net.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import net.steveperkins.fitnessjiffy.dto.UserDTO;
import net.steveperkins.fitnessjiffy.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import java.sql.Date;
import java.util.List;

@Controller
public class ExerciseController extends AbstractController {

    @Autowired
    ExerciseService exerciseService;

    @RequestMapping(value = "/exercise", method = RequestMethod.GET)
    @Nonnull
    public String viewMainProfilePage(
            @Nonnull
            @RequestParam(value = "date", defaultValue = TODAY)
            final String dateString,

            @Nonnull final Model model
    ) {
        final UserDTO user = currentAuthenticatedUser();
        final Date date = stringToSqlDate(dateString);
        final List<ExercisePerformedDTO> exercisePerformedThisDate = exerciseService.findPerformedOnDate(user.getId(), date);

        model.addAttribute("dateString", dateString);
        System.out.println("Setting dateString == " + dateString);
        model.addAttribute("exercisesPerformedThisDate", exercisePerformedThisDate);
        return EXERCISE_TEMPLATE;
    }

}
