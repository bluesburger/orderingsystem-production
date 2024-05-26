package br.com.bluesburguer.production.infra.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.bluesburguer.production.infra.database.entity.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {

}
