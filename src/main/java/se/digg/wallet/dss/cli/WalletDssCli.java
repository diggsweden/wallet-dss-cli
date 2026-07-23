// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: CC0-1.0

package se.digg.wallet.dss.cli;

import eu.europa.esig.dss.model.DSSDocument;
import java.io.PrintStream;

/** CLI Entry point for wallet-dss-cli. */
public class WalletDssCli {

  private final PrintStream out;

  /** Default constructor, outputs to System.out. */
  public WalletDssCli() {
    this(System.out);
  }

  /**
   * Constructor with explicit output stream.
   *
   * @param out the PrintStream to write the output to
   */
  public WalletDssCli(PrintStream out) {
    this.out = out;
  }

  /**
   * Main entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try {
      WalletDssCli cli = new WalletDssCli();
      int exitCode = cli.run(args);
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
  public int run(String[] args) throws Exception {
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

    signedDocument.writeTo(out);
    out.flush();
    return 0;
  }
}
