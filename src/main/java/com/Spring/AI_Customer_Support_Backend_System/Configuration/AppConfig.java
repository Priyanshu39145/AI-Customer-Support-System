package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    public ModelMapper modelMapper()    {
        return new ModelMapper();
    }
}
