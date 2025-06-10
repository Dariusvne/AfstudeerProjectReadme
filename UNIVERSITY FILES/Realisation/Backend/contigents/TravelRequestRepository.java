package com.swisscom.travelmate.engine.modules.travelrequest.repository;


import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequest;
import com.swisscom.travelmate.engine.modules.travelrequest.model.TravelRequestStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository("travelRequestRepository")
public interface TravelRequestRepository extends MongoRepository<TravelRequest, String> {
    public List<TravelRequest> findByUserId(String userId);

    @Query("{ 'status': ?0, 'userId': { $in: ?1 }, '$or': [ { 'departureDate': { $gte: ?2, $lte: ?3 } } ] }")
    List<TravelRequest> findByStatusAndUserIdsAndDateRange(TravelRequestStatus status, List<String> userIds, LocalDate from, LocalDate until);
}
