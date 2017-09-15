package com.zjaxn.jobs;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 应用主类
 *
 * @author
 */
public class Executor {

    /**
     * 应用启动入口，完成日志系统加载框架加载
     */
    public static ApplicationContext context;

    public static void main(String[] args) {

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

//                PropertyConfigurator.configure(".." + File.separator + "res" + File.separator + "log4j.properties");
                @SuppressWarnings("resource")
                ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application.xml", "application-task.xml");
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}


