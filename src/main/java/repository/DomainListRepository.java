package repository;

import entity.DomainList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainListRepository extends JpaRepository<DomainList, Long> {
    List<DomainList> findAllByOrderByCreatedAtDesc();
}