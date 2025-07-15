package com.steveperkins.fitnessjiffy.controller;

import com.steveperkins.fitnessjiffy.dto.ExerciseDTO;
import com.steveperkins.fitnessjiffy.dto.ExercisePerformedDTO;
import com.steveperkins.fitnessjiffy.dto.UserDTO;
import com.steveperkins.fitnessjiffy.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Controller
final class ExerciseController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExerciseController.class);

    private final ExerciseService exerciseService;

    private final Function<ExerciseDTO, ExerciseDTO> truncateExerciseDescriptionFunction = (ExerciseDTO exerciseDTO) -> {
        if (exerciseDTO.getDescription().length() > 50) {
            final String description = exerciseDTO.getDescription().substring(0, 47) + "...";
            exerciseDTO.setDescription(description);
        }
        return exerciseDTO;
    };

    @Autowired
    public ExerciseController(@Nonnull final ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @RequestMapping(value = "/exercise", method = RequestMethod.GET)
    @Nonnull
    public final String viewMainExercisePage(
            @Nullable
            @RequestParam(value = "date", required = false)
            final String dateString,

            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final Date date = dateString == null ? todaySqlDateForUser(userDTO) : stringToSqlDate(dateString);

        final List<ExerciseDTO> exercisesPerformedRecently = exerciseService.findPerformedRecently(userDTO.getId(), date)
                .stream()
                .map(truncateExerciseDescriptionFunction)
                .collect(toList());

        final List<String> categories = exerciseService.findAllCategories();
        final String firstCategory = (categories.size() > 0) ? categories.get(0) : "";
        final List<ExerciseDTO> exercisesInCategory = exerciseService.findExercisesInCategory(firstCategory)
                .stream()
                .map(truncateExerciseDescriptionFunction)
                .collect(toList());

        final List<ExercisePerformedDTO> exercisePerformedThisDate = exerciseService.findPerformedOnDate(userDTO.getId(), date);
        int totalMinutes = 0;
        int totalCaloriesBurned = 0;
        for (final ExercisePerformedDTO exercisePerformed : exercisePerformedThisDate) {
            totalMinutes += exercisePerformed.getMinutes();
            totalCaloriesBurned += exercisePerformed.getCaloriesBurned();
        }

        model.addAttribute("dateString", dateString);
        model.addAttribute("exercisesPerformedRecently", exercisesPerformedRecently);
        model.addAttribute("categories", categories);
        model.addAttribute("exercisesInCategory", exercisesInCategory);
        model.addAttribute("exercisesPerformedThisDate", exercisePerformedThisDate);
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("totalCaloriesBurned", totalCaloriesBurned);
        return EXERCISE_TEMPLATE;
    }

    @RequestMapping(value = "/exercise/performed/add")
    @Nonnull
    public final String addExercisePerformed(
            @Nonnull @RequestParam(value = "exerciseId", required = true) final String exerciseIdString,
            @Nullable @RequestParam(value = "date", required = false) final String dateString,
            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final Date date = dateString == null ? todaySqlDateForUser(userDTO) : stringToSqlDate(dateString);
        final UUID exerciseId = UUID.fromString(exerciseIdString);
        exerciseService.addExercisePerformed(userDTO.getId(), exerciseId, date);
        return viewMainExercisePage(dateString, model);
    }

    @RequestMapping(value = "/exercise/performed/update")
    @Nonnull
    public final String updateExercisePerformed(
            @Nonnull @RequestParam(value = "exercisePerformedId", required = true) final String exercisePerformedId,
            @RequestParam(value = "minutes", required = true, defaultValue = "1") final int minutes,
            @Nonnull @RequestParam(value = "action", required = true) final String action,
            @Nonnull final Model model
    ) {
        final UserDTO userDTO = currentAuthenticatedUser();
        final UUID exercisePerformedUUID = UUID.fromString(exercisePerformedId);
        final ExercisePerformedDTO exercisePerformedDTO = exerciseService.findExercisePerformedById(exercisePerformedUUID);
        final String dateString = dateFormat.format(exercisePerformedDTO.getDate());
        if (!userDTO.getId().equals(exercisePerformedDTO.getUserId())) {
            LOGGER.info("This user is unable to update this exercise performed");
        } else if (action.equalsIgnoreCase("update")) {
            exerciseService.updateExercisePerformed(exercisePerformedUUID, minutes);
        } else if (action.equalsIgnoreCase("delete")) {
            exerciseService.deleteExercisePerformed(exercisePerformedUUID);
        }
        return viewMainExercisePage(dateString, model);
    }

    @RequestMapping(value = "/exercise/bycategory/{category}")
    @Nonnull
    public final
    @ResponseBody
    List<ExerciseDTO> findExercisesInCategory(@Nonnull @PathVariable final String category) {
        return exerciseService.findExercisesInCategory(category);
    }

    @RequestMapping(value = "/exercise/search/{searchString}")
    @Nonnull
    public final
    @ResponseBody
    List<ExerciseDTO> searchExercises(@Nonnull @PathVariable final String searchString) {
        return exerciseService.searchExercises(searchString);
    }

}
