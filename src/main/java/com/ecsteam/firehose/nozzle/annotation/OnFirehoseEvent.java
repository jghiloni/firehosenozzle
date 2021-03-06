package com.ecsteam.firehose.nozzle.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.cloudfoundry.doppler.EventType;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnFirehoseEvent {
	EventType[] eventTypes() default {EventType.COUNTER_EVENT, EventType.VALUE_METRIC,EventType.CONTAINER_METRIC, EventType.HTTP_START_STOP, EventType.LOG_MESSAGE };
}
