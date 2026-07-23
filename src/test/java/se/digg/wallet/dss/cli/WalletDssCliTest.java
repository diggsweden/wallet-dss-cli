// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: CC0-1.0

package se.digg.wallet.dss.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class WalletDssCliTest {

  private static final String TEST_XML = "src/test/resources/test.xml";
  private static final String TEST_KEY = "src/test/resources/test-key.pem";
  private static final String TEST_CERT = "src/test/resources/test-cert.pem";
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @Test
  void testSignSuccess() throws Exception {
    String[] args = {"sign", TEST_XML, TEST_KEY, TEST_CERT};
    WalletDssCli cli = new WalletDssCli(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    int exitCode = cli.run(args);

    assertEquals(0, exitCode, "Exit code should be 0 on success");

    String signedContent = outContent.toString(StandardCharsets.UTF_8);
    assertTrue(
        signedContent.contains("ds:Signature"),
        "Signed XML should contain a ds:Signature block");
  }

  @Test
  void testSignInvalidArguments() throws Exception {
    String[] args = {"sign", TEST_XML};
    WalletDssCli cli = new WalletDssCli(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    int exitCode = cli.run(args);

    assertEquals(1, exitCode, "Exit code should be 1 for invalid arguments");
  }
}
