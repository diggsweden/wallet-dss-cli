// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: CC0-1.0

package se.digg.wallet.dss.cli;

import eu.europa.esig.dss.model.DSSDocument;

/** CLI Entry point for wallet-dss-cli. */
public class WalletDssCli {

  /**
   * Main entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try {
      int exitCode = run(args);
      if (exitCode != 0) {
        System.exit(exitCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(2);
    }
  }

  /**
   * Run the CLI logic without calling System.exit (useful for testing).
   *
   * @param args command line arguments
   * @return exit code
   * @throws Exception if an error occurs
   */
  public static int run(String[] args) throws Exception {
    if (args.length < 4 || !args[0].equals("sign")) {
      System.err.println(
          "Usage: java -jar wallet-dss-cli.jar sign <path-to-file-to-be-signed> <path-to-signing-key> <path-to-certificate-chain>");
      return 1;
    }

    String fileToSign = args[1];
    String signingKey = args[2];
    String certificateChain = args[3];

    DssSigner signer = new DssSigner();
    DSSDocument signedDocument = signer.sign(fileToSign, signingKey, certificateChain);

    String outFile = fileToSign;
    if (outFile.endsWith(".xml")) {
      outFile = outFile.substring(0, outFile.length() - 4) + "-signed.xml";
    } else {
      outFile += "-signed.xml";
    }

    signedDocument.save(outFile);
    System.out.println("Successfully created signed document at: " + outFile);
    return 0;
  }
}
