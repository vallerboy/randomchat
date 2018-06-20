package pl.oskarpolak.randomchat.models.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.oskarpolak.randomchat.models.BanEntity;

@Repository
public interface BanRepository extends CrudRepository<BanEntity, Integer> {
    boolean existsByIp(String ip);
}
