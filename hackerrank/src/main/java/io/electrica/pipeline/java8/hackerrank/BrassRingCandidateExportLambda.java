package io.electrica.pipeline.java8.hackerrank;

import com.google.auto.service.AutoService;
import io.electrica.pipeline.java8.spi.BackgroundProcessLambda;
import io.electrica.pipeline.java8.spi.Lambda;
import io.electrica.sdk.java8.api.Connection;
import io.electrica.sdk.java8.api.Connector;
import io.electrica.sdk.java8.api.Electrica;
import io.electrica.sdk.java8.hackerrank.v3.tests.v1.HackerRankV3Candidates;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static io.electrica.pipeline.java8.hackerrank.CandidateExportMessageListener.MESSAGE_FILTER;

@Slf4j
@AutoService(Lambda.class)
public class BrassRingCandidateExportLambda extends BackgroundProcessLambda {

    private static final String LAMBDA_NAME = "BrassRingCandidateExport";
    private static final String HACKERRANK_CONNECTION_NAME = "BrassRing";

    private Connection connection;
    private UUID listenerId;

    @Override
    public String getName() {
        return LAMBDA_NAME;
    }

    @Override
    public void initialize(Electrica electrica) throws Exception {
        super.initialize(electrica);

        Connector connector = electrica.connector(HackerRankV3Candidates.ERN);
        connection = connector.connection(HACKERRANK_CONNECTION_NAME);
        HackerRankV3Candidates candidates = new HackerRankV3Candidates(connection);
        listenerId = connection.addMessageListener(MESSAGE_FILTER, new CandidateExportMessageListener(candidates));
    }

    @Override
    public void destroy(Electrica electrica) throws Exception {
        super.destroy(electrica);

        connection.removeMessageListener(listenerId);
    }
}
