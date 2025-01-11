module kitchensink.main {
    requires com.fasterxml.jackson.databind;
    requires com.nimbusds.jose.jwt;
    requires jakarta.validation;
    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.mapstruct.processor;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.mongodb;
    requires spring.security.config;
    requires spring.security.core;
    requires spring.security.crypto;
    requires spring.security.web;
    requires spring.web;
    requires spring.webmvc;
    requires org.slf4j;
}