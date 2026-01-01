package com.java.shoes_service.repository.order;

import com.java.shoes_service.entity.order.PurchaseOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends MongoRepository<PurchaseOrderEntity, String> {

    Page<PurchaseOrderEntity> findByUserIdAndStatus(String userId, Boolean status, Pageable pageable);

}

