package com.swisscom.travelmate.engine.shared.custom.contigents.service;

import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.service.TravelRequestService;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.custom.TravelRequestCompactedDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.service.AnyOrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CustomContingentsService {

    @Autowired
    private UserService userService;

    @Autowired
    private TravelRequestService travelRequestService;

    public List<TravelRequestCompactedDto> getApprovedTravelRequestsByUserGroup(UserGroup userGroup, LocalDate from, LocalDate until) {

        List<String> userIds = userGroup.getUserIds();
        if(userIds.isEmpty())
            return List.of();

        List<TravelRequest> filteredTravelRequests = travelRequestService.getApprovedTravelRequestsBetweenDatesByUserIds(userIds, from, until);
        return filteredTravelRequests.stream().map(this::parseTravelRequest).toList();

    }

    private TravelRequestCompactedDto parseTravelRequest(TravelRequest travelRequest){
        Optional<User> user = userService.getUserDataRegardlessOfScaffolding(travelRequest.getUserId());
        return TravelRequestCompactedDto.fromTravelRequest(travelRequest, user);
    }
}
