package net.steveperkins.fitnessjiffy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nonnull;

@Controller
public class ReportController extends AbstractController {

    @RequestMapping(value = {"/report"}, method = RequestMethod.GET)
    @Nonnull
    public String viewMainReportPage() {
        return REPORT_TEMPLATE;
    }

}
