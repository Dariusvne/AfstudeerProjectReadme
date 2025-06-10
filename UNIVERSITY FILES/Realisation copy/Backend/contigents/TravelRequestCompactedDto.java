package com.swisscom.travelmate.engine.shared.custom;

import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.user.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter @Setter
public class TravelRequestCompactedDto {
    String id;
    TravelRequestCompactedUserDto user;
    List<TravelDetailsCompactedDto> travelDetails;
    LocalDate departureDate;
    LocalDate returnDate;
    LocalDate officeArrivalDate;
    String country;
    Map<String, Object> metaData;


    public TravelRequestCompactedDto(String id, TravelRequestCompactedUserDto user, List<TravelDetailsCompactedDto> travelDetails, LocalDate departureDate, LocalDate returnDate, LocalDate officeArrivalDate, String country, Map<String, Object> metaData) {
        this.id = id;
        this.user = user;
        this.travelDetails = travelDetails;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.officeArrivalDate = officeArrivalDate;
        this.country = country;
        this.metaData = metaData;
    }

    public static TravelRequestCompactedDto fromTravelRequest(TravelRequest travelRequest, Optional<User> user){
        List<TravelDetailsCompactedDto> travelDetails = travelRequest.getTravelDetails().stream().map(TravelDetailsCompactedDto::fromTravelDetails).toList();

        return new TravelRequestCompactedDto(
            travelRequest.getId(),
            user.map(TravelRequestCompactedUserDto::fromUser).orElse(null),
            travelDetails,
            travelRequest.getDepartureDate(),
            travelRequest.getReturnDate(),
            travelRequest.getOfficeArrivalDate(),
            travelRequest.getCountry().name(),
            travelRequest.getMetaData()
        );
    }
}
