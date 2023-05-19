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

import com.io7m.seltzer.api.SStructuredError;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Integer.toUnsignedString;
import static java.util.Map.entry;

/**
 * Functions to determine which adjustments need to be made to a system.
 */

public final class UDelta
{
  private UDelta()
  {

  }

  /**
   * Calculate the adjustments needed to make the system match the desired
   * state.
   *
   * @param userDatabase  A snapshot of the current user database
   * @param groupDatabase A snapshot of the current group database
   * @param configuration The desired configuration state
   *
   * @return A list of adjustments
   *
   * @throws UException On errors
   */

  public static List<UAdjustmentType> delta(
    final UUserDatabase userDatabase,
    final UGroupDatabase groupDatabase,
    final UConfiguration configuration)
    throws UException
  {
    Objects.requireNonNull(userDatabase, "userDatabase");
    Objects.requireNonNull(groupDatabase, "groupDatabase");
    Objects.requireNonNull(configuration, "configuration");

    final var adjustments =
      new LinkedList<UAdjustmentType>();
    final var errors =
      new LinkedList<SStructuredError<String>>();

    userAdjustments(
      userDatabase,
      configuration,
      adjustments,
      errors
    );
    groupAdjustments(
      userDatabase,
      groupDatabase,
      configuration,
      adjustments,
      errors
    );

    if (!errors.isEmpty()) {
      final var first = errors.removeFirst();
      throw new UException(
        first.message(),
        first.errorCode(),
        first.attributes(),
        first.remediatingAction(),
        errors
      );
    }

    return List.copyOf(adjustments);
  }

  private static void groupAdjustments(
    final UUserDatabase userDatabase,
    final UGroupDatabase groupDatabase,
    final UConfiguration configuration,
    final LinkedList<UAdjustmentType> adjustments,
    final LinkedList<SStructuredError<String>> errors)
  {
    for (final var group : configuration.groups()) {
      groupAdjustment(groupDatabase, adjustments, errors, group);
    }
  }

  private static void groupAdjustment(
    final UGroupDatabase groupDatabase,
    final LinkedList<UAdjustmentType> adjustments,
    final LinkedList<SStructuredError<String>> errors,
    final UGroup group)
  {
    final var existingByName =
      groupDatabase.groupForName(group.name());
    final var existingById =
      groupDatabase.groupForId(group.id());

    if (existingByName.isEmpty() && existingById.isEmpty()) {
      adjustments.add(0, new UAdjustmentGroupCreate(group));
      return;
    }

    if (existingByName.isPresent() && existingById.isEmpty()) {
      adjustments.add(new UAdjustmentGroupChangeGID(group));
      return;
    }

    final var exist1 = existingById.get();
    if (existingByName.isEmpty()) {
      adjustments.add(new UAdjustmentGroupChangeName(
        exist1.groupName(),
        group));
      return;
    }

    final var exist0 = existingByName.get();
    errors.add(new SStructuredError<>(
      "error-group-conflict",
      "Unsolvable group ID/Name conflict.",
      Map.ofEntries(
        entry("Requested Group ID", toUnsignedString(group.id())),
        entry("Requested Group Name", group.name()),
        entry("Existing Group (0) Name", exist0.groupName()),
        entry("Existing Group (0) ID", toUnsignedString(exist0.gid())),
        entry("Existing Group (1) Name", exist1.groupName()),
        entry("Existing Group (1) ID", toUnsignedString(exist1.gid()))
      ),
      Optional.of("Remove one of the conflicting groups."),
      Optional.empty()
    ));
  }

  private static void userAdjustments(
    final UUserDatabase userDatabase,
    final UConfiguration configuration,
    final LinkedList<UAdjustmentType> adjustments,
    final LinkedList<SStructuredError<String>> errors)
  {
    for (final var user : configuration.users()) {
      userAdjustment(userDatabase, adjustments, errors, user);
    }
  }

  private static void userAdjustment(
    final UUserDatabase userDatabase,
    final LinkedList<UAdjustmentType> adjustments,
    final LinkedList<SStructuredError<String>> errors,
    final UUser user)
  {
    final var existingByName =
      userDatabase.userForName(user.name());
    final var existingById =
      userDatabase.userForId(user.id());

    if (existingByName.isEmpty() && existingById.isEmpty()) {
      adjustments.add(new UAdjustmentUserCreate(user));
      return;
    }

    if (existingByName.isPresent() && existingById.isEmpty()) {
      adjustments.add(new UAdjustmentUserChangeUID(user));
      return;
    }

    final var exist1 = existingById.get();
    if (existingByName.isEmpty()) {
      adjustments.add(new UAdjustmentUserChangeName(exist1.name(), user));
      return;
    }

    final var exist0 = existingByName.get();
    errors.add(new SStructuredError<>(
      "error-user-conflict",
      "Unsolvable user ID/Name conflict.",
      Map.ofEntries(
        entry("Requested User ID", toUnsignedString(user.id())),
        entry("Requested User Name", user.name()),
        entry("Existing User (0) Name", exist0.name()),
        entry("Existing User (0) ID", toUnsignedString(exist0.id())),
        entry("Existing User (1) Name", exist1.name()),
        entry("Existing User (1) ID", toUnsignedString(exist1.id()))
      ),
      Optional.of("Remove one of the conflicting users."),
      Optional.empty()
    ));
  }
}
