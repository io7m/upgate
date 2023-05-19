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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Functions to parse configurations.
 */

public final class UConfigurations
{
  private static final Logger LOG =
    LoggerFactory.getLogger(UConfigurations.class);
  private static final String NS =
    "urn:com.io7m.upgate:1";

  private UConfigurations()
  {

  }

  /**
   * Parse the given configuration file.
   *
   * @param file The file
   *
   * @return The configuration
   *
   * @throws Exception On errors
   */

  public static UConfiguration parse(
    final Path file)
    throws Exception
  {
    return consumeFile(parseFile(file));
  }

  private static UConfiguration consumeFile(
    final Document document)
  {
    final var root =
      document.getDocumentElement();

    final var users =
      new ArrayList<UUser>();

    {
      final var usersE =
        (Element) root.getElementsByTagNameNS(NS, "Users")
          .item(0);

      final var usersList =
        usersE.getElementsByTagNameNS(NS, "User");

      for (int index = 0; index < usersList.getLength(); ++index) {
        final var userE = (Element) usersList.item(index);
        users.add(new UUser(
          Integer.parseUnsignedInt(userE.getAttribute("ID")),
          Integer.parseUnsignedInt(userE.getAttribute("GID")),
          userE.getAttribute("Name")
        ));
      }
    }

    final var groups =
      new ArrayList<UGroup>();

    {
      final var groupsE =
        (Element) root.getElementsByTagNameNS(NS, "Groups")
          .item(0);

      final var groupsList =
        groupsE.getElementsByTagNameNS(NS, "Group");

      for (int index = 0; index < groupsList.getLength(); ++index) {
        final var groupE = (Element) groupsList.item(index);

        final var membersE =
          groupE.getElementsByTagNameNS(NS, "GroupMember");

        final var groupUsers =
          new HashMap<String, UUser>();

        for (int gmIndex = 0; gmIndex < membersE.getLength(); ++gmIndex) {
          final var memberE =
            (Element) membersE.item(gmIndex);
          final var name =
            memberE.getAttribute("User");

          final var user =
            users.stream()
              .filter(u -> Objects.equals(u.name(), name))
              .findFirst()
              .orElseThrow();

          groupUsers.put(name, user);
        }

        groups.add(new UGroup(
          Integer.parseUnsignedInt(groupE.getAttribute("ID")),
          groupE.getAttribute("Name"),
          Map.copyOf(groupUsers)
        ));
      }
    }

    return new UConfiguration(
      List.copyOf(users),
      List.copyOf(groups)
    );
  }

  private static Document parseFile(final Path file)
    throws SAXException, ParserConfigurationException, IOException
  {
    final var schemas =
      SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final var schema =
      schemas.newSchema(
        UConfigurations.class.getResource(
          "/com/io7m/upgate/core/upgate-1.xsd")
      );

    final var documentBuilders =
      DocumentBuilderFactory.newDefaultNSInstance();

    documentBuilders.setSchema(schema);
    documentBuilders.setValidating(false);
    documentBuilders.setExpandEntityReferences(false);
    documentBuilders.setXIncludeAware(false);

    final var documentBuilder =
      documentBuilders.newDocumentBuilder();

    final var failed = new AtomicBoolean(false);
    documentBuilder.setErrorHandler(new LoggingErrorHandler(failed));

    final Document document;
    try (var stream = Files.newInputStream(file)) {
      document = documentBuilder.parse(stream, file.toString());
    }

    if (failed.get()) {
      throw new IOException("One or more parse/validation errors occurred.");
    }
    return document;
  }

  private static final class LoggingErrorHandler
    implements ErrorHandler
  {
    private final AtomicBoolean failed;

    private LoggingErrorHandler(
      final AtomicBoolean inFailed)
    {
      this.failed = inFailed;
    }

    @Override
    public void warning(
      final SAXParseException exception)
    {
      LOG.warn(
        "{}:{}: {}",
        Integer.valueOf(exception.getLineNumber()),
        Integer.valueOf(exception.getColumnNumber()),
        exception.getMessage()
      );
    }

    @Override
    public void error(
      final SAXParseException exception)
    {
      LOG.error(
        "{}:{}: {}",
        Integer.valueOf(exception.getLineNumber()),
        Integer.valueOf(exception.getColumnNumber()),
        exception.getMessage()
      );
      this.failed.set(true);
    }

    @Override
    public void fatalError(
      final SAXParseException exception)
    {
      LOG.error(
        "{}:{}: {}",
        Integer.valueOf(exception.getLineNumber()),
        Integer.valueOf(exception.getColumnNumber()),
        exception.getMessage()
      );
      this.failed.set(true);
    }
  }
}
