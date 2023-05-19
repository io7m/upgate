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

import com.io7m.upgate.core.UAdjustmentExecutor;
import com.io7m.upgate.core.UAdjustmentGroupChangeGID;
import com.io7m.upgate.core.UAdjustmentGroupChangeName;
import com.io7m.upgate.core.UAdjustmentGroupCreate;
import com.io7m.upgate.core.UAdjustmentType;
import com.io7m.upgate.core.UAdjustmentUserChangeName;
import com.io7m.upgate.core.UAdjustmentUserChangeShell;
import com.io7m.upgate.core.UAdjustmentUserChangeUID;
import com.io7m.upgate.core.UAdjustmentUserCreate;
import com.io7m.upgate.core.UException;
import com.io7m.upgate.core.UGroup;
import com.io7m.upgate.core.UUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class UExecutorTest
{
  private static final String SHELL = "/sbin/nologin";

  private static List<String> execute(
    final List<UAdjustmentType> adjustments)
    throws UException
  {
    final var stringWriter =
      new StringWriter();
    final var writer =
      new PrintWriter(stringWriter);
    final var executor =
      UAdjustmentExecutor.ofDryRun(writer);

    executor.execute(adjustments);

    writer.flush();
    final var lines = stringWriter.toString().lines().toList();
    return lines;
  }

  @BeforeEach
  public void setup()
  {

  }

  @Test
  public void testNoChanges()
    throws UException
  {
    final var lines =
      execute(List.of());

    assertEquals(List.of(), lines);
  }

  @Test
  public void testUserCreate()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentUserCreate(new UUser(1001, 1001, "user0", SHELL))
      ));

    assertEquals(
      "useradd --uid 1001 --gid 1001 --no-create-home user0",
      lines.get(0)
    );
  }

  @Test
  public void testUserChangeName()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentUserChangeName("y", new UUser(1001, 1001, "user0", SHELL))
      ));

    assertEquals(
      "usermod --login user0 y",
      lines.get(0)
    );
  }

  @Test
  public void testUserChangeUID()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentUserChangeUID(new UUser(1003, 1001, "user0", SHELL))
      ));

    assertEquals(
      "usermod --uid 1003 user0",
      lines.get(0)
    );
  }

  @Test
  public void testGroupCreate()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentGroupCreate(new UGroup(1001, "user0", Map.of()))
      ));

    assertEquals(
      "groupadd --gid 1001 user0",
      lines.get(0)
    );
  }

  @Test
  public void testGroupChangeName()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentGroupChangeName("y", new UGroup(1001, "x", Map.of()))
      ));

    assertEquals(
      "groupmod --new-name x y",
      lines.get(0)
    );
  }

  @Test
  public void testGroupChangeUID()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentGroupChangeGID(new UGroup(1003, "x", Map.of()))
      ));

    assertEquals(
      "groupmod --gid 1003 x",
      lines.get(0)
    );
  }

  @Test
  public void testUserChangeShell()
    throws UException
  {
    final var lines =
      execute(List.of(
        new UAdjustmentUserChangeShell(new UUser(1003, 1001, "user0", SHELL))
      ));

    assertEquals(
      "usermod --shell /sbin/nologin user0",
      lines.get(0)
    );
  }
}
