package com.example.performance.interfaces;

import com.typesafe.scalalogging.Logger;

public interface StrictLogging extends com.typesafe.scalalogging.StrictLogging {

    default void com$typesafe$scalalogging$StrictLogging$_setter_$logger_$eq(Logger x$1) {

    }

    default Logger logger() {
        return null;
    }
}
