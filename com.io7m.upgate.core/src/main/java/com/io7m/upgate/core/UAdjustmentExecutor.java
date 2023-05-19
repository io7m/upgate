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
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The main adjustment executor.
 */

public final class UAdjustmentExecutor
  implements UAdjustmentExecutorType
{
  private final UAdjustmentCommandExecutorType executor;

  private UAdjustmentExecutor(
    final UAdjustmentCommandExecutorType inExecutor)
  {
    this.executor =
      Objects.requireNonNull(inExecutor, "executor");
  }

  /**
   * Produce an executor that prints commands instead of running them.
   *
   * @param writer The output writer
   *
   * @return An executor
   */

  public static UAdjustmentExecutorType ofDryRun(
    final PrintWriter writer)
  {
    return new UAdjustmentExecutor(command -> {
      writer.println(String.join(" ", command));
    });
  }

  /**
   * Produce an executor that executes commands on the system.
   *
   * @return An executor
   */

  public static UAdjustmentExecutorType ofSystem()
  {
    return new UAdjustmentExecutor(command -> {
      try {
        final var proc =
          new ProcessBuilder(command)
            .start();

        final var exitCode = proc.waitFor();
        if (exitCode != 0) {
          throw new UException(
            "Command failed.",
            "error-command-failed",
            Map.ofEntries(
              Map.entry("Command", String.join(" ", command)),
              Map.entry("Exit Code", Integer.toUnsignedString(exitCode))
            ),
            Optional.empty(),
            List.of()
          );
        }
      } catch (IOException | InterruptedException e) {
        throw new UException(
          "Command failed.",
          e,
          "error-command-failed",
          Map.ofEntries(
            Map.entry("Command", String.join(" ", command))
          ),
          Optional.empty(),
          List.of()
        );
      }
    });
  }

  @Override
  public void execute(
    final List<UAdjustmentType> adjustments)
    throws UException
  {
    for (final var adjustment : adjustments) {
      this.executeAdjustment(adjustment);
    }
  }

  private void executeAdjustment(
    final UAdjustmentType adjustment)
    throws UException
  {
    if (adjustment instanceof final UAdjustmentGroupChangeGID u) {
      this.executeGroupChangeGID(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentGroupChangeName u) {
      this.executeGroupChangeName(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentGroupCreate u) {
      this.executeGroupCreate(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentUserChangeUID u) {
      this.executeUserChangeUID(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentUserChangeName u) {
      this.executeUserChangeName(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentUserCreate u) {
      this.executeUserCreate(u);
      return;
    }
    if (adjustment instanceof final UAdjustmentUserChangeShell u) {
      this.executeUserChangeShell(u);
      return;
    }
  }

  private void executeGroupChangeGID(
    final UAdjustmentGroupChangeGID adjustment)
    throws UException
  {
    final var group = adjustment.group();
    this.executor.execute(List.of(
      "groupmod",
      "--gid",
      Integer.toUnsignedString(group.id()),
      group.name()
    ));
  }

  private void executeGroupChangeName(
    final UAdjustmentGroupChangeName adjustment)
    throws UException
  {
    final var group = adjustment.group();
    this.executor.execute(List.of(
      "groupmod",
      "--new-name",
      group.name(),
      adjustment.oldName()
    ));
  }

  private void executeGroupCreate(
    final UAdjustmentGroupCreate adjustment)
    throws UException
  {
    final var group = adjustment.group();
    this.executor.execute(List.of(
      "groupadd",
      "--gid",
      Integer.toUnsignedString(group.id()),
      group.name()
    ));
  }

  private void executeUserChangeUID(
    final UAdjustmentUserChangeUID adjustment)
    throws UException
  {
    final var user = adjustment.user();
    this.executor.execute(List.of(
      "usermod",
      "--uid",
      Integer.toUnsignedString(user.id()),
      user.name()
    ));
  }

  private void executeUserChangeName(
    final UAdjustmentUserChangeName adjustment)
    throws UException
  {
    final var user = adjustment.user();
    this.executor.execute(List.of(
      "usermod",
      "--login",
      user.name(),
      adjustment.oldName()
    ));
  }

  private void executeUserCreate(
    final UAdjustmentUserCreate adjustment)
    throws UException
  {
    final var user = adjustment.user();
    this.executor.execute(List.of(
      "useradd",
      "--uid",
      Integer.toUnsignedString(user.id()),
      "--gid",
      Integer.toUnsignedString(user.groupId()),
      "--no-create-home",
      user.name()
    ));
  }

  private void executeUserChangeShell(
    final UAdjustmentUserChangeShell adjustment)
    throws UException
  {
    final var user = adjustment.user();
    this.executor.execute(List.of(
      "usermod",
      "--shell",
      user.shell(),
      user.name()
    ));
  }
}
