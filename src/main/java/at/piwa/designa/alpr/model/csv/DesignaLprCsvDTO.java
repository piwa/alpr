package at.piwa.designa.alpr.model.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class DesignaLprCsvDTO {

    @CsvBindByPosition(position = 0)
    private String laneid;
    @CsvBindByPosition(position = 1)
    private String datedevice;
    @CsvBindByPosition(position = 2)
    private String Jurisdiction;
    @CsvBindByPosition(position = 3)
    private String JurisdictionConfidence;
    @CsvBindByPosition(position = 4)
    private String identifier;
    @CsvBindByPosition(position = 5)
    private String Confidence;
    @CsvBindByPosition(position = 6)
    private String LicensePlate;
    @CsvBindByPosition(position = 7)
    private String LicensePlate2;

}
