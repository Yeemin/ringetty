package org.example.ringetty.demo.rest;

import org.example.ringetty.web.Rest;

@Rest
public class DemoRest {

    @Rest("/demo")
    public String demo() {
        return "SUCCESS";
    }

}
