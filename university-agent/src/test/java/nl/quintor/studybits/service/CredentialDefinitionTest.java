package nl.quintor.studybits.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.messages.CredentialDefinitionType;
import org.hyperledger.indy.sdk.IndyException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
public class CredentialDefinitionTest {

    @TestConfiguration
    static class CredentialDefinitionTestConfiguration {
        @Bean
        public CredentialDefinitionService credentialDefinitionService() {
            return new CredentialDefinitionService();
        }
    }

    @Autowired
    private CredentialDefinitionService sut;

    private String credDefId;

    @MockBean
    Issuer universityIssuer;

    @Before
    public void setUp() {
        credDefId = "testCredDefId";
    }

    @Test
    public void createCredentialDefintionTest() throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(credDefId);
        Mockito.when(universityIssuer.defineCredential(anyString())).thenReturn(future);

        String cred = sut.createCredentialDefintion("ShemaId", CredentialDefinitionType.DOCUMENT);

        assertThat(cred).isEqualTo(credDefId);
    }

    @Test
    public void createCredentialDefintionTwiceTest() throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(credDefId);
        Mockito.when(universityIssuer.defineCredential(anyString())).thenReturn(future);

        String result = sut.createCredentialDefintion("ShemaId", CredentialDefinitionType.DOCUMENT);
        String result2 = sut.createCredentialDefintion("ShemaId", CredentialDefinitionType.DOCUMENT);

        assertThat(result).isEqualTo(result2);
    }

    @Test
    public void getCredentialDefinitionId() throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete(credDefId);
        Mockito.when(universityIssuer.defineCredential(anyString())).thenReturn(future);

        String original = sut.createCredentialDefintion("ShemaId", CredentialDefinitionType.DOCUMENT);

        String result = sut.getCredentialDefinitionId(CredentialDefinitionType.DOCUMENT);

        assertThat(result).isEqualTo(original);
    }
}
