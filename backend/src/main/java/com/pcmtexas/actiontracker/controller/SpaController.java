package com.pcmtexas.actiontracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards all unknown routes to index.html so Angular's client-side router handles them.
 * API, actuator, oauth2, and login routes are excluded by having more specific mappings.
 */
@Controller
public class SpaController {

    @RequestMapping(value = { "/", "/{path:^(?!api|actuator|oauth2|login|error).*$}/**" })
    public String forward() {
        return "forward:/index.html";
    }
}
