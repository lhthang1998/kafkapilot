package com.example.performance.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface StrictLogging extends com.typesafe.scalalogging.StrictLogging {
    Logger logger = LoggerFactory.getLogger(StrictLogging.class);

    default void com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq(com.typesafe.scalalogging.Logger x$1) {
    }

    default com.typesafe.scalalogging.Logger logger() {
        return new com.typesafe.scalalogging.Logger(logger);
    }
}
