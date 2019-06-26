package nl.quintor.studybits.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.entity.DocumentDTO;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthcryptableDocuments implements Serializable {
    private List<DocumentDTO> documents;
}
