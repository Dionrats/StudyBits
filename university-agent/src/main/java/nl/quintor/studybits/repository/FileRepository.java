package nl.quintor.studybits.repository;

import nl.quintor.studybits.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<Document, Long> {
    List<Document> getDocumentsByStudent_Id(long studentId);
}
