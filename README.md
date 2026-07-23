# wallet-dss-cli

[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/diggsweden/wallet-dss-cli/badge)](https://securityscorecards.dev/viewer/?uri=github.com/diggsweden/wallet-dss-cli)

This is a repository providing a command line interface for
[the EU Digital Signature Service library](https://github.com/esig/dss).
It can be used to sign XML documents using the XAdES formats
used by EU trusted lists.

## Usage

Build the project (requires Java 25):

```bash
mvn clean install -DskipTests
```

The CLI strictly outputs the signed XML to Standard Out (`stdout`). This allows you to easily pipe or redirect the output directly into a new file.

### Signing an XML Document

```bash
java -jar target/wallet-dss-cli.jar sign <unsigned.xml> <private-key.pem> <certificate.pem> > <signed.xml>
```

**Example:**

```bash
java -jar target/wallet-dss-cli.jar sign src/test/resources/test.xml src/test/resources/test-key.pem src/test/resources/test-cert.pem > test-signed.xml
```
