package at.piwa.designa.alpr.configuration;

import at.piwa.designa.alpr.model.DesignaLpr;
import at.piwa.designa.alpr.model.csv.DesignaLprCsvDTO;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.map.repository.config.EnableMapRepositories;

import java.io.File;
import java.nio.file.Path;

@Configuration
@EnableAutoConfiguration
@EnableMapRepositories
public class MainConfiguration {

    @Bean
    public ModelMapper modelMapper() {



        ModelMapper modelMapper = new ModelMapper();


        TypeMap<DesignaLprCsvDTO, DesignaLpr> typeMap = modelMapper.createTypeMap(DesignaLprCsvDTO.class, DesignaLpr.class);

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS");
        Converter<String, DateTime> stringToDateTimeConverter = ctx -> ctx.getSource() == null ? null : DateTime.parse(ctx.getSource(), dateTimeFormatter);
        typeMap.addMappings(mapper -> mapper.using(stringToDateTimeConverter).map(DesignaLprCsvDTO::getDatedevice, DesignaLpr::setTimestamp));

        typeMap.addMapping(DesignaLprCsvDTO::getLicensePlate, DesignaLpr::setLicensePlateImagePath);

        Converter<String, String> fileNameConverter = ctx -> ctx.getSource() == null ? null : FilenameUtils.getName(ctx.getSource());
        typeMap.addMappings(mapper -> mapper.using(fileNameConverter).map(DesignaLprCsvDTO::getLicensePlate, DesignaLpr::setFilename));

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper;
    }

}
