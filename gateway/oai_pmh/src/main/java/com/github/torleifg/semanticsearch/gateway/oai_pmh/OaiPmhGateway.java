package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.repository.LastModifiedRepository;
import com.github.torleifg.semanticsearch.book.repository.ResumptionToken;
import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
import info.lc.xmlns.marcxchange_v1.RecordType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.StatusType;
import org.w3c.dom.Element;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Slf4j
class OaiPmhGateway implements MetadataGateway {
    private final OaiPmhClient oaiPmhClient;
    private final OaiPmhMapper oaiPmhMapper;
    private final OaiPmhProperties oaiPmhProperties;

    private final ResumptionTokenRepository resumptionTokenRepository;
    private final LastModifiedRepository lastModifiedRepository;

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(RecordType.class);
        } catch (JAXBException e) {
            throw new OaiPmhException(e);
        }
    }

    OaiPmhGateway(OaiPmhClient oaiPmhClient, OaiPmhMapper oaiPmhMapper, OaiPmhProperties oaiPmhProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        this.oaiPmhClient = oaiPmhClient;
        this.oaiPmhMapper = oaiPmhMapper;
        this.oaiPmhProperties = oaiPmhProperties;

        this.resumptionTokenRepository = resumptionTokenRepository;
        this.lastModifiedRepository = lastModifiedRepository;
    }

    @Override
    public List<MetadataDTO> find() {
        final String serviceUri = oaiPmhProperties.getServiceUri();
        final String requestUri = createRequestUri(serviceUri);

        final OaiPmhResponse response = OaiPmhResponse.from(oaiPmhClient.get(requestUri));

        if (response.hasErrors()) {

            if (response.hasBadResumptionTokenError()) {
                resumptionTokenRepository.delete(serviceUri);
            }

            if (response.hasNoRecordsMatchError()) {
                log.info("Received 0 records from {}", requestUri);

                return List.of();
            }

            throw new OaiPmhException(response.errorsToString());
        }

        final Optional<String> resumptionToken = response.getResumptionToken();

        if (resumptionToken.isPresent()) {
            resumptionTokenRepository.save(serviceUri, resumptionToken.get());
        } else {
            resumptionTokenRepository.get(serviceUri)
                    .filter(token -> token.isNotExpired(oaiPmhProperties.getTtl()))
                    .ifPresent(token -> resumptionTokenRepository.save(serviceUri, token.value()));
        }

        if (!response.hasRecords()) {
            log.info("Received 0 record(s) from {}", requestUri);

            return List.of();
        }

        final var oaiPmhrecords = response.getRecords();

        log.info("Received {} record(s) from {}", oaiPmhrecords.size(), requestUri);

        final Unmarshaller unmarshaller;
        try {
            unmarshaller = JAXB_CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new OaiPmhException(e);
        }

        final List<MetadataDTO> metadata = new ArrayList<>();

        for (final var oaiPmhRecord : oaiPmhrecords) {
            final String identifier = oaiPmhRecord.getHeader().getIdentifier();

            if (oaiPmhRecord.getHeader().getStatus() == StatusType.DELETED) {
                metadata.add(oaiPmhMapper.from(identifier));

                continue;
            }

            if (!(oaiPmhRecord.getMetadata().getAny() instanceof Element element)) {
                continue;
            }

            final RecordType marcRecord;
            try {
                marcRecord = unmarshaller.unmarshal(element, RecordType.class).getValue();
            } catch (JAXBException e) {
                throw new OaiPmhException(e);
            }

            metadata.add(oaiPmhMapper.from(identifier, marcRecord));
        }

        Optional.of(oaiPmhrecords.getLast())
                .map(org.openarchives.oai._2.RecordType::getHeader)
                .map(HeaderType::getDatestamp)
                .map(Instant::parse)
                .ifPresent(lastModified -> lastModifiedRepository.save(serviceUri, lastModified.plusSeconds(1L)));

        return metadata;
    }

    private String createRequestUri(String serviceUri) {
        final Optional<ResumptionToken> resumptionToken = resumptionTokenRepository.get(serviceUri);

        if (resumptionToken.isPresent() && resumptionToken.get().isNotExpired(oaiPmhProperties.getTtl())) {
            return serviceUri + "?verb=ListRecords&resumptionToken=" + resumptionToken.get().value();
        }

        return lastModifiedRepository.get(serviceUri)
                .map(lastModified -> serviceUri + "?verb=ListRecords&metadataPrefix=marc21&from=" + ISO_INSTANT.format(lastModified))
                .orElse(serviceUri + "?verb=ListRecords&metadataPrefix=marc21");
    }
}
