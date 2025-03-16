package telran.monitoring;

import telran.monitoring.logging.*;

public class TestStream<T> implements MiddlewareDataStream<T> {
Logger logger;
public TestStream(String loggerName){
    logger = new LoggerStandard(loggerName);
}
    @Override
    public void publish(T obj) {
       logger.log("info", obj.toString());
    }

}
