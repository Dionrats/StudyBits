package nl.quintor.studybits.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.utils.CredentialDefinitionType;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.concurrent.ExecutionException;

@Component
public class CredentialDefinitionService {

    //TODO enummap. Kan problemen veroorzaken, maybe?
    private EnumMap<CredentialDefinitionType, String> credentialDefinitionIds;

    private final Issuer universityIssuer;

    @Autowired
    public CredentialDefinitionService(Issuer universityIssuer) {
        this.universityIssuer = universityIssuer;
        credentialDefinitionIds = new EnumMap<>(CredentialDefinitionType.class);
    }

    public String createCredentialDefintion(String schemaId, CredentialDefinitionType type) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        if(credentialDefinitionIds.containsKey(type))
            return credentialDefinitionIds.get(type);

        String credDefId = universityIssuer.defineCredential(schemaId).get();
        credentialDefinitionIds.put(type, credDefId);
        return credDefId;
    }

    public String getCredentialDefinitionId(CredentialDefinitionType type) {
        return credentialDefinitionIds.get(type);
    }

}
