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


package com.io7m.upgate.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A snapshot of the group database.
 *
 * @param entries The entries
 */

public record UGroupDatabase(
  List<UGroupDatabaseEntry> entries)
{
  /**
   * A snapshot of the group database.
   *
   * @param entries The entries
   */

  public UGroupDatabase
  {
    Objects.requireNonNull(entries, "entries");
  }

  /**
   * Read the group database from the current system.
   *
   * @return The group database
   *
   * @throws IOException          On errors
   * @throws InterruptedException On interruption
   */

  public static UGroupDatabase get()
    throws IOException, InterruptedException
  {
    final var proc0 =
      new ProcessBuilder()
        .command(List.of("getent", "group"))
        .start();

    proc0.waitFor(1L, TimeUnit.SECONDS);
    final var exit = proc0.exitValue();
    if (exit != 0) {
      throw new IOException("getent command failed.");
    }

    final var entries = new ArrayList<UGroupDatabaseEntry>();
    try (var reader = proc0.inputReader()) {
      while (true) {
        final var line = reader.readLine();
        if (line == null) {
          break;
        }
        final var segments = List.of(line.split(":"));
        entries.add(
          new UGroupDatabaseEntry(
            segments.get(0),
            Integer.parseUnsignedInt(segments.get(2)),
            List.of(segments.get(1).split(","))
          )
        );
      }
    }
    return new UGroupDatabase(List.copyOf(entries));
  }

  /**
   * Find a group by name, if one exists.
   *
   * @param name The name
   *
   * @return The group
   */

  public Optional<UGroupDatabaseEntry> groupForName(
    final String name)
  {
    return this.entries.stream()
      .filter(g -> Objects.equals(g.groupName, name))
      .findFirst();
  }

  /**
   * Find a group by ID, if one exists.
   *
   * @param id The id
   *
   * @return The group
   */

  public Optional<UGroupDatabaseEntry> groupForId(
    final int id)
  {
    return this.entries.stream()
      .filter(g -> g.gid == id)
      .findFirst();
  }

  /**
   * A group database entry.
   *
   * @param groupName The group name
   * @param gid       The group ID
   * @param members   The group members
   */

  public record UGroupDatabaseEntry(
    String groupName,
    int gid,
    List<String> members)
  {

  }
}
