package nl.quintor.studybits.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {
    @Id
    @GeneratedValue
    private long id;
    @Column
    private String name;
    @Column
    private String type;
    @Column
    private byte[] data;
    @Column
    private String nonce;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Override
    public String toString() {
        return "Document(id:" + id +
                ", name:" + name +
                ", type:" + type +
                ", dataSize:" + data.length +
                ", owner:" + student.getId();
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name + "." + type);
        jsonObject.put("type", type);

        JSONObject owner = new JSONObject();
        owner.put("firstName", student.getFirstName());
        owner.put("lastName", student.getLastName());
        owner.put("id", student.getStudentId());

        jsonObject.put("owner", owner);
        jsonObject.put("targetDid", student.getStudentDid());
        jsonObject.put("dataSize", (data.length / 1024) + "Kb");

        return jsonObject;
    }

}