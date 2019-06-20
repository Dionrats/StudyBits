package nl.quintor.studybits.repository;

import nl.quintor.studybits.entity.CredentialOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CredentialOfferRepository extends JpaRepository<CredentialOffer, Long> {
}
