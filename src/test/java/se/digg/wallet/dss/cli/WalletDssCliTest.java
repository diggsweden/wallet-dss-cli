// SPDX-FileCopyrightText: 2026 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: CC0-1.0

package se.digg.wallet.dss.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WalletDssCliTest {

  private static final String TEST_XML = "src/test/resources/test.xml";
  private static final String TEST_KEY = "src/test/resources/test-key.pem";
  private static final String TEST_CERT = "src/test/resources/test-cert.pem";
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @BeforeEach
  void setUp() {
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void tearDown() {
    System.setOut(originalOut);
  }

  @Test
  void testSignSuccess() throws Exception {
    String[] args = {"sign", TEST_XML, TEST_KEY, TEST_CERT};
    int exitCode = WalletDssCli.run(args);

    assertEquals(0, exitCode, "Exit code should be 0 on success");

    String signedContent = outContent.toString();
    assertTrue(
        signedContent.contains("ds:Signature"),
        "Signed XML should contain a ds:Signature block");
  }

  @Test
  void testSignInvalidArguments() throws Exception {
    String[] args = {"sign", TEST_XML};
    int exitCode = WalletDssCli.run(args);

    assertEquals(1, exitCode, "Exit code should be 1 for invalid arguments");
  }
}
