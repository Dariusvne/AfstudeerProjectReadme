package com.swisscom.travelmate.engine.shared.custom;

import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelDetails;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class TravelDetailsCompactedDto {
    LocalDate date;
    String officeLocation;

    public TravelDetailsCompactedDto(LocalDate date, String officeLocation) {
        this.date = date;
        this.officeLocation = officeLocation;
    }

    public static TravelDetailsCompactedDto fromTravelDetails(TravelDetails travelDetails){
        return new TravelDetailsCompactedDto(
            travelDetails.getDate(),
            travelDetails.getOfficeLocation()
        );
    }
}
