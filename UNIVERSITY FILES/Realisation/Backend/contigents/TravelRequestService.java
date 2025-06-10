package com.swisscom.travelmate.engine.modules.travelrequest.service;

import com.swisscom.travelmate.engine.modules.travelrequest.dto.TravelRequestStatusDto;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import com.swisscom.travelmate.engine.modules.travelrequest.repository.TravelRequestRepository;
import com.swisscom.travelmate.engine.shared.enrichers.facades.PeopleGroupsAgileMembershipsEnrichmentFacade;
import com.swisscom.travelmate.engine.modules.user.model.Deputy;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileRole;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.service.AnyOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelRequestService {
    @Autowired
    private TravelRequestRepository travelRequestRepository;


    public Optional<TravelRequest> getTravelRequestById(String travelRequestId){
        return travelRequestRepository.findById(travelRequestId);
    }

    public List<TravelRequest> getApprovedTravelRequestsBetweenDates(LocalDate from, LocalDate until){
        return travelRequestRepository.findByStatusAndReturnAndDepartureDateBetween(TravelRequestStatus.APPROVED, from, until);
    }
}
