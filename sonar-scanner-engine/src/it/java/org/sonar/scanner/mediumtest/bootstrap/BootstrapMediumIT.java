/*
 * SonarQube
 * Copyright (C) 2009-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.scanner.mediumtest.bootstrap;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.scanner.bootstrap.ScannerMain;
import org.sonarqube.ws.Ce;
import org.sonarqube.ws.Qualityprofiles;
import org.sonarqube.ws.Rules;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testutils.TestUtils.protobufBody;

class BootstrapMediumIT {

  public static final String PROJECT_KEY = "my-project";
  public static final String QPROFILE_KEY = "profile123";
  @RegisterExtension
  LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @RegisterExtension
  static WireMockExtension sonarqube = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

  @BeforeEach
  void mockBareMinimalServerEndpoints() {
    sonarqube.stubFor(get("/api/plugins/installed")
      .willReturn(okJson("{\n"
        + "  \"plugins\": []\n"
        + "}")));

    sonarqube.stubFor(get("/api/qualityprofiles/search.protobuf?project=" + PROJECT_KEY)
      .willReturn(aResponse()
        .withResponseBody(protobufBody(Qualityprofiles.SearchWsResponse.newBuilder()
          .addProfiles(Qualityprofiles.SearchWsResponse.QualityProfile.newBuilder()
            .setKey(QPROFILE_KEY)
            .setName("My Profile")
            .setRulesUpdatedAt("2021-01-01T00:00:00+0000")
            .build())
          .build()))));

    sonarqube.stubFor(get("/api/rules/list.protobuf?qprofile=" + QPROFILE_KEY + "&ps=500&p=1")
      .willReturn(aResponse()
        .withResponseBody(protobufBody(Rules.ListResponse.newBuilder()
          .build()))));

    sonarqube.stubFor(get("/api/languages/list")
      .willReturn(okJson("{\n"
        + "  \"languages\": []\n"
        + "}")));

    sonarqube.stubFor(get("/api/metrics/search?ps=500&p=1")
      .willReturn(okJson("{\n"
        + "  \"metrics\": [],\n"
        + "  \"total\": 0,\n"
        + "  \"p\": 1,\n"
        + "  \"ps\": 100"
        + "}")));

    sonarqube.stubFor(post("/api/ce/submit?projectKey=" + PROJECT_KEY)
      .willReturn(aResponse()
        .withResponseBody(protobufBody(Ce.SubmitResponse.newBuilder()
          .build()))));
  }

  @Test
  void should_fail_if_invalid_json_input() {
    var in = new ByteArrayInputStream("}".getBytes());
    var e = assertThrows(IllegalArgumentException.class, () -> ScannerMain.run(in));
    assertThat(e).hasMessage("Failed to parse JSON input");

    assertThat(logTester.logs()).contains("Starting SonarScanner Engine...");
  }

  @Test
  void should_warn_if_null_property_key() {
    try {
      ScannerMain.run(new ByteArrayInputStream("{\"scannerProperties\": [{\"value\": \"aValueWithoutKey\"}]}".getBytes()));
    } catch (Exception ignored) {
    }
    assertThat(logTester.logs()).contains("Ignoring property with null key: 'aValueWithoutKey'");
  }

  @Test
  void should_warn_if_duplicate_property_keys() {
    try {
      ScannerMain.run(new ByteArrayInputStream("{\"scannerProperties\": [{\"key\": \"aKey\"}, {\"key\": \"aKey\"}]}".getBytes()));
    } catch (Exception ignored) {
    }
    assertThat(logTester.logs()).contains("Duplicated properties with key: 'aKey'");
  }

  @Test
  void should_warn_if_null_property() {
    try {
      ScannerMain.run(new ByteArrayInputStream("{\"scannerProperties\": [{\"key\": \"aKey\", \"value\": \"aValue\"},]}".getBytes()));
    } catch (Exception ignored) {
    }
    assertThat(logTester.logs()).contains("Ignoring null property");
  }

  /**
   * For now this test is just checking that the scanner completes successfully, with no input files, and mocking server responses to the bare minimum.
   */
  @Test
  void should_complete_successfully(@TempDir Path baseDir) {

    ScannerMain.run(new ByteArrayInputStream(("{\"scannerProperties\": ["
      + "{\"key\": \"sonar.host.url\", \"value\": \"" + sonarqube.baseUrl() + "\"},"
      + "{\"key\": \"sonar.projectKey\", \"value\": \"" + PROJECT_KEY + "\"},"
      + "{\"key\": \"sonar.projectBaseDir\", \"value\": \"" + baseDir + "\"}"
      + "]}").getBytes()));

    assertThat(logTester.logs()).contains("SonarScanner Engine completed successfully");
  }

  @Test
  void should_enable_verbose(@TempDir Path baseDir) {

    ScannerMain.run(new ByteArrayInputStream(("{\"scannerProperties\": ["
      + "{\"key\": \"sonar.host.url\", \"value\": \"" + sonarqube.baseUrl() + "\"},"
      + "{\"key\": \"sonar.projectKey\", \"value\": \"" + PROJECT_KEY + "\"},"
      + "{\"key\": \"sonar.projectBaseDir\", \"value\": \"" + baseDir + "\"}"
      + "]}").getBytes()));

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();

    ScannerMain.run(new ByteArrayInputStream(("{\"scannerProperties\": ["
      + "{\"key\": \"sonar.host.url\", \"value\": \"" + sonarqube.baseUrl() + "\"},"
      + "{\"key\": \"sonar.projectKey\", \"value\": \"" + PROJECT_KEY + "\"},"
      + "{\"key\": \"sonar.projectBaseDir\", \"value\": \"" + baseDir + "\"},"
      + "{\"key\": \"sonar.verbose\", \"value\": \"true\"}"
      + "]}").getBytes()));

    assertThat(logTester.logs(Level.DEBUG)).isNotEmpty();
  }
}
