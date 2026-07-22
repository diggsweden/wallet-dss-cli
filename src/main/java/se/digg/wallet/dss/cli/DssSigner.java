// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: CC0-1.0

package se.digg.wallet.dss.cli;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

/** Service to handle signing documents with XAdES. */
public class DssSigner {

  /**
   * Signs an XML document with the given private key and certificate chain.
   *
   * @param fileToSign path to the XML file to be signed
   * @param signingKey path to the PEM private key
   * @param certificateChain path to the PEM certificate chain
   * @return the signed DSSDocument
   * @throws Exception if signing fails
   */
  public DSSDocument sign(String fileToSign, String signingKey, String certificateChain)
      throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    // 1. Read the private key
    PrivateKey privateKey;
    try (PEMParser pemParser = new PEMParser(new FileReader(signingKey, StandardCharsets.UTF_8))) {
      Object object = pemParser.readObject();
      JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
      if (object instanceof PEMKeyPair) {
        KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
        privateKey = kp.getPrivate();
      } else {
        privateKey = converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
      }
    }

    // 2. Read the certificate chain
    List<Certificate> certList = new ArrayList<>();
    try (PEMParser pemParser =
        new PEMParser(new FileReader(certificateChain, StandardCharsets.UTF_8))) {
      JcaX509CertificateConverter converter = new JcaX509CertificateConverter().setProvider("BC");
      Object object;
      while ((object = pemParser.readObject()) != null) {
        if (object instanceof X509CertificateHolder) {
          X509Certificate cert = converter.getCertificate((X509CertificateHolder) object);
          certList.add(cert);
        }
      }
    }

    if (certList.isEmpty()) {
      throw new IllegalArgumentException("No certificates found in " + certificateChain);
    }

    // 3. Create an in-memory PKCS12 KeyStore
    KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
    ks.load(null, null);
    ks.setKeyEntry(
        "alias", privateKey, "password".toCharArray(), certList.toArray(new Certificate[0]));

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ks.store(baos, "password".toCharArray());

    // 4. Create the signature token
    try (Pkcs12SignatureToken signingToken =
        new Pkcs12SignatureToken(
            new ByteArrayInputStream(baos.toByteArray()),
            new KeyStore.PasswordProtection("password".toCharArray()))) {

      DSSPrivateKeyEntry dssPrivateKey = signingToken.getKeys().get(0);

      // 5. Prepare DSS parameters
      DSSDocument toSignDocument = new FileDocument(fileToSign);

      XAdESSignatureParameters parameters = new XAdESSignatureParameters();
      parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
      parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
      parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

      parameters.setSigningCertificate(dssPrivateKey.getCertificate());
      parameters.setCertificateChain(dssPrivateKey.getCertificateChain());

      CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
      XAdESService xadesService = new XAdESService(commonCertificateVerifier);

      ToBeSigned dataToSign = xadesService.getDataToSign(toSignDocument, parameters);

      DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();
      SignatureValue signatureValue = signingToken.sign(dataToSign, digestAlgorithm, dssPrivateKey);

      return xadesService.signDocument(toSignDocument, parameters, signatureValue);
    }
  }
}
