package uk.co.revsys.oddball.worker;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class OddballWorker {

    
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("applicationContext-worker.xml");
    }
    
}
