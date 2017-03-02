package com.ecsteam.firehose.nozzle;

import com.ecsteam.firehose.nozzle.annotation.FirehoseNozzle;
import com.ecsteam.firehose.nozzle.annotation.OnFirehoseEvent;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@ToString
@Service
@Slf4j
public class FirehoseReader implements SmartLifecycle {

    private final FirehoseProperties props;
    private final ReactorDopplerClient dopplerClient;
    private final String subscriptionId;
    private final Object bean;
    private Method onEventMethod;
    private HashMap<String, EventType> eventTypes;

    private boolean running = false;


    @Autowired
    public FirehoseReader(FirehoseProperties props, ApplicationContext context, ReactorDopplerClient dopplerClient) {
        this.props = props;
        this.dopplerClient = dopplerClient;

        String[] names = context.getBeanNamesForAnnotation(FirehoseNozzle.class);
        FirehoseNozzle fn = context.findAnnotationOnBean(names[0],FirehoseNozzle.class);
        this.bean = context.getBean(names[0]);
        this.subscriptionId = fn.subscriptionId();

        log.info("************ FirehoseReader CONSTRUCTED! (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        log.info("************ " + this.toString());
    }

    @Override
    public boolean isAutoStartup() {
        log.info("************ FirehoseReader isAutoStartup() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        log.info("************ FirehoseReader stop(Runnable) (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        callback.run();
        stop();
    }

    @Override
    public void start() {
        log.info("************ FirehoseReader start() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        running = true;
        
        
        Method[] allMethods = bean.getClass().getMethods();
        for (Method method: allMethods) {
        	if (method.isAnnotationPresent(OnFirehoseEvent.class)) {
        		OnFirehoseEvent annotationInstance = method.getAnnotation(OnFirehoseEvent.class);
        		
        		
                
                
                Class[] methodParams = method.getParameterTypes();
                if (methodParams.length != 1) {
                	log.error("*********** incorrect number of parameters declared for onFirehoseEvent annotated method ****");
                }
                else {
                	if (methodParams[0] == Envelope.class) {
                		onEventMethod = method;
                		log.info("************ FirehoseReader onEvent discovered! " + Calendar.getInstance().getTimeInMillis() + " **************");
                        log.info("************ " + onEventMethod.toString());
                        EventType[] annotatedTypes = annotationInstance.eventTypes();
                        eventTypes = new HashMap<String, EventType>();
                        for (EventType type : annotatedTypes) {
                        	log.info("****** filtering on type " + type.toString() +  "*********");
                        	eventTypes.put(type.toString(), type);
                        }
                        
                        
                	}
                	else {
                		log.error("*********** single parameter for onFirehoseEvent annotated method is of class " + methodParams[0].toGenericString() + " and needs to be of type Envelope  ****");
                	}
                	
                }
        	}
        }
        

        log.info("Building a Firehose Request object");
        FirehoseRequest request = FirehoseRequest.builder()
                .subscriptionId(this.subscriptionId).build();

        
        log.info("Connecting to the Firehose");
        dopplerClient.firehose(request)
                .doOnError(this::receiveConnectionError)
                //.retry()
                .subscribe(this::receiveEvent, this::receiveError);
        
        log.info("Connected to the Firehose");


    }

    @Override
    public void stop() {
        log.info("************ FirehoseReader stop() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        running = false;
    }

    @Override
    public boolean isRunning() {
        log.info("************ FirehoseReader isRunning() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        return running;
    }

    @Override
    public int getPhase() {
        log.info("************ FirehoseReader getPhase() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        return 0;
    }


    private void receiveEvent(Envelope envelope) {
        /*
    	log.info("************ FirehoseReader receiveEvent() (" + this.hashCode() + ") " + Calendar.getInstance().getTimeInMillis() + " **************");
        log.info(envelope.toString());
        log.info("********************************************************************************");
        */
    	
    	
    	if (eventTypes.containsKey(envelope.getEventType().toString())) {
	    	try {
				onEventMethod.invoke(bean, envelope);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    	
    	

        /*
        switch (envelope.getEventType()) {
            case COUNTER_EVENT:
            case VALUE_METRIC:
                break;
            default:
        }
        */
    }
    
    private void receiveConnectionError(Throwable error) {
        log.error("Error in connecting to Firehose : {}", error.getMessage());
        if (log.isDebugEnabled()) {
            error.printStackTrace();
        }
    }

    private void receiveError(Throwable error) {
        log.error("Error in receiving Firehose event: {}", error.getMessage());
        if (log.isDebugEnabled()) {
            error.printStackTrace();
        }
    }
}
