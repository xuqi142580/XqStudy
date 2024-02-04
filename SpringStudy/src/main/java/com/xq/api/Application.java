package com.xq.api;

import com.xq.config.AppConfig;
import com.xq.context.AnnotionContextApplication;


public class Application {

    public static void main(String[] args) throws Exception {
        AnnotionContextApplication annotionContextApplication = new AnnotionContextApplication(AppConfig.class);

    }
}
