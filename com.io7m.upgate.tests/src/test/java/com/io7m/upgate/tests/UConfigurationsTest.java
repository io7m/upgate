/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.upgate.tests;

import com.io7m.upgate.core.UConfigurations;
import com.io7m.upgate.core.UUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class UConfigurationsTest
{
  private static final String SHELL = "/sbin/nologin";
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = UTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    UTestDirectories.deleteDirectory(this.directory);
  }

  @TestFactory
  public Stream<DynamicTest> testErrors()
  {
    return Stream.of(
        "error-user-duplicate-id.xml",
        "error-user-duplicate-name.xml",
        "error-user-missing.xml")
      .map(name -> {
        return DynamicTest.dynamicTest("testErrors_" + name, () -> {
          final var file =
            UTestDirectories.resourceOf(
              UConfigurationsTest.class,
              this.directory,
              name
            );

          assertThrows(IOException.class, () -> {
            UConfigurations.parse(file);
          });
        });
      });
  }

  @Test
  public void testConfig0()
    throws Exception
  {
    final var file =
      UTestDirectories.resourceOf(
        UConfigurationsTest.class,
        this.directory,
        "config0.xml");

    final var configuration =
      UConfigurations.parse(file);

    final var users = configuration.users();
    assertEquals(7, users.size());
    assertEquals(new UUser(1001, 1001, "_registry", SHELL), users.get(0));
    assertEquals(new UUser(1002, 1002, "_nexus", SHELL), users.get(1));
    assertEquals(new UUser(1003, 1003, "_jenkins", SHELL), users.get(2));
    assertEquals(new UUser(1004, 1004, "_jenkins_node", SHELL), users.get(3));
    assertEquals(new UUser(1005, 1005, "_idstore_db", SHELL), users.get(4));
    assertEquals(new UUser(1006, 1006, "_idstore", SHELL), users.get(5));
    assertEquals(new UUser(1007, 1007, "_gtyrell", SHELL), users.get(6));

    final var groups = configuration.groups();
    assertEquals(7, groups.size());
    assertEquals(
      new UUser(1001, 1001, "_registry", SHELL),
      groups.get(0).users().get("_registry"));
    assertEquals(
      new UUser(1002, 1002, "_nexus", SHELL),
      groups.get(1).users().get("_nexus"));
    assertEquals(
      new UUser(1003, 1003, "_jenkins", SHELL),
      groups.get(2).users().get("_jenkins"));
    assertEquals(
      new UUser(1004, 1004, "_jenkins_node", SHELL),
      groups.get(3).users().get("_jenkins_node"));
    assertEquals(
      new UUser(1005, 1005, "_idstore_db", SHELL),
      groups.get(4).users().get("_idstore_db"));
    assertEquals(
      new UUser(1006, 1006, "_idstore", SHELL),
      groups.get(5).users().get("_idstore"));
    assertEquals(
      new UUser(1007, 1007, "_gtyrell", SHELL),
      groups.get(6).users().get("_gtyrell"));
  }
}
