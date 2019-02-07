package io.electrica.pipeline.java8.launcher;

import io.electrica.pipeline.java8.spi.Lambda;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

@Slf4j
class LambdaLoader {

    private LambdaLoader() {
    }

    static List<Lambda> load() {
        Map<String, Lambda> lambdas = new HashMap<>();
        for (Lambda lambda : ServiceLoader.load(Lambda.class)) {
            String name = lambda.getName();
            checkArgument(
                    !isNullOrEmpty(name),
                    "Lambda name cannot be null: %s",
                    lambda.getClass().getName()
            );
            Lambda old = lambdas.put(name, lambda);
            checkArgument(isNull(old), "Duplicate lambda for name: %s", name);
        }

        // initialize lambdas
        List<Lambda> result = lambdas.values().stream()
                .sorted(Comparator.comparing(Lambda::getName))
                .collect(Collectors.toList());


        log.info(
                "Successfully loaded following lambdas: [{}]",
                result.stream()
                        .map(Lambda::getName)
                        .collect(Collectors.joining(", "))
        );

        return result;
    }

    static Lambda newInstanceOf(Lambda source) throws Exception {
        return source.getClass().newInstance();
    }
}
