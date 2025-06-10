package com.swisscom.travelmate.engine.shared.custom.contigents.api;

import com.swisscom.travelmate.engine.modules.rule.service.TravelRuleService;
import com.swisscom.travelmate.engine.modules.user.model.UserGroup;
import com.swisscom.travelmate.engine.modules.user.service.UserService;
import com.swisscom.travelmate.engine.shared.custom.TravelRequestCompactedDto;
import com.swisscom.travelmate.engine.shared.custom.contigents.service.CustomContingentsService;
import com.swisscom.travelmate.engine.shared.role.RoleAuthorize;
import com.swisscom.travelmate.engine.shared.role.models.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/v1/custom/contingents")
public class CustomContingentsController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CustomContingentsService customContigentsService;

    @Autowired
    private UserService userService;

    @GetMapping("/travelrequests")
    public ResponseEntity<List<TravelRequestCompactedDto>> getTravelRequestFromUserGroup(
            @RequestParam("userGroupId") String userGroupId,
            @RequestParam("from") LocalDate from,
            @RequestParam("until") LocalDate until){
        try {
            Optional<UserGroup> userGroup = userService.getUserGroupById(userGroupId);
            if (userGroup.isEmpty()){
                logger.error("UserGroup does not exist with id: ", userGroupId);
                return ResponseEntity.badRequest().build();
            }

            List<TravelRequestCompactedDto> travelRequestDtos = customContigentsService.getApprovedTravelRequestsByUserGroup(userGroup.get(), from, until);
            return ResponseEntity.ok(travelRequestDtos);
        } catch (Exception e) {
            logger.error("Could not get traveldata: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
