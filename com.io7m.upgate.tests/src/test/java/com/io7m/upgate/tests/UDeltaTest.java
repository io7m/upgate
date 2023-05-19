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

import com.io7m.upgate.core.UAdjustmentGroupChangeGID;
import com.io7m.upgate.core.UAdjustmentGroupChangeName;
import com.io7m.upgate.core.UAdjustmentGroupCreate;
import com.io7m.upgate.core.UAdjustmentUserChangeName;
import com.io7m.upgate.core.UAdjustmentUserChangeUID;
import com.io7m.upgate.core.UAdjustmentUserCreate;
import com.io7m.upgate.core.UConfiguration;
import com.io7m.upgate.core.UDelta;
import com.io7m.upgate.core.UException;
import com.io7m.upgate.core.UGroup;
import com.io7m.upgate.core.UGroupDatabase;
import com.io7m.upgate.core.UUser;
import com.io7m.upgate.core.UUserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class UDeltaTest
{
  @BeforeEach
  public void setup()
  {

  }

  @Test
  public void testNoChanges()
    throws UException
  {
    final var configuration =
      new UConfiguration(List.of(), List.of());
    final var users =
      new UUserDatabase(List.of());
    final var groups =
      new UGroupDatabase(List.of());

    assertEquals(List.of(), UDelta.delta(users, groups, configuration));
  }

  @Test
  public void testUserConflict()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(
          new UUser(2000, 3000, "x")
        ),
        List.of());
    final var users =
      new UUserDatabase(List.of(
        new UUserDatabase.UUserDatabaseEntry(
          "x",
          1000,
          3000
        ),
        new UUserDatabase.UUserDatabaseEntry(
          "y",
          2000,
          3000
        )
      ));
    final var groups =
      new UGroupDatabase(List.of());

    final var ex = assertThrows(UException.class, () -> {
      UDelta.delta(users, groups, configuration);
    });
    assertEquals("error-user-conflict", ex.errorCode());
  }

  @Test
  public void testUserCreate0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(
          new UUser(2000, 3000, "x")
        ),
        List.of());
    final var users =
      new UUserDatabase(List.of());
    final var groups =
      new UGroupDatabase(List.of());

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentUserCreate(new UUser(2000, 3000, "x")),
      delta.get(0)
    );
  }

  @Test
  public void testUserChangeName0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(
          new UUser(2000, 3000, "x")
        ),
        List.of());
    final var users =
      new UUserDatabase(List.of(
        new UUserDatabase.UUserDatabaseEntry("y", 2000, 3000)
      ));
    final var groups =
      new UGroupDatabase(List.of());

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentUserChangeName("y", new UUser(2000, 3000, "x")),
      delta.get(0)
    );
  }

  @Test
  public void testUserChangeID0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(
          new UUser(2000, 3000, "x")
        ),
        List.of());
    final var users =
      new UUserDatabase(List.of(
        new UUserDatabase.UUserDatabaseEntry("x", 2001, 3000)
      ));
    final var groups =
      new UGroupDatabase(List.of());

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentUserChangeUID(new UUser(2000, 3000, "x")),
      delta.get(0)
    );
  }

  @Test
  public void testGroupCreate0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(),
        List.of(
          new UGroup(2000, "x", Map.of())
        ));
    final var users =
      new UUserDatabase(List.of());
    final var groups =
      new UGroupDatabase(List.of());

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentGroupCreate(new UGroup(2000, "x", Map.of())),
      delta.get(0)
    );
  }

  @Test
  public void testGroupChangeName0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(

        ),
        List.of(
          new UGroup(2000, "x", Map.of())
        ));
    final var users =
      new UUserDatabase(List.of(

      ));
    final var groups =
      new UGroupDatabase(List.of(
        new UGroupDatabase.UGroupDatabaseEntry("y", 2000, List.of())
      ));

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentGroupChangeName("y", new UGroup(2000,"x", Map.of())),
      delta.get(0)
    );
  }

  @Test
  public void testGroupChangeID0()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(

        ),
        List.of(
          new UGroup(2001, "x", Map.of())
        ));
    final var users =
      new UUserDatabase(List.of(

      ));
    final var groups =
      new UGroupDatabase(List.of(
        new UGroupDatabase.UGroupDatabaseEntry("x", 2000, List.of())
      ));

    final var delta =
      UDelta.delta(users, groups, configuration);

    assertEquals(
      new UAdjustmentGroupChangeGID(new UGroup(2001, "x", Map.of())),
      delta.get(0)
    );
  }

  @Test
  public void testGroupConflict()
    throws UException
  {
    final var configuration =
      new UConfiguration(
        List.of(),
        List.of(
          new UGroup(2000, "x", Map.of())
        ));
    final var users =
      new UUserDatabase(List.of());
    final var groups =
      new UGroupDatabase(List.of(
        new UGroupDatabase.UGroupDatabaseEntry(
          "x",
          2001,
          List.of()
        ),
        new UGroupDatabase.UGroupDatabaseEntry(
          "y",
          2000,
          List.of()
        )
      ));

    final var ex = assertThrows(UException.class, () -> {
      UDelta.delta(users, groups, configuration);
    });
    assertEquals("error-group-conflict", ex.errorCode());
  }
}
