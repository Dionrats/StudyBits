package nl.quintor.studybits.config;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.indy.wrapper.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.indy.wrapper.util.PoolUtils;
import nl.quintor.studybits.messages.StudyBitsMessageTypes;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class IndyConfiguration {
    @Value("${nl.quintor.studybits.university.pool}")
    private String testPoolIp;
    @Value("${nl.quintor.studybits.university.name}")
    private String universityName;

    private boolean shouldReload = true;


    @Bean
    public TrustAnchor universityTrustAnchor(IndyWallet universityWallet) {
        return new TrustAnchor(universityWallet);
    }

    @Bean
    public Verifier universityVerifier(IndyWallet universityWallet) {
        return new Verifier(universityWallet);
    }

    @Bean
    public Issuer universityIssuer(IndyWallet universityWallet) {
        return new Issuer(universityWallet);
    }

    @Bean
    public MessageEnvelopeCodec universityCodec(IndyWallet universityWallet) {
        return new MessageEnvelopeCodec(universityWallet);
    }

    @Bean
    public Prover universityProver(IndyWallet universityWallet) {
        return new Prover(universityWallet, universityName.replace(" ", ""));
    }

    @Bean
    @Primary
    public IndyWallet universityWallet() throws IndyException, ExecutionException, InterruptedException, IOException {
        Pool.setProtocolVersion(PoolUtils.PROTOCOL_VERSION).get();
        StudyBitsMessageTypes.init();
        IndyMessageTypes.init();

        log.debug("Initializing wallet using name {}", universityName);

        String name = universityName.replace(" ", "");
        String seed = StringUtils.leftPad(name, 32, '0');
        String poolName = "default_pool";

        if(shouldReload){
            poolName = PoolUtils.createPoolLedgerConfig(testPoolIp);
            return IndyWallet.create(new IndyPool(poolName), name, seed);
        }
        return IndyWallet.open(new IndyPool(poolName), name, seed, "SYqJSzcfsJMhSt7qjcQ8CC");
    }
}

