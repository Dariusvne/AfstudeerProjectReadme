package com.swisscom.travelmate.engine.shared.external.workforceidentity.service;

import com.swisscom.travelmate.engine.modules.message.model.Message;
import com.swisscom.travelmate.engine.modules.user.model.User;
import com.swisscom.travelmate.engine.shared.external.AuthStyle;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.AgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.anyorg.model.PeopleGroupsAgileMembershipsDto;
import com.swisscom.travelmate.engine.shared.external.authorization.AuthorizationTokenClientConfig;
import com.swisscom.travelmate.engine.shared.external.authorization.oauth2.OAuth2JwtService;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.client.WFIClient;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserDto;
import com.swisscom.travelmate.engine.shared.external.workforceidentity.model.WFIUserResponseDto;
import com.swisscom.travelmate.engine.shared.monitoring.Observer;
import com.swisscom.travelmate.engine.shared.monitoring.PrometheusObserver;
import com.swisscom.travelmate.engine.shared.util.ObserverUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WFIService {
    @Value("${wfi.client.id}")
    private String wfiClientId;

    @Value("${wfi.client.secret}")
    private String wfiClientSecret;

    @Autowired
    OAuth2JwtService secureM2MJwtService;

    @Autowired
    WFIClient wfiClient;

    private static final Observer<WFIUserDto> prometheusObserver =
            new PrometheusObserver<>(
                    "wfi_service_requests_total",
                    "Total number of successful or failed single-user lookups via the WFI service."
            );

    private List<Observer<WFIUserDto>> observers = new ArrayList<>();

    public WFIService(){
        observers.add(prometheusObserver);
    }

    @Cacheable(value = "fetchWfiUserByEmpNr", key = "#employeeNumber.concat('employeeNumber')")
    public Optional<WFIUserDto> getUserByEmployeeNumber(String employeeNumber){
        String query = String.format("personalnumber eq \"%s\"", employeeNumber);

        try {
            WFIUserDto user = wfiClient.queryUsers(query,
                    secureM2MJwtService.getTokenWithBearerPrefix(
                            new AuthorizationTokenClientConfig(wfiClientId, wfiClientSecret, AuthStyle.Basic)),
                    1).getBody().getResult().getFirst();

            ObserverUtils.notifyObservers(user != null, user, "WFI", observers);
            return user != null ? Optional.of(user) : Optional.empty();
        } catch (Exception e) {
            ObserverUtils.notifyObservers(false, null, "WFI", observers);
            throw e;
        }
    }

    @Cacheable(value = "fetchWfiUserByEmpNrs", key = "#employeeNumbers.toString()")
    public WFIUserResponseDto getUsersByEmployeeNumbers(List<String> employeeNumbers){

        String query = employeeNumbers.stream()
                .map(number -> String.format("personalnumber eq \"%s\"", number))
                .collect(Collectors.joining(" or "));
        return wfiClient.queryUsers(query, secureM2MJwtService.getTokenWithBearerPrefix(
                new AuthorizationTokenClientConfig(wfiClientId, wfiClientSecret, AuthStyle.Basic)
        ), 1).getBody();
    }

    @Cacheable(value = "fetchWfiUserByEmails", key = "#email.toString()")
    public Optional<WFIUserDto> getUserByEmail(String email){
        try {
            String query = String.format("mailInternal eq \"%s\"", email);
            return Optional.of(
                Objects.requireNonNull(
                    wfiClient.queryUsers(
                        query,
                        secureM2MJwtService.getTokenWithBearerPrefix(
                            new AuthorizationTokenClientConfig(
                                wfiClientId,
                                wfiClientSecret,
                                AuthStyle.Basic
                            )
                        ),
                    1).getBody()).getResult().getFirst()
            );
        }catch(NoSuchElementException e){
            //Should not throw error when nothing is found, but instead should return an empty object
            return Optional.empty();
        }
    }

    public Optional<WFIUserDto> getUserByFirstAndLastName(String firstName, String lastName){
        try {
            String query = String.format("(firstName eq \"%s\" and lastName eq \"%s\")", firstName, lastName);
            return Optional.of(
                Objects.requireNonNull(
                    wfiClient.queryUsers(
                        query,
                        secureM2MJwtService.getTokenWithBearerPrefix(
                            new AuthorizationTokenClientConfig(
                                wfiClientId,
                                wfiClientSecret,
                                AuthStyle.Basic
                            )
                        ),
                1).getBody()).getResult().getFirst()
            );
        }catch(NoSuchElementException e){
            //Should not throw error when nothing is found, but instead should return an empty object
            return Optional.empty();
        }
    }

    //Migration only, not useful outside of it
    public Optional<WFIUserDto> getUserByOnlyOneName(String fullName){
        try{
            String firstPartOfName = Arrays.stream(fullName.split(" ")).findFirst().get();
            String lastPartOfName = Arrays.stream(fullName.split(" ")).reduce((first, second) -> second).get();
            String query = String.format("(displayName co \"%s\" and displayName co \"%s\")", firstPartOfName, lastPartOfName);
            return Optional.of(
                Objects.requireNonNull(wfiClient.queryUsers(
                    query,
                    secureM2MJwtService.getTokenWithBearerPrefix(
                        new AuthorizationTokenClientConfig(
                            wfiClientId,
                            wfiClientSecret,
                            AuthStyle.Basic
                        )
                    ),
                1).getBody()).getResult().getFirst()
            );
        }catch(NoSuchElementException e){
            //Should not throw error when nothing is found, but instead should return an empty object
            return Optional.empty();
        }
    }


    public WFIUserResponseDto getUsersByName(List<AgileMembershipsDto> agileMembers){
        String query = agileMembers.stream()
                .map(agileMember -> String.format("(firstName eq \"%s\" and lastName eq \"%s\")", agileMember.firstName, agileMember.lastName))
                .collect(Collectors.joining(" or "));
        return wfiClient.queryUsers(query, secureM2MJwtService.getTokenWithBearerPrefix(
                new AuthorizationTokenClientConfig(wfiClientId, wfiClientSecret, AuthStyle.Basic)
        ), 1).getBody();
    }
}
