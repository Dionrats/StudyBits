package nl.quintor.studybits.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UniversityIssuer {
    private String universityName;
    private String universityDid;
    private List<String> schemaIds;
}