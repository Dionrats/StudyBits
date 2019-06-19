package nl.quintor.studybits.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDTO {
    private String name;
    private String type;
    private Student student;
    private MultipartFile file;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("type", type);

        JSONObject owner = new JSONObject();
        owner.put("name", student.getFirstName());
        owner.put("lastName", student.getLastName());
        owner.put("studentId", student.getStudentId());

        json.put("student", owner);

        return json;
    }
}
