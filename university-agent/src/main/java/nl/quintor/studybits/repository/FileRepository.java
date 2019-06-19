package nl.quintor.studybits.repository;

import nl.quintor.studybits.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<Document, Long> {

}
