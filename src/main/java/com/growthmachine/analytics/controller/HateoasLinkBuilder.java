package com.growthmachine.analytics.controller;

import org.springframework.hateoas.EntityModel;

public interface HateoasLinkBuilder<T> {
    void addLinks(EntityModel<T> model);
}