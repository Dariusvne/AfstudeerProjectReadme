package com.swisscom.travelmate.engine.shared.custom;

import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TravelRequestCompactedUserDto {
    String firstName;
    String lastName;
    String employeeNr;
    String departmentName;
    String country;

    public TravelRequestCompactedUserDto(String firstName, String lastName, String employeeNr, String departmentName, String country) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.employeeNr = employeeNr;
        this.departmentName = departmentName;
        this.country = country;
    }

    public static TravelRequestCompactedUserDto fromAgileMembership(AgileMembershipsDto agileMembership, String country){
        return new TravelRequestCompactedUserDto(
            agileMembership.firstName,
            agileMembership.lastName,
            agileMembership.employeeNr,
            agileMembership.unitName,
            country
        );
    }


    public static TravelRequestCompactedUserDto fromUser(User user){
        return new TravelRequestCompactedUserDto(
            user.getFirstName(),
            user.getLastName(),
            user.getEmployeeNr(),
            user.getDepartmentName(),
            user.getHomeCountry().name()
        );
    }

}
