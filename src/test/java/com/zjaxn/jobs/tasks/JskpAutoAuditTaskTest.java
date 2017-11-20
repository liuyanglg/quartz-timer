package com.zjaxn.jobs.tasks;

import com.base.BaseTest;
import com.zjaxn.jobs.Executor;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JskpAutoAuditTaskTest extends BaseTest {
    @Test
    public void autoAuditCard() throws Exception {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                InputStream is = Executor.class.getResourceAsStream("/log4j.properties");
                Properties properties = new Properties();
                try {
                    properties.load(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PropertyConfigurator.configure(properties);
                @SuppressWarnings("resource")
                ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml");
            }
        };
        Thread t = new Thread(r);
        t.start();

        JskpAutoAuditTask jskpAutoAuditTask = new JskpAutoAuditTask();
        jskpAutoAuditTask.batchAutoAudit();

    }
}