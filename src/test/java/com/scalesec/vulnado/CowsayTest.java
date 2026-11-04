package com.scalesec.vulnado;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class CowsayTest {

  private static void assumeCowsayInstalled() {
    assumeTrue("cowsay is not installed", new File("/usr/games/cowsay").canExecute());
  }

  @Test
  public void runRendersPlainInput() {
    assumeCowsayInstalled();
    String output = Cowsay.run("hello");
    assertTrue(output.contains("hello"));
  }

  @Test
  public void runDoesNotExecuteInjectedShellCommand() throws Exception {
    assumeCowsayInstalled();
    Path marker = Files.createTempFile("cowsay-injection", ".marker");
    Files.delete(marker);
    try {
      // Classic single-quote breakout that would create the marker file if run through a shell.
      String payload = "x'; touch " + marker + "; echo '";
      String output = Cowsay.run(payload);

      assertFalse("Injected command must not be executed", Files.exists(marker));
      // The payload must be treated as literal text by cowsay.
      assertTrue(output.contains("touch"));
    } finally {
      Files.deleteIfExists(marker);
    }
  }

  @Test
  public void runDoesNotExpandShellSubstitution() {
    assumeCowsayInstalled();
    String output = Cowsay.run("$(echo INJECTED)");
    assertTrue(output.contains("$(echo INJECTED)"));
  }

  @Test
  public void runTreatsLeadingDashInputAsText() {
    assumeCowsayInstalled();
    String output = Cowsay.run("-e XX");
    assertTrue(output.contains("-e XX"));
  }
}
