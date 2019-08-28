#!/usr/bin/env bash
shopt -s expand_aliases
alias seeder='java -jar ./target/seeder-1.0-SNAPSHOT-jar-with-dependencies.jar'

GRONINGEN_NAME="RijksuniversiteitGroningen"
GRONINGEN_SEED="000000RijksuniversiteitGroningen"
GRONINGEN_DOMAIN=http://$TEST_POOL_IP:8080
echo GroningenDomain: $GRONINGEN_DOMAIN
GENT_NAME="UniversiteitGent"
GENT_SEED="0000000000000000UniversiteitGent"
# Create steward
seeder did 000000000000000000000000Steward1 steward
GRONINGEN_DID=$(seeder did $GRONINGEN_SEED $GRONINGEN_NAME)
GENT_DID=$(seeder did $GENT_SEED $GENT_NAME)

seeder onboard $GRONINGEN_SEED $GRONINGEN_NAME $GRONINGEN_DID
seeder onboard $GENT_SEED $GENT_NAME $GENT_DID

SCHEMA_ID_TRANSCRIPT=$(seeder schema-transcript)
SCHEMA_ID_DOCUMENT=$(seeder schema-document)


seeder student $GRONINGEN_DOMAIN 12345678
CRED_DEF_ID_TRANSCRIPT=$(seeder cred-def $GRONINGEN_DOMAIN $SCHEMA_ID_TRANSCRIPT "TRANSCRIPT")
echo "CRED DEF ID $CRED_DEF_ID_TRANSCRIPT"

CRED_DEF_ID_DOCUMENT=$(seeder cred-def $GRONINGEN_DOMAIN $SCHEMA_ID_DOCUMENT "DOCUMENT")
echo "CRED DEF ID $CRED_DEF_ID_DOCUMENT"

seeder exchange-position $GRONINGEN_DOMAIN $CRED_DEF_ID_TRANSCRIPT

touch /finished.txt

sleep 30000

