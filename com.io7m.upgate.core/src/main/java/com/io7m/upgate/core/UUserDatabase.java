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
 * A snapshot of the user database.
 *
 * @param entries The entries
 */

public record UUserDatabase(
  List<UUserDatabaseEntry> entries)
{
  /**
   * A snapshot of the user database.
   *
   * @param entries The entries
   */

  public UUserDatabase
  {
    Objects.requireNonNull(entries, "entries");
  }

  /**
   * Read the user database from the current system.
   *
   * @return The user database
   *
   * @throws IOException          On errors
   * @throws InterruptedException On interruption
   */

  public static UUserDatabase get()
    throws IOException, InterruptedException
  {
    final var proc0 =
      new ProcessBuilder()
        .command(List.of("getent", "passwd"))
        .start();

    proc0.waitFor(1L, TimeUnit.SECONDS);
    final var exit = proc0.exitValue();
    if (exit != 0) {
      throw new IOException("getent command failed.");
    }

    final var entries = new ArrayList<UUserDatabaseEntry>();
    try (var reader = proc0.inputReader()) {
      while (true) {
        final var line = reader.readLine();
        if (line == null) {
          break;
        }
        final var segments = List.of(line.split(":"));
        entries.add(
          new UUserDatabaseEntry(
            segments.get(0),
            Integer.parseUnsignedInt(segments.get(2)),
            Integer.parseUnsignedInt(segments.get(3))
          )
        );
      }
    }
    return new UUserDatabase(List.copyOf(entries));
  }

  /**
   * Find the user with the given name.
   *
   * @param name The name
   *
   * @return The user
   */

  public Optional<UUser> userForName(
    final String name)
  {
    return this.entries.stream()
      .filter(u -> Objects.equals(u.userName, name))
      .findFirst()
      .map(entry -> new UUser(entry.uid(), entry.gid(), entry.userName()));
  }

  /**
   * Find the user with the given ID.
   *
   * @param id The id
   *
   * @return The user
   */

  public Optional<UUser> userForId(
    final int id)
  {
    return this.entries.stream()
      .filter(u -> u.uid() == id)
      .findFirst()
      .map(entry -> new UUser(entry.uid(), entry.gid(), entry.userName()));
  }

  /**
   * A user database entry.
   *
   * @param userName The user name
   * @param gid      The primary group ID
   * @param uid      The user ID
   */

  public record UUserDatabaseEntry(
    String userName,
    int uid,
    int gid)
  {

  }
}
