package net.steveperkins.fitnessjiffy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Controller
public final class AuthenticationController {

    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    @Nonnull
    public String viewLoginPage(
            @Nullable @RequestParam(value = "date", required = false) final String dateString,
            @Nonnull final Model model
    ) {

        // TODO: Render login (and/or OpenID/OAuth provider selector) page

        return "";
    }

    // TODO: Add method(s) to process login form submission, as well as OpenID/OAuth callbacks

}
