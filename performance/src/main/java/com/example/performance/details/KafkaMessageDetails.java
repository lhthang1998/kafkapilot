package com.example.performance.details;

import com.example.performance.kafka.Representable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.lang.String.format;


@Getter
@AllArgsConstructor
public class KafkaMessageDetails<K> implements Representable {
    private K key;


    @Override
    public String getRepr() {
        return format("%s", key);
    }
}
