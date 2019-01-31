package io.electrica.pipeline.java8.hackerrank;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.electrica.connector.brassring.application.v1.model.Envelope;
import io.electrica.connector.brassring.application.v1.model.Packet;
import io.electrica.connector.brassring.application.v1.model.Status;
import io.electrica.pipeline.java8.hackerrank.dto.CandidateDto;
import io.electrica.pipeline.java8.hackerrank.dto.RequisitionDto;
import io.electrica.sdk.java8.api.MessageListener;
import io.electrica.sdk.java8.api.exception.IntegrationException;
import io.electrica.sdk.java8.api.http.Message;
import io.electrica.sdk.java8.hackerrank.v3.tests.v1.HackerRankV3Candidates;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class CandidateExportMessageListener implements MessageListener {

    private static final String MESSAGE_TAG_KEY = "tag";
    private static final String MESSAGE_TAG = "BrassRingCandidateExport";
    static final Predicate<Message> MESSAGE_FILTER = message -> {
        String tag = message.getPropertiesMap().get(MESSAGE_TAG_KEY);
        return Objects.equals(MESSAGE_TAG, tag);
    };

    private final HackerRankV3Candidates candidates;
    private final XmlMapper mapper;

    CandidateExportMessageListener(HackerRankV3Candidates candidates) {
        this.candidates = candidates;
        mapper = new XmlMapper();
    }

    private static Status createStatus(String code, String shortDescription, String longDescription) {
        Status status = new Status();
        status.setCode(code);
        status.setShortDescription(shortDescription);
        status.setLongDescription(longDescription);
        return status;
    }

    private static Envelope.TransactInfo createResponseTransactInfo(Envelope request, Status status) {
        Envelope.TransactInfo result = new Envelope.TransactInfo();
        result.setTransactType("response");
        result.setTransactId(request.getTransactInfo().getTransactId()); //TODO should be the same???
        result.setTimeStamp(request.getTransactInfo().getTimeStamp()); //TODO should be the same???
        result.setStatus(status);
        return result;
    }

    private static Packet createResponsePacket(Status status, Packet requestPacket) {
        Packet.PacketInfo packetInfo = new Packet.PacketInfo();
        packetInfo.setPacketType("response");
        packetInfo.setPacketId(requestPacket.getPacketInfo().getPacketId());
        packetInfo.setAction(requestPacket.getPacketInfo().getAction());
        packetInfo.setManifest(requestPacket.getPacketInfo().getManifest());
        packetInfo.setStatus(status);

        Packet result = new Packet();
        result.setPacketInfo(packetInfo);
        return result;
    }

    private static Envelope createResponseEnvelope(Envelope request, Status status) {
        Envelope result = new Envelope();
        result.setVersion(request.getVersion());
        result.setSender(request.getSender());
        result.setPackets(request.getPackets().stream()
                .map(requestPacket -> createResponsePacket(status, requestPacket))
                .collect(Collectors.toList())
        );
        result.setRecipient(request.getRecipient());
        result.setTransactInfo(createResponseTransactInfo(request, status));
        return result;
    }

    @Nullable
    @Override
    public String onMessage(Message message) {
        Envelope request;
        try {
            request = mapper.readValue(message.getPayload(), Envelope.class);
        } catch (Exception e) {
            log.error("Envelope deserialization error", e);
            return e.getMessage();
        }

        Status status;
        try {
            CandidateDto candidate = request.getPackets().stream()
                    .filter(packet -> packet.getPayload().contains(CandidateDto.ROOT_TAG))
                    .findFirst()
                    .map(this::parseCandidate)
                    .orElseThrow(() -> new IllegalArgumentException("Candidate payload not found"));

            RequisitionDto requisition = request.getPackets().stream()
                    .filter(packet -> packet.getPayload().contains(RequisitionDto.ROOT_TAG))
                    .findFirst()
                    .map(this::parseRequisition)
                    .orElse(null);

            exportCandidate(candidate, requisition);
            status = createStatus(
                    "200",
                    "Candidate data was exported successfully",
                    "Candidate data was exported successfully"
            );
        } catch (IntegrationException e) {
            log.error("Integration error occur: " + e, e);
            status = createStatus(
                    "405",
                    "Background Request Submission Not Successful",
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error("Generic error occur", e);
            status = createStatus(
                    "405",
                    "Generic Error Occur",
                    e.getMessage()
            );
        }

        try {
            Envelope response = createResponseEnvelope(request, status);
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Envelope serialization error", e);
            return e.getMessage();
        }
    }

    private void exportCandidate(
            CandidateDto candidate,
            @Nullable RequisitionDto requisition
    ) throws IntegrationException {
        // TODO implement me
    }

    @SneakyThrows
    private RequisitionDto parseRequisition(Packet packet) {
        return mapper.readValue(packet.getPayload(), RequisitionDto.class);
    }

    @SneakyThrows
    private CandidateDto parseCandidate(Packet packet) {
        return mapper.readValue(packet.getPayload(), CandidateDto.class);
    }

}
