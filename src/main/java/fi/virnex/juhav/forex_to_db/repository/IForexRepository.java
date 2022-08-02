package fi.virnex.juhav.forex_to_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.virnex.juhav.forex_to_db.model.ForexRate;

@Repository
public interface IForexRepository extends JpaRepository<ForexRate,Long> {
}
