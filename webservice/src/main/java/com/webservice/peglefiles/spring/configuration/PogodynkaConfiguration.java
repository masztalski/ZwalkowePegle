package com.webservice.peglefiles.spring.configuration;

import com.webservice.peglefiles.webservices.ListaStacjiWebService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import retrofit.RestAdapter;

/**
 * Created by mmar12 on 2015-10-21.
 */
@Configuration
public class PogodynkaConfiguration {
    @Bean
    public ListaStacjiWebService listaStacji(RestAdapter.Builder restAdapter) {
        return restAdapter.setEndpoint("http://monitor.pogodynka.pl/api").build().create(ListaStacjiWebService.class);
    }

    @Bean
    @Scope("prototype")
    public RestAdapter.Builder restAdapter() {
        return new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.BASIC);
    }
}
