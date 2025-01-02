package com.github.torleifg.semanticsearch.gateway.oai_pmh;

import com.github.torleifg.semanticsearch.book.service.MetadataDTO;
import info.lc.xmlns.marcxchange_v1.DataFieldType;
import info.lc.xmlns.marcxchange_v1.RecordType;
import info.lc.xmlns.marcxchange_v1.SubfieldatafieldType;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

class OaiPmhDefaultMapper implements OaiPmhMapper {

    @Override
    public MetadataDTO from(String id) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(true);

        return metadata;
    }

    @Override
    public MetadataDTO from(String id, RecordType record) {
        final MetadataDTO metadata = new MetadataDTO();
        metadata.setExternalId(id);
        metadata.setDeleted(false);

        final Map<String, List<DataFieldType>> dataFieldsByTag = record.getDatafield().stream()
                .collect(groupingBy(DataFieldType::getTag, LinkedHashMap::new, toList()));

        dataFieldsByTag.getOrDefault("020", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "a"))
                .findFirst()
                .ifPresent(metadata::setIsbn);

        final Optional<String> title = dataFieldsByTag.getOrDefault("245", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "a"))
                .findFirst();

        final Optional<String> remainderOfTitle = dataFieldsByTag.getOrDefault("245", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "b"))
                .findFirst();

        if (title.isPresent() && remainderOfTitle.isPresent()) {
            metadata.setTitle(title.get() + " : " + remainderOfTitle.get());
        } else title.ifPresent(metadata::setTitle);

        dataFieldsByTag.getOrDefault("264", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "b"))
                .findFirst()
                .ifPresent(metadata::setPublisher);

        final List<DataFieldType> dataFields = Stream.concat(
                        dataFieldsByTag.getOrDefault("100", List.of()).stream(),
                        dataFieldsByTag.getOrDefault("700", List.of()).stream())
                .filter(dataField -> dataField.getSubfield().stream().anyMatch(subfield -> subfield.getCode().equals("4")))
                .toList();

        for (final DataFieldType dataField : dataFields) {
            final String name = getSubfieldValue(dataField.getSubfield(), "a")
                    .findFirst()
                    .orElse(null);

            final List<MetadataDTO.Contributor.Role> roles = getSubfieldValue(dataField.getSubfield(), "4")
                    .map(String::toUpperCase)
                    .map(MetadataDTO.Contributor.Role::valueOf)
                    .toList();

            metadata.getContributors().add(new MetadataDTO.Contributor(roles, name));
        }

        dataFieldsByTag.getOrDefault("264", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "c"))
                .findFirst()
                .ifPresent(metadata::setPublishedYear);

        dataFieldsByTag.getOrDefault("520", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "a"))
                .findFirst()
                .ifPresent(metadata::setDescription);

        final Map<String, List<MetadataDTO.Classification>> aboutById = dataFieldsByTag.getOrDefault("650", List.of()).stream()
                .map(OaiPmhDefaultMapper::getClassification)
                .collect(groupingBy(MetadataDTO.Classification::id, LinkedHashMap::new, toList()));

        aboutById.entrySet().stream()
                .map(entry -> new MetadataDTO.Classification(entry.getKey(), getNames(entry.getValue())))
                .forEach(metadata.getAbout()::add);

        final Map<String, List<MetadataDTO.Classification>> genreAndFormById = dataFieldsByTag.getOrDefault("655", List.of()).stream()
                .map(OaiPmhDefaultMapper::getClassification)
                .collect(groupingBy(MetadataDTO.Classification::id, LinkedHashMap::new, toList()));

        genreAndFormById.entrySet().stream()
                .map(entry -> new MetadataDTO.Classification(entry.getKey(), getNames(entry.getValue())))
                .forEach(metadata.getGenreAndForm()::add);

        dataFieldsByTag.getOrDefault("856", List.of()).stream()
                .flatMap(dataField -> getSubfieldValue(dataField.getSubfield(), "u"))
                .map(URI::create)
                .forEach(metadata::setThumbnailUrl);

        return metadata;
    }

    private static Stream<String> getSubfieldValue(List<SubfieldatafieldType> subfields, String subfieldCode) {
        return subfields.stream()
                .filter(subfield -> subfield.getCode().equals(subfieldCode))
                .map(SubfieldatafieldType::getValue);
    }

    private static MetadataDTO.Classification getClassification(DataFieldType dataField) {
        final String id = getSubfieldValue(dataField.getSubfield(), "0")
                .findFirst()
                .orElse(null);

        final String language = getSubfieldValue(dataField.getSubfield(), "9")
                .findFirst()
                .orElse(null);

        final String text = getSubfieldValue(dataField.getSubfield(), "a")
                .findFirst()
                .orElse(null);

        return new MetadataDTO.Classification(id, List.of(new MetadataDTO.LocalizedString(language, text)));
    }

    private static List<MetadataDTO.LocalizedString> getNames(List<MetadataDTO.Classification> classifications) {
        return classifications.stream()
                .map(MetadataDTO.Classification::names)
                .flatMap(List::stream)
                .toList();
    }
}
